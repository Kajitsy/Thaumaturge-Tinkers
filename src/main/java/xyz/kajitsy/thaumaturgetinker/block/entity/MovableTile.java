package xyz.kajitsy.thaumaturgetinker.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Native equivalent of build164's movable tile contract for locomotive passengers and dislocators. */
public interface MovableTile {
    default boolean prepareToMove() {
        return true;
    }

    static void install(ServerLevel level, BlockPos pos, BlockState state, CompoundTag data) {
        level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        if (data != null) {
            var copy = data.copy();
            copy.putInt("x", pos.getX());
            copy.putInt("y", pos.getY());
            copy.putInt("z", pos.getZ());
            var tile = BlockEntity.loadStatic(pos, state, copy, level.registryAccess());
            if (tile == null) throw new IllegalStateException("Cannot restore relocated tile at " + pos);
            level.setBlockEntity(tile);
            tile.setChanged();
        }
        level.invalidateCapabilities(pos);
        level.sendBlockUpdated(pos, state, state, 3);
    }
}
