/*
 * Copyright (c) 2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.trails.shape;

import me.deecaad.core.utils.MutableRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Utility class that contains all shapes used in the {@link Shape} serializer.
 * This class is empty as it serves as a namespace for shape-related classes.
 */
public final class Shapes {

    /**
     * The registry for all globally registered shapes.
     */
    public static final @NotNull MutableRegistry<Shape> REGISTRY
            = new MutableRegistry.SimpleMutableRegistry<>(new HashMap<>());

    public static final @NotNull Shape LINE = register(new Line());
    public static final @NotNull Shape PARAMETRIC_FUNCTION = register(new ParametricFunctionShape());
    public static final @NotNull Shape SPIRAL = register(new Spiral());
    public static final @NotNull Shape STRING_FUNCTION = register(new StringFunctionShape());


    private Shapes() {
    }

    private static @NotNull Shape register(@NotNull Shape shape) {
        REGISTRY.add(shape);
        return shape;
    }

}
