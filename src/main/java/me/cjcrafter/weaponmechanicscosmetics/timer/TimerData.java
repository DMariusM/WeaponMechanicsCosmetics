package me.cjcrafter.weaponmechanicscosmetics.timer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Stores the {@link Timer} task id and the time to complete the timer.
 */
public class TimerData {

    public final Timer timer;
    public final Player player;
    public final World world;
    public final ItemStack weapon;
    public final int taskId;
    public final long startTicks;
    public final int totalTicks;

    public TimerData(Timer timer, Player player, ItemStack weapon, int taskId, int totalTicks) {
        this.timer = timer;
        this.player = player;
        this.world = player.getWorld();
        this.weapon = weapon;
        this.taskId = taskId;
        this.startTicks = world.getFullTime();
        this.totalTicks = totalTicks;
    }

    public int getElapsedTime() {

        // Just as an extra check, don't use `player.getWorld()`, just in case
        // the player teleports and causes a desync. Instead, we simply reuse
        // the old world.
        return (int) (world.getFullTime() - startTicks);
    }

    public void cancel() {
        // First check if the event is cancelled already
        if (!Bukkit.getScheduler().isQueued(taskId))
            return;

        Bukkit.getScheduler().cancelTask(taskId);
        timer.cancel(this);
    }
}
