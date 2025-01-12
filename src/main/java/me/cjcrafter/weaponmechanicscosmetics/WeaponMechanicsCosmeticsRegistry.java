package me.cjcrafter.weaponmechanicscosmetics;

import me.cjcrafter.weaponmechanicscosmetics.trails.Trail;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.Line;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.ParametricFunctionShape;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.Shape;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.Spiral;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.StringFunctionShape;
import org.bukkit.Registry;

import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanicsRegistry.SimpleWritableRegistry;

/**
 * Utility class of all {@link Registry registries} for created by WMC.
 */
public final class WeaponMechanicsCosmeticsRegistry {

    /**
     * All shapes that can be used in the {@link Trail} serializer.
     */
    public static final SimpleWritableRegistry<Shape> TRAIL_SHAPES = new SimpleWritableRegistry<>(
        List.of(new Line(), new ParametricFunctionShape(), new Spiral(), new StringFunctionShape())
    );
}
