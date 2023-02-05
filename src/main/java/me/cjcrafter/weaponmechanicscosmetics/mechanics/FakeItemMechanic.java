package me.cjcrafter.weaponmechanicscosmetics.mechanics;

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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class FakeItemMechanic extends Mechanic {

    private ItemStack item;
    private VectorSerializer velocity;
    private int time;

    /**
     * Default constructor for serializers.
     */
    public FakeItemMechanic() {
    }

    public FakeItemMechanic(ItemStack item, VectorSerializer velocity, int time) {
        this.item = item;
        this.velocity = velocity;
        this.time = time;
    }

    public ItemStack getItem() {
        return item;
    }

    public VectorSerializer getVelocity() {
        return velocity;
    }

    public int getTime() {
        return time;
    }

    @Override
    public void use0(CastData cast) {
        Location location = cast.hasTargetLocation() ? cast.getTargetLocation() : cast.getTarget().getEyeLocation();

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
    public Mechanic serialize(SerializeData data) throws SerializerException {

        ItemStack item = new ItemSerializer().serialize(data);
        int time = data.of("Time").getInt(100);
        VectorSerializer velocity = data.of("Velocity").serialize(VectorSerializer.class);

        if (velocity == null)
            velocity = VectorSerializer.from(new Vector());

        return applyParentArgs(data, new FakeItemMechanic(item, velocity, time));
    }
}