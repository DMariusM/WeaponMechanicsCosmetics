package me.deecaad.weaponmechanicscosmetics.trails.shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Spiral implements Shape {

    private final List<List<Vec2>> points;

    public Spiral(int spirals, double radius, int points) {
        List<List<Vec2>> cache = new ArrayList<>(points);

        double delta = 2 * Math.PI / points;
        double spiralDelta = 2 * Math.PI / spirals;

        for (int i = 0; i < points; i++) {
            List<Vec2> temp = new ArrayList<>(spirals);

            for (int j = 0; j < spirals; j++) {

                double theta = delta * i + j * spiralDelta;
                double x = radius * Math.cos(theta);
                double y = radius * Math.sin(theta);

                temp.add(new Vec2(x, y));
            }

            // Don't let anyone modify
            cache.add(Collections.unmodifiableList(temp));
        }

        // Don't let anyone modify
        this.points = Collections.unmodifiableList(cache);
    }

    @Override
    public List<Vec2> getPoint(int index) {
        return points.get(index % getPoints());
    }

    @Override
    public int getPoints() {
        return points.size();
    }
}
