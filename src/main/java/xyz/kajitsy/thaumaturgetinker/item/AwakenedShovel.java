package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredItem;

public final class AwakenedShovel extends Item implements AdvancedTool {
    public static final DeferredItem<AwakenedShovel> ITEM = KamiRegistries.ITEMS.register("ichor_shovel_gem", AwakenedShovel::new);

    public AwakenedShovel() {
        super(KamiTools.properties("ichor_shovel_gem").shovel(KamiTools.TIER, 1, -3.0f));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public String toolType() {
        return "shovel";
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        var result = tryToggleMode(level, p, p.getItemInHand(hand));
        return result != InteractionResult.PASS ? result : super.use(level, p, hand);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> builder, TooltipFlag flags) {
        super.appendHoverText(stack, context, display, builder, flags);
        builder.accept(AdvancedToolModes.description("shovel", stack));
    }

    @Override
    public void startMining(ServerPlayer p, BlockPos pos) {
        if (!AdvancedToolMining.material(p.level().getBlockState(pos), "shovel")) return;
        var hit = AdvancedTool.raycast(p);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        AdvancedToolMining.harvest(p, pos, AdvancedToolModes.shovel(AdvancedToolModes.get(p.getMainHandItem()), hit.getDirection()), s -> AdvancedToolMining.material(s, "shovel"));
    }
}
