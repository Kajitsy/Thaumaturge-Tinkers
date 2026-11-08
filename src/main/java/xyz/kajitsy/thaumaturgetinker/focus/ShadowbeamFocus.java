package xyz.kajitsy.thaumaturgetinker.focus;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.client.fx.ShadowbeamFx;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ShadowbeamFocus extends TinkererFocus {
    public static final DeferredItem<ShadowbeamFocus> ITEM = KamiRegistries.ITEMS.register("focus_shadowbeam", ShadowbeamFocus::new);

    public ShadowbeamFocus() {
        super("focus_shadowbeam", Rarity.EPIC);
    }

    public static void init(IEventBus bus) {}

    @Override
    public Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus) {
        return Map.of(TTAspects.ORDO, 25, TTAspects.PERDITIO, 25, TTAspects.AER, 15);
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Override
    public String getSortingHelperBase() {
        return "SHADOWBEAM";
    }

    @Override
    public int[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new int[0];
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 4915283;
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int ticks) {
        if (!(player.level() instanceof ServerLevel level) || !consumeVis(wand, player)) return;
        if (level.getRandom().nextInt(10) == 0) {
            level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.5f, 1.0f);
        }
        var start = ShadowbeamPath.start(player.getX(), player.getY(), player.getZ(), player.getEyeHeight(), player.getYRot(), player.getXRot());
        if (FMLEnvironment.getDist().isClient()) {
            var points = new java.util.ArrayList<Vec3>();
            ShadowbeamTrace.run(level, player, start, points::add);
            ShadowbeamFx.emit(level.dimension(), points);
        } else {
            ShadowbeamTrace.run(level, player, start);
        }
    }
}
