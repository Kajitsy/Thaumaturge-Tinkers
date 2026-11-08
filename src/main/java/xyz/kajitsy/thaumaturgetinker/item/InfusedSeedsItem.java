package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFarmlandBlock;
import xyz.kajitsy.thaumaturgetinker.block.entity.InfusedGrainEntity;
import xyz.kajitsy.thaumaturgetinker.potion.InfusedSeedData;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.gameevent.GameEvent;

public final class InfusedSeedsItem extends Item {
    public InfusedSeedsItem() {
        super(ThaumicTinkererModern.itemProps("infused_seeds", new Properties().rarity(Rarity.RARE)));
    }

    public static ItemStack stack(ResourceKey<IAspect> aspect) {
        var stack = new ItemStack(ThaumicTinkererModern.INFUSED_SEEDS.get());
        new InfusedSeedData(aspect, Map.of()).write(stack);
        return stack;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        var data = InfusedSeedData.read(stack);
        if (data.mainAspect() != null) {
            builder.accept(Component.translatable("aspect." + data.mainAspect().identifier().getNamespace() + "." + data.mainAspect().identifier().getPath()));
        }
        for (var entry : data.tendencies().entrySet()) {
            var key = entry.getKey();
            var name = Component.translatable("aspect." + key.identifier().getNamespace() + "." + key.identifier().getPath());
            builder.accept(name.append(Component.literal(": " + entry.getValue())));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var above = pos.above();
        var player = context.getPlayer();
        var stack = context.getItemInHand();
        if (context.getClickedFace() != Direction.UP || player == null || !(level.getBlockState(pos).getBlock() instanceof FarmlandBlock)
                || !level.isEmptyBlock(above) || !level.mayInteract(player, pos) || !level.mayInteract(player, above)
                || !player.mayUseItemAt(pos, Direction.UP, stack) || !player.mayUseItemAt(above, Direction.UP, stack)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        var data = InfusedSeedData.read(stack);
        var previous = level.getBlockState(pos);
        if (!(previous.getBlock() instanceof InfusedFarmlandBlock) && !level.setBlockAndUpdate(pos, ThaumicTinkererModern.INFUSED_FARMLAND.get().defaultBlockState())) {
            return InteractionResult.FAIL;
        }
        if (!level.setBlockAndUpdate(above, ThaumicTinkererModern.INFUSED_GRAIN.get().defaultBlockState())) {
            if (!(previous.getBlock() instanceof InfusedFarmlandBlock)) level.setBlockAndUpdate(pos, previous);
            return InteractionResult.FAIL;
        }
        if (level.getBlockEntity(above) instanceof InfusedGrainEntity grain) {
            grain.data(data);
        }
        // Build 164 consumes seeds even in creative mode.
        stack.shrink(1);
        level.gameEvent(player, GameEvent.BLOCK_PLACE, above);
        return InteractionResult.CONSUME;
    }
}
