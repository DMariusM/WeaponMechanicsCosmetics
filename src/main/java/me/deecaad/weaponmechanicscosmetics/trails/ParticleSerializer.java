package me.deecaad.weaponmechanicscosmetics.trails;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ColorSerializer;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ParticleSerializer implements Serializer<ParticleSerializer> {

    private final Particle particle;
    private final int count;
    private final Vector offset; // sometimes velocity
    private final Object options;

    public ParticleSerializer(Particle particle, int count, Vector offset, Object options) {
        this.particle = particle;
        this.count = count;
        this.offset = offset;
        this.options = options;
    }

    @NotNull
    @Override
    public ParticleSerializer serialize(SerializeData data) throws SerializerException {
        Particle particle = data.of("Type").assertExists().getEnum(Particle.class);
        Color color = data.of("Color").serializeNonStandardSerializer(new ColorSerializer());
        Color fade = data.of("Fade_Color").serializeNonStandardSerializer(new ColorSerializer());
        float size = (float) data.of("Size").assertPositive().getDouble(1.0);
        int count = data.of("Count").assertPositive().getInt(-1);

        Vector vector;
        // todo

        return null;
    }
}
