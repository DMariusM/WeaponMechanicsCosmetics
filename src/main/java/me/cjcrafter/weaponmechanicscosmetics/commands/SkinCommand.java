/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.commands;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.commands.CommandBuilder;
import me.deecaad.core.commands.CommandExecutor;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.weapon.skin.SkinList;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;

public class SkinCommand {

    public static void registerPermissions() {
        Configuration config = WeaponMechanics.getConfigurations();

        Permission global = new Permission("weaponmechanics.skin.*");
        global.setDescription("Ability to use all skins for any weapon");

        for (String weaponTitle : WeaponMechanics.getWeaponHandler().getInfoHandler().getSortedWeaponList()) {
            SkinList skins = config.getObject(weaponTitle + ".Skin", SkinList.class);
            if (skins == null)
                continue;

            Permission weapon = new Permission("weaponmechanics.skin." + weaponTitle + ".*");
            weapon.setDescription("Ability to use all skins for " + weaponTitle);

            for (String skin : skins.getSkins()) {

                // Default skin can be used by everyone, since it is default.
                if ("Default".equals(skin))
                    continue;

                Permission permission = new Permission("weaponmechanics.skin." + weaponTitle + "." + skin);
                permission.setDescription("Ability to use the " + skin + " skin for " + weaponTitle);
                permission.addParent(global, true);
                permission.addParent(weapon, true);

                WeaponMechanicsCosmetics.getInstance().getDebug().debug("Registered: " + permission);
            }
        }
    }

    public static void register() {
        CommandBuilder builder = new CommandBuilder("skin")
                .withAliases("skins", "weaponskin")
                .withPermission("weaponmechanicscosmetics.commands.skin")
                .withDescription("Change the skin for your weapon")
                .executes(CommandExecutor.player((player, args) -> {
                    PlayerInventory inv = player.getInventory();
                    ItemStack weapon = empty(inv.getItemInMainHand()) ? inv.getItemInOffHand() : inv.getItemInMainHand();

                    if (empty(weapon) || WeaponMechanicsAPI.getWeaponTitle(weapon) == null) {
                        player.sendMessage(ChatColor.RED + "You must hold a weapon");
                        return;
                    }

                    String title = WeaponMechanicsAPI.getWeaponTitle(weapon);

                }));
    }

    private static boolean empty(ItemStack item) {
        return true;
    }
}
