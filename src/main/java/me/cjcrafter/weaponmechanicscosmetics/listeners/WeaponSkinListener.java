package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponSkinEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.StatsData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class WeaponSkinListener implements Listener {

    @EventHandler
    public void onSkin(WeaponSkinEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        StatsData stats = WeaponMechanics.getPlayerWrapper((Player) event.getShooter()).getStatsData();
        if (stats == null)
            return;

        String skin = (String) stats.get(event.getWeaponTitle(), WeaponStat.SKIN, null);

        if (skin != null)
            event.setSkin(skin);
    }

    @EventHandler
    public void onEquip(PlayerItemHeldEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (item == null)
            return;

        String weaponTitle = WeaponMechanicsAPI.getWeaponTitle(item);
        if (weaponTitle == null)
            return;

        // Reapply the skin to the gun.
        EntityWrapper wrapper = WeaponMechanics.getPlayerWrapper(event.getPlayer());
        WeaponMechanics.getWeaponHandler().getSkinHandler().tryUse(wrapper, weaponTitle, item, EquipmentSlot.HAND);
    }
}
