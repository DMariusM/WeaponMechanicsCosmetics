package me.cjcrafter.weaponmechanicscosmetics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.logging.Level;

public class WeaponMechanicsCosmeticsLoader extends JavaPlugin {

    private WeaponMechanicsCosmetics plugin;

    @Override
    public void onLoad() {
        ensureWeaponMechanics();
        if (Bukkit.getPluginManager().getPlugin("WeaponMechanics") == null) {
            return;
        }

        plugin = new WeaponMechanicsCosmetics(this);
        plugin.onLoad();
    }

    @Override
    public void onDisable() {
        plugin.onDisable();
    }

    @Override
    public void onEnable() {
        plugin.onEnable();
    }

    ClassLoader getClassLoader0() {
        return getClassLoader();
    }

    File getFile0() {
        return getFile();
    }

    private void ensureWeaponMechanics() {

        // WeaponMechanicsCosmetics NEEDS WeaponMechanics to run, however, people have the
        // incredible ability of having 0 abilities. AKA, they cannot read
        // "UnknownDependencyException". Instead of writing out a stacktrace
        // and having some people ask for help, we should:
        //      A. Try to download/copy/install MechanicsCore for them
        //      B. Disable the plugin
        if (Bukkit.getPluginManager().getPlugin("MechanicsCore") == null) {

            getLogger().log(Level.WARNING, "Missing MechanicsCore.jar, we will try to install it automatically",
                    "To disable this, go to the WeaponMechanics config.yml file");
            boolean installed = false;

            try {
                // Downloading WeaponMechanics will also cause WeaponMechanics
                // to download MechanicsCore, if needed.
                VersionParser downloader = new VersionParser();
                downloader.runWeapon();
                installed = true;
            } catch (IOException | InvalidDescriptionException | InvalidPluginException e) {
                getLogger().log(Level.WARNING, "Error download MechanicsCore", e);
            }

            if (installed) {
                getLogger().log(Level.INFO, "Successfully downloaded MechanicsCore");
                return;
            }

            // This will happen if the user has disabled the auto-downloader,
            // or if the download failed for any reason.
            getLogger().log(Level.SEVERE, " !!!");
            getLogger().log(Level.SEVERE, "WeaponMechanicsCosmetics requires WeaponMechanics and MechanicsCore in order to run!");
            getLogger().log(Level.SEVERE, "Please download those plugins here: https://www.spigotmc.org/resources/.99913/");
            getLogger().log(Level.SEVERE, "Disabling WeaponMechanics to avoid error.");

            getPluginLoader().disablePlugin(this);
        }
    }

    private class VersionParser {

        private String coreVersion;
        private String weaponVersion;
        private String packVersion;

        private VersionParser() throws IOException {
            String link = "https://github.com/WeaponMechanics/MechanicsMain/releases/latest/download/versions.txt";
            URL url = new URL(link);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(getConfig().getInt("Weapon_Mechanics_Download.Connection_Timeout", 10) * 1000); // 10 seconds
            connection.setReadTimeout(getConfig().getInt("Weapon_Mechanics_Download.Read_Timeout", 30) * 1000); // 30 seconds

            InputStream in = connection.getInputStream();
            Scanner scanner = new Scanner(in);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty())
                    continue;

                String[] split = line.split(": ?");
                String id = split[0];
                String version = split[1];

                switch (id) {
                    case "MechanicsCore":
                        coreVersion = version;
                        break;
                    case "WeaponMechanics":
                        weaponVersion = version;
                        break;
                    case "WeaponMechanicsResourcePack":
                        packVersion = version;
                        break;
                }
            }
        }

        private void downloadWeapon(File target) throws IOException {
            String link = "https://github.com/WeaponMechanics/MechanicsMain/releases/latest/download/WeaponMechanics-" + weaponVersion + ".jar";
            URL url = new URL(link);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(getConfig().getInt("Mechanics_Core_Download.Connection_Timeout", 10) * 1000); // 10 seconds
            connection.setReadTimeout(getConfig().getInt("Mechanics_Core_Download.Read_Timeout", 30) * 1000); // 30 seconds

            InputStream in = connection.getInputStream();
            Files.copy(in, target.toPath());
        }

        private void runWeapon() throws IOException, InvalidPluginException, InvalidDescriptionException {
            File target = new File(getDataFolder().getParent(), "WeaponMechanics-" + weaponVersion + ".jar");
            downloadWeapon(target);
            Plugin plugin = Bukkit.getPluginManager().loadPlugin(target);
            plugin.onLoad();
        }
    }
}
