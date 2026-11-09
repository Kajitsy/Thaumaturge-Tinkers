package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class LocomotiveRelayEntity extends BlockEntity implements MovableTile {
    BlockPos partner;

    public LocomotiveRelayEntity(BlockPos pos, BlockState state) {
        super(ThaumicTinkererModern.LOCOMOTIVE_RELAY_ENTITY.get(), pos, state);
    }

    boolean verify() {
        if (partner == null || !level.isLoaded(partner)) return false;
        if (!(level.getBlockEntity(partner) instanceof LocomotiveRelayEntity relay) || !worldPosition.equals(relay.partner)) {
            partner = null;
            changed();
            return false;
        }
        return true;
    }

    void search() {
        if (partner != null) return;
        for (int i = 1; i <= 32; i++) {
            if (pair(worldPosition.offset(0, 0, i)) || pair(worldPosition.offset(0, 0, -i)) || pair(worldPosition.offset(i, 0, 0)) || pair(worldPosition.offset(-i, 0, 0))) return;
        }
    }

    boolean pair(BlockPos at) {
        if (!level.isLoaded(at)) return false;
        if (level.getBlockEntity(at) instanceof LocomotiveRelayEntity other && !other.verify()) {
            partner = at;
            other.partner = worldPosition;
            changed();
            other.changed();
            return true;
        }
        return false;
    }

    /** Original relay sparkle call, with bounded symmetric iteration fixing negative-direction loop errors. */
    private static void emitSparkles(Level level, BlockPos pos, BlockPos end) {
        if (end == null || end.getY() != pos.getY() || (end.getX() != pos.getX() && end.getZ() != pos.getZ())) return;
        int dx = Integer.signum(end.getX() - pos.getX());
        int dz = Integer.signum(end.getZ() - pos.getZ());
        int distance = Math.max(Math.abs(end.getX() - pos.getX()), Math.abs(end.getZ() - pos.getZ()));
        if (distance > 32) return;
        double phase = .05 * (level.getGameTime() % 20);
        int color = dx > 0 || dz > 0 ? 2 : 14;
        for (int i = 0; i < distance; i++) {
            level.addParticle(new SparkleParticleOptions(color, 1.0f, 0, 0.05f, 0f, 10, true),
                    pos.getX() + .5 + dx * (i + phase),
                    pos.getY() + .5,
                    pos.getZ() + .5 + dz * (i + phase),
                    0, 0, 0);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LocomotiveRelayEntity tile) {
        if (level.isClientSide()) {
            emitSparkles(level, pos, tile.partner);
            return;
        }
        if (!tile.verify()) tile.search();
        if (!tile.verify()) return;
        var end = tile.partner;
        int dx = Integer.signum(end.getX() - pos.getX()), dz = Integer.signum(end.getZ() - pos.getZ());
        int distance = Math.max(Math.abs(end.getX() - pos.getX()), Math.abs(end.getZ() - pos.getZ()));
        for (int i = 1; i < distance; i++) {
            var at = pos.offset(dx * i, 0, dz * i);
            if (level.isLoaded(at) && level.getBlockEntity(at) instanceof LocomotiveEntity train && !train.verify()) {
                train.first = pos;
                train.second = end;
                train.linked = true;
                train.direction = dx != 0 ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
                train.setChanged();
            }
        }
    }

    void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() { return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this); }
    @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) { super.saveAdditional(output); if (partner != null) output.putLong("Partner", partner.asLong()); }
    @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) { super.loadAdditional(input); partner = input.getLong("Partner").map(BlockPos::of).orElse(null); }
}
