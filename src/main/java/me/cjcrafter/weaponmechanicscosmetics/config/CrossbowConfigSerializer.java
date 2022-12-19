package me.cjcrafter.weaponmechanicscosmetics.config;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class CrossbowConfigSerializer implements Serializer<CrossbowConfigSerializer> {

    private ItemStack item;
    private boolean onlyScope;

    /**
     * Default constructor for serializer
     */
    public CrossbowConfigSerializer() {
    }

    public CrossbowConfigSerializer(ItemStack item, boolean onlyScope) {
        this.item = item;
        this.onlyScope = onlyScope;
    }

    public ItemStack getItem() {
        return item;
    }

    public boolean isOnlyScope() {
        return onlyScope;
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
            if (ReflectionUtil.getMCVersion() >= 14 && item.getType() == Material.CROSSBOW) {
                CrossbowMeta meta = (CrossbowMeta) item.getItemMeta();
                meta.setChargedProjectiles(List.of(new ItemStack(Material.ARROW)));
                item.setItemMeta(meta);
            }
        }

        // CROSSBOW was only added in Minecraft 1.14. If people explicitly set
        // item, they are allowed to use this feature (Maybe they'll get creative?)
        else if (ReflectionUtil.getMCVersion() < 14) {
            throw data.exception(null, "Cannot use 'CROSSBOW' in Minecraft '" + ReflectionUtil.getMCVersion() + "'",
                    "Crossbows were added in Minecraft 1.14, the 'Village and Pillage' update. Update your server to use the Crossbow feature.");
        }

        // We need to infer which custom model data to use based off of skins.
        else {
            // TODO this is hacky AF... Add late pass serializer
            String weaponTitle = data.key.split("\\.")[0];
            int model = data.config.getInt(weaponTitle + ".Skin.Default.Custom_Model_Data", -1);

            if (model == -1) {
                throw data.exception("Could not infer model data for '" + weaponTitle + "'",
                        "Make sure you have a skin defined at '" + weaponTitle + ".Skin.Default.Custom_Model_Data'",
                        "If your Skin is located in a different file then your weapon, you will have to define 'Item' manually.");
            }

            item = new ItemStack(Material.CROSSBOW);
            CrossbowMeta meta = (CrossbowMeta) item.getItemMeta();
            meta.setCustomModelData(model);
            meta.setChargedProjectiles(List.of(new ItemStack(Material.ARROW)));
            item.setItemMeta(meta);
        }

        boolean onlyScope = data.of("Only_When_Scoping").assertExists().getBool();
        return new CrossbowConfigSerializer(item, onlyScope);
    }

    public ItemStack item() {
        return item;
    }

    public boolean onlyScope() {
        return onlyScope;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CrossbowConfigSerializer) obj;
        return Objects.equals(this.item, that.item) &&
                this.onlyScope == that.onlyScope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, onlyScope);
    }

    @Override
    public String toString() {
        return "CrossbowConfigSerializer[" +
                "item=" + item + ", " +
                "onlyScope=" + onlyScope + ']';
    }

}
