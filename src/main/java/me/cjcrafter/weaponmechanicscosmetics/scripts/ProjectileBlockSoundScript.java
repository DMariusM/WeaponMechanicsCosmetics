package me.cjcrafter.weaponmechanicscosmetics.scripts;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.block.BlockCompatibility;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class ProjectileBlockSoundScript extends ProjectileScript<WeaponProjectile> {

    public ProjectileBlockSoundScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        super(owner, projectile);
    }

    @Override
    public void onCollide(@NotNull Block block) {
        Object data = ReflectionUtil.getMCVersion() < 13 ? new MaterialData(block.getType(), block.getData()) : block.getBlockData();
        BlockCompatibility.SoundData sound = CompatibilityAPI.getBlockCompatibility().getBlockSound(data, BlockCompatibility.SoundType.PLACE);
        World world = projectile.getWorld();
        Location location = projectile.getLocation().toLocation(world);
        world.playSound(location, sound.sound, sound.volume, sound.pitch + ThreadLocalRandom.current().nextFloat(-0.1f, 0.0f));
    }
}
