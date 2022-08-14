/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics;

import me.cjcrafter.weaponmechanicscosmetics.general.PerPlayerSoundMechanic;
import me.deecaad.core.file.*;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import me.deecaad.weaponmechanics.mechanics.defaultmechanics.SoundMechanic;
import me.deecaad.weaponmechanics.weapon.explode.BlockDamage;

public class GeneralCosmeticsValidator implements IValidator {

    /**
     * Default constructor for validator
     */
    public GeneralCosmeticsValidator() {
    }

    @Override
    public boolean denyKeys() {
        return true;
    }

    @Override
    public String getKeyword() {
        return "Cosmetics";
    }

    @Override
    public void validate(Configuration configuration, SerializeData data) throws SerializerException {
        String key = data.key;

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

        // Block damage stuff
        configuration.set(key + ".Block_Damage", data.of("Block_Damage").serialize(BlockDamage.class));
        configuration.set(key + ".Block_Damage.Ticks_Before_Regenerate", data.of("Block_Damage.Ticks_Before_Regenerate").assertRange(-1, 20 * 60 * 60).getInt(-1));
    }
}
