package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;

/** Build 164 range is four blocks per axis. Links never load a chunk. */
public final class TransvectorEntity extends BlockEntity implements MovableTile, TransvectorLink, CamouflagedTile {
    private BlockPos target;
    private BlockState camouflage;

    @Override
    public BlockState camouflage() {
        return camouflage;
    }

    public void camouflage(BlockState state) {
        camouflage = state;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private boolean resolving;

    public TransvectorEntity(BlockPos pos, BlockState state) {
        super(ThaumicTinkererModern.TRANSVECTOR_ENTITY.get(), pos, state);
    }

    public BlockPos target() {
        return target;
    }

    public boolean inRange(BlockPos pos) {
        return !pos.equals(worldPosition)
                && Math.abs((long) pos.getX() - worldPosition.getX()) <= 4
                && Math.abs((long) pos.getY() - worldPosition.getY()) <= 4
                && Math.abs((long) pos.getZ() - worldPosition.getZ()) <= 4;
    }

    public boolean link(BlockPos pos) {
        if (level == null || !inRange(pos) || !level.isLoaded(pos)) return false;
        if (level.getBlockState(pos).isAir() || level.getBlockEntity(pos) instanceof TransvectorEntity) return false;
        target = pos.immutable();
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        level.invalidateCapabilities(worldPosition);
        com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport.refreshConnectionsAround(level, worldPosition);
        return true;
    }

    public <T, C> T forward(BlockCapability<T, C> capability, C context) {
        if (resolving || level == null || target == null || !inRange(target) || !level.isLoaded(target)) return null;
        if (level.getBlockState(target).isAir() || level.getBlockEntity(target) instanceof TransvectorEntity) return null;
        resolving = true;
        try {
            return level.getCapability(capability, target, context);
        } finally {
            resolving = false;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (target != null) tag.putLong("Target", target.asLong());
        if (camouflage != null) {
            BlockState.CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), camouflage)
                .result().ifPresent(t -> tag.put("Camouflage", t));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        target = tag.contains("Target") ? BlockPos.of(tag.getLong("Target")) : null;
        if (tag.contains("Camouflage")) {
            camouflage = BlockState.CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag.get("Camouflage"))
                .result().orElse(null);
        } else {
            camouflage = null;
        }
    }
}
