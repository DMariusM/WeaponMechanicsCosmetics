package me.cjcrafter.weaponmechanicscosmetics;

import me.deecaad.core.MechanicsCore;
import org.bukkit.plugin.java.JavaPlugin;

public class WeaponMechanicsCosmeticsLoader extends JavaPlugin {

    @Override
    public void onEnable() {
        MechanicsCore.debug.info("WeaponMechanicsCosmetics injected trails");
        new CosmeticsScriptManager(this).register();
    }
}
