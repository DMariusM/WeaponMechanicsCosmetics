/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.trails.shape;

public class Spiral extends FunctionShape {

    private final double radius;

    public Spiral(double radius, int points, int loops) {
        super(points, loops);

        // must set radius before caching
        this.radius = radius;
        cache();
    }

    @Override
    public double radiusFunction(double theta) {
        return radius;
    }
}
