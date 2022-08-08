package me.cjcrafter.weaponmechanicscosmetics.scripts;

import me.cjcrafter.weaponmechanicscosmetics.WeaponMechanicsCosmetics;
import me.deecaad.core.file.Configuration;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import me.deecaad.weaponmechanics.weapon.explode.BlockDamage;
import me.deecaad.weaponmechanics.weapon.projectile.ProjectileScript;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class BlockCrackScript extends ProjectileScript<WeaponProjectile> {

    private BlockDamage damage;
    private int regenDelay;

    public BlockCrackScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile) {
        super(owner, projectile);

        Configuration config = WeaponMechanics.getConfigurations();
        this.damage = config.getObject(projectile.getWeaponTitle() + ".Cosmetics.Block_Damage", BlockDamage.class);
        this.regenDelay = config.getInt(projectile.getWeaponTitle() + ".Cosmetics.Block_Damage.Ticks_Before_Regenerate", -1);
    }

    public BlockCrackScript(@NotNull Plugin owner, @NotNull WeaponProjectile projectile, BlockDamage damage, int regenDelay) {
        super(owner, projectile);

        this.damage = damage;
        this.regenDelay = regenDelay;
    }

    public BlockDamage getDamage() {
        return damage;
    }

    public void setDamage(BlockDamage damage) {
        this.damage = damage;
    }

    public int getRegenDelay() {
        return regenDelay;
    }

    public void setRegenDelay(int regenDelay) {
        this.regenDelay = regenDelay;
    }

    @Override
    public void onCollide(@NotNull Block block) {
        if (damage == null)
            return;

        BlockDamageData.DamageData data = damage.damage(block);

        // Didn't damage block
        if (data == null)
            return;

        if (regenDelay != -1) {
            if (damage.isBreakBlocks() && data.isBroken()) {
                new BukkitRunnable() {
                    public void run() {
                        data.regenerate();
                        data.remove();
                    }
                }.runTaskLater(WeaponMechanicsCosmetics.getInstance().getPlugin(), regenDelay); // 10 seconds
            }
        } else if (data.isBroken()) {
            data.remove();
        }
    }
}
