package xyz.kajitsy.thaumaturgetinker.focus;

import xyz.kajitsy.thaumaturgetinker.TinkererConfig;
import xyz.kajitsy.thaumaturgetinker.mixin.FlightConnectionAccessor;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class FlightFocus extends TinkererFocus {
    public FlightFocus() {
        super("focus_flight");
    }

    @Override
    public Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus) {
        return Map.of(TTAspects.AER, 15);
    }

    @Override
    public String getSortingHelperBase() {
        return "FLIGHT";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x9EF2FF;
    }

    @Override
    public InteractionResult onCast(ItemStack wand, Level level, Player player, InteractionHand hand) {
        if (!TinkererConfig.flightEnabled()) return InteractionResult.PASS;
        if (!level.isClientSide() && consumeVis(wand, player)) {
            double force = 1.0 / 1.5;
            player.setDeltaMovement(player.getLookAngle().scale(force));
            player.fallDistance = 0;
            player.hurtMarked = true;
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
                ((xyz.kajitsy.thaumaturgetinker.mixin.FlightConnectionAccessor) serverPlayer.connection).thaumaturgetinker$resetFloatingTicks(0);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.4f, 1.0f);
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 5, 0.2, 0.1, 0.2, 0.02);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
