/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.trails.shape;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

public class StringFunctionShape extends FunctionShape {

    private final Expression expression;
    private final Argument theta;

    public StringFunctionShape(int points, int loops, String function, boolean cache) {
        super(points, loops);

        this.theta = new Argument("theta", 0.0);
        this.expression = new Expression(function, theta);

        if (cache)
            cache();
    }

    @Override
    public double radiusFunction(double theta) {
        this.theta.setArgumentValue(theta);
        return expression.calculate();
    }
}
