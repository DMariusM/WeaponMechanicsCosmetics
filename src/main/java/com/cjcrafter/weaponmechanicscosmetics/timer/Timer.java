/*
 * Copyright (c) 2022-2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.timer;

import com.cjcrafter.foliascheduler.ServerImplementation;
import com.cjcrafter.foliascheduler.TaskImplementation;
import com.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderMessage;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.utils.CustomTag;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.function.Consumer;

/**
 * Shows a timer in the item cool down, actionbar, title, boss bar, or
 * experience. This is useful for showing a cool down or some other kind of
 * count down.
 */
public class Timer implements Serializer<Timer>{

    public static final DecimalFormat ROUND = new DecimalFormat("0.0");
    public static final int DEFAULT_TICK_RATE = 1;
    public static final Title.Times TITLE_TIMES = Title.Times.times(Ticks.duration(0), Ticks.duration(2), Ticks.duration(5));

    private boolean showItemCooldown;
    private PlaceholderMessage actionBar;
    private PlaceholderMessage actionBarCancelled;
    private PlaceholderMessage title;
    private PlaceholderMessage subtitle;
    private PlaceholderMessage bossBar;
    private BossBar.Color color;
    private BossBar.Overlay style;
    private boolean showExp;
    private StringBar bar;

    /**
     * Default constructor for serializer.
     */
    public Timer() {
    }

    public Timer(boolean showItemCooldown, String actionBar, String actionBarCancelled, String title, String subtitle,
                 String bossBar, BossBar.Color color, BossBar.Overlay style, boolean showExp, StringBar bar) {

        this.showItemCooldown = showItemCooldown;
        this.actionBar = actionBar == null ? null : new PlaceholderMessage(actionBar);
        this.actionBarCancelled = actionBarCancelled == null ? null : new PlaceholderMessage(actionBarCancelled);
        this.title = title == null ? null : new PlaceholderMessage(title);
        this.subtitle = subtitle == null ? null : new PlaceholderMessage(subtitle);
        this.bossBar = bossBar == null ? null : new PlaceholderMessage(bossBar);
        this.color = color;
        this.style = style;
        this.showExp = showExp;
        this.bar = bar;
    }

    /**
     * Plays the timer effects for the given player. The returned value
     * contains the task id for {@link org.bukkit.scheduler.BukkitScheduler#cancelTask(int)},
     * as well as the value of <code>totalTicks</code>. This is useful if you
     * want to call the {@link #cancel(Player, ItemStack, int, int)} method to
     * end the timer early.
     *
     * @param player     The non-null player to show the messages.
     * @param weapon     The nullable item that has the cool-down.
     * @param totalTicks The amount of time to show the timer for.
     * @return The non-null task id and expected time to complete.
     */
    public TimerData play(Player player, ItemStack weapon, int totalTicks) {
        if (showItemCooldown && weapon != null)
            player.setCooldown(weapon.getType(), totalTicks);

        send(player, weapon, 0, totalTicks);

        ServerImplementation scheduler = WeaponMechanicsCosmetics.getInstance().getFoliaScheduler();
        TaskImplementation<Void> task = scheduler.async().runAtFixedRate(new Consumer<>() {
            int ticks = 0;

            @Override
            public void accept(TaskImplementation<Void> voidTaskImplementation) {
                if ((ticks += DEFAULT_TICK_RATE) >= totalTicks) {
                    Timer.this.cancel(player, weapon, ticks, totalTicks);
                    voidTaskImplementation.cancel();
                    return;
                }
                send(player, weapon, ticks, totalTicks);
            }
        }, 0, DEFAULT_TICK_RATE);

        return new TimerData(this, player, weapon, task, totalTicks);
    }

    /**
     * Shorthand for {@link #cancel(Player, ItemStack, int, int)}.
     *
     * @param data The non-null data.
     * @see #play(Player, ItemStack, int).
     */
    public void cancel(TimerData data) {
        cancel(data.player, data.weapon, data.getElapsedTime(), data.totalTicks);
    }

