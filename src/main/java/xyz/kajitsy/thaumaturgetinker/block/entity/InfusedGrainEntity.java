package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.InfusedGrainBlock;
import xyz.kajitsy.thaumaturgetinker.potion.InfusedCropProcess;
import xyz.kajitsy.thaumaturgetinker.potion.InfusedSeedData;


import java.util.ArrayList;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class InfusedGrainEntity extends BlockEntity {
    private InfusedSeedData data = new InfusedSeedData(null, Map.of());
    private static final Direction[] DIRECTIONS = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public InfusedGrainEntity(BlockPos pos, BlockState state) {
        super(ThaumicTinkererModern.INFUSED_GRAIN_ENTITY.get(), pos, state);
    }

    public InfusedSeedData data() {
        return new InfusedSeedData(data.mainAspect(), data.tendencies());
    }

    public void data(InfusedSeedData value) {
        data = new InfusedSeedData(value.mainAspect(), value.tendencies());
        setChanged();
        if (level != null && !level.isClientSide()) {
            var state = getBlockState();
            if (state.getBlock() instanceof InfusedGrainBlock) {
                level.setBlock(worldPosition, state.setValue(InfusedGrainBlock.ASPECT, InfusedGrainBlock.texture(data.mainAspect())), 2);
            }
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(ServerLevel level, BlockPos pos, BlockState state, InfusedGrainEntity crop) {
        int age = state.getValue(CropBlock.AGE);
        int next = InfusedCropProcess.grow(crop.data, age, level.getMaxLocalRawBrightness(pos.above()), level.getRandom());
        if (next != age) level.setBlock(pos, state.setValue(CropBlock.AGE, next), 2);
        var neighbors = new ArrayList<InfusedSeedData>();
        var tiles = new ArrayList<InfusedGrainEntity>();
        for (var direction : DIRECTIONS) {
            var neighborPos = pos.relative(direction);
            var tile = level.isLoaded(neighborPos) && level.getBlockEntity(neighborPos) instanceof InfusedGrainEntity c ? c : null;
            tiles.add(tile);
            neighbors.add(tile == null ? null : tile.data);
        }
        var transfer = InfusedCropProcess.transfer(crop.data, neighbors, level.getRandom());
        if (transfer != null) {
            crop.data(transfer.recipient());
            tiles.get(transfer.neighbor()).data(transfer.donor());
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        data.write(output);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        data = InfusedSeedData.read(input);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
