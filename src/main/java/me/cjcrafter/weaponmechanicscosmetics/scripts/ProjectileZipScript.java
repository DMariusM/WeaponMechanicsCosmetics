package me.cjcrafter.weaponmechanicscosmetics.scripts;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.VectorUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.mechanics.defaultmechanics.SoundMechanic;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;

public class ProjectileZipScript extends ProjectileScript<WeaponProjectile> {

    private final double distanceSquared;
    private final Mechanics mechanics;

    public ProjectileZipScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        super(owner, projectile);

        Configuration config = WeaponMechanics.getConfigurations();
        double distance = config.getDouble(projectile.getWeaponTitle() + ".Cosmetics.Bullet_Zip.Maximum_Distance", -1.0);
        this.distanceSquared = distance * distance;
        this.mechanics = config.getObject(projectile.getWeaponTitle() + ".Cosmetics.Bullet_Zip.Mechanics", Mechanics.class);

        if (distance == -1.0 || mechanics == null)
            throw new IllegalArgumentException("todo");
    }

    @Override
    public void onTickEnd() {
        Debugger debug = WeaponMechanicsCosmetics.getInstance().getDebug();
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

            //if (debug.canLog(LogLevel.DEBUG)) {
            //    debugPoint(world, start, Color.BLACK);
            //    debugPoint(world, stop, Color.WHITE);
            //    debugRay(world, point, a, closeEnough ? Color.GREEN : Color.RED);
            //}

            if (closeEnough)
                mechanics.use(new CastData(point.toLocation(world)));
        }
    }

    private static Collection<LivingEntity> getPlayers(World world, Vector min, Vector max) {
        Collection<LivingEntity> temp = new LinkedList<>();

        int minX = floor(min.getX() - 1.0D) >> 4;
        int maxX = floor(max.getX() + 1.0D) >> 4;
        int minZ = floor(min.getZ() - 1.0D) >> 4;
        int maxZ = floor(max.getZ() + 1.0D) >> 4;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                Chunk chunk = world.getChunkAt(x, z);

                for (Entity entity : chunk.getEntities()) {
                    if (entity.getType().isAlive())
                        temp.add((LivingEntity) entity);
                }
            }
        }

        return temp;
    }


    public void debugRay(World world, Vector min, Vector max, Color color) {
        int particles = 15;
        for (int i = 0; i < particles; i++) {
            Vector point = VectorUtil.lerp(min, max, (double) i / particles);
            debugPoint(world, point, i == 0 || i == particles - 1 ? Color.BLUE : color);
        }
    }

    public void debugPoint(World world, Vector point, Color color) {
        Particle.DustOptions options = new Particle.DustOptions(color, 0.8f);
        new BukkitRunnable() {
            int count = 15;
            @Override
            public void run() {
                if (count-- < 0)
                    cancel();

                world.spawnParticle(Particle.REDSTONE, point.getX(), point.getY(), point.getZ(), 1, options);
            }
        }.runTaskTimerAsynchronously(WeaponMechanicsCosmetics.getInstance().getPlugin(), 0, 1);
    }

    public static int floor(double value) {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }
}
