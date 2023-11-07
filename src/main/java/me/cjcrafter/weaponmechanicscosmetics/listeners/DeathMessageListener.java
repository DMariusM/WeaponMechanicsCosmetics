package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.lib.adventure.text.Component;
import me.deecaad.core.lib.adventure.text.serializer.gson.GsonComponentSerializer;
import me.deecaad.core.lib.adventure.text.serializer.legacy.LegacyComponentSerializer;
import me.deecaad.core.placeholder.PlaceholderData;
import me.deecaad.core.placeholder.PlaceholderMessage;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.events.WeaponMechanicsEntityDamageByEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponKillEntityEvent;
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

    private Map<LivingEntity, WeaponDamageEntityEvent> killMap = new IdentityHashMap<>(); // identity for performance
    private LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private LegacyComponentSerializer HEX = LegacyComponentSerializer.builder()
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
        if (event instanceof WeaponMechanicsEntityDamageByEntityEvent)
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

        PlaceholderMessage deathMessage = NumberUtil.random(deathMessages);
        PlaceholderData placeholderData = PlaceholderData.builder()
                .setItem(killData.getWeaponStack())
                .setItemTitle(killData.getWeaponTitle())
                .setPlayer(event.getEntity())
                .setPlaceholder("target_name", killData.getVictim().getName())
                .setPlaceholder("source_name", killData.getShooter().getName());

        Component component = deathMessage.replaceAndDeserialize(placeholderData);
        String newMessage;
        if (ReflectionUtil.getMCVersion() < 16)
            newMessage = LEGACY.serialize(component);
        else
            newMessage = HEX.serialize(component);

        event.setDeathMessage(newMessage);
    }
}
