/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package com.cjcrafter.weaponmechanicscosmetics.config;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.MechanicManager;
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
        String key = data.getKey();

        // Just check datatype
        configuration.set(key + ".Muzzle_Flash", data.of("Muzzle_Flash").getBool().orElse(false));
        configuration.set(key + ".Explosion_Effects", data.of("Explosion_Effects").getBool().orElse(false));

        data.of("Splash_Mechanics").serialize(new MechanicManager())
                .ifPresent(splashMechanics -> configuration.set(key + ".Splash_Mechanics", splashMechanics));

        data.of("Block_Hit_Mechanics").serialize(new MechanicManager())
                .ifPresent(blockHitMechanics -> configuration.set(key + ".Block_Hit_Mechanics", blockHitMechanics));

        // Value should be less than 8 for good results, but we'll *allow*
        // larger values for experimentation.
        configuration.set(key + ".Bullet_Zip.Maximum_Distance", data.of("Bullet_Zip.Maximum_Distance").assertRange(0.0, 16.0).getDouble().orElse(0.0));
        MechanicManager zipMechanics = data.of("Bullet_Zip.Sounds").serialize(new MechanicManager()).orElse(null);
        configuration.set(key + ".Bullet_Zip.Sounds", zipMechanics);

        // Block damage stuff
        configuration.set(key + ".Block_Damage", data.of("Block_Damage").serialize(BlockDamage.class).orElse(null));
        configuration.set(key + ".Block_Damage.Ticks_Before_Regenerate", data.of("Block_Damage.Ticks_Before_Regenerate").assertRange(-1, 20 * 60 * 60).getInt().orElse(-1));

        // Hit Marker
        String adventure = data.of("Hit_Marker").getAdventure().orElse(null);
        if (adventure != null) {
            PlaceholderMessage msg = new PlaceholderMessage(adventure);
            configuration.set(key + ".Hit_Marker", msg);
        }

        // Death Message Overrides
        List<String> deathMessages = data.of("Death_Messages").get(List.class).orElse(List.of());
        List<PlaceholderMessage> placeholderMessages = new ArrayList<>(deathMessages.size());
        for (int i = 0; i < deathMessages.size(); i++) {
            String deathMessage = deathMessages.get(i);

            deathMessage = StringUtil.colorAdventure(deathMessage);
            placeholderMessages.add(new PlaceholderMessage(deathMessage));
        }

        configuration.set(key + ".Death_Messages", placeholderMessages);

        if (data.has("Crossbow")) {
            throw data.exception("Crossbow", "The crossbow feature is no longer supported. crossbow.json and whatnot are all old",
                    "Instead, use the new Third_Person_Pose feature to configure poses for your weapons");
        }
    }
}
