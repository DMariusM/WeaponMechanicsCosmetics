/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.trails.shape;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

public class StringFunctionShape extends FunctionShape {

    private Expression expression;
    private Argument theta;

    /**
     * Default constructor for serializer
     */
    public StringFunctionShape() {
    }

    public StringFunctionShape(int points, int loops, String function, boolean cache) {
        super(points, loops);

        this.theta = new Argument("theta", 0.0);
        this.expression = new Expression(function, theta);

        if (cache)
            cache();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey("weaponmechanicscosmetics", "polar");
    }

    @Override
    public double radiusFunction(double theta) {
        this.theta.setArgumentValue(theta);
        return expression.calculate();
    }

    @Override
    public @NotNull Shape serialize(@NotNull SerializeData data) throws SerializerException {
        int points = data.of("Points").assertExists().assertRange(1, null).getInt().getAsInt();
        int loops = data.of("Loops").assertRange(1, null).getInt().orElse(1);
        String function = data.of("Function").assertExists().get(String.class).get();
        boolean cache = data.of("Cache").getBool().orElse(true);

        return new StringFunctionShape(points, loops, function, cache);
    }
}
