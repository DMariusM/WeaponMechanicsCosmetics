/*
 * Copyright (c) 2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.config;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.simple.RegistryValueSerializer;
import me.deecaad.core.file.simple.StringSerializer;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.MechanicManager;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.utils.RandomUtil;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class BlockSoundSerializer implements Serializer<BlockSoundSerializer> {

    private SoundGroupType type;
    private float randomness;
    private Map<BlockType, Mechanic> overrides;
    private Set<BlockType> materialBlacklist;
    private Set<String> weaponBlacklist;

    /**
     * Default constructor for serializer
     */
    public BlockSoundSerializer() {
    }

    public BlockSoundSerializer(SoundGroupType type, float randomness, Map<BlockType, Mechanic> overrides,
                                Set<BlockType> materialBlacklist, Set<String> weaponBlacklist) {
        this.type = type;
        this.randomness = randomness;
        this.overrides = overrides;
        this.materialBlacklist = materialBlacklist;
        this.weaponBlacklist = weaponBlacklist;
    }

    public void play(WeaponProjectile projectile, BlockState block) {
        World world = projectile.getWorld();
        Location loc = projectile.getLocation().toLocation(world);

        // Handle blacklists
        if (materialBlacklist.contains(block.getType().asBlockType()) || weaponBlacklist.contains(projectile.getWeaponTitle()))
            return;

        // Handle sound overrides
        Mechanic override = overrides.get(block.getType());
        if (override != null && projectile.getShooter() != null) {
            CastData cast = new CastData(projectile.getShooter(), projectile.getWeaponTitle(), projectile.getWeaponStack());
            cast.setTargetLocation(loc);
            override.use(cast);
            return;
        }

        // Play default block sound
        SoundGroup group = block.getBlockData().getSoundGroup();
        play(loc, type.getSound(group), group.getVolume(), group.getPitch(), randomness);
    }

    public void play(Location loc, String sound, float volume, float pitch, float randomness) {
        pitch += RandomUtil.range(-randomness, randomness);
        loc.getWorld().playSound(loc, sound, SoundCategory.BLOCKS, volume, pitch);
    }

    public void play(Location loc, Sound sound, float volume, float pitch, float randomness) {
        pitch += RandomUtil.range(-randomness, randomness);
        loc.getWorld().playSound(loc, sound, SoundCategory.BLOCKS, volume, pitch);
    }

    @NotNull
    @Override
    public BlockSoundSerializer serialize(SerializeData data) throws SerializerException {

        // Let people completely disable the feature
        boolean enabled = data.of("Enabled").assertExists().getBool().get();
        if (!enabled) {
            return new BlockSoundSerializer() {
                @Override
                public void play(WeaponProjectile projectile, BlockState block) {
                    // do nothing...
                }
            };
        }

        SoundGroupType type = data.of("Type").assertExists().getEnum(SoundGroupType.class).get();
        float defaultRandomness = (float) data.of("Default_Randomness").assertExists().assertRange(0.0, 1.0).getDouble().getAsDouble();

        // Construct a list of overrides to use in case the default block
        // sounds are not good enough. Also supports playing custom sounds
        // instead of bukkit sounds. This could also be used to replace
        // every block sound, so let's take performance into consideration.
        Map<BlockType, Mechanic> overrides = new HashMap<>();
        List<?> list = data.of("Overrides").get(List.class).orElse(List.of());
        for (int i = 0; i < list.size(); i++) {
            String str = list.get(i).toString();
            int split = str.indexOf(" ");

            if (split == -1)
                throw data.listException("Overrides", i, "Override format should be: <Material> <SoundMechanic>",
                    "Found value: " + str);

            // Extract and parse the data from the string
            String materialStr = str.substring(0, split);
            List<BlockType> materials = new RegistryValueSerializer<>(BlockType.class, true).deserialize(materialStr, data.ofList("Overrides").getLocation(i));
            String mechanicStr = str.substring(split + 1);
            Mechanic mechanic = new MechanicManager().serializeOne(data, mechanicStr);

            // Fill the overrides map
            for (BlockType mat : materials)
                overrides.put(mat, mechanic);
        }

        // Construct a list of materials that shouldn't have any effects.
        // We use a map since EnumMap is very fast, and there is no EnumSet
        // equivalent.
        Set<BlockType> materialBlacklist = new HashSet<>();
        List<List<Optional<Object>>> temp = data.ofList("Material_Blacklist")
            .addArgument(new RegistryValueSerializer<>(BlockType.class, true))
            .assertExists().assertList();

        for (List<Optional<Object>> split : temp) {
            List<BlockType> materials = (List<BlockType>) split.get(0).get();

            materialBlacklist.addAll(materials);
        }

        // Construct a list of weapons that should not use the block
        // effects. We check each weapon to make sure it exists in
        // WeaponMechanics.
        Set<String> weaponBlacklist = data.ofList("Weapon_Blacklist")
            .addArgument(new StringSerializer())
            .requireAllPreviousArgs()
            .assertExists()
            .assertList()
            .stream()
            .map(split -> split.get(0).get().toString().trim())
            .collect(Collectors.toSet());

        return new BlockSoundSerializer(type, defaultRandomness, overrides, materialBlacklist, weaponBlacklist);
    }

    @Override
    public String getKeyword() {
        return "Block_Sounds";
    }
}