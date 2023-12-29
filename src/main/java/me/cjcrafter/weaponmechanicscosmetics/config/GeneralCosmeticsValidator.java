/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.config;

import me.deecaad.core.file.*;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.placeholder.PlaceholderMessage;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.weapon.explode.BlockDamage;

import java.util.ArrayList;
import java.util.List;

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
        if (splashMechanics != null)
            configuration.set(key + ".Splash_Mechanics", splashMechanics);

        Mechanics blockHitMechanics = data.of("Block_Hit_Mechanics").serialize(new Mechanics());
        if (blockHitMechanics != null)
            configuration.set(key + ".Block_Hit_Mechanics", blockHitMechanics);

        // Value should be less than 8 for good results, but we'll *allow*
        // larger values for experimentation.
        configuration.set(key + ".Bullet_Zip.Maximum_Distance", data.of("Bullet_Zip.Maximum_Distance").assertRange(0.0, 16.0).getDouble(0.0));
        Mechanics zipMechanics = data.of("Bullet_Zip.Sounds").serialize(new Mechanics());
        configuration.set(key + ".Bullet_Zip.Sounds", zipMechanics);

        // Block damage stuff
        configuration.set(key + ".Block_Damage", data.of("Block_Damage").serialize(BlockDamage.class));
        configuration.set(key + ".Block_Damage.Ticks_Before_Regenerate", data.of("Block_Damage.Ticks_Before_Regenerate").assertRange(-1, 20 * 60 * 60).getInt(-1));

        // Hit Marker
        String adventure = data.of("Hit_Marker").getAdventure(null);
        if (adventure != null) {
            PlaceholderMessage msg = new PlaceholderMessage(adventure);
            configuration.set(key + ".Hit_Marker", msg);
        }

        // Death Message Overrides
        List<String> deathMessages = data.of("Death_Messages").assertType(List.class).get(List.of());
        List<PlaceholderMessage> placeholderMessages = new ArrayList<>(deathMessages.size());
        for (int i = 0; i < deathMessages.size(); i++) {
            String deathMessage = deathMessages.get(i);

            deathMessage = StringUtil.colorAdventure(deathMessage);
            placeholderMessages.add(new PlaceholderMessage(deathMessage));
        }

        configuration.set(key + ".Death_Messages", deathMessages);
    }
}
