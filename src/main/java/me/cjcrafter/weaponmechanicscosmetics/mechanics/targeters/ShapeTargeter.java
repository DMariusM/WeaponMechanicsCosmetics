package me.cjcrafter.weaponmechanicscosmetics.mechanics.targeters;

import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.targeters.Targeter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public abstract class ShapeTargeter extends Targeter {

    @Override
    protected List<CastData> getTargets0(CastData cast) {
        Location[] points = getPoints(cast);
        List<CastData> targets = new ArrayList<>(points.length);

        for (Location point : points) {
            CastData clone = cast.clone();
            clone.setTargetLocation(point);
            targets.add(clone);
        }

        return targets;
    }

    public abstract Location[] getPoints(CastData cast);
}
