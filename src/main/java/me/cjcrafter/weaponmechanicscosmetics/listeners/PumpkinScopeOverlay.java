package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.Debugger;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponScopeEvent;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.ZoomData;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class PumpkinScopeOverlay implements Listener {

    private final ItemStack survivalPumpkin;
    private final ItemStack creativePumpkin;

    private final Set<Player> injectedPlayers = new HashSet<>();

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
        else if (!pumpkin.getType().name().endsWith("PUMPKIN"))
            debug.error("'" + key + "' was not a pumpkin... Did you change it? " + pumpkin);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();
        PlayerWrapper wrapper = WeaponMechanics.getPlayerWrapper(player);

        // When the player is wearing a helmet with durability, the packet
        // will be sent to override the pumpkin_overlay. We can skip these
        // checks if the player is not wearing a helmet.
        if (player.getInventory().getHelmet() == null)
            return;

        ZoomData main = wrapper.getMainHandData().getZoomData();
        ZoomData off = wrapper.getOffHandData().getZoomData();

        // 2 ticks are needed, 1 doesn't work
        if (main.isZooming() || off.isZooming()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (main.isZooming() || off.isZooming() && injectedPlayers.contains(player))
                        sendPumpkin(player);
                }
            }.runTaskLater(WeaponMechanicsCosmetics.getInstance().getPlugin(), 2L);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onScope(WeaponScopeEvent event) {
        if (event.getShooter().getType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getShooter();

        // Check to see if we should apply/reapply a pumpkin to the player's
        // head. We don't technically know if the player tried to remove it,
        // so adding it back every stack is a good idea.
        if (event.getScopeType() != WeaponScopeEvent.ScopeType.OUT && event.isPumpkinOverlay()) {
            injectedPlayers.add(player);
            sendPumpkin(player);
            return;
        }

        // Always try to reset the head
        injectedPlayers.remove(player);
        CompatibilityAPI.getEntityCompatibility().setSlot(player, EquipmentSlot.HEAD, null);
    }

    private void sendPumpkin(Player player) {
        ItemStack pumpkin = player.getGameMode() == GameMode.CREATIVE ? creativePumpkin : survivalPumpkin;
        CompatibilityAPI.getEntityCompatibility().setSlot(player, EquipmentSlot.HEAD, pumpkin);
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
