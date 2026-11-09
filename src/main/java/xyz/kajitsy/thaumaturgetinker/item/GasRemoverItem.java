package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.GasBlock;
import xyz.kajitsy.thaumaturgetinker.block.NitorLightBlock;


import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public final class GasRemoverItem extends Item {
    public GasRemoverItem() {
        super(ThaumicTinkererModern.itemProps("gas_remover", new Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && !level.isClientSide()) {
            clear(level, player.blockPosition());
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, .2f, 1f);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static int clear(Level level, BlockPos center) {
        int count = 0;
        for (int x = -3; x < 3; x++) {
            for (int y = -3; y < 3; y++) {
                for (int z = -3; z < 3; z++) {
                    var pos = center.offset(x, y, z);
                    if (!level.isLoaded(pos)) continue;
                    var block = level.getBlockState(pos).getBlock();
                    if (block instanceof GasBlock || block instanceof NitorLightBlock) {
                        level.removeBlock(pos, false);
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
