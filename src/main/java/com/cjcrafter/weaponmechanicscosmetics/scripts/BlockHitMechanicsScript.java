package com.cjcrafter.weaponmechanicscosmetics.scripts;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.MechanicManager;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.utils.ray.BlockTraceResult;
import me.deecaad.core.utils.ray.RayTraceResult;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class BlockHitMechanicsScript extends ProjectileScript<WeaponProjectile> {

    private MechanicManager mechanics;

    public BlockHitMechanicsScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        super(owner, projectile);

        Configuration config = WeaponMechanics.getInstance().getWeaponConfigurations();
        mechanics = config.getObject(projectile.getWeaponTitle() + ".Cosmetics.Block_Hit_Mechanics", MechanicManager.class);
    }

    public BlockHitMechanicsScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile, @NotNull MechanicManager mechanics) {
        super(owner, projectile);
        this.mechanics = mechanics;
    }

    public @NotNull MechanicManager getMechanics() {
        return mechanics;
    }

    public void setMechanics(@NotNull MechanicManager mechanics) {
        this.mechanics = mechanics;
    }

    @Override
    public void onCollide(@NotNull RayTraceResult hit) {
        LivingEntity shooter = projectile.getShooter();
        if (shooter == null)
            return;

        if (hit instanceof BlockTraceResult blockHit && !SplashScript.isWater(blockHit.getBlock())) {
            CastData cast = new CastData(shooter, projectile.getWeaponTitle(), projectile.getWeaponStack());
            cast.setTargetLocation(hit.getHitLocation().toLocation(projectile.getWorld()));
            mechanics.use(cast);
        }
    }
}