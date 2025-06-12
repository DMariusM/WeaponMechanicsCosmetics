/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package com.cjcrafter.weaponmechanicscosmetics.trails.shape;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

public class ParametricFunctionShape extends FunctionShape {

    private Expression expressionX;
    private Expression expressionY;
    private Argument theta;

    /**
     * Default constructor for serializer
     */
    public ParametricFunctionShape() {
    }

    public ParametricFunctionShape(int points, int loops, String function, boolean cache) {
        super(points, loops);

        this.theta = new Argument("theta", 0.0);
        this.expressionX = new Expression(function.split(",")[0].trim(), theta);
        this.expressionY = new Expression(function.split(",")[1].trim(), theta);

        if (cache)
            cache();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey("weaponmechanicscosmetics", "parametric");
    }

    @Override
    public Vec2 offsetFunction(double theta) {
        this.theta.setArgumentValue(theta);
        return new Vec2(expressionX.calculate(), expressionY.calculate());
    }

    @Override
    public double radiusFunction(double theta) {
        // This method is NEVER called from WMC. Only have the implementation
        // for consistency
        this.theta.setArgumentValue(theta);
        double x = -expressionX.calculate();
        double y = expressionY.calculate();
        return Math.sqrt(x * x + y * y);
    }

    @Override
    public @NotNull Shape serialize(@NotNull SerializeData data) throws SerializerException {
        int points = data.of("Points").assertExists().assertRange(1, null).getInt().getAsInt();
        int loops = data.of("Loops").assertRange(1, null).getInt().orElse(1);
        String function = data.of("Function").assertExists().get(String.class).get();
        boolean cache = data.of("Cache").getBool().orElse(true);

        return new ParametricFunctionShape(points, loops, function, cache);
    }
}
