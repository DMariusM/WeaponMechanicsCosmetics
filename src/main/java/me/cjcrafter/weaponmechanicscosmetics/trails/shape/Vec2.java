package me.cjcrafter.weaponmechanicscosmetics.trails.shape;

/**
 * Simple class representing a vector in 2D space. Used by {@link Shape} to
 * store a list of points.
 */
public class Vec2 {

    public double x;
    public double y;

    public Vec2() {
    }

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
