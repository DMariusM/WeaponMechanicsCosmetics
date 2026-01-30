/*
 * Copyright (c) 2026. All rights reserved. Distribution of this file, similar
 * files, related files, or related projects is strictly controlled.
 */

package com.cjcrafter.weaponmechanicscosmetics.config;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.github.retrooper.packetevents.protocol.component.builtin.item.ItemConsumable.Animation;

public final class ThirdPersonPose implements Serializer<ThirdPersonPose> {

    private PoseOverride defaultPose;
    private PoseOverride scopePose;
    private PoseOverride reloadPose;
    private PoseOverride firearmActionPose;

    /**
     * Default constructor for serializer
     */
    public ThirdPersonPose() {
    }

    public ThirdPersonPose(@NotNull PoseOverride defaultPose, @NotNull PoseOverride scopePose,
                           @NotNull PoseOverride reloadPose, @NotNull PoseOverride firearmActionPose) {
        this.defaultPose = defaultPose;
        this.scopePose = scopePose;
        this.reloadPose = reloadPose;
        this.firearmActionPose = firearmActionPose;
    }

    public @NotNull PoseOverride getDefaultPose() {
        return defaultPose;
    }

    public @NotNull PoseOverride getScopePose() {
        return scopePose;
    }

    public @NotNull PoseOverride getReloadPose() {
        return reloadPose;
    }

    public @NotNull PoseOverride getFirearmActionPose() {
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
        PoseOverride defaultPose = data.of("Default").serialize(PoseOverride.class).orElse(new PoseOverride());
        PoseOverride scopePose = data.of("Scope").serialize(PoseOverride.class).orElse(defaultPose);
        PoseOverride reloadPose = data.of("Reload").serialize(PoseOverride.class).orElse(defaultPose);
        PoseOverride firearmActionPose = data.of("Firearm_Action").serialize(PoseOverride.class).orElse(defaultPose);

        return new ThirdPersonPose(defaultPose, scopePose, reloadPose, firearmActionPose);
    }

    public record PoseOverride(@NotNull Animation pose,
                               @Nullable ItemStack overrideItem) implements Serializer<PoseOverride> {
        public PoseOverride() {
            this(Animation.NONE, null);
        }

        @Override
        public @NotNull PoseOverride serialize(@NotNull SerializeData data) throws SerializerException {
            // simple formatting, no item override options
            if (data.of().is(String.class)) {
                Animation pose = data.of().getEnum(Animation.class).orElse(Animation.NONE);
                return new PoseOverride(pose, null);
            } else {
                Animation pose = data.of("Pose").getEnum(Animation.class).orElse(Animation.NONE);
                ItemStack overrideItem = null;
                org.bukkit.inventory.ItemStack overrideBukkit = data.of("Override_Visual_Item").serialize(ItemSerializer.class).orElse(null);
                if (overrideBukkit != null) {
                    overrideItem = SpigotConversionUtil.fromBukkitItemStack(overrideBukkit);
                }
                return new PoseOverride(pose, overrideItem);
            }
        }

    }
}
