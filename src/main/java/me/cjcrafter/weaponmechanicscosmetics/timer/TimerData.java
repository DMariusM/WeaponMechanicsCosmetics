package me.cjcrafter.weaponmechanicscosmetics.timer;

/**
 * Stores the {@link Timer} task id and the time to complete the timer.
 */
public class TimerData {

    public final int taskId;
    public final int totalTicks;

    public TimerData(int taskId, int totalTicks) {
        this.taskId = taskId;
        this.totalTicks = totalTicks;
    }
}
