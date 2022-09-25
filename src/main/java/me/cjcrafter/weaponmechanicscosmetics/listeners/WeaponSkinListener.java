package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.weapon.skin.Skin;
import me.deecaad.weaponmechanics.weapon.skin.SkinHandler;
import me.deecaad.weaponmechanics.weapon.skin.SkinList;
import me.deecaad.weaponmechanics.weapon.stats.WeaponStat;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponSkinEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.StatsData;
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
    public void onSkin(WeaponSkinEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        StatsData stats = WeaponMechanics.getPlayerWrapper((Player) event.getShooter()).getStatsData();
        if (stats != null) {
            String skin = (String) stats.get(event.getWeaponTitle(), WeaponStat.SKIN, null);

            if (skin != null)
                event.setSkin(skin);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void handSkin(WeaponSkinEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        SkinList list = config.getObject(event.getWeaponTitle() + ".Hand", SkinList.class);
        if (list == null)
            return;

        PlayerWrapper wrapper = WeaponMechanics.getPlayerWrapper((Player) event.getShooter());
        //wrapper.getPlayer().sendMessage("Yoyoyo");
        StatsData stats = wrapper.getStatsData();
        if (stats != null) {
            String skin = (String) stats.get(event.getWeaponTitle(), WeaponStat.HAND_SKIN, null);
            SkinHandler handler = WeaponMechanics.getWeaponHandler().getSkinHandler();

            // TODO Determine which hand is holding the weapon, when weapon is in offhand and hand should be in mainhand
            HandData handData = wrapper.getOffHandData();
            Skin toApply = handler.getSkin(list, skin, handData, event.getWeaponStack(), null);
            ItemStack handItem = config.getObject(event.getWeaponTitle() + ".Hand.Item", ItemStack.class).clone();
            toApply.apply(handItem);

            CompatibilityAPI.getEntityCompatibility().setSlot(wrapper.getPlayer(), EquipmentSlot.OFF_HAND, handItem);
        }
    }

    @EventHandler
    public void onEquip(PlayerItemHeldEvent event) {
        // Always update off-hand in case of weapon.Hand.Item
        ItemStack offhand = event.getPlayer().getInventory().getItemInOffHand();
        CompatibilityAPI.getEntityCompatibility().setSlot(event.getPlayer(), EquipmentSlot.OFF_HAND, offhand);

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
