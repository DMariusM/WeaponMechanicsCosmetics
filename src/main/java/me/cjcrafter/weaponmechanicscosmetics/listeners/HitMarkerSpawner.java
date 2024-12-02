package me.cjcrafter.weaponmechanicscosmetics.listeners;

import com.cjcrafter.foliascheduler.ServerImplementation;
import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderMessage;
import me.deecaad.core.utils.RandomUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;

public class HitMarkerSpawner implements Listener {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    @EventHandler (priority = EventPriority.MONITOR)
    public void onDamage(WeaponDamageEntityEvent event) {
        Configuration config = WeaponMechanics.getConfigurations();
        PlaceholderMessage format = config.getObject(event.getWeaponTitle() + ".Cosmetics.Hit_Marker", PlaceholderMessage.class);

        // Don't use hit marker if it wasn't in config
        if (format == null)
            return;

        PlaceholderData placeholderData = PlaceholderData.builder()
                .setItem(event.getWeaponStack())
                .setItemTitle(event.getWeaponTitle())
                .setPlayer(event.getVictim() instanceof Player player ? player : null)
                .setPlaceholder("damage", DECIMAL_FORMAT.format(event.getFinalDamage()));

        Component component = format.replaceAndDeserialize(placeholderData);
        String display = LegacyComponentSerializer.legacySection().serialize(component);

        double width = event.getVictim().getWidth();
        Location pos = event.getVictim().getEyeLocation();
        pos.add(RandomUtil.range(-width, width), -2, RandomUtil.range(-width, width));
        FakeEntity entity = CompatibilityAPI.getEntityCompatibility().generateFakeEntity(pos, EntityType.ARMOR_STAND, null);
        entity.setInvisible(true);
        entity.setGravity(false);
        entity.setDisplay(display);
        //entity.setMotion(0, 0.05, 0);
        entity.show();

        entity.setPosition(pos.getX(), pos.getY() + 1.0, pos.getZ());

        ServerImplementation scheduler = WeaponMechanicsCosmetics.getInstance().getScheduler();
        scheduler.region(pos).runDelayed(() -> entity.remove(), 20);
    }
}
