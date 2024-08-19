/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.scripts;

import com.cjcrafter.foliascheduler.MinecraftVersions;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.utils.ray.BlockTraceResult;
import me.deecaad.core.utils.ray.RayTraceResult;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
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
        wasInWater = isInWater();
    }

    public SplashScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile, Mechanics mechanics) {
        super(owner, projectile);
        this.mechanics = mechanics;
        wasInWater = isInWater();
    }

    public Mechanics getMechanics() {
        return mechanics;
    }

    public void setMechanics(Mechanics mechanics) {
        this.mechanics = mechanics;
    }

    @Override
    public void onTickEnd() {
        wasInWater = isInWater();
    }

    @Override
    public void onCollide(@NotNull RayTraceResult hit) {
        LivingEntity shooter = projectile.getShooter();
        if (shooter == null)
            return;

        if (hit instanceof BlockTraceResult blockHit && isWater(blockHit.getBlock()) && !wasInWater) {
            CastData cast = new CastData(shooter, projectile.getWeaponTitle(), projectile.getWeaponStack());
            cast.setTargetLocation(hit.getHitLocation().toLocation(projectile.getWorld()));
            mechanics.use(cast);
        }
    }

    public boolean isInWater() {
        Block block = projectile.getWorld().getBlockAt((int) projectile.getX(), (int) projectile.getY(), (int) projectile.getZ());
        return isWater(block);
    }

    public static boolean isWater(Block block) {
        // Weird water check for version compatibility... "STATIONARY_WATER"
        if (MinecraftVersions.UPDATE_AQUATIC.isAtLeast())
            return block.isLiquid() && block.getType() == Material.WATER;
        else
            return block.isLiquid() && block.getType().name().endsWith("WATER");
    }
}
