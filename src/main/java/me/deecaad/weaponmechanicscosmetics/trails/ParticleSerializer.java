package me.deecaad.weaponmechanicscosmetics.trails;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ColorSerializer;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ParticleSerializer implements Serializer<ParticleSerializer> {

    private Particle particle;
    private int count;
    private double extra;
    private Vector offset; // sometimes velocity
    private Object options;

    public ParticleSerializer() {
    }

    public ParticleSerializer(Particle particle, int count, double extra, Vector offset, Object options) {
        this.particle = particle;
        this.count = count;
        this.extra = extra;
        this.offset = offset;
        this.options = options;
    }

    public void display(Location location) {
        display(location.getWorld(), location.getX(), location.getY(), location.getZ());
    }

    public void display(World world, Vector position) {
        display(world, position.getX(), position.getY(), position.getZ());
    }

    public void display(World world, double x, double y, double z) {
        if (world == null)
            throw new IllegalArgumentException("World cannot be null");

        world.spawnParticle(particle, x, y, z, count, offset.getX(), offset.getY(), offset.getZ(), extra, options);
    }

    @NotNull
    @Override
    public ParticleSerializer serialize(SerializeData data) throws SerializerException {

        // This Serializer was developed using information from this thread:
        // https://www.spigotmc.org/threads/comprehensive-particle-spawning-guide-1-13-1-18.343001/
        // Big thanks to Esophose who compiled all of that information for
        // anybody to use.

        Particle particle = data.of("Type").assertExists().getEnum(Particle.class);
        int count = data.of("Count").assertPositive().getInt(-1);
        double extra = 0.0;
        Vector offset = new Vector();
        Object options = null;

        // Dust transition was added in 1.17, which accompanies the skulk
        // sensor. It can fade from one color to another color. Can also
        // modify particle size.
        switch (particle.name()) {
            case "DUST_COLOR_TRANSITION": {
                Color color = data.of("Color").assertExists().serializeNonStandardSerializer(new ColorSerializer());
                Color fade = data.of("Fade_Color").assertExists().serializeNonStandardSerializer(new ColorSerializer());
                float size = (float) data.of("Size").assertPositive().getDouble(1.0);
                noVelocity(particle, data);
                noBlock(particle, data);

                options = new Particle.DustTransition(color, fade, size);
                break;
            }

            // Redstone dust can be colored to any rgb value. This uses the
            // DustOptions class. Can also modify particle size.
            case "REDSTONE": {
                Color color = data.of("Color").assertExists().serializeNonStandardSerializer(new ColorSerializer());
                float size = (float) data.of("Size").assertPositive().getDouble(1.0);
                noVelocity(particle, data);
                noFade(particle, data);
                noBlock(particle, data);

                options = new Particle.DustOptions(color, size);
                break;
            }

            // mob_spells can use colors, but instead of using the extra data,
            // the color is stored in the offset vector. This means we should warn
            // the user about using offset with the mob_spell particle. Note that
            // extra=1 and count=0
            case "SPELL_MOB": {
                Color color = data.of("Color").assertExists().serializeNonStandardSerializer(new ColorSerializer());
                if (data.has("Count")) throw data.exception("Count", "'SPELL_MOB' cannot use the 'Count' argument!", "Consider using 'REDSTONE' particles instead.");
                if (data.has("Noise")) throw data.exception("Noise", "'SPELL_MOB' cannot use the 'Noise' argument!", "Consider using 'REDSTONE' particles instead.");
                noVelocity(particle, data);
                noFade(particle, data);
                noBlock(particle, data);

                count = 0;
                extra = 1.0;
                offset = new Vector(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0);
                break;
            }

            // Shows the item breaking animation (tools running out of durability,
            // for example). In all versions, this simply took a bukkit ItemStack.
            case "ITEM_CRACK":
                noVelocity(particle, data);
                noColor(particle, data);
                noFade(particle, data);

                options = data.of("Material_Data").assertExists().serializeNonStandardSerializer(new ItemSerializer());
                break;

            // In pre 1.13 versions, these 3 particle types took a MaterialData
            // argument instead of a BlockData argument.
            case "BLOCK_CRACK":
            case "BLOCK_DUST":
            case "FALLING_DUST":
                noVelocity(particle, data);
                noColor(particle, data);
                noFade(particle, data);

                if (ReflectionUtil.getMCVersion() < 13) {
                    if (data.config.isConfigurationSection(data.key + ".Material_Data")) {
                        ItemStack item = data.of("Material_Data").assertExists().serializeNonStandardSerializer(new ItemSerializer());
                        options = item.getData();
                    } else {
                        Material material = data.of("Material_Data").assertExists().getEnum(Material.class);
                        options = new MaterialData(material);
                    }

                } else {
                    if (data.config.isConfigurationSection(data.key + ".Material_Data")) {
                        ItemStack item = data.of("Material_Data").assertExists().serializeNonStandardSerializer(new ItemSerializer());
                        options = item.getType().createBlockData();

                    } else {
                        Material material = data.of("Material_Data").assertExists().getEnum(Material.class);
                        options = material.createBlockData();
                    }
                }
                break;

            // All of these particles can be directional by setting count=0.
            // This means that instead of using offset, we should use velocity.
            case "EXPLOSION_NORMAL":
            case "FIREWORKS_SPARK":
            case "WATER_BUBBLE":
            case "WATER_WAKE":
            case "CRIT":
            case "CRIT_MAGIC":
            case "SMOKE_NORMAL":
            case "SMOKE_LARGE":
            case "PORTAL":
            case "ENCHANTMENT_TABLE":
            case "FLAME":
            case "CLOUD":
            case "DRAGON_BREATH":
            case "END_ROD":
            case "DAMAGE_INDICATOR":
            case "TOTEM":
            case "SPIT":
            case "SQUID_INK":
            case "BUBBLE_POP":
                noBlock(particle, data);
                noColor(particle, data);
                noFade(particle, data);

                if (data.has("Velocity") && data.has("Noise"))
                    throw data.exception(null, "Cannot use both 'Velocity' and 'Noise' at the same time!");

                // When the user doesn't define a count, we will define it for
                // them. 0 for velocity, 1 for offset.
                if (count == -1) {
                    if (data.has("Velocity"))
                        count = 0;
                    else
                        count = 1;
                }

                // When the user defines
                if (count == 0 && data.has("Noise"))
                    throw data.exception("Noise", "Cannot use 'Noise' when 'Count=0'. Count must be 1 or higher!");
                if (count != 0 && data.has("Velocity"))
                    throw data.exception("Velocity", "Cannot use 'Velocity' when 'Count\u22600'. Count must be 0!");

                if (count == 0) {
                    data.of("Velocity").assertExists();
                    String raw = data.config.getString("Velocity");
                    assert raw != null;

                    // TODO allow projectile motion inheritance!
                    String match = StringUtil.match("\\d+[~ -]\\d+[~ -]\\d+", raw);
                    if (match != null) {
                        String[] split = StringUtil.split(match);
                        double x = Double.parseDouble(split[0]); // these should be safe since we checked regex
                        double y = Double.parseDouble(split[1]);
                        double z = Double.parseDouble(split[2]);

                        offset = new Vector(x, y, z);
                    }
                } else {
                    String raw = data.config.getString("Velocity");
                    if (raw == null)
                        break;

                    String match = StringUtil.match("\\d+[~ -]\\d+[~ -]\\d+", raw);
                    if (match != null) {
                        String[] split = StringUtil.split(match);
                        double x = Double.parseDouble(split[0]); // these should be safe since we checked regex
                        double y = Double.parseDouble(split[1]);
                        double z = Double.parseDouble(split[2]);

                        offset = new Vector(x, y, z);
                    }
                }
                break;


            // Now that we have run out of special cases, lets make sure the user
            // did not try to add any extra data.
            default:
                noVelocity(particle, data);
                noBlock(particle, data);
                noColor(particle, data);
                noFade(particle, data);
                break;
        }

        // When the user didn't specify count and the plugin couldn't
        // automatically determine a count, we should set it to 1.
        if (count == -1)
            count = 1;

        return new ParticleSerializer(particle, count, extra, offset, options);
    }

    private void noVelocity(Particle particle, SerializeData data) throws SerializerException {
        if (data.has("Velocity")) {
            throw data.exception("Velocity", "'" + particle + "' cannot use the 'Velocity' argument.",
                    "Only 'EXPLOSION_NORMAL', 'FIREWORKS_SPARK', 'WATER_BUBBLE', 'WATER_WAKE', 'CRIT', 'CRIT_MAGIC', 'SMOKE_NORMAL', 'SMOKE_LARGE', 'PORTAL', 'ENCHANTMENT_TABLE', 'FLAME', 'CLOUD', 'DRAGON_BREATH', 'END_ROD', 'DAMAGE_INDICATOR', 'TOTEM', 'SPIT', 'SQUID_INK', and 'BUBBLE_POP' can use the 'Velocity' argument",
                    "Note that not all of the above particles may be available in your MC version.");
        }
    }

    private void noBlock(Particle particle, SerializeData data) throws SerializerException {
        if (data.has("Material_Data")) {
            throw data.exception("Material_Data", "'" + particle + "' cannot use the 'Material_Data' argument",
                    "Only 'BLOCK_CRACK', 'ITEM_CRACK', 'BLOCK_DUST', and 'FALLING_DUST' can use 'Material_Data'");
        }
    }

    private void noColor(Particle particle, SerializeData data) throws SerializerException {
        if (data.has("Color")) {
            throw data.exception("Color", "'" + particle + "' cannot use the 'Color' argument",
                    "Only 'REDSTONE', 'SPELL_MOB', and 'DUST_COLOR_TRANSITION'(1.17+) can use 'Color'");
        }
    }

    private void noFade(Particle particle, SerializeData data) throws SerializerException {
        if (data.has("Fade_Color")) {
            throw data.exception("Fade_Color", "'" + particle + "' cannot use the 'Fade_Color' argument",
                    "Only 'DUST_COLOR_TRANSITION'(1.17+) can use 'Fade_Color'");
        }
    }
}
