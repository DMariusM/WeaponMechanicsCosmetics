package me.cjcrafter.weaponmechanicscosmetics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Timer implements Serializer<Timer> {

    public static final int DEFAULT_TICK_RATE = 2; // 0.1 seconds between updates
    public static final MathContext ROUND = new MathContext(1, RoundingMode.HALF_UP);

    private String[] messages;
    private BarData bar;

    public Timer() {
    }

    public Timer(String[] messages, BarData bar) {
        this.messages = messages;
        this.bar = bar;
    }

    public String message(int ticks, int totalTicks) {

        // By splitting the message "Reloading... %time%s %bar% %time%s"
        // into an array ["Reloading... ", "%time%", "s ", "%bar%", "s ", "%time%"],
        // we save on performance since we don't need to iterate through
        // the entire message every time to replace the variables. We further
        // save on string concatenation performance by using a StringBuilder.
        StringBuilder builder = new StringBuilder();
        String timeCache = null;

        for (String str : messages) {
            switch (str) {
                case "%time%":
                    if (timeCache == null)
                        timeCache = new BigDecimal((double) ticks / totalTicks, ROUND).toString();
                    builder.append(timeCache);
                    break;
                case "%bar%":
                    bar.append(builder, ticks, totalTicks);
                    break;
                default:
                    builder.append(str);
            }
        }
        return builder.toString();
    }

    public int play(Player player, int totalTicks) {
        return new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ >= totalTicks)
                    cancel();

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message(ticks, totalTicks)));
            }
        }.runTaskTimerAsynchronously(WeaponMechanics.getPlugin(), 0, DEFAULT_TICK_RATE).getTaskId();
    }

    @NotNull
    @Override
    public Timer serialize(SerializeData data) throws SerializerException {
        String message = data.of("Message").assertExists().assertType(String.class).get();

        Matcher matcher = Pattern.compile("%.+?%").matcher(message);
        List<String> matches = new ArrayList<>();
        String match;
        while ((match = matcher.group()) != null)
            matches.add(match);

        boolean usesBar = false;

        List<String> messages = new ArrayList<>();
        String[] split = message.split("%.+?%");
        for (int i = 0; i < split.length; i++) {
            String str = split[i];

            // Storing empty strings probably won't cause any performance
            // issues, but it is probably best to skip them all the same.
            if (!str.isEmpty())
                messages.add(str);

            // If the user wants to use the %bar% variable, then we should
            // validate that the bar options exist.
            String var = matches.get(i).toLowerCase(Locale.ROOT);
            if (var.equals("{!bar}"))
                usesBar = true;

            messages.add(var);
        }

        BarData bar = null;
        if (usesBar)
            bar = data.of("Bar").assertExists().serialize(BarData.class);

        return new Timer(messages.toArray(new String[0]), bar);
    }



    public static class BarData implements Serializer<BarData> {

        private String leftColor;
        private String rightColor;
        private String leftCharacter;
        private String rightCharacter;
        private int amount;

        public BarData() {
        }

        public BarData(String leftColor, String rightColor, String leftCharacter, String rightCharacter, int amount) {
            this.leftColor = leftColor;
            this.rightColor = rightColor;
            this.leftCharacter = leftCharacter;
            this.rightCharacter = rightCharacter;
            this.amount = amount;
        }

        public void append(StringBuilder builder, int ticks, int totalTicks) {
            int delimiter = (int) ((double) ticks / totalTicks * amount);

            builder.append(rightColor);
            for (int i = 0; i < amount; i++) {
                if (i < delimiter) {
                    builder.append(rightCharacter);
                } else if (i == delimiter) {
                    builder.append(leftColor);
                    builder.append(leftCharacter);
                } else {
                    builder.append(leftCharacter);
                }
            }
        }

        @NotNull
        @Override
        public BarData serialize(SerializeData data) throws SerializerException {
            String leftColor = data.of("Left_Color").assertExists().get().toString();
            String leftSymbol = data.of("Left_Symbol").assertExists().get().toString();
            String rightColor = data.of("Right_Color").assertExists().get().toString();
            String rightSymbol = data.of("Right_Symbol").get((Object) leftSymbol).toString();
            int symbolAmount = data.of("Symbol_Amount").assertExists().assertRange(1, 100).getInt();

            if (!leftColor.startsWith("&"))
                leftColor = "&" + leftColor;
            if (!rightColor.startsWith("&"))
                rightColor = "&" + rightColor;

            leftColor = StringUtil.color(leftColor);
            rightColor = StringUtil.color(rightColor);

            return new BarData(leftColor, rightColor, leftSymbol, rightSymbol, symbolAmount);

        }
    }
}
