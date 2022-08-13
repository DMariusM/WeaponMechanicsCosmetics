package me.cjcrafter.weaponmechanicscosmetics.scripts;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.RayTraceResult;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * This script checks if the projectile enters water every tick. When the
 * projectile enters water, Mechanics are used. This is useful for splash
 * particles, and a splash sound effect.
 */
public class SplashScript extends ProjectileScript<WeaponProjectile> {

    private Mechanics mechanics;
    private boolean wasInWater;

    public SplashScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        super(owner, projectile);

        Configuration config = WeaponMechanics.getConfigurations();
        mechanics = config.getObject(projectile.getWeaponTitle() + ".Cosmetics.Splash_Mechanics", Mechanics.class);
    }

    public SplashScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile, Mechanics mechanics) {
        super(owner, projectile);
        this.mechanics = mechanics;
    }

    @Override
    public void onTickEnd() {
        wasInWater = isInWater();
    }

    @Override
    public void onCollide(@NotNull RayTraceResult hit) {
        if (hit.isBlock() && isWater(hit.getBlock()) && !wasInWater) {
            CastData cast = new CastData(projectile);
            mechanics.use(cast);
        }
    }

    public boolean isInWater() {
        Block block = projectile.getWorld().getBlockAt((int) projectile.getX(), (int) projectile.getY(), (int) projectile.getZ());
        return isWater(block);
    }

    public boolean isWater(Block block) {
        // Weird water check for version compatibility... "STATIONARY_WATER"
        return block.isLiquid() && block.getType().name().endsWith("WATER");
    }
}
