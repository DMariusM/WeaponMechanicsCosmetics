package me.cjcrafter.weaponmechanicscosmetics.timer;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class TimerValidator implements IValidator {

    @Override
    public boolean denyKeys() {
        return true;
    }

    @Override
    public String getKeyword() {
        return "Timer";
    }

    @Override
    public void validate(Configuration configuration, File file, ConfigurationSection config, String s) throws SerializerException {

        List<String> keys = Arrays.asList("Delay_Between_Shots", "Shoot_Delay_After_Scope", "Weapon_Equip_Delay", "Reload_Time",
                "Firearms_Actions_Time", "Melee_Hit_Delay", "Melee_Miss_Delay");

        for (String key : keys) {
            SerializeData data = new SerializeData(new Timer(), file, s + "." + key, config);
            Timer timer = data.of().serialize(new Timer());
            configuration.set(s + "." + key, timer);
        }
    }
}
