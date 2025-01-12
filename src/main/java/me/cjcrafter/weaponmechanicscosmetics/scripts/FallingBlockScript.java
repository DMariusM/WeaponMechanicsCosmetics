/*
 * Copyright (c) 2022 CJCrafter <collinjbarber@gmail.com> - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited proprietary and confidential.
 */

package me.cjcrafter.weaponmechanicscosmetics.scripts;

import com.cryptomorin.xseries.particles.XParticle;
import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.SoundGroup;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Adds special effects for falling blocks spawned by an
 * {@link me.deecaad.weaponmechanics.weapon.explode.Explosion}, including
 * particles and sounds.
 */
public class FallingBlockScript extends ProjectileScript<AProjectile> {

    private BlockData data;

    public FallingBlockScript(@NotNull Plugin owner, @NotNull AProjectile projectile) {
        super(owner, projectile);

        if (projectile.getDisguise() == null || projectile.getDisguise().getType() != EntityType.FALLING_BLOCK)
            throw new IllegalArgumentException("Tried to attach to " + projectile + " when it doesn't have falling block");

        data = (BlockData) projectile.getDisguise().getData();
    }

    public BlockData getData() {
        return data;
    }

    public void setData(BlockData data) {
        this.data = data;
    }

    @Override
    public void onTickEnd() {
        Configuration config = WeaponMechanicsCosmetics.getInstance().getConfiguration();
        int amount = config.getInt("Explosion_Effects.Falling_Block_Dust.Amount");
        double spread = config.getDouble("Explosion_Effects.Falling_Block_Dust.Spread");

        if (amount != 0) {
            World world = projectile.getWorld();
            Location location = projectile.getLocation().toLocation(world);
            world.spawnParticle(Particle.FALLING_DUST, location, amount, spread, spread, spread, data);
        }
    }

    @Override
    public void onEnd() {
        Configuration config = WeaponMechanicsCosmetics.getInstance().getConfiguration();
        int amount = config.getInt("Explosion_Effects.Falling_Block_Break.Amount");
        double spread = config.getDouble("Explosion_Effects.Falling_Block_Break.Spread");
        boolean playSound = config.getBoolean("Explosion_Effects.Falling_Block_Break.Play_Break_Sound");

        World world = projectile.getWorld();
        Location location = projectile.getLocation().toLocation(world);

        if (amount != 0) {
            world.spawnParticle(XParticle.BLOCK.get(), location, amount, spread, spread, spread, data);
        }

        if (playSound) {
            SoundGroup group = data.getSoundGroup();
            world.playSound(location, group.getBreakSound(), group.getVolume(), group.getPitch());
        }
    }
}
