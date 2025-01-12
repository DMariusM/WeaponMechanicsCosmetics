package me.cjcrafter.weaponmechanicscosmetics.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemConsumable;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.cjcrafter.weaponmechanicscosmetics.config.ThirdPersonPose;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * This packet listener detects when an outgoing set entity equipment packet
 * contains a WeaponMechanics item. If that WeaponMechanics item has a
 * configured crossbow, we should replace the item with the crossbow. This way,
 * the player's arms stick up and appear to be "aiming"
 */
public class CrossbowPacketListener implements PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            handleEntityMetadata(event);
        } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            handleEntityEquipment(event);
        }

    }

    private void handleEntityEquipment(PacketSendEvent event) {
        WrapperPlayServerEntityEquipment equipmentPacket = new WrapperPlayServerEntityEquipment(event);
        Player receiver = event.getPlayer();

        // prevents the offhand weapon from visually appearing as a crossbow for the holder...
        if (receiver.getEntityId() == equipmentPacket.getEntityId())
            return;

        // Only players can have proper animations
        Entity shooter = SpigotConversionUtil.getEntityById(receiver.getWorld(), equipmentPacket.getEntityId());
        if (!(shooter instanceof Player))
            return;

        // Used to check if the shooter is reloading/scoping
        PlayerWrapper playerWrapper = WeaponMechanics.getPlayerWrapper((Player) shooter);

        boolean shouldUpdatePacket = false;
        for (Equipment equipment : equipmentPacket.getEquipment()) {
            if (equipment == null)
                continue;
            if (equipment.getSlot() != EquipmentSlot.MAIN_HAND && equipment.getSlot() != EquipmentSlot.OFF_HAND)
                continue;
            if (equipment.getItem() == null)
                continue;

            com.github.retrooper.packetevents.protocol.item.ItemStack item = equipment.getItem();
            String weaponTitle = getWeaponTitle(item);
            if (weaponTitle == null)
                continue;

            // The configuration if the weapon uses poses
            ThirdPersonPose poses = WeaponMechanics.getConfigurations().getObject(weaponTitle + ".Cosmetics.Third_Person_Pose", ThirdPersonPose.class);
            if (poses == null)
                continue;

            // Determine which pose the gun should be in
            boolean isMainhand = equipment.getSlot() == EquipmentSlot.MAIN_HAND;
            HandData handData = playerWrapper.getHandData(isMainhand);
            ItemConsumable.Animation type;
            if (handData.hasRunningFirearmAction())
                type = poses.getFirearmActionPose();
            else if (handData.isReloading())
                type = poses.getReloadPose();
            else if (handData.getZoomData().isZooming())
                type = poses.getScopePose();
            else
                type = poses.getDefaultPose();

            // No need to change the item if we have no pose
            if (type == ItemConsumable.Animation.NONE) {
                scheduleItemUsePacket(receiver, equipmentPacket.getEntityId(), false, isMainhand);
                continue;
            }

            // Set the components to make the weapon "consumable" (yes, into food).
            // We can then send the "USE ITEM" packet to make the shooter hold the
            // weapon in the correct pose.
            ItemConsumable consumable = new ItemConsumable(
                Float.MAX_VALUE, // a really long time, so the player never eats the item
                type,
                Sounds.ITEM_CROSSBOW_QUICK_CHARGE_1,  // something quiet, should never be heard since consume time is high
                false, // no particles
                List.of() // no effects
            );
            item.setComponent(ComponentTypes.CONSUMABLE, consumable);
            shouldUpdatePacket = true;

            scheduleItemUsePacket(receiver, equipmentPacket.getEntityId(), true, isMainhand);
        }

        if (shouldUpdatePacket) {
            equipmentPacket.write();
        }
    }


    private void handleEntityMetadata(PacketSendEvent event) {
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(event);
        Player receiver = event.getPlayer();

        // If entity id is inverted, this is one of our packets... let it through
        if (metadataPacket.getEntityId() < 0) {
            metadataPacket.setEntityId(-metadataPacket.getEntityId());
            return;
        }

        // prevents the offhand weapon from visually appearing as a crossbow for the holder...
        if (receiver.getEntityId() == metadataPacket.getEntityId())
            return;

        // Only players can have proper animations
        Entity shooter = SpigotConversionUtil.getEntityById(receiver.getWorld(), metadataPacket.getEntityId());
        if (!(shooter instanceof Player))
            return;

        // Used to check if the shooter is reloading/scoping
        PlayerWrapper playerWrapper = WeaponMechanics.getPlayerWrapper((Player) shooter);

        // If the metadata packet contains the "active hand" data, we need to remove it
        // so the player doesn't appear to be holding the weapon in the wrong hand
        List<EntityData> metadata = metadataPacket.getEntityMetadata();
        for (Iterator<EntityData> iterator = metadata.iterator(); iterator.hasNext(); ) {
            EntityData data = iterator.next();
            if (data.getType() == EntityDataTypes.BYTE && data.getIndex() == 8) {
                byte value = (byte) data.getValue();
                boolean isActive = (value & 0b01) == 0b01;
                boolean isMainhand = (value & 0b10) == 0b00;

                // If the player is holding a weapon, we need to modify the packet
                org.bukkit.inventory.ItemStack itemInHand = playerWrapper.getPlayer().getEquipment().getItem(isMainhand ? org.bukkit.inventory.EquipmentSlot.HAND : org.bukkit.inventory.EquipmentSlot.OFF_HAND);
                String weaponTitle = CustomTag.WEAPON_TITLE.getString(itemInHand);
                if (weaponTitle == null)
                    break;

                // Determine which pose the gun should be in
                ThirdPersonPose poses = WeaponMechanics.getConfigurations().getObject(weaponTitle + ".Cosmetics.Third_Person_Pose", ThirdPersonPose.class);
                if (poses == null)
                    break;

                // Now we know that poses are already handled, so we can remove the metadata
                iterator.remove();
                break;
            }
        }
    }

    private @Nullable String getWeaponTitle(com.github.retrooper.packetevents.protocol.item.ItemStack equipment) {
        NBTCompound customData = equipment.getComponent(ComponentTypes.CUSTOM_DATA).orElse(null);
        if (customData == null)
            return null;

        NBTCompound publicBukkitValues = customData.getCompoundTagOrNull("PublicBukkitValues");
        if (publicBukkitValues == null)
            return null;

        return publicBukkitValues.getStringTagValueOrNull("weaponmechanics:weapon-title");
    }

    private void scheduleItemUsePacket(Player receiver, int shooterId, boolean isActive, boolean isMainhand) {
        WeaponMechanics.getInstance().getFoliaScheduler().async().runDelayed(() -> {
            int activeMask = isActive ? 0b01 : 0b00;
            int handMask = isMainhand ? 0b00 : 0b10;
            EntityData eatData = new EntityData(8, EntityDataTypes.BYTE, activeMask | handMask);
            WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(-shooterId, List.of(eatData));

            PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, metadataPacket);
        }, 1L);
    }
}
