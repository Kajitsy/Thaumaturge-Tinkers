package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredItem;

public final class AwakenedAxe extends AxeItem implements AdvancedTool {
    public static final DeferredItem<AwakenedAxe> ITEM = KamiRegistries.ITEMS.register("ichor_axe_gem", AwakenedAxe::new);

    public AwakenedAxe() {
        super(KamiTools.TIER, 3, -3.0f, KamiTools.properties("ichor_axe_gem"));
    }

    @Override
    public String toolType() {
        return "axe";
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
        builder.accept(AdvancedToolModes.description("axe", stack));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return Math.max(super.getDestroySpeed(stack, state), Items.SHEARS.getDestroySpeed(stack, state));
    }

    @Override
    public void startMining(ServerPlayer p, BlockPos pos) {
        if (!AdvancedToolMining.material(p.level().getBlockState(pos), "axe")) return;
        var hit = AdvancedTool.raycast(p);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        if (AdvancedToolModes.get(p.getMainHandItem()) == 2) {
            AdvancedToolMining.fell(p, pos);
            return;
        }
        AdvancedToolMining.harvest(p, pos, AdvancedToolModes.pick(AdvancedToolModes.get(p.getMainHandItem()), hit.getDirection()), s -> AdvancedToolMining.material(s, "axe"));
    }
}