    /**
     * Takes away the "leftover" effects from cancelling a timer early. If you
     * cancel the task via {@link org.bukkit.scheduler.BukkitScheduler#cancelTask(int)},
     * then you <b>MUST, IN ADDITION</b> call this method.
     *
     * @param player     The non-null player to see the messages.
     * @param weapon     The item that has a cool-down (or null).
     * @param ticks      How many ticks elapsed before the timer was cancelled.
     * @param totalTicks How many ticks the timer was expected to run for.
     */
    public void cancel(Player player, ItemStack weapon, int ticks, int totalTicks) {

        // Remove any cooldown if the player still has one (*should*
        // only happen when the event is cancelled).
        if (weapon != null && player.hasCooldown(weapon.getType())) {
            ServerImplementation scheduler = WeaponMechanicsCosmetics.getInstance().getFoliaScheduler();
            scheduler.entity(player).run(() -> player.setCooldown(weapon.getType(), 0));
        }

        PlaceholderData data = PlaceholderData.of(player, weapon, CustomTag.WEAPON_TITLE.getString(weapon), null);
        data.placeholders().put("bar", bar == null ? "N/A" : bar.evaluate(ticks, totalTicks));
        data.placeholders().put("time", ROUND.format((totalTicks - ticks) / 20.0));

        // Make sure player experience is set to their old exp value
        sendExp(player, Float.NaN);

        // If the event was cancelled BEFORE it was completed...
        if (ticks < totalTicks && actionBarCancelled != null) {
            player.sendActionBar(actionBarCancelled.replaceAndDeserialize(data));
        }
    }

    public void send(Player player, ItemStack weapon, int ticks, int totalTicks) {
        PlaceholderData data = PlaceholderData.of(player, weapon, CustomTag.WEAPON_TITLE.getString(weapon), null);
        data.placeholders().put("bar", bar == null ? "N/A" : bar.evaluate(ticks, totalTicks));
        data.placeholders().put("time", ROUND.format((totalTicks - ticks) / 20.0));

        if (actionBar != null)
            player.sendActionBar(actionBar.replaceAndDeserialize(data));

        // Handle showing the title. The title disappears on its own, we don't
        // need to handle its removal.
        if (title != null || subtitle != null) {
            Component adventureTitle = title == null ? Component.empty() : title.replaceAndDeserialize(data);
            Component adventureSubtitle = subtitle == null ? Component.empty() : subtitle.replaceAndDeserialize(data);
            Title titleComponent = Title.title(adventureTitle, adventureSubtitle, TITLE_TIMES);
            player.showTitle(titleComponent);
        }

        // Handle showing the bossbar. Since the bossbar doesn't disappear on
        // its own, we have to schedule a task to remove it 1 tick later.
        float progress = NumberUtil.clamp01((float) ticks / totalTicks);
        if (bossBar != null) {
            BossBar bossComponent = BossBar.bossBar(bossBar.replaceAndDeserialize(data), progress, color, style);
            player.showBossBar(bossComponent);
            ServerImplementation scheduler = WeaponMechanicsCosmetics.getInstance().getFoliaScheduler();
            scheduler.async().runDelayed(() -> player.hideBossBar(bossComponent), 1);
        }

        // Handle showing experience
        if (showExp)
            sendExp(player, progress);
    }

    /**
     * Sends an experience packet to the given <code>player</code>. The
     * progress can be [0.0, 1.0], or <code>Float.NaN</code> (Which is treated
     * as the player's actual experience value).
     *
     * @param player   The non-null player to send the packet to.
     * @param progress The [0.0, 1.0] percentage of experience.
     */
    public void sendExp(Player player, float progress) {
        if (Float.isNaN(progress))
            progress = player.getExp();

        progress = NumberUtil.clamp01(progress);
        player.sendExperienceChange(progress, player.getLevel());
    }

