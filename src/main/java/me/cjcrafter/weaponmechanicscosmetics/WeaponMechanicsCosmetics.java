package me.cjcrafter.weaponmechanicscosmetics;

import me.cjcrafter.weaponmechanicscosmetics.listeners.ExplosionEffectSpawner;
import me.cjcrafter.weaponmechanicscosmetics.listeners.MuzzleFlashSpawner;
import me.cjcrafter.weaponmechanicscosmetics.scripts.ProjectileBlockSoundScript;
import me.cjcrafter.weaponmechanicscosmetics.timer.TimerSpawner;
import me.cjcrafter.weaponmechanicscosmetics.listeners.WeaponMechanicsSerializerListener;
import me.cjcrafter.weaponmechanicscosmetics.trails.ParticleMechanic;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.FileReader;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class WeaponMechanicsCosmetics {

    private static WeaponMechanicsCosmetics INSTANCE;
    private WeaponMechanicsCosmeticsLoader plugin;
    private Metrics metrics;
    private Debugger debug;
    private Configuration config;

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

        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new ExplosionEffectSpawner(), plugin);
        pm.registerEvents(new MuzzleFlashSpawner(), plugin);
        pm.registerEvents(new TimerSpawner(), plugin);
        pm.registerEvents(new WeaponMechanicsSerializerListener(), plugin);

        Mechanics.registerMechanic(plugin, new ParticleMechanic());

        registerBStats();
    }

    public void reloadConfig() {

        // Make sure config.yml exists
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            FileUtil.copyResourcesTo(getClassLoader().getResource("WeaponMechanicsCosmetics/config.yml"), file.toPath());
        }

        // Read config
        List<Serializer<?>> serializers = new ArrayList<>();
        serializers.add(new ProjectileBlockSoundScript.BlockSound());

        List<IValidator> validators = new ArrayList<>();
        validators.add(new ExplosionEffectSpawner.ExplosionEffectValidator());

        FileReader reader = new FileReader(debug, serializers, validators);
        config = reader.fillOneFile(file);
        reader.usePathToSerializersAndValidators(config);

        WeaponMechanics.getProjectilesRunnable().addScriptManager(new CosmeticsScriptManager(plugin));
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

    private void registerBStats() {
        if (metrics != null) return;

        debug.debug("Registering bStats");

        // See https://bstats.org/plugin/bukkit/WeaponMechanicsCosmetics/15790. This is
        // the bStats plugin id used to track information.
        int id = 15790;

        this.metrics = new Metrics(plugin, id);
    }


    public WeaponMechanicsCosmeticsLoader getPlugin() {
        return plugin;
    }

    public static WeaponMechanicsCosmetics getInstance() {
        return INSTANCE;
    }
}
