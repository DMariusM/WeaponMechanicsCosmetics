/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.commands;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.commands.*;
import me.deecaad.core.commands.arguments.StringArgumentType;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.weapon.skin.Skin;
import me.deecaad.weaponmechanics.weapon.skin.SkinList;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.StatsData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;

import java.util.Set;
import java.util.function.Function;

public class SkinCommand {

    private static Function<CommandData, Tooltip[]> SKIN_SUGGESTIONS = (data) -> {
        PlayerInventory inv = ((Player) data.sender).getInventory();
        ItemStack weapon = empty(inv.getItemInMainHand()) ? inv.getItemInOffHand() : inv.getItemInMainHand();
        String title = weapon == null ? null : WeaponMechanicsAPI.getWeaponTitle(weapon);
        SkinList skins = WeaponMechanics.getConfigurations().getObject(title + ".Skin", SkinList.class);

        if (skins == null)
            return new Tooltip[]{ Tooltip.of("N/A", title + " cannot have a skin") };

        // When giving player options, they shouldn't reselect a skin they are
        // already using.
        Set<String> options = skins.getSkins();
        StatsData stats = WeaponMechanics.getPlayerWrapper((Player) data.sender).getStatsData();
        if (stats != null) {
            String skin = (String) stats.get(title, WeaponStat.SKIN, null);
            options.remove(skin);
        }

        if (options.isEmpty())
            return new Tooltip[]{ Tooltip.of("N/A", title + " only has default skin") };

        return SuggestionsBuilder.from(skins.getSkins()).apply(data);
    };

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
                Bukkit.getPluginManager().addPermission(permission);

                WeaponMechanicsCosmetics.getInstance().getDebug().debug("Registered: " + permission);
            }
            Bukkit.getPluginManager().addPermission(weapon);
        }
        Bukkit.getPluginManager().addPermission(global);
    }

    public static void register() {
        CommandBuilder builder = new CommandBuilder("skin")
                .withAliases("skins", "weaponskin")
                .withPermission("weaponmechanicscosmetics.commands.skin")
                .withDescription("Change the skin for your weapon")
                .withArgument(new Argument<>("skin", new StringArgumentType()).replace(SKIN_SUGGESTIONS))
                .executes(CommandExecutor.player((player, args) -> {
                    PlayerInventory inv = player.getInventory();
                    ItemStack weapon = empty(inv.getItemInMainHand()) ? inv.getItemInOffHand() : inv.getItemInMainHand();
                    boolean mainHand = !empty(inv.getItemInOffHand());

                    if (empty(weapon) || WeaponMechanicsAPI.getWeaponTitle(weapon) == null) {
                        player.sendMessage(ChatColor.RED + "You must hold a weapon");
                        return;
                    }

                    String title = WeaponMechanicsAPI.getWeaponTitle(weapon);
                    PlayerWrapper wrapper = WeaponMechanics.getPlayerWrapper(player);
                    StatsData stats = wrapper.getStatsData();

                    if (stats == null) {
                        player.sendMessage(ChatColor.RED + "You do not have any data to store");
                        return;
                    }

                    SkinList list = WeaponMechanics.getConfigurations().getObject(title + ".Skin", SkinList.class);
                    if (list == null) {
                        player.sendMessage(ChatColor.RED + title + " does not have any skin.");
                        return;
                    }

                    String skin = (String) args[0];
                    if (list.getSkin(skin, null) == null) {
                        player.sendMessage(ChatColor.RED + title + " unknown skin: " + skin);
                        return;
                    }

                    // Apply the skin
                    wrapper.getStatsData().set(title, WeaponStat.SKIN, skin);
                    player.sendMessage(ChatColor.GREEN + "Now using " + skin + " for the " + title);
                    WeaponMechanics.getWeaponHandler().getSkinHandler().tryUse(wrapper, title, weapon, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
                }));

        builder.register();
    }

    private static boolean empty(ItemStack item) {
        return item == null || item.getAmount() == 0 || item.getType().name().endsWith("AIR");
    }
}
