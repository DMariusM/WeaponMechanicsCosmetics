/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.trails;

import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.cjcrafter.weaponmechanicscosmetics.trails.shape.Vec2;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.Color;
import org.bukkit.Particle;
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

    public TrailScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        this(owner, projectile, WeaponMechanics.getConfigurations().getObject(projectile.getWeaponTitle() + ".Trail", Trail.class));
    }

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
        Vector normal = direction.clone();

        // Spill over occurs when there was a "small distance"
        // (<trail.getDelta()) left over from the previous tick. So we move the
        // current point backwards a little, and make sure to trace forwards
        // a little more.
        distance += spillOverDistance;
        Vector current = projectile.getLastLocation().subtract(direction.clone().multiply(spillOverDistance));

        // Create an axis of 3 perpendicular vectors... direction is the third
        Vector a = VectorUtil.getPerpendicular(direction).normalize();
        Vector b = direction.clone().crossProduct(a).normalize();

        // Make sure this happens after perpendicular vector calculations
        // 'direction' now stores the step size
        direction.multiply(trail.getDelta());

        // Instead of using a do-while loop, we add 1 extra iteration to the
        // loop. We need to do this since the while loop condition executes
        // before the code block executes.
        distance += trail.getDelta();
        while ((distance -= trail.getDelta()) >= trail.getDelta()) {

            // Usually you don't want to show the trail for the first 0.5
            // blocks, since it covers the player's screen too much
            if (trail.getSkipUpdates() >= updates) {
                current.add(direction);
                updates++;
                continue;
            }

            ParticleSerializer particle = trail.getParticle(updates);
            List<Vec2> points = trail.getShape().getPoint(updates);

            for (Vec2 point : points) {
                double x = current.getX() + point.x * a.getX() + point.y * b.getX();
                double y = current.getY() + point.x * a.getY() + point.y * b.getY();
                double z = current.getZ() + point.x * a.getZ() + point.y * b.getZ();

                particle.display(projectile.getWorld(), x, y, z, normal);
            }

            current.add(direction);
            updates++;
        }

        spillOverDistance = distance;
    }
}
