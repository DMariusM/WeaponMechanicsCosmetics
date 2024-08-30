/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.timer;

import com.cjcrafter.foliascheduler.ServerImplementation;
import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.events.EntityEquipmentEvent;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.weaponevents.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class TimerSpawner implements Listener {

    private final Map<Player, TimerData> tasks;

    public TimerSpawner() {
        tasks = new HashMap<>();
    }

    @EventHandler
    public void onEquip(WeaponEquipEvent event) {
        // We have to run this 1 tick later, since otherwise the timer would
        // be cancelled by onDequip(PlayerItemHeldEvent).
        ServerImplementation scheduler = WeaponMechanicsCosmetics.getInstance().getScheduler();
        scheduler.entity(event.getShooter()).run(() -> {
            playTimer(event, ".Show_Time.Weapon_Equip_Delay", ".Info.Weapon_Equip_Delay");
        });
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMeleeHit(WeaponMeleeHitEvent event) {
        playTimer(event, ".Show_Time.Melee_Hit_Delay", event.getMeleeHitDelay());
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMeleeMiss(WeaponMeleeMissEvent event) {
        playTimer(event, ".Show_Time.Melee_Miss_Delay", event.getMeleeMissDelay());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onReload(WeaponReloadEvent event) {
        playTimer(event, ".Show_Time.Reload", event.getReloadCompleteTime());
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onScope(WeaponScopeEvent event) {
        if (event.getScopeType() != WeaponScopeEvent.ScopeType.IN)
            return;

        playTimer(event, ".Show_Time.Shoot_Delay_After_Scope", ".Scope.Shoot_Delay_After_Scope");
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFirearm(WeaponFirearmEvent event) {
        playTimer(event, ".Show_Time.Firearm_Actions", event.getTime());
    }

    @EventHandler
    public void onShoot(WeaponPostShootEvent event) {
        // weaponStack is null, it was shot through the API
        if (event.getWeaponStack() == null)
            return;

        playTimer(event, ".Show_Time.Delay_Between_Shots", ".Shoot.Delay_Between_Shots");
    }

    private void playTimer(WeaponEvent event, String timerPath, String delayPath) {
        Configuration config = WeaponMechanics.getConfigurations();
        int ticks = config.getInt(event.getWeaponTitle() + delayPath) / 50; // divide by 50 for millis -> ticks
        playTimer(event, timerPath, ticks);
    }

    /**
     * Pulls the timer from config and starts it with the given amount of time,
     * in ticks. If the amount of ticks is defined in config, use
     * {@link #playTimer(WeaponEvent, String, String)}.
     *
     * @param event     The non-null event that triggered the timer.
     * @param timerPath The path to the {@link Timer} in config.
     * @param ticks     The time, in ticks, to play the timer.
     */
    private void playTimer(WeaponEvent event, String timerPath, int ticks) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + timerPath, Timer.class);
        if (timer == null)
            return;

        TimerData task = timer.play((Player) event.getShooter(), event.getWeaponStack().clone(), ticks);
        TimerData old = tasks.put(task.player, task);
        if (old != null)
            old.cancel();
    }


    //
    // * TIMER CANCEL HANDLER CODE
    //
    // The following code handlers cancelling the timer(s) when the player
    // stops it (Via swapping hands, for example).

    @EventHandler
    public void onReloadCancel(WeaponReloadCancelEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();
        TimerData task = tasks.get(player);
        if (task == null)
            return;

        task.cancel();
    }

    @EventHandler
    public void onReloadComplete(WeaponReloadCompleteEvent event) {
        tasks.remove(event.getEntity());
    }

    @EventHandler
    public void onDequip(EntityEquipmentEvent event) {
        if (!event.isDequipping() || event.isArmor() || event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();
        TimerData task = tasks.remove(player);
        if (task == null)
            return;

        task.cancel();
    }
}
