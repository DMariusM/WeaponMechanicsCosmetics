package me.deecaad.weaponmechanicscosmetics.trails.shape;

import java.util.List;

public interface Shape {

    /**
     * Returns a list of 2D vectors which contain the (horizontal, vertical)
     * displacement (from the projectile's frame of reference) from the
     * projectile. 1 particle will be spawned for each element in the returned
     * list. The returned method should be treated as immutable (do not modify
     * the returned list! The trail is allowed to keep and return a cache!)
     *
     * <p>If the given index > {@link #getPoints()}, the parameter is
     * automatically wrapped to a valid index.
     *
     * @param index The non-negative current index.
     * @return A non-null, non-empty list of offsets.
     */
    List<Vec2> getPoint(int index);

    /**
     * Returns the maximum number of points contained by this shape.
     * @return
     */
    int getPoints();


}
