/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.commands;

import com.google.common.collect.Maps;
import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.*;
import me.deecaad.core.commands.arguments.StringArgumentType;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.lib.adventure.text.Component;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.weapon.skin.Skin;
import me.deecaad.weaponmechanics.weapon.skin.SkinList;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.StatsData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;

import java.util.*;
import java.util.function.Function;

public class SkinCommand {

    private static Function<CommandData, Tooltip[]> SKIN_SUGGESTIONS(boolean hand) {
        return (data) -> {
            PlayerInventory inv = ((Player) data.sender).getInventory();
            ItemStack weapon = empty(inv.getItemInMainHand()) ? inv.getItemInOffHand() : inv.getItemInMainHand();
            String title = weapon == null ? null : WeaponMechanicsAPI.getWeaponTitle(weapon);
            SkinList skins = WeaponMechanics.getConfigurations().getObject(title + (hand ? ".Hand" : ".Skin"), SkinList.class);

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
    }

    public static void registerPermissions(String key) {
        String keyLower = key.toUpperCase(Locale.ROOT);
        Configuration config = WeaponMechanics.getConfigurations();

        Permission global = new Permission("weaponmechanics." + key + ".*");
        global.setDescription("Ability to use all " + key + "s for any weapon");

        for (String weaponTitle : WeaponMechanics.getWeaponHandler().getInfoHandler().getSortedWeaponList()) {
            SkinList skins = config.getObject(weaponTitle + "." + keyLower, SkinList.class);
            if (skins == null)
                continue;

            Permission weapon = new Permission("weaponmechanics." + keyLower + "." + weaponTitle + ".*");
            weapon.setDescription("Ability to use all " + key + "s for " + weaponTitle);

            for (String skin : skins.getSkins()) {

                // Default skin can be used by everyone, since it is default.
                if ("default".equals(skin))
                    continue;

                Permission permission = new Permission("weaponmechanics." + keyLower + "." + weaponTitle + "." + skin);
                permission.setDescription("Ability to use the " + skin + " " + key + " for " + weaponTitle);
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
        new CommandBuilder("skin")
                .withAliases("skins", "weaponskin")
                .withPermission("weaponmechanicscosmetics.commands.skin")
                .withDescription("Change the skin for your weapon")
                .withArgument(new Argument<>("skin", new StringArgumentType()).replace(SKIN_SUGGESTIONS(false)))
                .executes(CommandExecutor.player((player, args) -> {
                    applySkin(player, "Skin", (String) args[0]);
                })).register();

        new CommandBuilder("handskin")
                .withPermission("weaponmechanicscosmetics.commands.handskin")
                .withDescription("Change the skin for your hand")
                .withArgument(new Argument<>("skin", new StringArgumentType()).replace(SKIN_SUGGESTIONS(true)))
                .executes(CommandExecutor.player((player, args) -> {
                    applySkin(player, "Hand", (String) args[0]);
                })).register();
    }

    public static void applySkin(Player player, String key, String skin) {
        String keylower = key.toLowerCase(Locale.ROOT);
        PlayerInventory inv = player.getInventory();
        boolean mainHand = !empty(inv.getItemInMainHand());
        ItemStack weapon = mainHand ? inv.getItemInMainHand() : inv.getItemInOffHand();

        if (empty(weapon) || WeaponMechanicsAPI.getWeaponTitle(weapon) == null) {
            sendMessage(player, "skin-hold-weapon", weapon, null, mainHand, Collections.singletonMap("%hand%", key.equals("Hand") ? "hand" : ""));
            return;
        }

        String title = WeaponMechanicsAPI.getWeaponTitle(weapon);
        PlayerWrapper wrapper = WeaponMechanics.getPlayerWrapper(player);
        StatsData stats = wrapper.getStatsData();

        Map<String, String> variables = new HashMap<>();
        variables.put("%hand%", key.equals("Hand") ? "hand" : "");
        variables.put("%weapon%", title);
        variables.put("%skin%", skin);

        if (stats == null) {
            sendMessage(player, "skin-player-data", weapon, title, mainHand, variables);
            return;
        }

        SkinList list = WeaponMechanics.getConfigurations().getObject(title + "." + key, SkinList.class);
        if (list == null) {
            sendMessage(player, "skin-list", weapon, title, mainHand, variables);
            return;
        }

        if (list.getSkin(skin, null) == null) {
            sendMessage(player, "skin-option", weapon, title, mainHand, variables);
            return;
        }

        if (!skin.equals("default") && !player.hasPermission("weaponmechanics." + keylower + "." + title + "." + skin)) {
            sendMessage(player, "skin-permission", weapon, title, mainHand, variables);
            return;
        }

        // Apply the skin
        wrapper.getStatsData().set(title, key.equals("Hand") ? WeaponStat.HAND_SKIN : WeaponStat.SKIN, skin);
        sendMessage(player, "skin-success", weapon, title, mainHand, variables);
        WeaponMechanics.getWeaponHandler().getSkinHandler().tryUse(wrapper, title, weapon, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
    }

    public static void sendMessage(Player player, String key, ItemStack weapon, String title, boolean mainHand, Map<String, String> variables) {
        String input = WeaponMechanicsCosmetics.getInstance().getLang(key);
        input = PlaceholderAPI.applyPlaceholders(input, player, weapon, title, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND, variables);
        Component adventure = MechanicsCore.getPlugin().message.deserialize(input);
        MechanicsCore.getPlugin().adventure.player(player).sendMessage(adventure);
    }

    private static boolean empty(ItemStack item) {
        return item == null || item.getAmount() == 0 || item.getType().name().endsWith("AIR");
    }
}
