package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.Debugger;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponScopeEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PumpkinScopeOverlay implements Listener {

    private final ItemStack survivalPumpkin;
    private final ItemStack creativePumpkin;

    public PumpkinScopeOverlay() {
        Configuration config = WeaponMechanicsCosmetics.getInstance().getConfiguration();
        survivalPumpkin = config.getObject("Survival_Pumpkin", ItemStack.class);
        creativePumpkin = config.getObject("Creative_Pumpkin", ItemStack.class);

        validate("Survival_Pumpkin", survivalPumpkin);
        validate("Creative_Pumpkin", creativePumpkin);
    }

    private void validate(String key, ItemStack pumpkin) {
        Debugger debug = WeaponMechanicsCosmetics.getInstance().getDebug();
        if (pumpkin == null)
            debug.error("Could not find '" + key + "' in config.yml... Did you delete it?");
        else if (pumpkin.getType().name().endsWith("PUMPKIN"))
            debug.error("'" + key + "' was not a pumpkin... Did you change it? " + pumpkin);
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

                ItemStack pumpkin = player.getGameMode() == GameMode.CREATIVE ? creativePumpkin : survivalPumpkin;
                CompatibilityAPI.getEntityCompatibility().setSlot(player, EquipmentSlot.HEAD, pumpkin);
                return;
            }
        }

        // Always try to reset the head
        CompatibilityAPI.getEntityCompatibility().setSlot(player,  EquipmentSlot.HEAD, null);
    }

    public static class PumpkinSurvivalSerializer extends ItemSerializer {

        @Override
        public String getKeyword() {
            return "Survival_Pumpkin";
        }
    }

    public static class PumpkinCreativeSerializer extends ItemSerializer {

        @Override
        public String getKeyword() {
            return "Creative_Pumpkin";
        }
    }
}
