package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.cjcrafter.weaponmechanicscosmetics.timer.TimerValidator;
import me.deecaad.core.events.QueueSerializerEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WeaponMechanicsSerializerListener implements Listener {

    @EventHandler
    public void queueSerializers(QueueSerializerEvent event) {
        if (!event.getSourceName().equals("WeaponMechanics"))
            return;

        event.addValidators(new TimerValidator());
    }
}
