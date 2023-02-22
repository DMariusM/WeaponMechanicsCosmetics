/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.trails.shape;

import org.junit.jupiter.api.Test;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import static org.junit.jupiter.api.Assertions.*;

public class StringFunctionShapeTest {

    @Test
    void test() {
        StringFunctionShape shape = new StringFunctionShape(4, 1, "2*sin(theta)", true);

        assertEquals(0, shape.radiusFunction(0), 1E-8);
        assertEquals(2, shape.radiusFunction(Math.PI / 2.0), 1E-8);
    }

    @Test
    void unitCircleTest() {

        // This test is important since we reuse the same function for
        // a different theta input.
        StringFunctionShape cos = new StringFunctionShape(8, 1, "cos(theta)", true);
        StringFunctionShape sin = new StringFunctionShape(8, 1, "sin(theta)", false);

        double[] expectedCos = new double[]{ 1.0, Math.sqrt(2)/2, 0, -Math.sqrt(2)/2, -1.0, -Math.sqrt(2)/2, 0, Math.sqrt(2)/2 };
        double[] expectedSin = new double[]{ 0.0, Math.sqrt(2)/2, 1.0, Math.sqrt(2)/2, 0.0, -Math.sqrt(2)/2, -1.0, -Math.sqrt(2)/2 };

        for (int i = 0; i < 8; i++) {
            double theta = 2.0 * Math.PI / 8.0 * i;

            assertEquals(expectedCos[i], cos.radiusFunction(theta), 1E-8, "For index " + i + "(" + theta + ")");
            assertEquals(expectedSin[i], sin.radiusFunction(theta), 1E-8, "For index " + i + "(" + theta + ")");
        }
    }

    @Test
    void exponential() {

        Argument theta = new Argument("theta", 0.0);
        Expression expression = new Expression("exp(-theta)", theta);

        for (int i = 0; i < 100; i++) {
            theta.setArgumentValue(theta.getArgumentValue() + 0.025);
            System.out.println(expression.calculate());
        }
    }
}
