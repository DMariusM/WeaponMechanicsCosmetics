/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics;

import me.cjcrafter.auto.UpdateChecker;
import me.cjcrafter.auto.UpdateInfo;
import me.cjcrafter.weaponmechanicscosmetics.commands.SkinCommand;
import me.cjcrafter.weaponmechanicscosmetics.listeners.*;
import me.cjcrafter.weaponmechanicscosmetics.scripts.BlockSoundScript;
import me.cjcrafter.weaponmechanicscosmetics.timer.TimerSpawner;
import me.cjcrafter.weaponmechanicscosmetics.general.ParticleMechanic;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.*;
import me.deecaad.core.lib.adventure.text.Component;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Logger;

public class WeaponMechanicsCosmetics {

    private static WeaponMechanicsCosmetics INSTANCE;

    private WeaponMechanicsCosmeticsLoader plugin;
    private UpdateChecker update;
    private Metrics metrics;
    private Debugger debug;
    private Configuration config;
    private ClassLoader langLoader;

    WeaponMechanicsCosmetics(WeaponMechanicsCosmeticsLoader plugin) {
        this.plugin = plugin;

        INSTANCE = this;
    }

    public void onLoad() {
        int level = getConfig().getInt("Debug_Level", 2);
        boolean printTraces = getConfig().getBoolean("Print_Traces", false);
        debug = new Debugger(getLogger(), level, printTraces);

        // Write config from jar to datafolder
        if (!getDataFolder().exists() || getDataFolder().listFiles() == null || getDataFolder().listFiles().length == 0) {
            debug.info("Copying files from jar (This process may take up to 30 seconds during the first load!)");
            FileUtil.copyResourcesTo(getClassLoader().getResource("WeaponMechanicsCosmetics"), getDataFolder().toPath());
        }
    }

    public void onEnable() {

        Mechanics.registerMechanic(plugin, new ParticleMechanic());

        registerDebugger();
        registerBStats();
        registerUpdateChecker();

        // Register commands
        SkinCommand.register();

        // Separate from registerListeners
        Bukkit.getPluginManager().registerEvents(new WeaponMechanicsSerializerListener(), plugin);

        // Register permissions 2 ticks after server startup
        new BukkitRunnable() {
            @Override
            public void run() {
                SkinCommand.registerPermissions("Skin");
                SkinCommand.registerPermissions("Hand");
            }
        }.runTaskLater(plugin, 2);
    }

