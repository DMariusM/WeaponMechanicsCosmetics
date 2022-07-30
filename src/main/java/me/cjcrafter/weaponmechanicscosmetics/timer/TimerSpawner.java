package me.cjcrafter.weaponmechanicscosmetics.timer;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.weaponevents.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class TimerSpawner implements Listener {

    private final Map<Player, Integer> tasks;

    public TimerSpawner() {
        tasks = new HashMap<>();
    }

    @EventHandler
    public void onEquip(WeaponEquipEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + ".Timer.Weapon_Equip_Delay", Timer.class);
        if (timer == null)
            return;

        int delay = config.getInt(event.getWeaponTitle() + ".Info.Weapon_Equip_Delay") / 50; // todo TICKS!!!! STOP WITH THE MILLIS
        timer.play((Player) event.getShooter(), event.getWeaponStack(), delay);
    }

    //@EventHandler
    //public void onMeleeHit() {}

    //@EventHandler
    //public void onMeleeMiss() {}

    @EventHandler (priority = EventPriority.MONITOR)
    public void onReload(WeaponReloadEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + ".Timer.Reload_Timer", Timer.class);
        if (timer == null)
            return;

        Player player = (Player) event.getShooter();
        int delay = event.getReloadCompleteTime();
        int task = timer.play(player, event.getWeaponStack(), delay);
        tasks.put(player, task);
    }

    @EventHandler
    public void onReloadCancel(WeaponReloadCancelEvent event) {
        int task = tasks.getOrDefault(event.getEntity(), -1);
        if (task == -1)
            return;

        Bukkit.getScheduler().cancelTask(task);
    }

    @EventHandler
    public void onReloadComplete(WeaponReloadCompleteEvent event) {
        tasks.remove(event.getEntity());
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onScope(WeaponScopeEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + ".Timer.Shoot_Delay_After_Scope", Timer.class);
        if (timer == null)
            return;

        int delay = config.getInt(event.getWeaponTitle() + ".Scope.Shoot_Delay_After_Scope") / 50; // todo TICKS!!!! STOP WITH THE MILLIS
        timer.play((Player) event.getShooter(), event.getWeaponStack(), delay);
    }

    //@EventHandler
    //public void onFirearm() {}

    //@EventHandler
    //public void onShoot() {}
}
