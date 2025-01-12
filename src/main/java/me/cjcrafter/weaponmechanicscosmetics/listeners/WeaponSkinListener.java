package me.cjcrafter.weaponmechanicscosmetics.listeners;

import com.cjcrafter.vivecraft.VSE;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.skin.SkinHandler;
import me.deecaad.weaponmechanics.weapon.skin.SkinSelector;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponGenerateEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponSkinEvent;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.StatsData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class WeaponSkinListener implements Listener {

    @EventHandler
    public void onGiveWeapon(WeaponGenerateEvent event) {
        CommandSender sender = event.getSender();
        String skin = event.getOrDefault("skin", null);
        if (skin == null)
            return;

        SkinSelector selector = WeaponMechanics.getConfigurations().getObject(event.getWeaponTitle() + ".Skin", SkinSelector.class);
        if (selector == null) {
            if (sender != null)
                sender.sendMessage(ChatColor.RED + event.getWeaponTitle() + " does not use skins");
            return;
        }

        if (!selector.getCustomSkins().contains(skin)) {
            if (sender != null) {
                sender.sendMessage(ChatColor.RED + skin + " is not a valid skin for " + event.getWeaponTitle());
                sender.sendMessage(ChatColor.RED + "Valid skins: " + selector.getCustomSkins());
            }
            return;
        }

        CustomTag.WEAPON_SKIN.setString(event.getWeaponStack(), skin);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onSkin(WeaponSkinEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        // Check for per-item overrides
        String override = CustomTag.WEAPON_SKIN.getString(event.getWeaponStack());
        if (override != null) {
            event.setSkin(override);
            return;
        }

        StatsData stats = WeaponMechanics.getPlayerWrapper((Player) event.getShooter()).getStatsData();
        if (stats != null) {
            String skin = (String) stats.get(event.getWeaponTitle(), WeaponStat.SKIN, null);

            if (skin != null)
                event.setSkin(skin);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void handSkin(WeaponSkinEvent event) {
        if (event.isForceDefault() || event.getShooter().getType() != EntityType.PLAYER)
            return;

        // When a player is in VR, we should not try to assign an off-hand to
        // them (Since they already have a visual hand).
        Player player = (Player) event.getShooter();
        if (Bukkit.getPluginManager().getPlugin("VivecraftSpigot") != null && VSE.isVive(player))
            return;

        // Creative players cause item duplication
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        SkinSelector skins = config.getObject(event.getWeaponTitle() + ".Hand", SkinSelector.class);
        if (skins == null)
            return;

        PlayerWrapper wrapper = WeaponMechanics.getPlayerWrapper(player);
        //wrapper.getPlayer().sendMessage("Yoyoyo");
        StatsData stats = wrapper.getStatsData();
        if (stats != null) {
            String skin = (String) stats.get(event.getWeaponTitle(), WeaponStat.HAND_SKIN, null);
            SkinHandler handler = WeaponMechanics.getWeaponHandler().getSkinHandler();

            // TODO Determine which hand is holding the weapon, when weapon is in offhand and hand should be in mainhand
            HandData handData = wrapper.getMainHandData();
            ItemStack handItem = config.getObject(event.getWeaponTitle() + ".Hand.Item", ItemStack.class).clone();
            SkinSelector.SkinAction action = handler.getSkinAction(skins, skin, handData, event.getWeaponStack(), event.getCause());
            skins.apply(handItem, skin, action, CustomTag.ATTACHMENTS.getStringArray(event.getWeaponStack()));

            CompatibilityAPI.getEntityCompatibility().setSlot(wrapper.getPlayer(), EquipmentSlot.OFF_HAND, handItem);
        }
    }

    @EventHandler
    public void onEquip(PlayerItemHeldEvent event) {
        // Always update off-hand in case of weapon.Hand.Item
        CompatibilityAPI.getEntityCompatibility().setSlot(event.getPlayer(), EquipmentSlot.OFF_HAND, null);
    }
}
