/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.timer;

import me.deecaad.core.events.EntityEquipmentEvent;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.weaponevents.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.HashMap;
import java.util.Map;

public class TimerSpawner implements Listener {

    // TODO test usage of IdentityHashMap for potential performance boost
    private final Map<Player, TimerData> tasks;

    public TimerSpawner() {
        tasks = new HashMap<>();
    }

    @EventHandler
    public void onEquip(WeaponEquipEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + ".Show_Time.Weapon_Equip_Delay", Timer.class);
        if (timer == null)
            return;

        Player player = (Player) event.getShooter();
        int delay = config.getInt(event.getWeaponTitle() + ".Info.Weapon_Equip_Delay") / 50;
        TimerData task = timer.play(player, event.getWeaponStack(), delay);
        emplace(task);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMeleeHit(WeaponMeleeHitEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + ".Show_Time.Melee_Hit_Delay", Timer.class);
        if (timer == null)
            return;

        Player player = (Player) event.getShooter();
        int delay = event.getMeleeHitDelay();
        TimerData task = timer.play(player, event.getWeaponStack(), delay);
        emplace(task);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMeleeMiss(WeaponMeleeMissEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + ".Show_Time.Melee_Miss_Delay", Timer.class);
        if (timer == null)
            return;

        Player player = (Player) event.getShooter();
        int delay = event.getMeleeMissDelay();
        TimerData task = timer.play(player, event.getWeaponStack(), delay);
        emplace(task);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onReload(WeaponReloadEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + ".Show_Time.Reload", Timer.class);
        if (timer == null)
            return;

        Player player = (Player) event.getShooter();
        int delay = event.getReloadCompleteTime();
        TimerData task = timer.play(player, event.getWeaponStack(), delay);
        emplace(task);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onScope(WeaponScopeEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + ".Show_Time.Shoot_Delay_After_Scope", Timer.class);
        if (timer == null)
            return;

        int delay = config.getInt(event.getWeaponTitle() + ".Scope.Shoot_Delay_After_Scope") / 50;
        TimerData task = timer.play((Player) event.getShooter(), event.getWeaponStack(), delay);
        emplace(task);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFirearm(WeaponFirearmEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + ".Show_Time.Firearm_Actions", Timer.class);
        if (timer == null)
            return;

        TimerData task = timer.play((Player) event.getShooter(), event.getWeaponStack(), event.getTime());
        emplace(task);
    }

    @EventHandler
    public void onShoot(WeaponPostShootEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        // Extra check... when weaponStack is null, it was shot through the API
        if (event.getWeaponStack() == null)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + ".Show_Time.Delay_Between_Shots", Timer.class);
        if (timer == null)
            return;

        int delay = config.getInt(event.getWeaponTitle() + ".Shoot.Delay_Between_Shots") / 50;
        TimerData task = timer.play((Player) event.getShooter(), event.getWeaponStack(), delay);
        emplace(task);
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

    @EventHandler
    public void onDequip(PlayerItemHeldEvent event) {
        if (ReflectionUtil.getMCVersion() > 10)
            return;

        TimerData task = tasks.remove(event.getPlayer());
        if (task == null)
            return;

        task.cancel();
    }

    private void emplace(TimerData replacement) {
        TimerData old = tasks.put(replacement.player, replacement);
        if (old != null)
            old.cancel();
    }
}
