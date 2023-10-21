package me.cjcrafter.weaponmechanicscosmetics.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.targeters.ShapeTargeter;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.VectorUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

public class SphereTargeter extends ShapeTargeter {

    private Vector[] points;

    /**
     * Default constructor for serializer
     */
    public SphereTargeter() {
    }

    public SphereTargeter(int points, double radius) {
        this.points = new Vector[points];

        double phi = VectorUtil.GOLDEN_ANGLE;

        for (int i = 0; i < points; i++) {
            double y = 1 - (i / ((double) points - 1)) * 2;
            double r = Math.sqrt(1 - y * y);

            // y *= (radius / r); // Creates a cool diamond like shape

            double theta = phi * i;

            double x = r * Math.cos(theta);
            double z = r * Math.sin(theta);
            this.points[i] = new Vector(x * radius, y * radius, z * radius);
        }
    }

    @Override
    public @NotNull Iterator<Vector> getPoints(@NotNull CastData data) {
        return new Iterator<>() {
            int i = 0;
            @Override
            public boolean hasNext() {
                return i < points.length;
            }

            @Override
            public Vector next() {
                return points[i++];
            }
        };
    }

    public String getKeyword() {
        return "Sphere";
    }

    @NotNull
    @Override
    public Targeter serialize(SerializeData data) throws SerializerException {
        int points = data.of("Points").assertExists().getInt();
        double radius = data.of("Radius").assertExists().getDouble();

        return applyParentArgs(data, new SphereTargeter(points, radius));
    }
}
