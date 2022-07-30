package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.IdentityHashMap;
import java.util.Map;

public class TimerSpawner implements Listener {

    private Map<WeaponReloadEvent, Integer> tasks;

    public TimerSpawner() {
        tasks = new IdentityHashMap<>();
    }

    @EventHandler
    public void onReload(WeaponReloadEvent event) {
    }


}
