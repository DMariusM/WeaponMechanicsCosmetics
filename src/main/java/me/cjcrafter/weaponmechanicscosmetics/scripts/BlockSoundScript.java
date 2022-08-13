package me.cjcrafter.weaponmechanicscosmetics.scripts;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.block.BlockCompatibility;
import me.deecaad.core.file.*;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.RayTraceResult;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Adds hit-block sounds for projectiles.
 */
public class BlockSoundScript extends ProjectileScript<WeaponProjectile> {

    private BlockSound sound;

    public BlockSoundScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        super(owner, projectile);

        Configuration config = WeaponMechanicsCosmetics.getInstance().getConfiguration();
        Debugger debug = WeaponMechanicsCosmetics.getInstance().getDebug();
        sound = config.getObject("Block_Sounds", BlockSoundScript.BlockSound.class);
        if (sound == null) {
            debug.error("Did you delete in the 'Block_Sounds' section in config.yml?",
                    "You probably want to check the main config wiki",
                    "You can regenerate your config by deleting the config.yml file");
            return;
        }
    }

    public BlockSoundScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile, BlockSound sound) {
        super(owner, projectile);

        this.sound = sound;
    }

    @Override
    public void onCollide(@NotNull RayTraceResult hit) {
        if (!hit.isBlock())
            return;

        sound.play(projectile, hit.getBlock());
    }


    public static class BlockSound implements Serializer<BlockSound> {

        private BlockCompatibility.SoundType type;
        private float randomness;
        private Map<Material, SoundConfig> overrides;
        private Map<Material, Object> materialBlacklist;
        private Set<String> weaponBlacklist;

        /**
         * Default constructor for serializer
         */
        public BlockSound() {
        }

        public BlockSound(BlockCompatibility.SoundType type, float randomness,
                          Map<Material, SoundConfig> overrides, Map<Material, Object> materialBlacklist, Set<String> weaponBlacklist) {
            this.type = type;
            this.randomness = randomness;
            this.overrides = overrides;
            this.materialBlacklist = materialBlacklist;
            this.weaponBlacklist = weaponBlacklist;
        }

        public void play(WeaponProjectile projectile, Block block) {
            World world = projectile.getWorld();
            Location loc = projectile.getLocation().toLocation(world);

            // Handle blacklists
            if (materialBlacklist.containsKey(block.getType()) || weaponBlacklist.contains(projectile.getWeaponTitle()))
                return;

            // Handle sound overrides
            SoundConfig override = overrides.get(block.getType());
            if (override != null) {
                if (override.custom != null)
                    play(loc, override.custom, override.volume, override.pitch, override.randomness);
                else
                    play(loc, override.sound, override.volume, override.pitch, override.randomness);

                return;
            }

            // Play default block sound
            Object data = ReflectionUtil.getMCVersion() < 13 ? new MaterialData(block.getType(), block.getData()) : block.getBlockData();
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
        public BlockSound serialize(SerializeData data) throws SerializerException {
            BlockCompatibility.SoundType type = data.of("Type").assertExists().getEnum(BlockCompatibility.SoundType.class);
            float randomness = (float) data.of("Default_Randomness").assertExists().assertRange(0.0, 1.0).getDouble();

            // Construct a list of overrides to use in case the default block
            // sounds are not good enough. Also supports playing custom sounds
            // instead of bukkit sounds. This could also be used to replace
            // every block sound, so let's take performance into consideration.
            Map<Material, SoundConfig> overrides = new EnumMap<>(Material.class);
            List<String[]> temp = data.ofList("Overrides")
                    .addArgument(Material.class, true)
                    .addArgument(Sound.class, true, true)
                    .addArgument(double.class, true)
                    .addArgument(double.class, true)
                    .addArgument(double.class, false)
                    .assertExists().assertList().get();

            for (int i = 0; i < temp.size(); i++) {
                String[] split = temp.get(i);

                List<Material> materials = EnumUtil.parseEnums(Material.class, split[0]);
                SoundConfig sound = new SoundConfig();

                String stringSound = split[1].trim();
                if (stringSound.toLowerCase().startsWith("custom:")) {
                    sound.custom = stringSound.substring("custom:".length());
                } else {
                    try {
                        sound.sound = Sound.valueOf(stringSound.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException var13) {
                        throw new SerializerEnumException(this, Sound.class, stringSound, false, data.ofList("Overrides").getLocation(i));
                    }
                }

                sound.volume = Float.parseFloat(split[2]);
                sound.pitch = Float.parseFloat(split[3]);
                sound.randomness = split.length >= 5 ? Float.parseFloat(split[4]) : 0f;

                for (Material mat : materials)
                    overrides.put(mat, sound);
            }

            // Construct a list of materials that shouldn't have any effects.
            // We use a map since EnumMap is very fast, and there is no EnumSet
            // equivalent.
            Map<Material, Object> materialBlacklist = new EnumMap<>(Material.class);
            temp = data.ofList("Material_Blacklist")
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

            return new BlockSound(type, randomness, overrides, materialBlacklist, weaponBlacklist);
        }

        @Override
        public String getKeyword() {
            return "Block_Sounds";
        }
    }

    private static class SoundConfig {
        private String custom;
        private Sound sound;
        private float volume;
        private float pitch;
        private float randomness;
    }
}