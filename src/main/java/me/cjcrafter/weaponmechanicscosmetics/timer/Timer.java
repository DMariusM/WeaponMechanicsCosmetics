package me.cjcrafter.weaponmechanicscosmetics.timer;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.lib.adventure.audience.Audience;
import me.deecaad.core.lib.adventure.bossbar.BossBar;
import me.deecaad.core.lib.adventure.text.Component;
import me.deecaad.core.lib.adventure.title.Title;
import me.deecaad.core.lib.adventure.util.Ticks;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Timer implements Serializer<Timer>{

    public static final MathContext ROUND = new MathContext(1, RoundingMode.HALF_UP);
    public static final int DEFAULT_TICK_RATE = 2; // 0.1 seconds between updates
    public static final Title.Times TITLE_TIMES = Title.Times.times(Ticks.duration(0), Ticks.duration(1), Ticks.duration(5));

    private static Constructor<?> packetPlayOutExperienceConstructor;

    static {
        if (ReflectionUtil.getMCVersion() < 15) {
            packetPlayOutExperienceConstructor = ReflectionUtil.getConstructor(ReflectionUtil.getPacketClass("PacketPlayOutExperience"), float.class, int.class, int.class);
        }
    }

    private boolean showItemCooldown;
    private String actionBar;
    private Component actionBarComplete;
    private Component actionBarCancelled;
    private String title;
    private String subtitle;
    private String bossBar;
    private BossBar.Color color;
    private BossBar.Overlay style;
    private boolean showExp;
    private boolean showLevel;
    private StringBar bar;

    /**
     * Default constructor for serializer
     */
    public Timer() {
    }

    public Timer(boolean showItemCooldown, String actionBar, Component actionBarComplete, Component actionBarCancelled,
                 String title, String subtitle, String bossBar, BossBar.Color color, BossBar.Overlay style,
                 boolean showExp, StringBar bar) {

        this.showItemCooldown = showItemCooldown;
        this.actionBar = actionBar;
        this.actionBarComplete = actionBarComplete;
        this.actionBarCancelled = actionBarCancelled;
        this.title = title;
        this.subtitle = subtitle;
        this.bossBar = bossBar;
        this.color = color;
        this.style = style;
        this.showExp = showExp;
        this.bar = bar;
    }

    public int play(Player player, ItemStack weapon, int totalTicks) {
        if (showItemCooldown)
            CompatibilityAPI.getEntityCompatibility().setCooldown(player, weapon.getType(), totalTicks);

        send(player, 0, totalTicks);

        return new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if ((ticks += 2) >= totalTicks) {
                    cancel();
                }

                send(player, ticks, totalTicks);
            }

            @Override
            public void cancel() {
                super.cancel();

                Audience audience = MechanicsCore.getPlugin().adventure.player(player);

                // Remove any cooldown if the player still has one (*should*
                // only happen when the event is cancelled).
                if (CompatibilityAPI.getEntityCompatibility().hasCooldown(player, weapon.getType()))
                    CompatibilityAPI.getEntityCompatibility().setCooldown(player, weapon.getType(), 0);

                // Make sure player experience is set to their old exp value
                sendExp(player, Float.NaN);

                // If the event was cancelled BEFORE it was completed...
                if (ticks > totalTicks && actionBarCancelled != null) {
                    audience.sendActionBar(actionBarCancelled);
                }

                // If the event was completed successfully
                else if (ticks <= totalTicks && actionBarComplete != null) {
                    audience.sendActionBar(actionBarComplete);
                }
            }
        }.runTaskTimerAsynchronously(WeaponMechanicsCosmetics.getInstance().getPlugin(), 0, DEFAULT_TICK_RATE).getTaskId();
    }

    public void send(Player player, int ticks, int totalTicks) {
        Audience audience = MechanicsCore.getPlugin().adventure.player(player);
        String barCache = bar == null ? null : bar.evaluate(ticks, totalTicks);
        String timeCache = new BigDecimal((double) ticks / totalTicks, ROUND).toString();

        if (actionBar != null) audience.sendActionBar(substitute(actionBar, barCache, timeCache));

        // Handle showing the title. The title disappears on its own, we don't
        // need to handle its removal.
        if (title != null || subtitle != null) {
            Title titleComponent = Title.title(substitute(title, barCache, timeCache), substitute(subtitle, barCache, timeCache), TITLE_TIMES);
            audience.showTitle(titleComponent);
        }

        // Handle showing the bossbar. Since the bossbar doesn't disappear on
        // its own, we have to schedule a task to remove it 1 tick later.
        float progress = (float) ticks / totalTicks;
        BossBar bossComponent = BossBar.bossBar(substitute(bossBar, barCache, timeCache), progress, color, style);
        new BukkitRunnable() {
            @Override
            public void run() {
                audience.hideBossBar(bossComponent);
            }
        }.runTaskAsynchronously(WeaponMechanicsCosmetics.getInstance().getPlugin());

        // Handle showing experience
        sendExp(player, progress);
    }

    public void sendExp(Player player, float progress) {
        if (Float.isNaN(progress))
            progress = player.getExp();

        progress = NumberUtil.minMax(0.0f, progress, 1.0f);

        if (ReflectionUtil.getMCVersion() < 15) {
            CompatibilityAPI.getCompatibility().sendPackets(player, ReflectionUtil.newInstance(packetPlayOutExperienceConstructor, progress, player.getTotalExperience(), player.getLevel()));
        } else {
            player.sendExperienceChange(progress, player.getLevel());
        }
    }

    /**
     * Substitutes the %bar% and %time% variables to the given
     * <code>base</code> string. Performance could be better, TODO.
     *
     * @param base The base string.
     * @param bar  %bar% replacement value.
     * @param time %time% replacement value.
     * @return The parsed adventure component.
     */
    private Component substitute(String base, String bar, String time) {
        if (base == null)
            return Component.empty();

        String content = base.replace("%bar%", bar).replace("%time%", time);
        return MechanicsCore.getPlugin().message.deserialize(content);
    }

    @NotNull
    @Override
    public Timer serialize(SerializeData data) throws SerializerException {

        String actionBar = data.of("Action_Bar").getAdventure(null);
        String actionBarCompleteStr = data.of("Action_Bar_Complete").getAdventure(null);
        String actionBarCancelledStr = data.of("Action_Bar_Cancelled").getAdventure(null);
        String title = data.of("Title").getAdventure(null);
        String subTitle = data.of("Subtitle").getAdventure(null);

        // actionBarComplete is the only message we can actually cache
        Component actionBarComplete = actionBarCompleteStr == null ? null : MechanicsCore.getPlugin().message.deserialize(actionBarCompleteStr);
        Component actionBarCancelled = actionBarCancelledStr == null ? null : MechanicsCore.getPlugin().message.deserialize(actionBarCancelledStr);

        // Bossbar stuff... Set to null first, so we can do assertExists ONLY
        // when the admin uses "Boss_Bar"
        String bossBar = null;
        BossBar.Color color = null;
        BossBar.Overlay style = null;

        if (data.has("Boss_Bar")) {
            bossBar = data.of("Boss_Bar.Message").assertExists().getAdventure();
            color = data.of("Boss_Bar.Color").getEnum(BossBar.Color.class, BossBar.Color.WHITE);
            style = data.of("Boss_Bar.Style").getEnum(BossBar.Overlay.class, BossBar.Overlay.PROGRESS);
        }

        // Before we serialize bar, lets check to see if any of our messages
        // uses %bar%. That way we can tell the user if they need to use it or not.
        boolean contains = check(actionBar) || check(title) || check(subTitle) || check(bossBar);

        // So when *NONE* of the messages uses %bar%, but the admin defined it
        // anyway, they probably made a mistake or forgot to delete something.
        if (!contains && data.has("Bar"))
            throw data.exception("Bar", "You tried to use 'Bar' when you never used '%bar%' in any of your messages");

        StringBar bar = null;
        if (contains)
            bar = data.of("Bar").assertExists().serialize(StringBar.class);

        boolean showItemCooldown = data.of("Item_Cooldown").getBool(false);
        boolean showExp = data.of("Exp").getBool(false);

        return new Timer(showItemCooldown, actionBar, actionBarComplete, actionBarCancelled, title, subTitle, bossBar, color, style, showExp, bar);
    }

    private boolean check(String str) {
        return str != null && str.contains("%bar%");
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
            String leftColor = data.of("Left_Color").assertExists().getAdventure();
            String rightColor = data.of("Right_Color").assertExists().getAdventure();
            String leftSymbol = data.of("Left_Symbol").assertExists().getAdventure();
            String rightSymbol = data.of("Right_Symbol").getAdventure(leftSymbol);
            int symbolAmount = data.of("Symbol_Amount").assertExists().assertPositive().getInt();

            return new StringBar(leftColor, rightColor, leftSymbol, rightSymbol, symbolAmount);
        }
    }
}
