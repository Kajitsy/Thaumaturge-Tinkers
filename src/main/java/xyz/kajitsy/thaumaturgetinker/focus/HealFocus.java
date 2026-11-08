package xyz.kajitsy.thaumaturgetinker.focus;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class HealFocus extends TinkererFocus {
    public HealFocus() {
        super("focus_heal");
    }
    private final Map<UUID, Integer> progress = new HashMap<>();

    @Override
    public Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus) {
        return Map.of(TTAspects.TERRA, 45, TTAspects.AQUA, 45);
    }

    @Override
    public String getSortingHelperBase() {
        return "HEAL";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 16580822;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack wand, Level level, LivingEntity entity, int timeLeft) {
        progress.remove(entity.getUUID());
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        if (player.level().isClientSide() || !player.isAlive() || !player.isHurt()) return;
        int ticks = progress.getOrDefault(player.getUUID(), 0) + 1;
        progress.put(player.getUUID(), ticks);
        if (player.level() instanceof ServerLevel server) {
            var r = server.getRandom();
            double x = player.getX() + r.nextFloat() - 0.5, y = player.getY() + r.nextFloat(), z = player.getZ() + r.nextFloat() - 0.5;
            server.sendParticles(ParticleTypes.HEART, x, y, z, 1, 0, 0, 0, 0);
        }
        if (ticks >= 30) {
            progress.remove(player.getUUID());
            if (consumeVis(wand, player)) {
                player.heal(1);
                player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5f, 1);
            }
        }
    }
}
