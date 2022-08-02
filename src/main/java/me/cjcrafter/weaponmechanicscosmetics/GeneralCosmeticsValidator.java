package me.cjcrafter.weaponmechanicscosmetics;

import me.cjcrafter.weaponmechanicscosmetics.general.PerPlayerSoundMechanic;
import me.deecaad.core.file.*;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.mechanics.defaultmechanics.SoundMechanic;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class GeneralCosmeticsValidator implements IValidator {

    public static final Serializer<?> DUMMY = new Serializer<Object>() {
        @Override
        public String getKeyword() {
            return "Cosmetics";
        }

        @NotNull
        @Override
        public Object serialize(SerializeData serializeData) {
            throw new RuntimeException("Unreachable code");
        }
    };


    @Override
    public boolean denyKeys() {
        return true;
    }

    @Override
    public String getKeyword() {
        return "Cosmetics";
    }

    @Override
    public void validate(Configuration configuration, File file, ConfigurationSection config, String key) throws SerializerException {
        SerializeData data = new SerializeData(DUMMY, file, key, config);

        // Just check datatype
        configuration.set(key + ".Muzzle_Flash", data.of("Muzzle_Flash").getBool(false));
        configuration.set(key + ".Explosion_Effects", data.of("Explosion_Effects").getBool(false));

        Mechanics splashMechanics = data.of("Splash_Mechanics").serialize(new Mechanics());
        configuration.set(key + ".Splash_Mechanics", splashMechanics);

        // Value should be less than 8 for good results, but we'll *allow*
        // larger values for experimentation.
        configuration.set(key + ".Bullet_Zip.Maximum_Distance", data.of("Bullet_Zip.Maximum_Distance").assertRange(0.0, 16.0).getDouble(0.0));
        SoundMechanic zipMechanics = data.of("Bullet_Zip.Sounds").serializeNonStandardSerializer(new PerPlayerSoundMechanic());
        configuration.set(key + ".Bullet_Zip.Sounds", zipMechanics);
    }
}
