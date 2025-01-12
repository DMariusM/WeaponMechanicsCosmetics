package me.cjcrafter.weaponmechanicscosmetics.config;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.weaponmechanics.weapon.skin.BaseSkinSelector;
import me.deecaad.weaponmechanics.weapon.skin.SkinSelector;
import org.bukkit.inventory.ItemStack;

public class HandValidator implements IValidator {

    @Override
    public String getKeyword() {
        return "Hand";
    }

    @Override
    public void validate(Configuration configuration, SerializeData data) throws SerializerException {
        ItemStack baseItem = data.of("Item").assertExists().serialize(new ItemSerializer()).get();
        SkinSelector skins = data.of().assertExists().serialize(new BaseSkinSelector()).get();

        configuration.set(data.getKey() + ".Item", baseItem);
        configuration.set(data.getKey(), skins);
    }
}
