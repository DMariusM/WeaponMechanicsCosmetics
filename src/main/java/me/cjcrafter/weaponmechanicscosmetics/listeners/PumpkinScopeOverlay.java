package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponScopeEvent;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PumpkinScopeOverlay implements Listener {

    private final ItemStack pumpkin;

    public PumpkinScopeOverlay() {
        pumpkin = new ItemStack(ReflectionUtil.getMCVersion() < 13 ? Material.PUMPKIN : Material.CARVED_PUMPKIN);
    }

    @EventHandler
    public void onScope(WeaponScopeEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getShooter();

        // Check to see if we should apply/reapply a pumpkin to the player's
        // head. We don't technically know if the player tried to remove it,
        // so adding it back every stack is a good idea.
        if (event.getScopeType() != WeaponScopeEvent.ScopeType.OUT) {
            Configuration config = WeaponMechanics.getConfigurations();
            if (config.getBool(event.getWeaponTitle() + ".Scope.Pumpkin_Overlay")) {
                CompatibilityAPI.getEntityCompatibility().setSlot(player, EquipmentSlot.HEAD, pumpkin);
                return;
            }
        }

        // Always try to reset the head
        CompatibilityAPI.getEntityCompatibility().setSlot(player,  EquipmentSlot.HEAD, null);
    }
}
