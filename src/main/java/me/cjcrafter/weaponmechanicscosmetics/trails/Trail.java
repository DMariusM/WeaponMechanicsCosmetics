/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.trails;

import me.cjcrafter.weaponmechanicscosmetics.mechanics.ParticleMechanic;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.Shape;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.ShapeFactory;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.targeters.SourceTargeter;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.RandomUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Trail implements Serializer<Trail>, Cloneable {

    private double delta;
    private int skipUpdates;
    private ListChooser chooser;
    private List<ParticleMechanic> particles;
    private Shape shape;

    /**
     * Default constructor for serializer
     */
    public Trail() {
    }

    public Trail(double delta, int skipUpdates, ListChooser chooser, List<ParticleMechanic> particles, Shape shape) {
        this.delta = delta;
        this.skipUpdates = skipUpdates;
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

    public int getSkipUpdates() {
        return skipUpdates;
    }

    public void setSkipUpdates(int skipUpdates) {
        this.skipUpdates = skipUpdates;
    }

    public ListChooser getChooser() {
        return chooser;
    }

    public void setChooser(ListChooser chooser) {
        this.chooser = chooser;
    }

    public List<ParticleMechanic> getParticles() {
        return particles;
    }

    public void setParticles(List<ParticleMechanic> particles) {
        this.particles = particles;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public ParticleMechanic getParticle(int index) {
        return switch (chooser) {
            case LOOP -> particles.get(index % particles.size());
            case RANDOM -> RandomUtil.element(particles);
            case STOP -> particles.get(Math.min(index, particles.size() - 1));
        };
    }

    @Override
    public String getKeyword() {
        return "Trail";
    }

    @NotNull
    @Override
    public Trail serialize(SerializeData data) throws SerializerException {

        double delta = data.of("Distance_Between_Particles").assertExists().assertPositive().getDouble();
        int skipUpdates = (int) Math.round(data.of("Hide_Trail_For").assertPositive().getDouble(0.5) / delta);
        ListChooser selector = data.of("Particle_Chooser").getEnum(ListChooser.class, ListChooser.LOOP);

        List<Mechanic> temp = data.of("Particles").serialize(Mechanics.class).getMechanics();
        List<ParticleMechanic> particles = new ArrayList<>(temp.size());
        for (int i = 0; i < temp.size(); i++) {
            if (!(temp.get(i) instanceof ParticleMechanic particle))
                throw data.listException("Particles", i, "Expected a Particle, but got a '" + temp.get(i).getClass().getSimpleName() + "'");
            if (!(particle.getTargeter() instanceof SourceTargeter))
                throw data.listException("Particles", i, "Targeters are not supported here... You can only use particles");
            if (!particle.getConditions().isEmpty())
                throw data.listException("Particles", i, "Conditions are not supported here... You can only use particles");

            particles.add(particle);
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

        return new Trail(delta, skipUpdates, selector, particles, shape);
    }

    @Override
    public Trail clone() {
        try {
            Trail clone = (Trail) super.clone();
            clone.particles = new ArrayList<>(clone.particles);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public enum ListChooser {
        STOP, RANDOM, LOOP
    }
}
