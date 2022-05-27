package me.deecaad.weaponmechanicscosmetics;

public class WeaponMechanicsCosmetics {

    private static WeaponMechanicsCosmetics INSTANCE;
    private WeaponMechanicsCosmeticsLoader plugin;

    WeaponMechanicsCosmetics(WeaponMechanicsCosmeticsLoader plugin) {
        this.plugin = plugin;

        INSTANCE = this;
    }

    public WeaponMechanicsCosmeticsLoader getPlugin() {
        return plugin;
    }

    public static WeaponMechanicsCosmetics getInstance() {
        return INSTANCE;
    }
}
