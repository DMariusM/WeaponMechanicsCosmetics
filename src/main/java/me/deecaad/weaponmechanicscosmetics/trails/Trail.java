package me.deecaad.weaponmechanicscosmetics.trails;

import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanicscosmetics.trails.shape.Shape;

import java.util.List;

public class Trail {

    private double delta;
    private ListChooser chooser;
    private List<ParticleSerializer> particles;
    private Shape shape;

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

    public enum ListChooser {
        STOP, RANDOM, LOOP
    }
}
