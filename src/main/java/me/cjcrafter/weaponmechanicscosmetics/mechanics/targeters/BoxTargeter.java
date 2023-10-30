package me.cjcrafter.weaponmechanicscosmetics.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.targeters.ShapeTargeter;
import me.deecaad.core.mechanics.targeters.Targeter;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BoxTargeter extends ShapeTargeter {

    // These variables don't actually do anything, they are just here for getters
    private double width;
    private double height;
    private double delta;

    private List<Vector> points;

    /**
     * Default constructor for serializer
     */
    public BoxTargeter() {
    }

    public BoxTargeter(double width, double height, double delta) {
        this.width = width;
        this.height = height;
        this.delta = delta;

        Vector min = new Vector(-width / 2, -height / 2, -width / 2);
        Vector max = new Vector(width / 2, height / 2, width / 2);
        this.points = outlineBox(min, max, delta);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getDelta() {
        return delta;
    }

    @Override
    public @NotNull String getKey() {
        return "Box";
    }

    @Override
    public @NotNull Iterator<Vector> getPoints(@NotNull CastData cast) {
        return points.iterator();
    }

    @Override
    public @NotNull Targeter serialize(@NotNull SerializeData data) throws SerializerException {
        double width = data.of("Width").assertPositive().getDouble(1.0);
        double height = data.of("Height").assertPositive().getDouble(1.0);
        double delta = data.of("Distance_Between_Points").assertPositive().getDouble(0.5);

        if (width == 0)
            throw data.exception("Width", "Width cannot be 0");
        if (height == 0)
            throw data.exception("Height", "Height cannot be 0");
        if (delta == 0)
            throw data.exception("Distance_Between_Points", "Distance between points cannot be 0");

        return applyParentArgs(data, new BoxTargeter(width, height, delta));
    }


    public static List<Vector> outlineBox(Vector min, Vector max, double distanceBetweenPoints) {
        List<Vector> outlinePoints = new ArrayList<>();

        // Calculate number of points needed along each dimension
        int pointsX = (int) ((max.getX() - min.getX()) / distanceBetweenPoints) + 1;
        int pointsY = (int) ((max.getY() - min.getY()) / distanceBetweenPoints) + 1;
        int pointsZ = (int) ((max.getZ() - min.getZ()) / distanceBetweenPoints) + 1;

        // Generate points for the box's outline
        for (int i = 0; i < pointsX; i++) {
            double currentX = min.getX() + i * distanceBetweenPoints;

            // Bottom and top
            outlinePoints.add(new Vector(currentX, min.getY(), min.getZ()));
            outlinePoints.add(new Vector(currentX, min.getY(), max.getZ()));
            outlinePoints.add(new Vector(currentX, max.getY(), min.getZ()));
            outlinePoints.add(new Vector(currentX, max.getY(), max.getZ()));
        }

        for (int i = 1; i < pointsY - 1; i++) { // start from 1 and end at -1 to avoid corner duplications
            double currentY = min.getY() + i * distanceBetweenPoints;

            // Front and back
            outlinePoints.add(new Vector(min.getX(), currentY, min.getZ()));
            outlinePoints.add(new Vector(max.getX(), currentY, min.getZ()));
            outlinePoints.add(new Vector(min.getX(), currentY, max.getZ()));
            outlinePoints.add(new Vector(max.getX(), currentY, max.getZ()));
        }

        for (int i = 1; i < pointsZ - 1; i++) { // start from 1 and end at -1 to avoid corner duplications
            double currentZ = min.getZ() + i * distanceBetweenPoints;

            // Left and right
            outlinePoints.add(new Vector(min.getX(), min.getY(), currentZ));
            outlinePoints.add(new Vector(min.getX(), max.getY(), currentZ));
            outlinePoints.add(new Vector(max.getX(), min.getY(), currentZ));
            outlinePoints.add(new Vector(max.getX(), max.getY(), currentZ));
        }

        return outlinePoints;
    }
}
