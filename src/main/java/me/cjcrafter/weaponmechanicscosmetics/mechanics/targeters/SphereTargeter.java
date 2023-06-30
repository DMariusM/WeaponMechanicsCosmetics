package me.cjcrafter.weaponmechanicscosmetics.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.utils.VectorUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SphereTargeter extends ShapeTargeter {

    private final Location[] cache;
    private final Vector[] points;

    /**
     * Default constructor for serializer
     */
    public SphereTargeter() {
        cache = null;
        points = null;
    }

    public SphereTargeter(int points, double radius) {
        cache = new Location[points];
        for (int i = 0; i < points; i++) {
            cache[i] = new Location(null, 0, 0, 0);
        }

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
    public Location[] getPoints(CastData cast) {
        for (int i = 0; i < points.length; i++) {

            // To avoid instantiating 100+ locations every cast, we cache an
            // array of locations.
            Location cachedLocation = cache[i];
            Location sourceLocation = cast.getSourceLocation();
            cachedLocation.setX(sourceLocation.getX());
            cachedLocation.setY(sourceLocation.getY());
            cachedLocation.setZ(sourceLocation.getZ());
            cachedLocation.setWorld(sourceLocation.getWorld());
            cache[i] = cachedLocation.add(points[i]);
        }

        return cache;
    }

    @Override
    public boolean isEntity() {
        return false;
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
