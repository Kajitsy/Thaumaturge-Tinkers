package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.client.gui.AdvancedToolHud;
import xyz.kajitsy.thaumaturgetinker.world.BedrockPortal;


import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredItem;

public final class AwakenedPick extends Item implements AdvancedMiningTool {
    public static final DeferredItem<AwakenedPick> ITEM = KamiRegistries.ITEMS.register("ichor_pick_gem", AwakenedPick::new);

    public AwakenedPick() {
        super(KamiTools.properties("ichor_pick_gem").pickaxe(KamiTools.TIER, 2, -2.8f));
    }

    public static void init() {}

    @Override
    public String toolType() {
        return "pick";
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        var stack = p.getItemInHand(hand);
        if (p.isShiftKeyDown()) {
            AdvancedToolModes.change(stack);
            if (level.isClientSide()) {
                AdvancedToolHud.show(AdvancedToolModes.description("pick", stack).getString());
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(level, p, hand);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> builder, TooltipFlag flags) {
        super.appendHoverText(stack, context, display, builder, flags);
        builder.accept(AdvancedToolModes.description("pick", stack));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(Blocks.BEDROCK) ? Float.MAX_VALUE : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(Blocks.BEDROCK) || super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public void startMining(ServerPlayer p, BlockPos pos) {
        if (!AdvancedToolMining.material(p.level().getBlockState(pos), "pick")) return;
        var eye = p.getEyePosition();
        var hit = p.level().clip(new ClipContext(eye, eye.add(p.getLookAngle().scale(p.blockInteractionRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, p));
        if (hit.getType() != HitResult.Type.BLOCK) return;
        BedrockPortal.open(p, pos);
        AdvancedToolMining.harvest(p, pos, AdvancedToolModes.pick(AdvancedToolModes.get(p.getMainHandItem()), hit.getDirection()), s -> AdvancedToolMining.material(s, "pick"));
    }
}
