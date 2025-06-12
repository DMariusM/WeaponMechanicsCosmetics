/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package com.cjcrafter.weaponmechanicscosmetics.trails.shape;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public class Spiral extends FunctionShape {

    private double radius;

    /**
     * Default constructor for serializer
     */
    public Spiral() {
    }

    public Spiral(double radius, int points, int loops) {
        super(points, loops);

        // must set radius before caching
        this.radius = radius;
        cache();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey("weaponmechanicscosmetics", "spiral");
    }

    @Override
    public double radiusFunction(double theta) {
        return radius;
    }

    @Override
    public @NotNull Shape serialize(@NotNull SerializeData data) throws SerializerException {
        double radius = data.of("Radius").assertExists().assertRange(0, null).getDouble().getAsDouble();
        int points = data.of("Points").assertExists().assertRange(1, null).getInt().getAsInt();
        int loops = data.of("Loops").assertRange(1, null).getInt().orElse(1);

        return new Spiral(radius, points, loops);
    }
}
