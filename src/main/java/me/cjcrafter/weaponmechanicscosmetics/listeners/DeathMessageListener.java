package me.cjcrafter.weaponmechanicscosmetics.listeners;

import com.cjcrafter.foliascheduler.util.MinecraftVersions;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderMessage;
import me.deecaad.core.utils.RandomUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class DeathMessageListener implements Listener {

    private final Map<LivingEntity, WeaponDamageEntityEvent> killMap = new IdentityHashMap<>(); // identity for performance
    private final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private final LegacyComponentSerializer HEX = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexCharacter('#')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    @EventHandler
    public void onWeaponDamage(WeaponDamageEntityEvent event) {

        if (event.getVictim().getType() != EntityType.PLAYER)
            return;

        // Since Spigot does not provide a way to check who killed an entity,
        // we have to hack this shit together.
        Configuration config = WeaponMechanics.getConfigurations();
        List<String> deathMessages = config.getList(event.getWeaponTitle() + ".Cosmetics.Death_Messages");
        if (!deathMessages.isEmpty())
            killMap.put(event.getVictim(), event);
    }

    @EventHandler
    public void onVanillaDamage(EntityDamageByEntityEvent event) {

        // Skip damage by WM, since we want to detect when WM kills entities
        Entity entity = event.getEntity();
        if (entity.hasMetadata("doing-weapon-damage"))
            return;

        killMap.remove(event.getEntity());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        WeaponDamageEntityEvent killData = killMap.remove(event.getEntity());
        if (killData == null)
            return;

        Configuration config = WeaponMechanics.getConfigurations();
        List<PlaceholderMessage> deathMessages = config.getObject(killData.getWeaponTitle() + ".Cosmetics.Death_Messages", List.class);

        PlaceholderMessage deathMessage = RandomUtil.element(deathMessages);
        PlaceholderData placeholderData = PlaceholderData.builder()
                .setItem(killData.getWeaponStack())
                .setItemTitle(killData.getWeaponTitle())
                .setPlayer(event.getEntity())
                .setPlaceholder("target_name", killData.getVictim().getName())
                .setPlaceholder("source_name", killData.getShooter().getName());

        Component component = deathMessage.replaceAndDeserialize(placeholderData);
        String newMessage;
        if (MinecraftVersions.NETHER_UPDATE.isAtLeast())
            newMessage = HEX.serialize(component);
        else
            newMessage = LEGACY.serialize(component);

        event.setDeathMessage(newMessage);
    }
}
