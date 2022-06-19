package me.cjcrafter.weaponmechanicscosmetics.trails.shape;

import java.util.Collections;
import java.util.List;

public class Line implements Shape {

    private static final List<Vec2> CACHE = Collections.singletonList(new Vec2());

    @Override
    public List<Vec2> getPoint(int index) {
        return CACHE;
    }

    @Override
    public int getPoints() {
        return 1;
    }
}
