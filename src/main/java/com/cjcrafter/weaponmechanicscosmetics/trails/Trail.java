/*
 * Copyright (c) 2022-2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.trails;

import com.cjcrafter.weaponmechanicscosmetics.mechanics.ParticleMechanic;
import com.cjcrafter.weaponmechanicscosmetics.trails.shape.Line;
import com.cjcrafter.weaponmechanicscosmetics.trails.shape.Shape;
import com.cjcrafter.weaponmechanicscosmetics.trails.shape.Shapes;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.MechanicManager;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.mechanics.targeters.SourceTargeter;
import me.deecaad.core.utils.RandomUtil;
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

        double delta = data.of("Distance_Between_Particles").assertExists().assertRange(0.001, null).getDouble().getAsDouble();
        int skipUpdates = (int) Math.round(data.of("Hide_Trail_For").assertRange(0.0, null).getDouble().orElse(0.5) / delta);
        ListChooser selector = data.of("Particle_Chooser").getEnum(ListChooser.class).orElse(ListChooser.LOOP);

        List<Mechanic> temp = data.of("Particles").assertExists().serialize(MechanicManager.class).get().getMechanics();
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

        Shape shapeType = data.of("Shape").getBukkitRegistry(Shape.class, Shapes.REGISTRY).orElse(new Line());
        Shape finalShape = data.of("Shape_Data").serialize(shapeType).orElse(new Line());
        return new Trail(delta, skipUpdates, selector, particles, finalShape);
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
