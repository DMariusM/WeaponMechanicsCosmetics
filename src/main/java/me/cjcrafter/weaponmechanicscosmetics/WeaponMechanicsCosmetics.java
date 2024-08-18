/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics;

import com.cjcrafter.foliascheduler.FoliaCompatibility;
import com.cjcrafter.foliascheduler.MinecraftVersions;
import com.cjcrafter.foliascheduler.ServerImplementation;
import com.cjcrafter.foliascheduler.TaskImplementation;
import com.comphenix.protocol.ProtocolLibrary;
import me.cjcrafter.weaponmechanicscosmetics.commands.SkinCommand;
import me.cjcrafter.weaponmechanicscosmetics.config.BlockBreakParticleSerializer;
import me.cjcrafter.weaponmechanicscosmetics.config.BlockBreakSoundSerializer;
import me.cjcrafter.weaponmechanicscosmetics.config.BlockParticleSerializer;
import me.cjcrafter.weaponmechanicscosmetics.config.BlockSoundSerializer;
import me.cjcrafter.weaponmechanicscosmetics.listeners.*;
import me.cjcrafter.weaponmechanicscosmetics.timer.TimerSpawner;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.*;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.conditions.Condition;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import net.kyori.adventure.text.Component;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarFile;

public class WeaponMechanicsCosmetics extends JavaPlugin {

    private static JavaPlugin plugin;
    private static WeaponMechanicsCosmetics INSTANCE;

    private Metrics metrics;
    private Debugger debug;
    private Configuration config;
    private ServerImplementation scheduler;
    private ClassLoader langLoader;

    private boolean registeredMechanics;

    @Override
    public void onLoad() {
        INSTANCE = this;
        plugin = this;

        int level = getConfig().getInt("Debug_Level", 2);
        boolean printTraces = getConfig().getBoolean("Print_Traces", false);
        debug = new Debugger(getLogger(), level, printTraces);

        // Write config from jar to datafolder
        if (!getDataFolder().exists() || getDataFolder().listFiles() == null || getDataFolder().listFiles().length == 0) {
            debug.info("Copying files from jar (This process may take up to 30 seconds during the first load!)");
            FileUtil.copyResourcesTo(getClassLoader().getResource("WeaponMechanicsCosmetics"), getDataFolder().toPath());
        }

        // Search the jar file for Mechanics, Targeters, and Conditions. We
        // need to register them to the Mechanics.class registries.
        if (!registeredMechanics) {
            registeredMechanics = true;
            try {
                JarSearcher searcher = new JarSearcher(new JarFile(getFile()));

                searcher.findAllSubclasses(Mechanic.class, getClassLoader(), true)
                        .stream().map(ReflectionUtil::newInstance).forEach(Mechanics.MECHANICS::add);
                searcher.findAllSubclasses(Targeter.class, getClassLoader(), true)
                        .stream().map(ReflectionUtil::newInstance).forEach(Mechanics.TARGETERS::add);
                searcher.findAllSubclasses(Condition.class, getClassLoader(), true)
                        .stream().map(ReflectionUtil::newInstance).forEach(Mechanics.CONDITIONS::add);

            } catch (IOException ex) {
                debug.log(LogLevel.ERROR, "Error while searching Jar", ex);
            }
        }

        scheduler = new FoliaCompatibility(this).getServerImplementation();
    }

    @Override
    public void onEnable() {
        registerDebugger();
        registerBStats();

        // Register commands
        if (MinecraftVersions.UPDATE_AQUATIC.isAtLeast())
            SkinCommand.register();

        // Separate from registerListeners
        Bukkit.getPluginManager().registerEvents(new WeaponMechanicsSerializerListener(), plugin);

        // Register permissions 2 ticks after server startup
        scheduler.global().runDelayed(() -> {
            SkinCommand.registerPermissions("Skin");
            SkinCommand.registerPermissions("Hand");
        }, 2);
    }

    private void registerListeners() {
        HandlerList.unregisterAll(plugin);

        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new DeathMessageListener(), plugin);
        pm.registerEvents(new ExplosionEffectSpawner(), plugin);
        pm.registerEvents(new HitMarkerSpawner(), plugin);
        pm.registerEvents(new MuzzleFlashSpawner(), plugin);
        pm.registerEvents(new PumpkinScopeOverlay(), plugin);
        pm.registerEvents(new TimerSpawner(), plugin);
        pm.registerEvents(new WeaponMechanicsSerializerListener(), plugin);
        pm.registerEvents(new WeaponSkinListener(), plugin);
    }

    public CompletableFuture<TaskImplementation<Void>> reloadPlugin() {
        return scheduler.async().runNow(() -> {

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
        }).asFuture().thenCompose((previousTask) -> scheduler.global().run(() -> {
            // Read config
            List<Serializer<?>> serializers = new ArrayList<>();
            serializers.add(new BlockSoundSerializer());
            serializers.add(new BlockBreakSoundSerializer());
            serializers.add(new BlockParticleSerializer());
            serializers.add(new BlockBreakParticleSerializer());
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

            // Reload packet listeners
            ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
            ProtocolLibrary.getProtocolManager().addPacketListener(new CrossbowPacketListener(plugin));
        }).asFuture());
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

    private void registerBStats() {
        if (metrics != null) return;

        debug.debug("Registering bStats");

        // See https://bstats.org/plugin/bukkit/WeaponMechanicsCosmetics/15790. This is
        // the bStats plugin id used to track information.
        int id = 15790;

        this.metrics = new Metrics(plugin, id);
    }

    public String getLang(String key) {
        Locale locale = Locale.forLanguageTag(config == null ? "en-US" : config.getString("Language", "en-US"));
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

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public ServerImplementation getScheduler() {
        return scheduler;
    }

    public static WeaponMechanicsCosmetics getInstance() {
        return INSTANCE;
    }
}
