/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.general;

import me.cjcrafter.weaponmechanicscosmetics.scripts.ZipScript;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerEnumException;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.defaultmechanics.SoundMechanic;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This mechanic is important for the {@link ZipScript}.
 * Players need to hear the sound from a specific direction and distance, so
 * this is used to ensure that only 1 player hears the sound.
 */
public class PerPlayerSoundMechanic extends SoundMechanic {

    public PerPlayerSoundMechanic() {
    }

    public PerPlayerSoundMechanic(int delayCounter, List<SoundMechanicData> soundList) {
        super(delayCounter, soundList);
    }

    @Override
    protected SoundMechanicData serialize(SerializeData data, Sound sound, float volume, float pitch, int delay, float noise,
                                          double minDistance, double maxDistance, MaterialCategory mat, String category) throws SerializerException {
        return new PlayerBukkitSound(sound, volume, pitch, delay, noise, minDistance, maxDistance, mat, category);
    }

    @Override
    protected SoundMechanicData serialize(SerializeData data, String sound, float volume, float pitch, int delay, float noise,
                                          double minDistance, double maxDistance, MaterialCategory mat, String category) throws SerializerException {
        return new PlayerCustomSound(sound, volume, pitch, delay, noise, minDistance, maxDistance, mat, category);
    }

    /**
     * Since the sounds are per-player, we NEED a player.
     *
     * @return true.
     */
    @Override
    public boolean requirePlayer() {
        return true;
    }

    /**
     * Plays a custom sound for the player associated with {@link CastData}.
     */
    public static class PlayerCustomSound extends CustomSound {

        private final String sound;

        public PlayerCustomSound(String sound, float volume, float pitch, int delay, float noise, double minDistance,
                                 double maxDistance, MaterialCategory mat, String category) {
            super(sound, volume, pitch, delay, noise, minDistance, maxDistance, mat, category);
            this.sound = sound;
        }

        @Override
        public void play(CastData castData) {
            if (!(castData.getCaster() instanceof Player))
                return;

            Location location = castData.getCastLocation();

            Player player = (Player) castData.getCaster();
            if (!getHeadMaterial().test(player))
                return;

            if (ReflectionUtil.getMCVersion() >= 11) {
                SoundCategory category = EnumUtil.getIfPresent(SoundCategory.class, getSoundCategory()).orElse(SoundCategory.PLAYERS);
                player.playSound(location, sound, category, getVolume(), getRandomPitch());
            } else {
                player.playSound(location, sound, getVolume(), getRandomPitch());
            }
        }
    }

    /**
     * Plays a bukkit sound for the player associated with {@link CastData}.
     */
    public static class PlayerBukkitSound extends BukkitSound {

        private final Sound sound;

        public PlayerBukkitSound(Sound sound, float volume, float pitch, int delay, float noise, double minDistance,
                                 double maxDistance, MaterialCategory mat, String category) {
            super(sound, volume, pitch, delay, noise, minDistance, maxDistance, mat, category);
            this.sound = sound;
        }

        @Override
        public void play(CastData castData) {
            if (!(castData.getCaster() instanceof Player))
                return;

            Location location = castData.getCastLocation();

            Player player = (Player) castData.getCaster();
            if (!getHeadMaterial().test(player))
                return;

            if (ReflectionUtil.getMCVersion() >= 11) {
                SoundCategory category = EnumUtil.getIfPresent(SoundCategory.class, getSoundCategory()).orElse(SoundCategory.PLAYERS);
                player.playSound(location, sound, category, getVolume(), getRandomPitch());
            } else {
                player.playSound(location, sound, getVolume(), getRandomPitch());
            }
        }
    }
}
