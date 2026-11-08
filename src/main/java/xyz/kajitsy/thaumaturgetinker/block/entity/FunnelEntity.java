package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.content.essentia.jar.AbstractBlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarItem;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class FunnelEntity extends BlockEntity implements MovableTile {
    public final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return stack.getItem() instanceof JarItem;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            changed();
        }
    };

    public FunnelEntity(BlockPos pos, BlockState state) {
        super(ThaumicTinkererModern.FUNNEL_ENTITY.get(), pos, state);
    }

    void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FunnelEntity tile) {
        if (level.isClientSide()) return;
        var stack = tile.inventory.getItem(0);
        if (!tile.inventory.canPlaceItem(0, stack)) return;
        EssentiaList essentia = stack.get(TTDataComponents.ESSENTIA_CONTENTS.get());
        if (essentia == null || essentia.isEmpty()) return;
        var entry = essentia.contents().entries().getFirst();
        Holder<IAspect> aspect = entry.aspect();
        int amount = entry.amount();
        if (amount <= 0) return;
        var hopper = pos.below();
        if (!level.isLoaded(hopper) || !level.getBlockState(hopper).hasProperty(HopperBlock.FACING) || !(level.getBlockEntity(hopper) instanceof HopperBlockEntity)) return;
        var target = hopper.relative(level.getBlockState(hopper).getValue(HopperBlock.FACING));
        if (!level.isLoaded(target) || !(level.getBlockEntity(target) instanceof AbstractBlockEntityJar jar)) return;
        if (jar.addEssentia(aspect, 1, Direction.UP) == 1) {
            int remaining = amount - 1;
            if (remaining <= 0) {
                stack.remove(TTDataComponents.ESSENTIA_CONTENTS.get());
            } else {
                stack.set(TTDataComponents.ESSENTIA_CONTENTS.get(), new EssentiaList(AspectList.EMPTY.add(new AspectInstance(aspect, remaining))));
            }
            tile.changed();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) net.minecraft.world.Containers.dropContents(level, pos, inventory);
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.child("Inventory"), inventory.getItems());
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        inventory.clearContent();
        ContainerHelper.loadAllItems(input.childOrEmpty("Inventory"), inventory.getItems());
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
