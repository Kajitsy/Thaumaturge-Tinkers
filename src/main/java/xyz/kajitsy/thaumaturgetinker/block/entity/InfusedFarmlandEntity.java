package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.potion.InfusedSoilData;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class InfusedFarmlandEntity extends BlockEntity {
    private final InfusedSoilData soil = new InfusedSoilData();

    public InfusedFarmlandEntity(BlockPos pos, BlockState state) {
        super(ThaumicTinkererModern.INFUSED_FARMLAND_ENTITY.get(), pos, state);
    }

    public Map<ResourceKey<IAspect>, Integer> aspects() {
        return soil.aspects();
    }

    public void fertilize(ResourceKey<IAspect> aspect) {
        if (level == null || level.isClientSide() || aspect == null) return;
        soil.fertilize(aspect, level.getRandom());
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        soil.write(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        soil.read(tag);
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
