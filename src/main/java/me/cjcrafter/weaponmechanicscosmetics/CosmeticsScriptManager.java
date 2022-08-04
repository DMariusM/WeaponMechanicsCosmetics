package me.cjcrafter.weaponmechanicscosmetics;

import me.cjcrafter.weaponmechanicscosmetics.scripts.FallingBlockScript;
import me.cjcrafter.weaponmechanicscosmetics.scripts.ProjectileBlockSoundScript;
import me.cjcrafter.weaponmechanicscosmetics.scripts.ProjectileSplashScript;
import me.cjcrafter.weaponmechanicscosmetics.scripts.ProjectileZipScript;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScriptManager;
import me.cjcrafter.weaponmechanicscosmetics.trails.Trail;
import me.cjcrafter.weaponmechanicscosmetics.trails.TrailScript;
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

        Configuration config = WeaponMechanics.getConfigurations();

        if (aProjectile instanceof WeaponProjectile) {

            WeaponProjectile projectile = (WeaponProjectile) aProjectile;
            Trail trail = config.getObject(projectile.getWeaponTitle() + ".Trail", Trail.class);

            if (trail != null)
                projectile.addProjectileScript(new TrailScript(getPlugin(), projectile, trail));

            if (config.containsKey(projectile.getWeaponTitle() + ".Cosmetics.Bullet_Zip"))
                projectile.addProjectileScript(new ProjectileZipScript(getPlugin(), projectile));

            // More generalized weapon scripts
            projectile.addProjectileScript(new ProjectileSplashScript(getPlugin(), projectile));
            projectile.addProjectileScript(new ProjectileBlockSoundScript(getPlugin(), projectile));
        }
    }
}
