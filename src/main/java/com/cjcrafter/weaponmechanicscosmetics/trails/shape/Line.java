/*
 * Copyright (c) 2022-2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.trails.shape;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Line implements Shape {

    private static final List<Vec2> CACHE = Collections.singletonList(new Vec2());

    public Line() {
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey("weaponmechanicscosmetics", "line");
    }

    @Override
    public List<Vec2> getPoint(int index) {
        return CACHE;
    }

    @Override
    public int getPoints() {
        return 1;
    }

    @Override
    public @NotNull Shape serialize(@NotNull SerializeData data) throws SerializerException {
        return new Line();
    }
}
