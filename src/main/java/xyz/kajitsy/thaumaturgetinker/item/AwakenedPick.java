package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.world.BedrockPortal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredItem;

public final class AwakenedPick extends PickaxeItem implements AdvancedTool {
    public static final DeferredItem<AwakenedPick> ITEM = KamiRegistries.ITEMS.register("ichor_pick_gem", AwakenedPick::new);

    public AwakenedPick() {
        super(KamiTools.TIER, KamiTools.properties("ichor_pick_gem").attributes(DiggerItem.createAttributes(KamiTools.TIER, 2, -2.8f)));
    }

    @Override
    public String toolType() {
        return "pick";
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        var result = tryToggleMode(level, p, p.getItemInHand(hand));
        return result.getResult() != InteractionResult.PASS ? result : super.use(level, p, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flags) {
        super.appendHoverText(stack, context, tooltipComponents, flags);
        tooltipComponents.add(AdvancedToolModes.description("pick", stack));
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
        var hit = AdvancedTool.raycast(p);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        BedrockPortal.open(p, pos);
        AdvancedToolMining.harvest(p, pos, AdvancedToolModes.pick(AdvancedToolModes.get(p.getMainHandItem()), hit.getDirection()), s -> AdvancedToolMining.material(s, "pick"));
    }
}
