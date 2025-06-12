package com.cjcrafter.weaponmechanicscosmetics.mechanics.targeters;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.VectorProvider;
import me.deecaad.core.file.serializers.VectorSerializer;
import me.deecaad.core.mechanics.targeters.ShapeTargeter;
import me.deecaad.core.mechanics.targeters.Targeter;

public abstract class AxisShapeTargeter extends ShapeTargeter {
    protected VectorProvider axis;

    @Override
    protected Targeter applyParentArgs(SerializeData data, Targeter targeter) throws SerializerException {
        AxisShapeTargeter axisShape = (AxisShapeTargeter) targeter;
        axisShape.axis = data.of("Axis").serialize(VectorSerializer.class).orElse(null);
        return super.applyParentArgs(data, targeter);
    }
}
