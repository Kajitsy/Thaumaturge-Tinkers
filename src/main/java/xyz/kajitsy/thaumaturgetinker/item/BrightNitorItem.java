package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.NitorLightBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class BrightNitorItem extends Item {
    public BrightNitorItem() {
        super(ThaumicTinkererModern.itemProps("bright_nitor", new Properties().stacksTo(1)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide()) {
            place(level, new BlockPos(net.minecraft.util.Mth.floor(entity.getX()), (int) entity.getY() + 1, net.minecraft.util.Mth.floor(entity.getZ())), false);
        }
    }

    public static void place(Level level, BlockPos pos, boolean awakened) {
        if (level.isClientSide() || !level.isLoaded(pos)) return;
        var state = level.getBlockState(pos);
        if (state.isAir() || state.is(ThaumicTinkererModern.NITOR_LIGHT.get())) {
            level.setBlock(pos, ThaumicTinkererModern.NITOR_LIGHT.get().defaultBlockState().setValue(NitorLightBlock.AWAKENED, awakened), 2);
        }
    }
}
