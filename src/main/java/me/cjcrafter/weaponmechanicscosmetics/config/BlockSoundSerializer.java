package me.cjcrafter.weaponmechanicscosmetics.config;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.block.BlockCompatibility;
import me.deecaad.core.file.*;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BlockSoundSerializer implements Serializer<BlockSoundSerializer> {

    private BlockCompatibility.SoundType type;
    private float randomness;
    private Map<Material, Mechanic> overrides;
    private Map<Material, Object> materialBlacklist;
    private Set<String> weaponBlacklist;

    /**
     * Default constructor for serializer
     */
    public BlockSoundSerializer() {
    }

    public BlockSoundSerializer(BlockCompatibility.SoundType type, float randomness, Map<Material, Mechanic> overrides,
                      Map<Material, Object> materialBlacklist, Set<String> weaponBlacklist) {
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
        if (materialBlacklist.containsKey(block.getType()) || weaponBlacklist.contains(projectile.getWeaponTitle()))
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
        Object data = ReflectionUtil.getMCVersion() < 13 ? new MaterialData(block.getType(), block.getRawData()) : block.getBlockData();
        BlockCompatibility.SoundData sound = CompatibilityAPI.getBlockCompatibility().getBlockSound(data, type);
        play(loc, sound.sound, sound.volume, sound.pitch, randomness);
    }

    public void play(Location loc, String sound, float volume, float pitch, float randomness) {
        pitch += (float) NumberUtil.random(-randomness, randomness);
        if (ReflectionUtil.getMCVersion() < 11) {
            loc.getWorld().playSound(loc, sound, volume, pitch);
        } else {
            loc.getWorld().playSound(loc, sound, SoundCategory.BLOCKS, volume, pitch);
        }
    }

    public void play(Location loc, Sound sound, float volume, float pitch, float randomness) {
        pitch += (float) NumberUtil.random(-randomness, randomness);
        if (ReflectionUtil.getMCVersion() < 11) {
            loc.getWorld().playSound(loc, sound, volume, pitch);
        } else {
            loc.getWorld().playSound(loc, sound, SoundCategory.BLOCKS, volume, pitch);
        }
    }

    @NotNull
    @Override
    public BlockSoundSerializer serialize(SerializeData data) throws SerializerException {

        // Let people completely disable the feature
        boolean enabled = data.of("Enabled").assertExists().getBool();
        if (!enabled) {
            return new BlockSoundSerializer() {
                @Override
                public void play(WeaponProjectile projectile, BlockState block) {
                    // do nothing...
                }
            };
        }

        BlockCompatibility.SoundType type = data.of("Type").assertExists().getEnum(BlockCompatibility.SoundType.class);
        float defaultRandomness = (float) data.of("Default_Randomness").assertExists().assertRange(0.0, 1.0).getDouble();

        // Construct a list of overrides to use in case the default block
        // sounds are not good enough. Also supports playing custom sounds
        // instead of bukkit sounds. This could also be used to replace
        // every block sound, so let's take performance into consideration.
        Map<Material, Mechanic> overrides = new EnumMap<>(Material.class);
        List<?> list = data.config.getList(data.key + ".Overrides");
        for (int i = 0; i < list.size(); i++) {
            String str = list.get(i).toString();
            int split = str.indexOf(" ");

            if (split == -1)
                throw data.listException("Overrides", i, "Override format should be: <Material> <SoundMechanic>", SerializerException.forValue(str));

            // Extract and parse the data from the string
            String materialStr = str.substring(0, split);
            List<Material> materials = EnumUtil.parseEnums(Material.class, materialStr);
            String mechanicStr = str.substring(split + 1);
            Mechanic mechanic = new Mechanics().serializeOne(data, mechanicStr);

            // Fill the overrides map
            for (Material mat : materials)
                overrides.put(mat, mechanic);
        }

        // Construct a list of materials that shouldn't have any effects.
        // We use a map since EnumMap is very fast, and there is no EnumSet
        // equivalent.
        Map<Material, Object> materialBlacklist = new EnumMap<>(Material.class);
        List<String[]> temp = data.ofList("Material_Blacklist")
                .addArgument(Material.class, true)
                .assertExists().assertList().get();

        for (String[] split : temp) {
            List<Material> materials = EnumUtil.parseEnums(Material.class, split[0]);

            for (Material mat : materials)
                materialBlacklist.put(mat, null);
        }

        // Construct a list of weapons that should not use the block
        // effects. We check each weapon to make sure it exists in
        // WeaponMechanics.
        Set<String> weaponBlacklist = new HashSet<>();
        temp = data.ofList("Weapon_Blacklist")
                .addArgument(String.class, true, true)
                .assertExists().assertList().get();

        List<String> weaponOptions = WeaponMechanics.getWeaponHandler().getInfoHandler().getSortedWeaponList();
        for (int i = 0; i < temp.size(); i++) {
            String[] split = temp.get(i);

            // TODO cannot check weapons yet since they haven't been serialized!
            //if (!weaponOptions.contains(split[0]))
            //    throw new SerializerOptionsException(this, "Weapon", weaponOptions, split[0], data.ofList("Weapon_Blacklist").getLocation(i));

            weaponBlacklist.add(split[0]);
        }

        return new BlockSoundSerializer(type, defaultRandomness, overrides, materialBlacklist, weaponBlacklist);
    }

    @Override
    public String getKeyword() {
        return "Block_Sounds";
    }
}