/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.listeners;

import com.cjcrafter.foliascheduler.ServerImplementation;
import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponShootEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class MuzzleFlashSpawner implements Listener {

    private final ItemStack light;

    public MuzzleFlashSpawner() {
        light = new ItemStack(Material.TORCH);
    }

    @EventHandler
    public void onShoot(WeaponShootEvent event) {
        Configuration config = WeaponMechanics.getConfigurations();
        if (!config.getBool(event.getWeaponTitle() + ".Cosmetics.Muzzle_Flash"))
            return;

        FakeEntity entity = CompatibilityAPI.getEntityCompatibility().generateFakeEntity(event.getShooter().getLocation(), light);
        entity.setInvisible(true); // this doesn't actually work for items, even with optifine
        entity.show();

        ServerImplementation scheduler = WeaponMechanicsCosmetics.getInstance().getScheduler();
        scheduler.async().runNow(() -> entity.remove());
    }
}
