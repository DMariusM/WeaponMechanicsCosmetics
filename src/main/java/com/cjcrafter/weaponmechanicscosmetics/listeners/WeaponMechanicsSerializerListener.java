/*
 * Copyright (c) 2022-2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.listeners;

import com.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import com.cjcrafter.weaponmechanicscosmetics.config.ThirdPersonPose;
import com.cjcrafter.weaponmechanicscosmetics.config.GeneralCosmeticsValidator;
import com.cjcrafter.weaponmechanicscosmetics.config.HandValidator;
import com.cjcrafter.weaponmechanicscosmetics.timer.TimerValidator;
import com.cjcrafter.weaponmechanicscosmetics.trails.Trail;
import me.deecaad.core.events.QueueSerializerEvent;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WeaponMechanicsSerializerListener implements Listener {

    @EventHandler
    public void queueSerializers(QueueSerializerEvent event) {
        if (!event.getSourceName().equals("WeaponMechanics"))
            return;

        event.addValidators(new TimerValidator());
        event.addValidators(new GeneralCosmeticsValidator());
        event.addValidators(new HandValidator());
        event.addSerializers(new Trail());
        event.addSerializers(new ThirdPersonPose());

        // Whenever WeaponMechanics reloads, we should also reload
        // WeaponMechanicsCosmetics. TODO Move this to a proper location.
        WeaponMechanics.getInstance().getFoliaScheduler().global().runDelayed(() -> {
            WeaponMechanicsCosmetics.getInstance().reload();
        }, 1L);
    }
}
