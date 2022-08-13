package me.cjcrafter.weaponmechanicscosmetics.general;

import me.cjcrafter.weaponmechanicscosmetics.scripts.ZipScript;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerEnumException;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.defaultmechanics.SoundMechanic;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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

    @NotNull
    @Override
    public PerPlayerSoundMechanic serialize(SerializeData data) throws SerializerException {
        List<String[]> stringSoundList = data.ofList().addArgument(Sound.class, true, true).addArgument(Double.TYPE, true).assertArgumentPositive().addArgument(Double.TYPE, false).assertArgumentRange(0.5, 2.0).addArgument(Integer.TYPE, false).assertArgumentPositive().addArgument(Double.TYPE, false).assertArgumentRange(0.0, 1.0).assertList().assertExists().get();
        List<SoundMechanicData> soundList = new ArrayList<>();
        int delayedCounter = 0;

        for(int i = 0; i < stringSoundList.size(); ++i) {
            String[] split = stringSoundList.get(i);
            float volume = Float.parseFloat(split[1]);
            float pitch = split.length > 2 ? Float.parseFloat(split[2]) : 1.0F;
            int delay = split.length > 3 ? Integer.parseInt(split[3]) : 0;
            float noise = split.length > 4 ? Float.parseFloat(split[4]) : 0.0F;
            if (delay > 0) {
                ++delayedCounter;
            }

            String stringSound = split[0].trim();
            if (stringSound.toLowerCase().startsWith("custom:")) {
                stringSound = stringSound.substring("custom:".length());
                soundList.add(new PlayerCustomSound(stringSound, volume, pitch, delay, noise));
            } else {
                try {
                    Sound sound = Sound.valueOf(stringSound);
                    soundList.add(new PlayerBukkitSound(sound, volume, pitch, delay, noise));
                } catch (IllegalArgumentException var13) {
                    throw new SerializerEnumException(this, Sound.class, stringSound, false, data.ofList().getLocation(i));
                }
            }
        }

        return new PerPlayerSoundMechanic(delayedCounter, soundList);
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

        public PlayerCustomSound(String sound, float volume, float pitch, int delay, float noise) {
            super(sound, volume, pitch, delay, noise);
            this.sound = sound;
        }

        @Override
        public void play(CastData castData) {
            Player player = (Player) castData.getCaster();
            Location castLocation = castData.getCastLocation();
            if (CompatibilityAPI.getVersion() >= 1.11) {
                player.playSound(castLocation, this.sound, SoundCategory.PLAYERS, this.getVolume(), this.getRandomPitch());
            } else {
                player.playSound(castLocation, this.sound, this.getVolume(), this.getRandomPitch());
            }
        }
    }

    /**
     * Plays a bukkit sound for the player associated with {@link CastData}.
     */
    public static class PlayerBukkitSound extends BukkitSound {

        private final Sound sound;

        public PlayerBukkitSound(Sound sound, float volume, float pitch, int delay, float noise) {
            super(sound, volume, pitch, delay, noise);
            this.sound = sound;
        }

        @Override
        public void play(CastData castData) {
            Player player = (Player) castData.getCaster();
            Location castLocation = castData.getCastLocation();
            if (CompatibilityAPI.getVersion() >= 1.11) {
                player.playSound(castLocation, this.sound, SoundCategory.PLAYERS, this.getVolume(), this.getRandomPitch());
            } else {
                player.playSound(castLocation, this.sound, this.getVolume(), this.getRandomPitch());
            }
        }
    }
}
