package xyz.kajitsy.thaumaturgetinker.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/** Original IAdvancedTool type contract, shared with the inventory switcher. */
public interface AdvancedTool {
    String toolType();

    default void startMining(ServerPlayer player, BlockPos pos) {}

    default InteractionResult tryToggleMode(Level level, Player player, ItemStack stack) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        AdvancedToolModes.change(stack);
        if (level.isClientSide()) {
            AdvancedToolModes.notifyHud(AdvancedToolModes.description(toolType(), stack).getString());
        }
        return InteractionResult.SUCCESS;
    }

    static BlockHitResult raycast(Player player) {
        var eye = player.getEyePosition();
        return player.level().clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(player.blockInteractionRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
    }
}
