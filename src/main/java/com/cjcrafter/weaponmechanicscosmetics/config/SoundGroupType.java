/*
 * Copyright (c) 2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.config;

import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * The different types of sounds stored by a {@link SoundGroup}.
 */
public enum SoundGroupType {

    BREAK(SoundGroup::getBreakSound),
    STEP(SoundGroup::getStepSound),
    PLACE(SoundGroup::getPlaceSound),
    HIT(SoundGroup::getHitSound),
    FALL(SoundGroup::getFallSound);

    private final Function<SoundGroup, Sound> getSound;

    SoundGroupType(@NotNull Function<SoundGroup, Sound> getSound) {
        this.getSound = getSound;
    }

    /**
     * Gets the sound from the given {@link SoundGroup}.
     *
     * @param soundGroup The sound group to get the sound from
     * @return The sound from the sound group
     */
    public @NotNull Sound getSound(@NotNull SoundGroup soundGroup) {
        return getSound.apply(soundGroup);
    }
}
