package me.cjcrafter.weaponmechanicscosmetics.trails;

import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.Vec2;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This {@link ProjectileScript} handles the {@link Trail} feature by
 * spawning particles around the projectile at the end of every tick. Due
 * to many calls to {@link Math#sqrt(double)}, this feature can consume a
 * con
 */
public class TrailScript extends ProjectileScript<AProjectile> {

    private Trail trail;
    private double spillOverDistance;
    private int updates;

    public TrailScript(@NotNull Plugin owner, @NotNull AProjectile projectile, Trail trail) {
        super(owner, projectile);
        this.trail = trail;
    }

    public Trail getTrail() {
        return trail;
    }

    public void setTrail(Trail trail) {
        this.trail = trail;
    }

    @Override
    public void onTickEnd() {
        Vector direction = projectile.getLocation().subtract(projectile.getLastLocation());
        double distance = direction.length();
        direction.multiply(1.0 / distance);
        distance += spillOverDistance;

        Vector current = projectile.getLocation().subtract(direction.clone().multiply(spillOverDistance));

        Vector a = VectorUtil.getPerpendicular(direction).normalize();
        Vector b = direction.clone().crossProduct(a).normalize();

        // Make sure this happens after perpendicular vector calculations
        direction.multiply(trail.getDelta());

        while ((distance -= trail.getDelta()) >= trail.getDelta()) {
            ParticleSerializer particle = trail.getParticle(updates);
            List<Vec2> points = trail.getShape().getPoint(updates);

            for (Vec2 point : points) {
                double x = current.getX() + point.x * a.getX() + point.y * b.getX();
                double y = current.getY() + point.x * a.getY() + point.y * b.getY();
                double z = current.getZ() + point.x * a.getZ() + point.y * b.getZ();

                particle.display(projectile.getWorld(), x, y, z);
            }

            current.add(direction);
            updates++;
        }

        spillOverDistance = distance;
    }
}
