/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package com.cjcrafter.weaponmechanicscosmetics;

import com.cjcrafter.weaponmechanicscosmetics.scripts.*;
import com.cjcrafter.weaponmechanicscosmetics.trails.TrailScript;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScriptManager;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;


public class CosmeticsScriptManager extends ProjectileScriptManager {

    public CosmeticsScriptManager(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void attach(@NotNull AProjectile aProjectile) {
        if (aProjectile.getIntTag("explosion-falling-block") == 1) {
            FallingBlockScript script = new FallingBlockScript(getPlugin(), aProjectile);
            aProjectile.addProjectileScript(script);
            return;
        }

        Configuration config = WeaponMechanics.getInstance().getWeaponConfigurations();

        if (aProjectile instanceof WeaponProjectile projectile) {
            projectile.addProjectileScript(new BlockImpactScript(getPlugin(), projectile));

            if (config.contains(projectile.getWeaponTitle() + ".Trail"))
                projectile.addProjectileScript(new TrailScript(getPlugin(), projectile));

            if (config.contains(projectile.getWeaponTitle() + ".Cosmetics.Bullet_Zip.Sounds"))
                projectile.addProjectileScript(new ZipScript(getPlugin(), projectile));

            if (config.contains(projectile.getWeaponTitle() + ".Cosmetics.Block_Damage"))
                projectile.addProjectileScript(new BlockDamageScript(getPlugin(), projectile));

            // If the projectile has a disguise, then there is no need to show
            // splash effects (entities have splash effects in vanilla mc)
            if (config.contains(projectile.getWeaponTitle() + ".Cosmetics.Splash_Mechanics"))
                projectile.addProjectileScript(new SplashScript(getPlugin(), projectile));

            if (config.contains(projectile.getWeaponTitle() + ".Cosmetics.Block_Hit_Mechanics"))
                projectile.addProjectileScript(new BlockHitMechanicsScript(getPlugin(), projectile));
        }
    }
}
