package me.cjcrafter.weaponmechanicscosmetics.general;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.file.serializers.VectorSerializer;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class FakeItemMechanic extends Mechanic {

    private ItemStack item;
    private VectorSerializer offset;
    private VectorSerializer velocity;
    private int time;

    /**
     * Default constructor for serializers.
     */
    public FakeItemMechanic() {
    }

    public FakeItemMechanic(ItemStack item, VectorSerializer offset, VectorSerializer velocity, int time) {
        this.item = item;
        this.offset = offset;
        this.velocity = velocity;
        this.time = time;
    }

    public ItemStack getItem() {
        return item;
    }

    public VectorSerializer getOffset() {
        return offset;
    }

    public VectorSerializer getVelocity() {
        return velocity;
    }

    public int getTime() {
        return time;
    }

    @Override
    public void use0(CastData cast) {
        Location location = cast.getTarget() == null ? cast.getTargetLocation() : cast.getTarget().getEyeLocation();
        location.add(offset.getVector(cast.getTarget()));

        FakeEntity entity = CompatibilityAPI.getEntityCompatibility().generateFakeEntity(location, item);
        entity.setMotion(velocity.getVector(cast.getTarget()).multiply(1.0 / 20.0));
        entity.show();

        int task = new BukkitRunnable() {
            @Override
            public void run() {
                entity.remove();
            }
        }.runTaskLater(WeaponMechanicsCosmetics.getInstance().getPlugin(), time).getTaskId();

        if (cast.getTaskIdConsumer() != null)
            cast.getTaskIdConsumer().accept(task);
    }

    @Override
    public String getKeyword() {
        return "Fake_Item";
    }

    @NotNull
    @Override
    public FakeItemMechanic serialize(SerializeData data) throws SerializerException {

        ItemStack item = data.of("Item").assertExists().serialize(new ItemSerializer());
        int time = data.of("Time").assertExists().getInt();
        VectorSerializer offset = data.of("Offset").assertExists().serialize(VectorSerializer.class);
        VectorSerializer velocity = data.of("Velocity").assertExists().serialize(VectorSerializer.class);

        return new FakeItemMechanic(item, offset, velocity, time);
    }
}