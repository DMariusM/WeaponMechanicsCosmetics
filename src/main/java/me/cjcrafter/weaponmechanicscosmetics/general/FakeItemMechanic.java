package me.cjcrafter.weaponmechanicscosmetics.general;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.file.serializers.VectorSerializer;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FakeItemMechanic implements IMechanic<FakeItemMechanic> {

    private record ItemMechanicConfig(ItemStack item, VectorSerializer offset, VectorSerializer velocity, int time) {
    }

    private List<ItemMechanicConfig> items;

    /**
     * Default constructor for serializers.
     */
    public FakeItemMechanic() {
    }

    public FakeItemMechanic(List<ItemMechanicConfig> items) {
        this.items = items;
    }

    @Override
    public void use(CastData castData) {
        for (ItemMechanicConfig item : items) {
            Location location = castData.getCaster() == null ? castData.getCastLocation().clone() : castData.getCaster().getEyeLocation();
            location.add(item.offset.getVector(castData.getCaster()));

            FakeEntity entity = CompatibilityAPI.getEntityCompatibility().generateFakeEntity(location, item.item);
            entity.setMotion(item.velocity.getVector(castData.getCaster()).multiply(1.0 / 20.0));
            entity.show();

            new BukkitRunnable() {
                @Override
                public void run() {
                    entity.remove();
                }
            }.runTaskLater(WeaponMechanicsCosmetics.getInstance().getPlugin(), item.time);
        }
    }

    @Override
    public String getKeyword() {
        return "Items";
    }

    @NotNull
    @Override
    public FakeItemMechanic serialize(SerializeData data) throws SerializerException {
        ConfigurationSection config = data.of().assertType(ConfigurationSection.class).assertExists().get();

        List<ItemMechanicConfig> items = new ArrayList<>();
        for (String key : config.getKeys(false)) {
            SerializeData move = data.move(key);

            ItemStack item = move.of().assertExists().serializeNonStandardSerializer(new ItemSerializer());
            int time = move.of("Time").assertExists().getInt();
            VectorSerializer offset = move.of("Offset").assertExists().serialize(VectorSerializer.class);
            VectorSerializer velocity = move.of("Velocity").assertExists().serialize(VectorSerializer.class);

            items.add(new ItemMechanicConfig(item, offset, velocity, time));
        }

        return new FakeItemMechanic(items);
    }
}