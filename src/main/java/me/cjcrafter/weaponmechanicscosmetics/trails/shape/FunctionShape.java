/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.trails.shape;

import me.cjcrafter.weaponmechanicscosmetics.trails.Trail;
import me.deecaad.core.utils.VectorUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class FunctionShape implements Shape {

    private List<List<Vec2>> cache;
    private int points;
    private int loops;

    public FunctionShape(int points, int loops) {
        if (loops < 1)
            throw new IllegalArgumentException("Loops should be at least 1");

        this.points = points;
        this.loops = loops;
    }

    public void cache() {
        List<List<Vec2>> cache = new ArrayList<>(points);

        double delta = 2 * Math.PI / points;
        double spiralDelta = 2 * Math.PI / loops;

        for (int i = 0; i < points; i++) {
            List<Vec2> temp = new ArrayList<>(loops);
            for (int j = 0; j < loops; j++) {

                double theta = delta * i + j * spiralDelta;
                temp.add(offsetFunction(theta));
            }

            // Don't let anyone modify
            cache.add(Collections.unmodifiableList(temp));
        }

        // Don't let anyone modify
        this.cache = Collections.unmodifiableList(cache);
    }

    public void deleteCache() {
        cache = null;
    }

    @Override
    public List<Vec2> getPoint(int index) {

        // Cache should be null whenever the function simply cannot be cached.
        // A function can be cached when it has a "perfect loop" from
        // [0, period], where period is usually 2pi (though it can be changed
        // for some functions).
        if (cache == null) {
            double theta = VectorUtil.PI_2 * index / points;
            double delta = VectorUtil.PI_2 / loops;

            // todo consider storing a list
            List<Vec2> temp = new ArrayList<>(loops);
            for (int i = 0; i < loops; i++) {

                double finalTheta = theta + delta * i;
                temp.add(offsetFunction(finalTheta));
            }

            return temp;

        } else {
            return cache.get(index % getPoints());
        }
    }

    @Override
    public int getPoints() {
        return points;
    }

    /**
     * Use {@link #cache()} or {@link #deleteCache()} after calling this
     * method. Otherwise, your changes may not be reflected in the actual
     * {@link Trail}.
     *
     * @param points The number of points in the period.
     */
    public void setPoints(int points) {
        this.points = points;
    }

    public int getLoops() {
        return loops;
    }

    /**
     * Use {@link #cache()} or {@link #deleteCache()} after calling this
     * method. Otherwise, your changes may not be reflected in the actual
     * {@link Trail}.
     *
     * @param loops The number of particles to show per point.
     */
    public void setLoops(int loops) {
        this.loops = loops;
    }

    public Vec2 offsetFunction(double theta) {
        double radius = radiusFunction(theta);
        return new Vec2(Math.cos(theta) * radius, Math.sin(theta) * radius);
    }

    public abstract double radiusFunction(double theta);
}
