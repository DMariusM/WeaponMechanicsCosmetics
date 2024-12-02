package me.cjcrafter.weaponmechanicscosmetics.config;

import com.cjcrafter.foliascheduler.util.MinecraftVersions;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CrossbowConfigSerializer implements Serializer<CrossbowConfigSerializer> {

    private ItemStack item;
    private boolean onlyScope;
    private boolean copyModel;

    /**
     * Default constructor for serializer
     */
    public CrossbowConfigSerializer() {
    }

    public CrossbowConfigSerializer(ItemStack item, boolean onlyScope, boolean copyModel) {
        this.item = item;
        this.onlyScope = onlyScope;
        this.copyModel = copyModel;
    }

    public ItemStack getItem() {
        return item;
    }

    public boolean isOnlyScope() {
        return onlyScope;
    }

    public boolean isCopyModel() {
        return copyModel;
    }

    @Override
    public String getKeyword() {
        return "Crossbow";
    }

    @Override
    public List<String> getParentKeywords() {
        // make sure we don't accidentally serialize a weapon named 'crossbow'
        return List.of("Cosmetics");
    }

    @NotNull
    @Override
    public CrossbowConfigSerializer serialize(SerializeData data) throws SerializerException {
        ItemStack item;

        // People can explicitly set the crossbow item, if they want to.
        if (data.has("Item")) {
            item = data.of("Item").assertExists().serialize(new ItemSerializer());

            // I check IF the item is a crossbow since I figured someone might
            // find a creative use-case for this. We need the arrow in the crossbow
            // in order for it to be "charged" when the user holds it.
            if (MinecraftVersions.VILLAGE_AND_PILLAGE.isAtLeast() && item.getType() == Material.CROSSBOW) {
                CrossbowMeta meta = (CrossbowMeta) item.getItemMeta();
                meta.setChargedProjectiles(List.of(new ItemStack(Material.ARROW)));
                item.setItemMeta(meta);
            }
        }

        // CROSSBOW was only added in Minecraft 1.14. If people explicitly set
        // item, they are allowed to use this feature (Maybe they'll get creative?)
        else if (!MinecraftVersions.VILLAGE_AND_PILLAGE.isAtLeast()) {
            throw data.exception(null, "Cannot use 'CROSSBOW' in Minecraft '" + MinecraftVersions.getCurrent() + "'",
                    "Crossbows were added in Minecraft 1.14, the 'Village and Pillage' update. Update your server to use the Crossbow feature.");
        }

        // We need to infer which custom model data to use based off of skins.
        else {
            // TODO this is hacky AF... Add late pass serializer
            String weaponTitle = data.key.split("\\.")[0];
            int model = (Integer) data.config.get(weaponTitle + ".Skin.Default.Custom_Model_Data", -1);
            if (model == -1) model = (Integer) data.config.get(weaponTitle + ".Skin.Default", -1);

            if (model == -1) {
                throw data.exception(null, "Could not infer model data for '" + weaponTitle + "'",
                        "Make sure you have a skin defined at '" + weaponTitle + ".Skin.Default.Custom_Model_Data'",
                        "If your Skin is located in a different file then your weapon, you will have to define 'Item' manually.");
            }

            item = new ItemStack(Material.CROSSBOW);
            CrossbowMeta meta = (CrossbowMeta) item.getItemMeta();
            meta.setCustomModelData(model);
            meta.setChargedProjectiles(List.of(new ItemStack(Material.ARROW)));
            item.setItemMeta(meta);
        }

        boolean onlyScope = data.of("Only_When_Scoping").getBool(true);
        boolean copyModel = data.of("Copy_Custom_Model_Data").getBool(false);
        return new CrossbowConfigSerializer(item, onlyScope, copyModel);
    }
}