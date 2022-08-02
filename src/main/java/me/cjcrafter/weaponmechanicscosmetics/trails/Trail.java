package me.cjcrafter.weaponmechanicscosmetics.trails;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.NumberUtil;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.Shape;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.ShapeFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Trail implements Serializer<Trail> {

    private double delta;
    private ListChooser chooser;
    private List<ParticleSerializer> particles;
    private Shape shape;

    /**
     * Default constructor for serializer
     */
    public Trail() {
    }

    public Trail(double delta, ListChooser chooser, List<ParticleSerializer> particles, Shape shape) {
        this.delta = delta;
        this.chooser = chooser;
        this.particles = particles;
        this.shape = shape;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public ListChooser getChooser() {
        return chooser;
    }

    public void setChooser(ListChooser chooser) {
        this.chooser = chooser;
    }

    public List<ParticleSerializer> getParticles() {
        return particles;
    }

    public void setParticles(List<ParticleSerializer> particles) {
        this.particles = particles;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public ParticleSerializer getParticle(int index) {
        switch (chooser) {
            case LOOP:
                return particles.get(index % particles.size());
            case RANDOM:
                return particles.get(NumberUtil.random(particles.size()));
            case STOP:
                return particles.get(Math.min(index, particles.size()));
            default:
                throw new RuntimeException("unreachable code");
        }
    }

    @Override
    public String getKeyword() {
        return "Trail";
    }

    @NotNull
    @Override
    public Trail serialize(SerializeData data) throws SerializerException {

        double delta = data.of("Distance_Between_Particles").assertExists().assertPositive().getDouble();
        ListChooser selector = data.of("Particle_Chooser").getEnum(ListChooser.class, ListChooser.LOOP);

        // We need to serialize a list of ParticleSerializers. The user should
        // always define at least 1 particle.
        ConfigurationSection section = data.of("Particles").assertType(ConfigurationSection.class).assertExists().get();

        // Particles are generally stored like 'Particle_1', 'Particle_2', but
        // in reality, we don't care what they name it, as long as they use the
        // particle serializer correctly.
        List<ParticleSerializer> particles = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            particles.add(data.of("Particles." + key).assertExists().serialize(ParticleSerializer.class));
        }

        String shapeInput = data.of("Shape").get("LINE").trim().toUpperCase(Locale.ROOT);
        ConfigurationSection shapeConfig = data.of("Shape_Data").assertType(ConfigurationSection.class).assertExists(!shapeInput.equalsIgnoreCase("LINE")).get(null);

        Map<String, Object> shapeData = shapeConfig == null ? new HashMap<>() : shapeConfig.getValues(false);

        Shape shape;
        try {
            shape = ShapeFactory.getInstance().get(shapeInput, shapeData);
        } catch (SerializerException e) {
            e.setLocation(data.of("Shape_Data").getLocation());
            throw e;
        }

        return new Trail(delta, selector, particles, shape);
    }

    public enum ListChooser {
        STOP, RANDOM, LOOP
    }
}
