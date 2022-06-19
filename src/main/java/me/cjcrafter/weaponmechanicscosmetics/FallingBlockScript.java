package me.cjcrafter.weaponmechanicscosmetics;

import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FallingBlockScript extends ProjectileScript<AProjectile> {

    private MaterialData materialData;
    private BlockData data;

    public FallingBlockScript(@NotNull Plugin owner, @NotNull AProjectile projectile) {
        super(owner, projectile);

        if (projectile.getDisguise() == null || projectile.getDisguise().getType() != EntityType.FALLING_BLOCK)
            throw new IllegalArgumentException("Tried to attach to " + projectile + " when it doesn't have falling block");

        if (ReflectionUtil.getMCVersion() < 13) {
            materialData = null;
        } else {
            data = null;
        }
    }

    public MaterialData getMaterialData() {
        return materialData;
    }

    public void setMaterialData(MaterialData materialData) {
        this.materialData = materialData;
    }

    public BlockData getData() {
        return data;
    }

    public void setData(BlockData data) {
        this.data = data;
    }

    @Override
    public void onTickEnd() {
        World world = projectile.getWorld();
        Location location = projectile.getLocation().toLocation(world);
        world.spawnParticle(Particle.FALLING_DUST, location, 10, 0.5, 0.5, 0.5, ReflectionUtil.getMCVersion() < 13 ? materialData : data);
    }

    @Override
    public void onEnd() {
        World world = projectile.getWorld();
        Location location = projectile.getLocation().toLocation(world);
        world.spawnParticle(Particle.BLOCK_CRACK, location, 1, 0.3, 0.3, 0.3, ReflectionUtil.getMCVersion() < 13 ? materialData : data);
    }
}
