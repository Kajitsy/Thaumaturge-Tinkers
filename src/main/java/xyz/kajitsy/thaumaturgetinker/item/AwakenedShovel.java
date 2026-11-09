package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;

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
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredItem;

public final class AwakenedShovel extends ShovelItem implements AdvancedTool {
    public static final DeferredItem<AwakenedShovel> ITEM = KamiRegistries.ITEMS.register("ichor_shovel_gem", AwakenedShovel::new);

    public AwakenedShovel() {
        super(KamiTools.TIER, KamiTools.properties("ichor_shovel_gem").attributes(DiggerItem.createAttributes(KamiTools.TIER, 1, -3.0f)));
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
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        var result = tryToggleMode(level, p, p.getItemInHand(hand));
        return result.getResult() != InteractionResult.PASS ? result : super.use(level, p, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flags) {
        super.appendHoverText(stack, context, tooltipComponents, flags);
        tooltipComponents.add(AdvancedToolModes.description("shovel", stack));
    }

    @Override
    public void startMining(ServerPlayer p, BlockPos pos) {
        if (!AdvancedToolMining.material(p.level().getBlockState(pos), "shovel")) return;
        var hit = AdvancedTool.raycast(p);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        AdvancedToolMining.harvest(p, pos, AdvancedToolModes.shovel(AdvancedToolModes.get(p.getMainHandItem()), hit.getDirection()), s -> AdvancedToolMining.material(s, "shovel"));
    }
}
