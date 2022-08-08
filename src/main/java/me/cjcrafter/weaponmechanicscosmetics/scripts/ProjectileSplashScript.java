package me.cjcrafter.weaponmechanicscosmetics.scripts;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * This script checks if the projectile enters water every tick. When the
 * projectile enters water, Mechanics are used. This is useful for splash
 * particles, and a splash sound effect.
 */
public class ProjectileSplashScript extends ProjectileScript<WeaponProjectile> {

    private Mechanics mechanics;
    private boolean wasInWater;

    public ProjectileSplashScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        super(owner, projectile);

        Configuration config = WeaponMechanics.getConfigurations();
        mechanics = config.getObject(projectile.getWeaponTitle() + ".Cosmetics.Splash_Mechanics", Mechanics.class);
    }

    public ProjectileSplashScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile, Mechanics mechanics) {
        super(owner, projectile);
        this.mechanics = mechanics;
    }

    @Override
    public void onTickEnd() {
        boolean isInWater = isInWater();

        if (isInWater && !wasInWater) {

            CastData castData = new CastData(projectile);
            if (mechanics != null)
                mechanics.use(castData);
        }

        wasInWater = isInWater;
    }

    public boolean isInWater() {
        Block block = projectile.getWorld().getBlockAt((int) projectile.getX(), (int) projectile.getY(), (int) projectile.getZ());

        // Weird water check for version compatibility... "STATIONARY_WATER"
        return block.isLiquid() && block.getType().name().endsWith("WATER");
    }
}
