/*
 * Copyright (c) 2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.mechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Warden;
import org.jetbrains.annotations.NotNull;

public class WardenDisturbanceMechanic extends Mechanic {

    private double range;

    /**
     * Default constructor for serializers.
     */
    public WardenDisturbanceMechanic() {
    }

    public WardenDisturbanceMechanic(double range) {
        this.range = range;
    }

    @Override
    public void use0(CastData cast) {
        Location target = cast.getTargetLocation();

        for (Warden warden : target.getWorld().getEntitiesByClass(Warden.class)) {
            Location wardenLoc = warden.getLocation();
            if (Math.abs(wardenLoc.getX() - target.getX()) <= range &&
                    Math.abs(wardenLoc.getY() - target.getY()) <= range &&
                    Math.abs(wardenLoc.getZ() - target.getZ()) <= range) {
                warden.setDisturbanceLocation(target);
            }
        }
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey(MechanicsCore.getInstance(), "wardendisturbance");
    }

    @Override
    public @NotNull Mechanic serialize(SerializeData data) throws SerializerException {
        double range = data.of("Range").assertRange(0.0, null).getDouble().orElse(16.0);
        return applyParentArgs(data, new WardenDisturbanceMechanic(range));
    }
}