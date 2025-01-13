package me.cjcrafter.weaponmechanicscosmetics.config;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.retrooper.packetevents.protocol.component.builtin.item.ItemConsumable.Animation;

public final class ThirdPersonPose implements Serializer<ThirdPersonPose> {

    private Animation defaultPose;
    private Animation scopePose;
    private Animation reloadPose;
    private Animation firearmActionPose;

    /**
     * Default constructor for serializer
     */
    public ThirdPersonPose() {
    }

    public ThirdPersonPose(@NotNull Animation defaultPose, @NotNull Animation scopePose, @NotNull Animation reloadPose, @NotNull Animation firearmActionPose) {
        this.defaultPose = defaultPose;
        this.scopePose = scopePose;
        this.reloadPose = reloadPose;
        this.firearmActionPose = firearmActionPose;
    }

    public @NotNull Animation getDefaultPose() {
        return defaultPose;
    }

    public @NotNull Animation getScopePose() {
        return scopePose;
    }

    public @NotNull Animation getReloadPose() {
        return reloadPose;
    }

    public @NotNull Animation getFirearmActionPose() {
        return firearmActionPose;
    }

    @Override
    public @NotNull List<String> getParentKeywords() {
        return List.of("Cosmetics");
    }

    @Override
    public String getKeyword() {
        return "Third_Person_Pose";
    }

    @Override
    public @NotNull ThirdPersonPose serialize(@NotNull SerializeData data) throws SerializerException {
        Animation defaultPose = data.of("Default").getEnum(Animation.class).orElse(Animation.NONE);
        Animation scopePose = data.of("Scope").getEnum(Animation.class).orElse(defaultPose);
        Animation reloadPose = data.of("Reload").getEnum(Animation.class).orElse(defaultPose);
        Animation firearmActionPose = data.of("Firearm_Action").getEnum(Animation.class).orElse(defaultPose);

        return new ThirdPersonPose(defaultPose, scopePose, reloadPose, firearmActionPose);
    }
}
