/*
 * Copyright (c) 2022-2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.trails.shape;

import me.deecaad.core.file.Serializer;
import org.bukkit.Keyed;

import java.util.List;

/**
 * Represents a 2D shape that can be drawn out over time.
 */
public interface Shape extends Keyed, Serializer<Shape> {

    /**
     * Returns a list of points in 2D space. These points are offset by the
     * projectile's velocity's rotation matrix to be placed in 3D space. In
     * simpler terms, the points will be wrapped around the projectile.
     *
     * <p>DO NOT MODIFY THE RETURNED LIST!!! Shapes CAN and WILL cache the
     * points, so they don't have to repeat calculations.
     *
     * @param index The non-negative current index.
     * @return A non-null, non-empty list of offsets.
     */
    List<Vec2> getPoint(int index);

    /**
     * Returns the maximum number of points contained by this shape.
     *
     * @return A non-negative number representing the total amount of points.
     */
    int getPoints();
}
