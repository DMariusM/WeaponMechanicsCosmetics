package me.deecaad.weaponmechanicscosmetics.trails;

import me.deecaad.weaponmechanicscosmetics.trails.shape.Shape;

import java.util.List;

public class Trail {

    double delta;
    ListChooser chooser;
    List<ParticleSerializer> particles;
    Shape shape;


    public enum ListChooser {
        STOP, RANDOM, LOOP
    }
}
