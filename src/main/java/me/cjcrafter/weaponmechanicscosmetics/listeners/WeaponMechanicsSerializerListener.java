/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.cjcrafter.weaponmechanicscosmetics.config.CrossbowConfigSerializer;
import me.cjcrafter.weaponmechanicscosmetics.config.GeneralCosmeticsValidator;
import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.cjcrafter.weaponmechanicscosmetics.config.HandValidator;
import me.cjcrafter.weaponmechanicscosmetics.mechanics.FakeItemMechanic;
import me.cjcrafter.weaponmechanicscosmetics.timer.TimerValidator;
import me.cjcrafter.weaponmechanicscosmetics.mechanics.ParticleMechanic;
import me.cjcrafter.weaponmechanicscosmetics.trails.Trail;
import me.deecaad.core.events.QueueSerializerEvent;
import me.deecaad.core.mechanics.Mechanics;
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
        event.addSerializers(new CrossbowConfigSerializer());

        // Whenever WeaponMechanics reloads, we should also reload
        // WeaponMechanicsCosmetics. TODO Move this to a proper location.
        WeaponMechanicsCosmetics.getInstance().reloadConfig();
    }
}
