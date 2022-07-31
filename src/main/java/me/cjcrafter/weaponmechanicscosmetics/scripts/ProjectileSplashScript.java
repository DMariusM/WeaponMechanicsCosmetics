package me.cjcrafter.weaponmechanicscosmetics.scripts;

import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class ProjectileSplashScript extends ProjectileScript<WeaponProjectile> {

    private boolean wasInWater;

    public ProjectileSplashScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        super(owner, projectile);
    }

    @Override
    public void onTickEnd() {
        boolean isInWater = isInWater();

        if (isInWater && !wasInWater) {
            World world = projectile.getWorld();
            Location loc = projectile.getLocation().toLocation(world);
            world.playSound(loc, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.7f + ThreadLocalRandom.current().nextFloat(0.0f, 0.3f));
            world.spawnParticle(Particle.WATER_SPLASH, loc, 25, 0.2, 0.2, 0.2);
        }

        wasInWater = isInWater;
    }

    public boolean isInWater() {
        Block block = projectile.getWorld().getBlockAt((int) projectile.getX(), (int) projectile.getY(), (int) projectile.getZ());
        return block.isLiquid() && block.getType().name().endsWith("WATER");
    }
}