    private void registerListeners() {
        HandlerList.unregisterAll(plugin);

        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new ExplosionEffectSpawner(), plugin);
        pm.registerEvents(new HitMarkerSpawner(), plugin);
        pm.registerEvents(new MuzzleFlashSpawner(), plugin);
        pm.registerEvents(new PumpkinScopeOverlay(), plugin);
        pm.registerEvents(new TimerSpawner(), plugin);
        pm.registerEvents(new WeaponMechanicsSerializerListener(), plugin);
        pm.registerEvents(new WeaponSkinListener(), plugin);
    }

    public TaskChain reloadConfig() {
        return new TaskChain(plugin)
                .thenRunAsync(() -> {

                    // Writes files
                    if (!getDataFolder().exists() || getDataFolder().listFiles() == null || getDataFolder().listFiles().length == 0) {
                        debug.info("Copying files from jar (This process may take up to 30 seconds during the first load!)");
                        FileUtil.copyResourcesTo(getClassLoader().getResource("WeaponMechanicsCosmetics"), getDataFolder().toPath());
                    }

                    // Make sure config.yml exists
                    File config = new File(getDataFolder(), "config.yml");
                    FileUtil.ensureDefaults(getClassLoader().getResource("WeaponMechanicsCosmetics/config.yml"), config);

                    // Make sure the lang folder exists, and save resource locations
                    File langFolder = new File(getDataFolder(), "lang");
                    if (!langFolder.exists() || langFolder.listFiles() == null || langFolder.listFiles().length == 0) {
                        FileUtil.copyResourcesTo(getClassLoader().getResource("WeaponMechanicsCosmetics/lang"), langFolder.toPath());
                    }
                    try {
                        langLoader = new URLClassLoader(new URL[]{ langFolder.toURI().toURL() });
                    } catch (MalformedURLException e) {
                        debug.log(LogLevel.ERROR, "Error while loading Lang", e);
                    }
                })
                .thenRunSync(() -> {

                    // Read config
                    List<Serializer<?>> serializers = new ArrayList<>();
                    serializers.add(new BlockSoundScript.BlockSound());
                    serializers.add(new PumpkinScopeOverlay.PumpkinCreativeSerializer());
                    serializers.add(new PumpkinScopeOverlay.PumpkinSurvivalSerializer());

                    List<IValidator> validators = new ArrayList<>();
                    validators.add(new ExplosionEffectSpawner.ExplosionEffectValidator());

                    FileReader reader = new FileReader(debug, serializers, validators);
                    File file = new File(getDataFolder(), "config.yml");
                    config = reader.fillOneFile(file);
                    reader.usePathToSerializersAndValidators(config);

                    debug.info("Reloading plugin");
                    WeaponMechanics.getProjectilesRunnable().addScriptManager(new CosmeticsScriptManager(plugin));
                    registerListeners();
                });
    }

    public void onDisable() {
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }

    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    public ClassLoader getClassLoader() {
        return plugin.getClassLoader0();
    }

    public Debugger getDebug() {
        return debug;
    }

    public Configuration getConfiguration() {
        return config;
    }

    private void registerDebugger() {
        debug.permission = "weaponmechanicscosmetics.errorlog";
        debug.msg = "WeaponMechanicsCosmetics had %s error(s) in console.";
        debug.start(plugin);
    }

    private void registerUpdateChecker() {
        update = new UpdateChecker(plugin, UpdateChecker.spigot(104539, "WeaponMechanicsCosmetics"));

        try {
            UpdateInfo consoleUpdate = update.hasUpdate();
            if (consoleUpdate != null) {
                sendLang(Bukkit.getConsoleSender(), "update-checker", Map.of("old", consoleUpdate.current.toString(), "new", consoleUpdate.newest.toString()));
            }
        } catch (Throwable ex) {
            debug.log(LogLevel.DEBUG, "UpdateChecker error", ex);
            debug.error("UpdateChecker failed to connect: " + ex.getMessage());
            return;
        }

        Listener listener = new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                if (event.getPlayer().isOp()) {
                    new TaskChain(plugin)
                            .thenRunAsync((callback) -> update.hasUpdate())
                            .thenRunSync((callback) -> {
                                UpdateInfo update = (UpdateInfo) callback;
                                if (callback != null)
                                    sendLang(event.getPlayer(),"update-checker", Map.of("old", update.current.toString(), "new", update.newest.toString()));

                                return null;
                            });
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    private void registerBStats() {
        if (metrics != null) return;

        debug.debug("Registering bStats");

        // See https://bstats.org/plugin/bukkit/WeaponMechanicsCosmetics/15790. This is
        // the bStats plugin id used to track information.
        int id = 15790;

        this.metrics = new Metrics(plugin, id);
    }

    public String getLang(String key) {
        Locale locale = Locale.forLanguageTag(config.getString("Language", "en-US"));
        ResourceBundle lang = ResourceBundle.getBundle("Lang", locale, langLoader);

        try {
            return lang.getString(key);
        } catch (MissingResourceException ex) {
            debug.log(LogLevel.DEBUG, "Missing key '" + key + "'", ex);
            debug.error("Found a missing language key '" + key + "' in 'Lang_" + locale + ".properties'");
            return "missing-lang-key";
        }
    }

    public void sendLang(CommandSender sender, String key) {
        sendLang(sender, key, Collections.emptyMap());
    }

    public void sendLang(CommandSender sender, String key, Map<String, String> variables) {
        String msg = getLang(key);

        int startIndex = -1;

        StringBuilder temp = new StringBuilder(msg.length());
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c != '%') {
                if (startIndex == -1) temp.append(c);
                continue;
            }

            // Start tracking for substring
            if (startIndex == -1) {
                startIndex = i;
                continue;
            }

            // %% counts as escaped character
            if (i - startIndex == 1) {
                temp.append('%');
                continue;
            }

            String substring = msg.substring(startIndex + 1, i);
            temp.append(variables.get(substring));
            startIndex = -1;
        }

        Component component = MechanicsCore.getPlugin().message.deserialize(temp.toString());
        MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(component);
    }

    public WeaponMechanicsCosmeticsLoader getPlugin() {
        return plugin;
    }

    public static WeaponMechanicsCosmetics getInstance() {
        return INSTANCE;
    }
}
