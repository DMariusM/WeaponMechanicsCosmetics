package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.lib.adventure.text.serializer.gson.GsonComponentSerializer;
import me.deecaad.core.lib.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
        List<String> deathMessages = config.getList(killData.getWeaponTitle() + ".Cosmetics.Death_Messages");

        String deathMessage = NumberUtil.random(deathMessages);
        deathMessage = deathMessage.replace("%victim%", killData.getVictim().getName());
        deathMessage = deathMessage.replace("%shooter%", killData.getShooter().getName());
        if (ReflectionUtil.getMCVersion() < 16)
            deathMessage = LEGACY.serialize(MechanicsCore.getPlugin().message.deserialize(deathMessage));
        else
            deathMessage = HEX.serialize(MechanicsCore.getPlugin().message.deserialize(deathMessage));

        event.setDeathMessage(deathMessage);
    }
}
