package me.deecaad.weaponmechanicscosmetics;

import me.deecaad.weaponmechanics.weapon.projectile.AProjectile;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FallingBlockScript extends ProjectileScript<AProjectile> {

    public FallingBlockScript(@NotNull Plugin owner, @NotNull AProjectile projectile) {
        super(owner, projectile);

        if (projectile.getDisguise() == null || projectile.getDisguise().getType() != EntityType.FALLING_BLOCK)
            throw new IllegalArgumentException("Tried to attach to " + projectile + " when it doesn't have falling block");


    }

    @Override
    public void onTickEnd() {

    }

    @Override
    public void onEnd() {
        super.onEnd();
    }
}