    @NotNull
    @Override
    public Timer serialize(SerializeData data) throws SerializerException {

        String actionBar = data.of("Action_Bar").getAdventure().orElse(null);
        String actionBarCancelled = data.of("Action_Bar_Cancelled").getAdventure().orElse(null);
        String title = data.of("Title").getAdventure().orElse(null);
        String subTitle = data.of("Subtitle").getAdventure().orElse(null);

        // Bossbar stuff... Set to null first, so we can do assertExists ONLY
        // when the admin uses "Boss_Bar"
        String bossBar = null;
        BossBar.Color color = null;
        BossBar.Overlay style = null;

        if (data.has("Boss_Bar")) {
            bossBar = data.of("Boss_Bar.Message").assertExists().getAdventure().get();
            color = data.of("Boss_Bar.Color").getEnum(BossBar.Color.class).orElse(BossBar.Color.WHITE);
            style = data.of("Boss_Bar.Style").getEnum(BossBar.Overlay.class).orElse(BossBar.Overlay.PROGRESS);
        }

        // Before we serialize bar, lets check to see if any of our messages
        // uses %bar%. That way we can tell the user if they need to use it or not.
        boolean contains = check(actionBar) || check(title) || check(subTitle) || check(bossBar);

        // So when *NONE* of the messages uses %bar%, but the admin defined it
        // anyway, they probably made a mistake or forgot to delete something.
        if (!contains && data.has("Bar"))
            throw data.exception("Bar", "You tried to use 'Bar' when you never used '<bar>' in any of your messages");

        StringBar bar = null;
        if (contains)
            bar = data.of("Bar").assertExists().serialize(StringBar.class).get();

        boolean showItemCooldown = data.of("Item_Cooldown").getBool().orElse(false);
        boolean showExp = data.of("Exp").getBool().orElse(false);

        return new Timer(showItemCooldown, actionBar, actionBarCancelled, title, subTitle, bossBar, color, style, showExp, bar);
    }

    private boolean check(String str) {
        return str != null && str.contains("<bar>");
    }

    /**
     * This class stores the data on visual message bars. The
     * <code>%bar%</code> variable is replaced with this result.
     */
    public static class StringBar implements Serializer<StringBar> {

        private String leftColor;
        private String rightColor;
        private String leftSymbol;
        private String rightSymbol;
        private int symbolAmount;

        /**
         * Default constructor for serializer
         */
        public StringBar() {
        }

        public StringBar(String leftColor, String rightColor, String leftSymbol, String rightSymbol, int symbolAmount) {
            this.leftColor = leftColor;
            this.rightColor = rightColor;
            this.leftSymbol = leftSymbol;
            this.rightSymbol = rightSymbol;
            this.symbolAmount = symbolAmount;
        }

        public String evaluate(int ticks, int totalTicks) {
            int delimiter = (int) ((double) ticks / totalTicks * symbolAmount);

            StringBuilder builder = new StringBuilder(symbolAmount + leftColor.length() + rightColor.length());
            builder.append(leftColor);

            for (int i = 0; i < symbolAmount; i++) {
                if (i < delimiter) {
                    builder.append(leftSymbol);
                } else if (i == delimiter) {
                    builder.append(rightColor);
                    builder.append(rightSymbol);
                } else {
                    builder.append(rightSymbol);
                }
            }

            return builder.toString();
        }

        @NotNull
        @Override
        public StringBar serialize(SerializeData data) throws SerializerException {
            String leftColor = data.of("Left_Color").assertExists().getAdventure().get();
            String rightColor = data.of("Right_Color").assertExists().getAdventure().get();
            String leftSymbol = data.of("Left_Symbol").assertExists().getAdventure().get();
            String rightSymbol = data.of("Right_Symbol").getAdventure().orElse(leftSymbol);
            int symbolAmount = data.of("Symbol_Amount").assertExists().assertRange(1, null).getInt().getAsInt();

            return new StringBar(leftColor, rightColor, leftSymbol, rightSymbol, symbolAmount);
        }
    }
}
