/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package com.cjcrafter.weaponmechanicscosmetics.scripts;

import com.cjcrafter.foliascheduler.ServerImplementation;
import com.cjcrafter.foliascheduler.TaskImplementation;
import com.cryptomorin.xseries.particles.XParticle;
import com.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.MechanicManager;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;

public class ZipScript extends ProjectileScript<WeaponProjectile> {

    private double distanceSquared;
    private MechanicManager mechanics;

    public ZipScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        super(owner, projectile);

        Configuration config = WeaponMechanics.getInstance().getWeaponConfigurations();
        double distance = config.getDouble(projectile.getWeaponTitle() + ".Cosmetics.Bullet_Zip.Maximum_Distance", -1.0);
        this.distanceSquared = distance * distance;
        this.mechanics = config.getObject(projectile.getWeaponTitle() + ".Cosmetics.Bullet_Zip.Sounds", MechanicManager.class);

        if (distance == -1.0 || mechanics == null)
            throw new IllegalArgumentException('.' + projectile.getWeaponTitle() + ".Cosmetics.Bullet_Zip' is incomplete");
    }

    public ZipScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile, double distanceSquared, @NotNull MechanicManager mechanics) {
        super(owner, projectile);

        this.distanceSquared = distanceSquared;
        this.mechanics = mechanics;
    }

    public double getDistanceSquared() {
        return distanceSquared;
    }

    public void setDistanceSquared(double distanceSquared) {
        this.distanceSquared = distanceSquared;
    }

    public @NotNull MechanicManager getMechanics() {
        return mechanics;
    }

    public void setMechanics(@NotNull MechanicManager mechanics) {
        this.mechanics = mechanics;
    }

    @Override
    public void onTickEnd() {
        //Debugger debug = WeaponMechanicsCosmetics.getInstance().getDebug();
        World world = projectile.getWorld();

        // https://math.stackexchange.com/questions/1905533/find-perpendicular-distance-from-point-to-line-in-3d
        Vector start = projectile.getLastLocation();
        Vector stop = projectile.getLocation();

        Vector min = Vector.getMinimum(start, stop);
        Vector max = Vector.getMaximum(start, stop);

        for (LivingEntity player : getPlayers(world, min, max)) {
            if (player.equals(projectile.getShooter()))
                continue;

            Vector a = player.getEyeLocation().toVector();

            Vector d = start.clone().subtract(stop).normalize();
            Vector v = a.clone().subtract(stop);
            double t = v.dot(d);

            // This point is the closest point on the line AB to the player.
            // We need to check if the distance between 'point' and the player
            // is small enough the play the sound.
            Vector point = stop.clone().add(d.multiply(t));

            // Since direction vectors are infinite, and we only want to play
            // the sound between min and max, we have to make sure point is
            // between min and max.
            if (!point.isInAABB(min, max))
                continue;

            boolean closeEnough = a.distanceSquared(point) < distanceSquared;
            if (closeEnough) {
                CastData cast = new CastData(player, projectile.getWeaponTitle(), projectile.getWeaponStack());
                cast.setTargetLocation(point.toLocation(world));
                cast.setTargetEntity(player);
                mechanics.use(cast);
            }
        }
    }

    private static @NotNull Collection<Player> getPlayers(@NotNull World world, @NotNull Vector min, @NotNull Vector max) {
        Collection<Player> temp = new LinkedList<>();

        int minX = floor(min.getX() - 1.0D) >> 4;
        int maxX = floor(max.getX() + 1.0D) >> 4;
        int minZ = floor(min.getZ() - 1.0D) >> 4;
        int maxZ = floor(max.getZ() + 1.0D) >> 4;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                Chunk chunk = world.getChunkAt(x, z);

                for (Entity entity : chunk.getEntities()) {
                    if (entity.getType() == EntityType.PLAYER)
                        temp.add((Player) entity);
                }
            }
        }

        return temp;
    }


    public void debugRay(@NotNull World world, @NotNull Vector min, @NotNull Vector max, @NotNull Color color) {
        int particles = 15;
        for (int i = 0; i < particles; i++) {
            Vector point = VectorUtil.lerp(min, max, (double) i / particles);
            debugPoint(world, point, i == 0 || i == particles - 1 ? Color.BLUE : color);
        }
    }

    public void debugPoint(World world, Vector point, Color color) {
        Particle.DustOptions options = new Particle.DustOptions(color, 0.8f);
        ServerImplementation scheduler = WeaponMechanicsCosmetics.getInstance().getFoliaScheduler();
        scheduler.async().runAtFixedRate(new Consumer<>() {
            int count = 15;
            @Override
            public void accept(TaskImplementation<Void> scheduledTask) {
                if (count-- < 0)
                    scheduledTask.cancel();

                world.spawnParticle(XParticle.DUST.get(), point.getX(), point.getY(), point.getZ(), 1, options);
            }
        }, 0, 1);
    }

    public static int floor(double value) {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }
}
