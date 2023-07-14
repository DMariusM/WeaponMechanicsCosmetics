package me.cjcrafter.weaponmechanicscosmetics.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import org.bukkit.entity.LivingEntity;
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

        if (showToEveryone)
            WeaponCompatibilityAPI.getWeaponCompatibility().playHurtAnimation(target);
        else
            target.playHurtAnimation(0);
    }

    @Override
    public String getKeyword() {
        return "Flinch";
    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        boolean showToEveryone = data.of("Show_To_Everyone").getBool(false);

        // ShowToEveryone is forced below 1.19.4, since Spigot only added the
        // method in 1.19.4. All previous versions must hide it.
        showToEveryone |= ReflectionUtil.getMCVersion() < 19;

        return applyParentArgs(data, new FlinchMechanic(showToEveryone));
    }
}
