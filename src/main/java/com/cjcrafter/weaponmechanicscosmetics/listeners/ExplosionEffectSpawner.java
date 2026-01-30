/*
 * Copyright (c) 2022-2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.listeners;

import com.cryptomorin.xseries.particles.XParticle;
import com.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.simple.StringSerializer;
import me.deecaad.core.utils.RandomUtil;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileExplodeEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.stream.Collectors;

public class ExplosionEffectSpawner implements Listener {

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onExplode(ProjectileExplodeEvent event) {

        Configuration config = WeaponMechanicsCosmetics.getInstance().getConfiguration();

        Set<String> weaponBlacklist = config.getObject("Explosion_Effects.Weapon_Blacklist", Set.class);
        if (weaponBlacklist.contains(event.getWeaponTitle()))
            return;

        double explosionDensity = config.getDouble("Explosion_Effects.Explosion_Particle_Density");
        double explosionSpread = config.getDouble("Explosion_Effects.Explosion_Particle_Spread");
        double smokeDensity = config.getDouble("Explosion_Effects.Smoke_Particle_Density");

        // Iterate through each block, and check the configurable chance to
        // determine when to spawn the configurable particle.
        World world = event.getEntity().getWorld();
        Location reuse = new Location(world, 0.0, 0.0, 0.0);

        for (Block block : event.getBlocks()) {
            block.getLocation(reuse);

            if (RandomUtil.chance(explosionDensity)) {
                world.spawnParticle(XParticle.EXPLOSION_EMITTER.get(), reuse, 1, explosionSpread, explosionSpread, explosionSpread);
            }

            if (RandomUtil.chance(smokeDensity)) {
                Vector between = reuse.toVector().subtract(event.getLocation().toVector()).normalize().multiply(RandomUtil.range(0.01, 0.1));
                world.spawnParticle(XParticle.SMOKE.get(), reuse, 0, between.getX(), between.getY(), between.getZ());
            }
        }
    }

    public static class ExplosionEffectValidator implements IValidator {

        @Override
        public String getKeyword() {
            return "Explosion_Effects";
        }

        @Override
        public void validate(Configuration configuration, SerializeData data) throws SerializerException {

            // Just check that type/range is proper, no need to set anything
            data.of("Explosion_Particle_Density").assertExists().assertRange(0.0, 1.0).getDouble();
            data.of("Explosion_Particle_Spread").assertExists().assertRange(0.0, null).getDouble();
            data.of("Smoke_Particle_Density").assertExists().assertRange(0.0, 1.0).getDouble();

            // Construct a list of weapons that should not use the explosion
            // effects, and check to make sure each weapon actually exists.
            Set<String> weaponBlacklist = data.ofList("Weapon_Blacklist")
                .addArgument(new StringSerializer())
                .requireAllPreviousArgs()
                .assertExists()
                .assertList()
                .stream()
                .map(split -> split.get(0).get().toString())
                .collect(Collectors.toSet());

            configuration.set("Explosion_Effects.Weapon_Blacklist", weaponBlacklist);
        }
    }
}
