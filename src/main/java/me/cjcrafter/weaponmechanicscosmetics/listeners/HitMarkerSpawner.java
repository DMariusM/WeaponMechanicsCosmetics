package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.lib.adventure.text.Component;
import me.deecaad.core.lib.adventure.text.serializer.legacy.LegacyComponentSerializer;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;

public class HitMarkerSpawner implements Listener {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    @EventHandler (priority = EventPriority.MONITOR)
    public void onDamage(WeaponDamageEntityEvent event) {
        Configuration config = WeaponMechanics.getConfigurations();
        String format = config.getString(event.getWeaponTitle() + ".Cosmetics.Hit_Marker");

        // Don't use hit market if it wasn't in config
        if (format == null)
            return;

        format = format.replace("%damage%", DECIMAL_FORMAT.format(event.getFinalDamage()));

        // We need to parse it into a mini message then back to using the
        // vanilla color codes. We do this so we keep configs matching.
        // Performance on this is of course terrible... TODO
        Component component = MechanicsCore.getPlugin().message.deserialize(format);
        format = LegacyComponentSerializer.legacySection().serialize(component);

        double width = CompatibilityAPI.getEntityCompatibility().getWidth(event.getVictim());
        Location pos = event.getVictim().getEyeLocation();
        pos.add(NumberUtil.random(-width, width), -2, NumberUtil.random(-width, width));
        FakeEntity entity = CompatibilityAPI.getEntityCompatibility().generateFakeEntity(pos, EntityType.ARMOR_STAND, null);
        entity.setInvisible(true);
        entity.setGravity(false);
        entity.setDisplay(format);
        //entity.setMotion(0, 0.05, 0);
        entity.show();

        entity.setPosition(pos.getX(), pos.getY() + 1.0, pos.getZ());

        new BukkitRunnable() {
            @Override
            public void run() {
                entity.remove();
            }
        }.runTaskLater(WeaponMechanicsCosmetics.getInstance().getPlugin(), 20);
    }
}
