package xyz.kajitsy.thaumaturgetinker.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public record DislocatorSnapshot(BlockState state, CompoundTag tile) {
    public static DislocatorSnapshot capture(ServerLevel level, BlockPos pos) {
        var tile = level.getBlockEntity(pos);
        return new DislocatorSnapshot(level.getBlockState(pos), tile == null ? null : tile.saveWithFullMetadata(level.registryAccess()));
    }

    public void install(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        if (tile != null) {
            var data = tile.copy();
            data.putInt("x", pos.getX());
            data.putInt("y", pos.getY());
            data.putInt("z", pos.getZ());
            var restored = BlockEntity.loadStatic(pos, state, data, level.registryAccess());
            if (restored == null) throw new IllegalStateException("Unable to restore dislocated tile at " + pos);
            level.setBlockEntity(restored);
            restored.setChanged();
        }
        level.invalidateCapabilities(pos);
        level.sendBlockUpdated(pos, state, state, 3);
    }
}
