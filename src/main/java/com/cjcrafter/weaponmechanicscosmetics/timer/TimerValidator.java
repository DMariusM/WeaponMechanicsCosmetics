/*
 * Copyright (c) 2022-2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.timer;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;

import java.util.Arrays;
import java.util.List;

public class TimerValidator implements IValidator {

    @Override
    public boolean denyKeys() {
        return true;
    }

    @Override
    public String getKeyword() {
        return "Show_Time";
    }

    @Override
    public void validate(Configuration configuration, SerializeData data) throws SerializerException {

        List<String> keys = Arrays.asList("Delay_Between_Shots", "Shoot_Delay_After_Scope", "Weapon_Equip_Delay", "Reload",
                "Firearm_Actions", "Melee_Hit_Delay", "Melee_Miss_Delay");

        for (String key : keys) {
            Timer timer = data.of(key).serialize(Timer.class).orElse(null);

            if (timer != null)
                configuration.set(data.getKey() + "." + key, timer);
        }
    }
}
