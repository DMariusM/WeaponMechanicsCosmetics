package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.cjcrafter.weaponmechanicscosmetics.trails.Trail;
import me.cjcrafter.weaponmechanicscosmetics.trails.TrailScript;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponShootEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class TrailListener implements Listener {

    @EventHandler
    public void onProjectileStart(WeaponShootEvent event) {
        Configuration config = WeaponMechanics.getConfigurations();
        Trail trail = config.getObject(event.getWeaponTitle() + ".Trail", Trail.class);

        if (trail == null)
            return;

        Plugin plugin = WeaponMechanicsCosmetics.getInstance().getPlugin();
        TrailScript script = new TrailScript(plugin, event.getProjectile(), trail);
        event.getProjectile().addProjectileScript(script);
    }
}
