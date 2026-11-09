package xyz.kajitsy.thaumaturgetinker.focus;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** Build164 displacement shield: translate along motion without reversing velocity or changing owner. */
public final class DeflectFocus extends TinkererFocus {
    public DeflectFocus() {
        super("focus_deflect");
    }

    @Override
    public Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus) {
        return Map.of(TTAspects.ORDO, 8, TTAspects.AER, 4);
    }

    @Override
    public String getSortingHelperBase() {
        return "DEFLECT";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0xFFFFFF;
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Override
    public int[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new int[]{2};
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        if (player.level().isClientSide() || !consumeVis(wand, player)) return;
        protect(player);
    }

    public static void protect(Player player) {
        if (player.level().isClientSide()) return;
        var bounds = new AABB(player.position().subtract(4, 4, 4), player.position().add(3, 3, 3));
        for (var entity : player.level().getEntitiesOfClass(Projectile.class, bounds, e -> e.isAlive() && (e instanceof AbstractArrow || e instanceof ThrowableProjectile) && e.getClass() != ThrownExperienceBottle.class && e.getOwner() != player)) {
            var from = entity.position();
            var shift = entity.getDeltaMovement().normalize().scale(entity.distanceTo(player) * 2);
            if (player.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.ENCHANT, from.x, from.y, from.z, 5, 0.2, 0.2, 0.2, 0.05);
            }
            entity.setPos(from.add(shift));
            entity.hurtMarked = true;
        }
    }
}
