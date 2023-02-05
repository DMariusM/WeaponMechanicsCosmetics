package me.cjcrafter.weaponmechanicscosmetics.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import me.cjcrafter.weaponmechanicscosmetics.config.CrossbowConfigSerializer;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * This packet listener detects when an outgoing set entity equipment packet
 * contains a WeaponMechanics item. If that WeaponMechanics item has a
 * configured crossbow, we should replace the item with the crossbow. This way,
 * the player's arms stick up and appear to be "aiming"
 */
public class CrossbowPacketListener extends PacketAdapter {

    public CrossbowPacketListener(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();

        // Get the entity/wrapper who is holding the weapon.
        LivingEntity entity = (LivingEntity) packet.getEntityModifier(event).read(0);
        EntityWrapper wrapper = entity == null ? null : WeaponMechanics.getEntityWrapper(entity, true);

        // Wrapper can be null when the entity is from model engine (or any
        // other fake entity plugin), or when the entity does not have a
        // wrapper (Which is most non-player entities).
        if (wrapper == null)
            return;

        // From 1.9 -> 1.15, the SetEquipment packet had 3 variables: id, slot,
        // and item.
        if (ReflectionUtil.getMCVersion() <= 15) {
            EnumWrappers.ItemSlot slot = packet.getItemSlots().read(0);
            ItemStack equipment = packet.getItemModifier().read(0);
            packet.getItemModifier().write(0, getCrossbow(slot, equipment, wrapper));
            return;
        }

        // In 1.16+, the SetEquipment packet had 2 variables: id, and
        // List<Pair<Slot, ItemStack>>.
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slots = packet.getSlotStackPairLists().read(0);
        for (Pair<EnumWrappers.ItemSlot, ItemStack> pair : slots) {
            pair.setSecond(getCrossbow(pair.getFirst(), pair.getSecond(), wrapper));
        }
        packet.getSlotStackPairLists().write(0, slots);
    }

    private ItemStack getCrossbow(EnumWrappers.ItemSlot slot, ItemStack equipment, EntityWrapper wrapper) {
        if (slot != EnumWrappers.ItemSlot.MAINHAND && slot != EnumWrappers.ItemSlot.OFFHAND) {
            return equipment;
        }

        String weaponTitle = equipment.hasItemMeta() ? CustomTag.WEAPON_TITLE.getString(equipment) : null;
        if (weaponTitle == null)
            return equipment;

        CrossbowConfigSerializer crossbow = WeaponMechanics.getConfigurations().getObject(weaponTitle + ".Cosmetics.Crossbow", CrossbowConfigSerializer.class);
        boolean isScoping = wrapper != null && wrapper.getHandData(slot == EnumWrappers.ItemSlot.MAINHAND).getZoomData().isZooming();
        if (crossbow != null && !(crossbow.isOnlyScope() && !isScoping)) {
            ItemStack temp = crossbow.getItem();

            // Allow copying of model data for when the model itself changes
            // for scoping/sprinting/whatever
            if (crossbow.isCopyModel()) {
                temp = temp.clone();
                ItemMeta meta = temp.getItemMeta();
                meta.setCustomModelData(equipment.getItemMeta().getCustomModelData());
                temp.setItemMeta(meta);
            }

            return temp;
        }

        return equipment;
    }
}
