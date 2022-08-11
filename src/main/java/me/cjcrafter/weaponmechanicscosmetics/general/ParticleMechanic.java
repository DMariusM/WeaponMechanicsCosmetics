package me.cjcrafter.weaponmechanicscosmetics.general;

import me.cjcrafter.weaponmechanicscosmetics.trails.ParticleSerializer;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds particles to the {@link me.deecaad.weaponmechanics.mechanics.Mechanics}
 * serializer. Basically just a simple way to add particle effects.
 */
public class ParticleMechanic implements IMechanic<ParticleMechanic> {

    private List<ParticleSerializer> particles;

    /**
     * Default constructor for serializers.
     */
    public ParticleMechanic() {
    }

    public ParticleMechanic(List<ParticleSerializer> particles) {
        this.particles = particles;
    }

    @Override
    public void use(CastData castData) {
        for (ParticleSerializer particle : particles) {
            particle.display(castData.getCastLocation());
        }
    }

    @Override
    public String getKeyword() {
        return "Particles";
    }

    @NotNull
    @Override
    public ParticleMechanic serialize(SerializeData data) throws SerializerException {
        ConfigurationSection config = data.of().assertType(ConfigurationSection.class).assertExists().get();

        List<ParticleSerializer> particles = new ArrayList<>();
        for (String key : config.getKeys(false)) {
            ParticleSerializer particle = data.of(key).assertExists().serialize(ParticleSerializer.class);
            particles.add(particle);
        }

        return new ParticleMechanic(particles);
    }
}
