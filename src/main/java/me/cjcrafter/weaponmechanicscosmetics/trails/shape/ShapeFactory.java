/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.trails.shape;

import me.deecaad.weaponmechanics.utils.Factory;

public final class ShapeFactory extends Factory<Shape> {

    private static final ShapeFactory INSTANCE;

    static {
        INSTANCE = new ShapeFactory();

        INSTANCE.set("LINE,NONE", INSTANCE.new Arguments(
                Line.class,
                new String[]{ },
                new Class[]{ }
        ));

        INSTANCE.set("SPIRAL", INSTANCE.new Arguments(
                Spiral.class,
                new String[]{ "Radius", "Points", "Loops" },
                new Class[]{ double.class, int.class, int.class }
        ));

        INSTANCE.set("EQUATION,POLAR", INSTANCE.new Arguments(
                StringFunctionShape.class,
                new String[]{ "Points", "Loops", "Function", "Cache" },
                new Class[]{ int.class, int.class, String.class, boolean.class }
        ));

    }

    private ShapeFactory() {
        super(Shape.class);
    }

    public static ShapeFactory getInstance() {
        return INSTANCE;
    }
}
