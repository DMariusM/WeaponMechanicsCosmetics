package me.cjcrafter.weaponmechanicscosmetics;

import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScriptManager;
import me.cjcrafter.weaponmechanicscosmetics.trails.ParticleSerializer;
import me.cjcrafter.weaponmechanicscosmetics.trails.Trail;
import me.cjcrafter.weaponmechanicscosmetics.trails.TrailScript;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.FunctionShape;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.Shape;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CosmeticsScriptManager extends ProjectileScriptManager {

    public CosmeticsScriptManager(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void attach(@NotNull AProjectile aProjectile) {
        if (aProjectile.getIntTag("explosion-falling-block") == 1) {
            FallingBlockScript script = new FallingBlockScript(getPlugin(), aProjectile);
            aProjectile.addProjectileScript(script);
            return;
        }

        List<ParticleSerializer> list = Arrays.asList(
                new ParticleSerializer(Particle.DUST_COLOR_TRANSITION, 1, 1.0f, new Vector(), new Particle.DustTransition(Color.RED, Color.WHITE, 0.25f)),
                new ParticleSerializer(Particle.DUST_COLOR_TRANSITION, 1, 1.0f, new Vector(), new Particle.DustTransition(Color.WHITE, Color.BLUE, 0.25f)),
                new ParticleSerializer(Particle.DUST_COLOR_TRANSITION, 1, 1.0f, new Vector(), new Particle.DustTransition(Color.BLUE, Color.RED, 0.25f))
        );

        Shape shape = new FunctionShape(32, 32) {
            @Override
            public double radiusFunction(double theta) {
                return 2 * Math.cos(2 * theta);
            }
        };
        Trail trail = new Trail(0.2, Trail.ListChooser.LOOP, list, shape);
        aProjectile.addProjectileScript(new TrailScript(getPlugin(), aProjectile, trail));
    }
}
