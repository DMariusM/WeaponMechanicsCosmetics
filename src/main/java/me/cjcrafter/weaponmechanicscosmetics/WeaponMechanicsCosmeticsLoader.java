package me.cjcrafter.weaponmechanicscosmetics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * The entire goal of this class is to manually download and run
 * {@link me.deecaad.weaponmechanics.WeaponMechanics} if it is not detected on
 * the server. This class MUST be separate from
 * {@link WeaponMechanicsCosmetics}, since otherwise we would get missing class
 * exceptions.
 */
public class WeaponMechanicsCosmeticsLoader extends JavaPlugin {

    private WeaponMechanicsCosmetics plugin;
    private boolean success;

    @Override
    public void onLoad() {

        if (Bukkit.getPluginManager().getPlugin("MechanicsCore") == null) {
            return;
        }

        plugin = new WeaponMechanicsCosmetics(this);
        plugin.onLoad();
        success = true;
    }

    @Override
    public void onEnable() {
        plugin.onEnable();
    }

    @Override
    public void onDisable() {
        if (!success)
            return;

        plugin.onDisable();
        success = false;
    }

    ClassLoader getClassLoader0() {
        return getClassLoader();
    }

    File getFile0() {
        return getFile();
    }
}
