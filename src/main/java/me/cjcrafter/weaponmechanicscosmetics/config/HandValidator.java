package me.cjcrafter.weaponmechanicscosmetics.config;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.weaponmechanics.weapon.skin.SkinList;
import org.bukkit.inventory.ItemStack;

public class HandValidator implements IValidator {

    @Override
    public String getKeyword() {
        return "Hand";
    }

    @Override
    public void validate(Configuration configuration, SerializeData data) throws SerializerException {
        ItemStack baseItem = data.of("Item").assertExists().serializeNonStandardSerializer(new ItemSerializer());
        SkinList skins = data.of().assertExists().serialize(SkinList.class);

        configuration.set(data.key + ".Item", baseItem);
        configuration.set(data.key, skins);
    }
}
