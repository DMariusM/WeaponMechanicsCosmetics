package me.deecaad.weaponmechanicscosmetics;

import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScriptManager;
import me.deecaad.weaponmechanicscosmetics.trails.ParticleSerializer;
import me.deecaad.weaponmechanicscosmetics.trails.Trail;
import me.deecaad.weaponmechanicscosmetics.trails.TrailScript;
import me.deecaad.weaponmechanicscosmetics.trails.shape.Spiral;
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
        }

        List<ParticleSerializer> list = Arrays.asList(
                new ParticleSerializer(Particle.DUST_COLOR_TRANSITION, 1, 1.0f, new Vector(), new Particle.DustTransition(Color.RED, Color.WHITE, 1.0f)),
                new ParticleSerializer(Particle.DUST_COLOR_TRANSITION, 1, 1.0f, new Vector(), new Particle.DustTransition(Color.WHITE, Color.BLUE, 1.0f)),
                new ParticleSerializer(Particle.DUST_COLOR_TRANSITION, 1, 1.0f, new Vector(), new Particle.DustTransition(Color.BLUE, Color.RED, 1.0f))
        );
        Trail trail = new Trail(0.2, Trail.ListChooser.LOOP, list, new Spiral(2.0, 16, 2));
        aProjectile.addProjectileScript(new TrailScript(getPlugin(), aProjectile, trail));
    }
}
