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
    private final Map<Player, TimerData> equipTasks;
    private final Map<Player, TimerData> scopeTasks;
    private final Map<Player, TimerData> meleeHitTasks;
    private final Map<Player, TimerData> meleeMissTasks;
    private final Map<Player, TimerData> reloadTasks;

    public TimerSpawner() {
        equipTasks = new HashMap<>();
        scopeTasks = new HashMap<>();
        meleeHitTasks = new HashMap<>();
        meleeMissTasks = new HashMap<>();
        reloadTasks = new HashMap<>();
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
        emplace(player, equipTasks, task);
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
        emplace(player, meleeHitTasks, task);
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
        emplace(player, meleeMissTasks, task);
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
        reloadTasks.put(player, task);
    }

    @EventHandler
    public void onReloadCancel(WeaponReloadCancelEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();
        TimerData task = reloadTasks.get(player);
        if (task == null)
            return;

        task.cancel();
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onScope(WeaponScopeEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        Timer timer = config.getObject(event.getWeaponTitle() + ".Timer.Shoot_Delay_After_Scope", Timer.class);
        if (timer == null)
            return;

        int delay = config.getInt(event.getWeaponTitle() + ".Scope.Shoot_Delay_After_Scope") / 50;
        timer.play((Player) event.getShooter(), event.getWeaponStack(), delay);
    }

    //@EventHandler
    //public void onFirearm() {}

    //@EventHandler
    //public void onShoot() {}



    //
    // * TIMER CANCEL HANDLER CODE
    //
    // The following code handlers cancelling the timer(s) when the player
    // stops it (Via swapping hands, for example).

    @EventHandler
    public void onReloadComplete(WeaponReloadCompleteEvent event) {
        reloadTasks.remove(event.getEntity());
    }

    @EventHandler
    public void onDequip(EntityEquipmentEvent event) {
        if (!event.isDequipping() || event.isArmor() || event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();
        TimerData task = equipTasks.get(player);
        if (task == null)
            return;

        task.cancel();
    }

    @EventHandler
    public void onDequip(PlayerItemHeldEvent event) {
        if (ReflectionUtil.getMCVersion() > 10)
            return;

        TimerData task = equipTasks.get(event.getPlayer());
        if (task == null)
            return;

        task.cancel();
    }

    public void cancel(Player player) {
        cancel(player, equipTasks);
        cancel(player, scopeTasks);
        cancel(player, meleeHitTasks);
        cancel(player, meleeMissTasks);
    }

    private static void cancel(Player player, Map<Player, TimerData> map) {
        TimerData timer = map.remove(player);
        if (timer != null)
            timer.cancel();
    }

    private static void emplace(Player player, Map<Player, TimerData> map, TimerData replacement) {
        TimerData old = map.put(player, replacement);
        if (old != null)
            old.cancel();
    }
}
