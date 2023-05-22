package me.cjcrafter.weaponmechanicscosmetics.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import org.bukkit.Location;
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
    public String getKeyword() {
        return "Warden_Disturbance";
    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        double range = data.of("Range").assertPositive().getDouble(16.0);
        return applyParentArgs(data, new WardenDisturbanceMechanic(range));
    }
}