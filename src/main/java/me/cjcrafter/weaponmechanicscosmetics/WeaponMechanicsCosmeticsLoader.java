/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics;

import me.cjcrafter.auto.AutoMechanicsDownload;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

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

        // Attempt to automatically download MechanicsCore and WeaponMechanics.
        if (getConfig().getBoolean("Auto_Download.Enabled")) {
            AutoMechanicsDownload auto = new AutoMechanicsDownload(getConfig());
            auto.MECHANICS_CORE.install();
            auto.WEAPON_MECHANICS.install();
        }

        // Don't enable the plugin if either dependencies are absent
        if (Bukkit.getPluginManager().getPlugin("MechanicsCore") == null)
            return;
        if (Bukkit.getPluginManager().getPlugin("WeaponMechanics") == null)
            return;

        plugin = new WeaponMechanicsCosmetics(this);
        plugin.onLoad();
        success = true;
    }

    @Override
    public void onEnable() {
        if (!success) {
            getLogger().log(Level.SEVERE, "");
            getLogger().log(Level.SEVERE, " !!! MechanicsCore and/or WeaponMechanics was missing");
            getLogger().log(Level.SEVERE, " !!! Download them here: https://www.spigotmc.org/resources/99913/");
            getLogger().log(Level.SEVERE, "");
            return;
        }

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
