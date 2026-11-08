package xyz.kajitsy.thaumaturgetinker.focus;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.SkyPearlData;
import xyz.kajitsy.thaumaturgetinker.block.entity.WarpGateEntity;
import xyz.kajitsy.thaumaturgetinker.item.SkyPearl;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

/** Build164 recall: the first attuned hotbar pearl wins, including a wrong-dimension pearl. */
public final class RecallFocus extends TinkererFocus {
    public static final DeferredItem<RecallFocus> ITEM = KamiRegistries.ITEMS.register("focus_recall", RecallFocus::new);

    public RecallFocus() {
        super("focus_recall", Rarity.EPIC);
    }

    public static void init(IEventBus bus) {}

    @Override
    public Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus) {
        return Map.of(TTAspects.AER, 4000, TTAspects.TERRA, 4000, TTAspects.ORDO, 4000);
    }

    @Override
    public String getSortingHelperBase() {
        return "RECALL";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 10287359;
    }

    @Override
    public int[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new int[0];
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int remaining) {
        if (72000 - remaining <= 60) return;
        if (player instanceof ServerPlayer server) {
            ItemStack pearl = ItemStack.EMPTY;
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                var candidate = inv.getItem(i);
                if (candidate.is(SkyPearl.ITEM.get()) && SkyPearlData.attuned(candidate)) {
                    pearl = candidate;
                    break;
                }
            }
            if (!pearl.isEmpty()) {
                var targetDim = SkyPearlData.dimension(pearl);
                var mcServer = server.level().getServer();
                var targetLevel = mcServer != null ? mcServer.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, targetDim)) : null;
                if (targetLevel == null && targetDim.equals(player.level().dimension().identifier())) {
                    targetLevel = server.level();
                }
                if (targetLevel != null && consumeVis(wand, player)) {
                    WarpGateEntity.teleport(server, targetLevel, SkyPearlData.position(pearl));
                }
            }
        }
        player.stopUsingItem();
    }
}
