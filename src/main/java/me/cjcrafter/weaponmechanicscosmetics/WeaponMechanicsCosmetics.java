package me.cjcrafter.weaponmechanicscosmetics;

import me.cjcrafter.weaponmechanicscosmetics.listeners.TrailListener;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.FileReader;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.LinkedConfig;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class WeaponMechanicsCosmetics {

    private static WeaponMechanicsCosmetics INSTANCE;
    private WeaponMechanicsCosmeticsLoader plugin;
    private Debugger debug;
    private Configuration basicConfiguration;

    WeaponMechanicsCosmetics(WeaponMechanicsCosmeticsLoader plugin) {
        this.plugin = plugin;

        INSTANCE = this;
    }

    // * ----- HELPER METHODS ----- * //
    public Logger getLogger() { return plugin.getLogger(); }
    public FileConfiguration getConfig() { return plugin.getConfig(); }
    public File getDataFolder() { return plugin.getDataFolder(); }
    public ClassLoader getClassLoader() { return plugin.getClassLoader0();}
    public File getFile() { return plugin.getFile0(); }

    // * ----- STANDARD PLUGIN METHODS ----- * //
    public void onLoad() {
        setupDebugger();
    }

    public void onEnable() {
        long millisCurrent = System.currentTimeMillis();
        INSTANCE = this;

        writeFiles();
        registerEvents();

        //registerBStats();

        long tookMillis = System.currentTimeMillis() - millisCurrent;
        double seconds = NumberUtil.getAsRounded(tookMillis * 0.001, 2);
        debug.info("Enabled WeaponMechanicsCosmetics in " + seconds + "s");
        debug.start(plugin);
    }

    public void onDisable() {
        HandlerList.unregisterAll(plugin);
    }

    // * ----- INTERNAl METHODS ----- * //
    void setupDebugger() {
        Logger logger = getLogger();
        int level = getConfig().getInt("Debug_Level", 2);
        boolean isPrintTraces = getConfig().getBoolean("Print_Traces", false);
        debug = new Debugger(logger, level, isPrintTraces);
        debug.permission = "mechanicscore.errorlog";
        debug.msg = "WeaponMechanics had %s error(s) in console.";
    }

    void writeFiles() {
        debug.debug("Writing files and filling basic configuration");

        // Create files
        if (!getDataFolder().exists() || getDataFolder().listFiles() == null || getDataFolder().listFiles().length == 0) {
            debug.info("Copying files from jar (This process may take up to 30 seconds during the first load!)");
            try {
                FileUtil.copyResourcesTo(getClassLoader().getResource("WeaponMechanicsCosmetics"), getDataFolder().toPath());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }

        try {
            // TODO bad programmars comment out broken code
            //FileUtil.ensureDefaults(getClassLoader(), "WeaponMechanics/config.yml", new File(getDataFolder(), "config.yml"));
        } catch (YAMLException e) {
            debug.error("WeaponMechanics jar corruption... This is most likely caused by using /reload after building jar!");
        }

        // Fill config.yml mappings
        File configyml = new File(getDataFolder(), "config.yml");
        if (configyml.exists()) {
            List<IValidator> validators = new ArrayList<>();

            FileReader basicConfigurationReader = new FileReader(debug, null, validators);
            Configuration filledMap = basicConfigurationReader.fillOneFile(configyml);
            basicConfiguration = basicConfigurationReader.usePathToSerializersAndValidators(filledMap);
        } else {
            // Just creates empty map to prevent other issues
            basicConfiguration = new LinkedConfig();
            debug.log(LogLevel.WARN,
                    "Could not locate config.yml?",
                    "Make sure it exists in path " + getDataFolder() + "/config.yml");
        }
    }

    void registerEvents() {
        PluginManager manager = plugin.getServer().getPluginManager();
        manager.registerEvents(new TrailListener(), plugin);
    }

    public WeaponMechanicsCosmeticsLoader getPlugin() {
        return plugin;
    }

    public static WeaponMechanicsCosmetics getInstance() {
        return INSTANCE;
    }
}
