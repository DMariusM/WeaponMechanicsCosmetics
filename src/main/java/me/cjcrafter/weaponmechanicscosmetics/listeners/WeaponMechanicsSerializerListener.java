package me.cjcrafter.weaponmechanicscosmetics.listeners;

import me.cjcrafter.weaponmechanicscosmetics.CosmeticsScriptManager;
import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.cjcrafter.weaponmechanicscosmetics.timer.TimerValidator;
import me.cjcrafter.weaponmechanicscosmetics.trails.Trail;
import me.deecaad.core.events.QueueSerializerEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WeaponMechanicsSerializerListener implements Listener {

    @EventHandler
    public void queueSerializers(QueueSerializerEvent event) {
        if (!event.getSourceName().equals("WeaponMechanics"))
            return;

        event.addValidators(new TimerValidator());
        event.addSerializers(new Trail());
        new CosmeticsScriptManager(WeaponMechanicsCosmetics.getInstance().getPlugin()).register();
    }
}
