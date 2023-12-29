/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileExplodeEvent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

            if (NumberUtil.chance(explosionDensity)) {
                world.spawnParticle(Particle.EXPLOSION_LARGE, reuse, 1, explosionSpread, explosionSpread, explosionSpread);
            }

            if (NumberUtil.chance(smokeDensity)) {
                Vector between = reuse.toVector().subtract(event.getLocation().toVector()).normalize().multiply(NumberUtil.random(0.01, 0.1));
                world.spawnParticle(Particle.SMOKE_NORMAL, reuse, 0, between.getX(), between.getY(), between.getZ());
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
            data.of("Explosion_Particle_Spread").assertExists().assertPositive().getDouble();
            data.of("Smoke_Particle_Density").assertExists().assertRange(0.0, 1.0).getDouble();

            // Construct a list of weapons that should not use the explosion
            // effects, and check to make sure each weapon actually exists.
            Set<String> weaponBlacklist = new HashSet<>();
            List<String[]> temp = data.ofList("Weapon_Blacklist")
                    .addArgument(String.class, true, true)
                    .assertExists().assertList().get();

            List<String> weaponOptions = WeaponMechanics.getWeaponHandler().getInfoHandler().getSortedWeaponList();
            for (int i = 0; i < temp.size(); i++) {
                String[] split = temp.get(i);

                // TODO cannot check weapons yet since they haven't been serialized!
                //if (!weaponOptions.contains(split[0]))
                //    throw new SerializerOptionsException(getKeyword(), "Weapon", weaponOptions, split[0], data.ofList("Weapon_Blacklist").getLocation(i));

                weaponBlacklist.add(split[0]);
            }

            configuration.set("Explosion_Effects.Weapon_Blacklist", weaponBlacklist);
        }
    }
}
