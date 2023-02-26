/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.trails.shape;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

public class ParametricFunctionShape extends FunctionShape {

    private final Expression expressionX;
    private final Expression expressionY;
    private final Argument theta;

    public ParametricFunctionShape(int points, int loops, String function, boolean cache) {
        super(points, loops);

        this.theta = new Argument("theta", 0.0);
        this.expressionX = new Expression(function.split(",")[0].trim(), theta);
        this.expressionY = new Expression(function.split(",")[1].trim(), theta);

        if (cache)
            cache();
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
}
