package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.GasBlock;
import xyz.kajitsy.thaumaturgetinker.block.NitorLightBlock;


import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class GasItem extends Item {
    private final Supplier<GasBlock> gas;

    public GasItem(String name, Supplier<GasBlock> gas) {
        super(ThaumicTinkererModern.itemProps(name));
        this.gas = gas;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var pos = player.blockPosition().above();
        if (!player.mayUseItemAt(pos, Direction.UP, stack)) return InteractionResult.FAIL;
        if (!level.isClientSide()) {
            if (level.getBlockState(pos).isAir() || level.getBlockState(pos).getBlock() instanceof GasBlock || level.getBlockState(pos).getBlock() instanceof NitorLightBlock) {
                level.setBlock(pos, gas.get().defaultBlockState().setValue(GasBlock.POWER, 4), 2);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, .5f, .5f);
        }
        return InteractionResult.SUCCESS;
    }
}
