package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileExplodeEvent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class ExplosionEffectSpawner implements Listener {

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onExplode(ProjectileExplodeEvent event) {

        // Iterate through each block, and check the configurable chance to
        // determine when to spawn the configurable particle.
        World world = event.getEntity().getWorld();
        Location reuse = new Location(world, 0.0, 0.0, 0.0);

        for (Block block : event.getBlocks()) {
            block.getLocation(reuse);
            double noise = 1.0;

            // TODO check config.yml "Explosion_Effects.Explosion_Particles"
            if (NumberUtil.chance(0.30)) {
                world.spawnParticle(Particle.EXPLOSION_LARGE, reuse, 1, noise, noise, noise);
            }

            // TODO check config.yml "Explosion_Effects.Smoke_Particles"
            if (NumberUtil.chance(0.50)) {
                Vector between = reuse.toVector().add(event.getLocation().toVector());
                world.spawnParticle(Particle.SMOKE_LARGE, reuse, 0, between.getX(), between.getY(), between.getZ());
            }
        }
    }
}
