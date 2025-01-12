package me.cjcrafter.weaponmechanicscosmetics.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlinchMechanic extends Mechanic {

    private boolean showToEveryone;

    /**
     * Default constructor for serializer
     */
    public FlinchMechanic() {
    }

    public FlinchMechanic(boolean showToEveryone) {
        this.showToEveryone = showToEveryone;
    }

    @Override
    protected void use0(CastData cast) {
        LivingEntity target = cast.getTarget();
        if (target == null)
            return;

        if (showToEveryone) {
            target.playHurtAnimation(0f);
        } else if (target instanceof Player player) {
            player.sendHurtAnimation(0f);
        }
    }

    @Override
    public String getKeyword() {
        return "Flinch";
    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        boolean showToEveryone = data.of("Show_To_Everyone").getBool().orElse(false);
        return applyParentArgs(data, new FlinchMechanic(showToEveryone));
    }
}
