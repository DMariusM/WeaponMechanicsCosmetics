package me.cjcrafter.weaponmechanicscosmetics;

import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;

public class ProjectileWhizScript extends ProjectileScript<WeaponProjectile> {

    private final double distanceSquared;
    private final Mechanics mechanics;

    public ProjectileWhizScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        super(owner, projectile);

        Configuration config = WeaponMechanics.getConfigurations();
        double distance = config.getDouble(projectile.getWeaponTitle() + ".Effects.Bullet_Whiz.Maximum_Distance", -1.0);
        this.distanceSquared = distance * distance;
        this.mechanics = config.getObject(projectile.getWeaponTitle() + ".Effects.Bullet_Whiz.Mechanics", Mechanics.class);

        if (distance == -1.0 || mechanics == null)
            throw new IllegalArgumentException("todo");
    }

    @Override
    public void onTickEnd() {
        World world = projectile.getWorld();

        // https://math.stackexchange.com/questions/1905533/find-perpendicular-distance-from-point-to-line-in-3d
        Vector a = projectile.getLastLocation();
        Vector b = projectile.getLocation();

        for (Player player : getPlayers(world, Vector.getMinimum(a, b), Vector.getMaximum(a, b))) {
            Vector c = player.getEyeLocation().toVector();

            Vector d = c.clone().subtract(b).multiply(1.0 / c.distance(b));
            Vector v = a.subtract(b);
            double t = v.dot(d);

            // This point is the closest point on the line AB to the player.
            // We need to check if the distance between 'point' and the player
            // is small enough the play the sound.
            Vector point = b.add(d.multiply(t));
            if (point.distanceSquared(c) < distanceSquared)
                mechanics.use(new CastData(point.toLocation(world)));
        }
    }

    private static Collection<Player> getPlayers(World world, Vector min, Vector max) {
        Collection<Player> temp = new LinkedList<>();

        int minX = floor(min.getX() - 1.0D) << 4;
        int maxX = floor(max.getX() + 1.0D) << 4;
        int minZ = floor(min.getZ() - 1.0D) << 4;
        int maxZ = floor(max.getZ() + 1.0D) << 4;

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

    public static int floor(double value) {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }
}
