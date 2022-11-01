/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.scripts;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.cjcrafter.weaponmechanicscosmetics.config.BlockParticleSerializer;
import me.cjcrafter.weaponmechanicscosmetics.config.BlockSoundSerializer;
import me.deecaad.core.file.*;
import me.deecaad.core.utils.Debugger;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.RayTraceResult;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Adds hit-block sounds for projectiles.
 */
public class BlockImpactScript extends ProjectileScript<WeaponProjectile> {

    private BlockSoundSerializer sound;
    private BlockParticleSerializer particles;

    public BlockImpactScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        super(owner, projectile);

        Configuration config = WeaponMechanicsCosmetics.getInstance().getConfiguration();
        Debugger debug = WeaponMechanicsCosmetics.getInstance().getDebug();
        sound = config.getObject("Block_Sounds", BlockSoundSerializer.class);
        if (sound == null) {
            debug.error("Did you delete the 'Block_Sounds' section in config.yml?",
                    "You can regenerate your config by deleting the config.yml file");
        }
        particles = config.getObject("Block_Particles", BlockParticleSerializer.class);
        if (particles == null) {
            debug.error("Did you delete the 'Block_Particles' section in config.yml?",
                    "You can regenerate your config by deleting the config.yml file");
        }
    }

    public BlockImpactScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile, BlockSoundSerializer sound, BlockParticleSerializer particles) {
        super(owner, projectile);

        this.sound = sound;
        this.particles = particles;
    }

    public BlockSoundSerializer getSound() {
        return sound;
    }

    public void setSound(BlockSoundSerializer sound) {
        this.sound = sound;
    }

    public BlockParticleSerializer getParticles() {
        return particles;
    }

    public void setParticles(BlockParticleSerializer particles) {
        this.particles = particles;
    }

    @Override
    public void onCollide(@NotNull RayTraceResult hit) {
        if (!hit.isBlock())
            return;

        BlockState state = hit.getBlock().getState();
        sound.play(projectile, state);
        particles.play(projectile, state, hit.getHitLocation(), hit.getHitFace().getDirection());
    }
}