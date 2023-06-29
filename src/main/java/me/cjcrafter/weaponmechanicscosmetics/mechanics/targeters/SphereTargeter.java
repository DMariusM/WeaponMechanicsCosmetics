package me.cjcrafter.weaponmechanicscosmetics.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.targeters.Targeter;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class SphereTargeter extends ShapeTargeter {

    private Location[] cache;
    private Vector[] points;

    public SphereTargeter(int points, double radius) {

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
            loc
            cache[i] = cast.getSourceLocation().clone().add(points[i]);
        }

        return cache;
    }

    @Override
    public boolean isEntity() {
        return false;
    }

    @NotNull
    @Override
    public Targeter serialize(SerializeData serializeData) throws SerializerException {
        return null;
    }
}
