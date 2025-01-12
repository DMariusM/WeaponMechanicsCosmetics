/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.IStringTooltip;
import dev.jorel.commandapi.StringTooltip;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.commands.*;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.weapon.skin.SkinSelector;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.StatsData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class SkinCommand {

    private static Function<SuggestionInfo<CommandSender>, IStringTooltip[]> SKIN_SUGGESTIONS(boolean hand) {
        return (data) -> {
            PlayerInventory inv = ((Player) data.sender()).getInventory();
            ItemStack weapon = empty(inv.getItemInMainHand()) ? inv.getItemInOffHand() : inv.getItemInMainHand();
            String title = weapon == null ? null : WeaponMechanicsAPI.getWeaponTitle(weapon);
            SkinSelector skins = WeaponMechanics.getConfigurations().getObject(title + (hand ? ".Hand" : ".Skin"), SkinSelector.class);

            if (skins == null)
                return new IStringTooltip[]{ StringTooltip.ofString("N/A", title + " cannot have a skin") };

            // When giving player options, they shouldn't reselect a skin they are
            // already using.
            Set<String> options = skins.getCustomSkins();
            options.add("default");
            StatsData stats = WeaponMechanics.getPlayerWrapper((Player) data.sender()).getStatsData();
            if (stats != null) {
                String skin = (String) stats.get(title, WeaponStat.SKIN, null);
                options.remove(skin);
            }

            if (options.isEmpty())
                return new IStringTooltip[]{ StringTooltip.ofString("N/A", title + " only has default skin") };

            return options.stream().map(option -> StringTooltip.ofString(option, option)).toArray(IStringTooltip[]::new);
        };
    }

    public static void registerPermissions(String key) {
        String keyLower = key.toLowerCase(Locale.ROOT);
        Configuration config = WeaponMechanics.getConfigurations();

        Permission global = new Permission("weaponmechanics." + keyLower + ".*");
        global.setDescription("Ability to use all " + key + "s for any weapon");

        for (String weaponTitle : WeaponMechanics.getWeaponHandler().getInfoHandler().getSortedWeaponList()) {
            SkinSelector skins = config.getObject(weaponTitle + "." + keyLower, SkinSelector.class);
            if (skins == null)
                continue;

            Permission weapon = new Permission("weaponmechanics." + keyLower + "." + weaponTitle + ".*");
            weapon.setDescription("Ability to use all " + key + "s for " + weaponTitle);

            for (String skin : skins.getCustomSkins()) {

                // Default skin can be used by everyone, since it is default.
                if ("default".equalsIgnoreCase(skin))
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

        // Also add permissions for the /skin and /handskin commands
        Bukkit.getPluginManager().getPermission("weaponmechanicscosmetics.commands.handskin").setDefault(PermissionDefault.TRUE);
        Bukkit.getPluginManager().getPermission("weaponmechanicscosmetics.commands.skin").setDefault(PermissionDefault.TRUE);
    }

    public static void register() {
        new CommandAPICommand("skin")
            .withAliases("skins", "weaponskin")
            .withPermission("weaponmechanicscosmetics.commands.skin")
            .withShortDescription("Change the skin for your weapon")
            .withArguments(new StringArgument("skin").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(SKIN_SUGGESTIONS(false))))
            .executesPlayer((player, args) -> {
                applySkin(player, "Skin", (String) args.get(0));
            }).register();
        new CommandAPICommand("handskin")
            .withPermission("weaponmechanicscosmetics.commands.handskin")
            .withShortDescription("Change the skin for your hand")
            .withArguments(new StringArgument("skin").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(SKIN_SUGGESTIONS(true))))
            .executesPlayer((player, args) -> {
                applySkin(player, "Hand", (String) args.get(0));
            }).register();
    }

    public static void applySkin(Player player, String key, String skin) {
        String keylower = key.toLowerCase(Locale.ROOT);
        PlayerInventory inv = player.getInventory();
        boolean mainHand = !empty(inv.getItemInMainHand());
        ItemStack weapon = mainHand ? inv.getItemInMainHand() : inv.getItemInOffHand();

        if (empty(weapon) || WeaponMechanicsAPI.getWeaponTitle(weapon) == null) {
            WeaponMechanicsCosmetics.getInstance().sendLang(player, "skin-hold-weapon", Map.of("hand", key.equals("Hand") ? "hand" : ""));
            return;
        }

        String title = Objects.requireNonNull(WeaponMechanicsAPI.getWeaponTitle(weapon));
        PlayerWrapper wrapper = WeaponMechanics.getPlayerWrapper(player);
        StatsData stats = wrapper.getStatsData();

        Map<String, String> variables = Map.of("hand", key.equals("Hand") ? "hand" : "", "weapon", title, "skin", skin);

        if (stats == null) {
            WeaponMechanicsCosmetics.getInstance().sendLang(player, "skin-player-data", variables);
            return;
        }

        SkinSelector skins = WeaponMechanics.getConfigurations().getObject(title + "." + key, SkinSelector.class);
        if (skins == null) {
            WeaponMechanicsCosmetics.getInstance().sendLang(player, "skin-list", variables);
            return;
        }

        if (!skin.equals("default") && !skins.getCustomSkins().contains(skin)) {
            WeaponMechanicsCosmetics.getInstance().sendLang(player, "skin-option", variables);
            return;
        }

        if (!skin.equals("default") && !player.hasPermission("weaponmechanics." + keylower + "." + title + "." + skin)) {
            WeaponMechanicsCosmetics.getInstance().sendLang(player, "skin-permission", variables);
            return;
        }

        // Apply the skin
        stats.set(title, key.equals("Hand") ? WeaponStat.HAND_SKIN : WeaponStat.SKIN, skin);
        WeaponMechanicsCosmetics.getInstance().sendLang(player, "skin-success", variables);
        WeaponMechanics.getWeaponHandler().getSkinHandler().tryUse(wrapper, title, weapon, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
    }

    private static boolean empty(ItemStack item) {
        return item == null || item.getAmount() == 0 || item.getType().name().endsWith("AIR");
    }
}
