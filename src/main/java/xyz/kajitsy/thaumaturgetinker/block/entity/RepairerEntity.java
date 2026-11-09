package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.RepairerBlock;


import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import javax.annotation.Nullable;

public final class RepairerEntity extends BlockEntity implements MovableTile, IEssentiaTransport {
    public int ticks;
    public boolean repairing;
    public final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return stack.isDamageableItem();
        }

        @Override
        public void setChanged() {
            super.setChanged();
            changed();
        }
    };

    public RepairerEntity(BlockPos pos, BlockState state) {
        super(ThaumicTinkererModern.REPAIRER_ENTITY.get(), pos, state);
    }

    void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public Direction orientation() {
        return getBlockState().getValue(RepairerBlock.FACING);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RepairerEntity tile) {
        tile.ticks++;
        if (level.isClientSide() || tile.ticks % 10 != 0) return;
        var stack = tile.inventory.getItem(0);
        boolean was = tile.repairing;
        tile.repairing = false;
        if (!stack.isEmpty() && stack.getDamageValue() > 0) {
            int repair = tile.drawEssentia();
            if (repair > 0) {
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - repair));
                tile.repairing = true;
                tile.changed();
            }
        }
        if (was != tile.repairing) tile.changed();
    }

    int drawEssentia() {
        var face = orientation();
        var pos = worldPosition.relative(face);
        if (level == null || !level.isLoaded(pos)) return 0;
        var opposite = face.getOpposite();
        var transport = level.getCapability(EssentiaCapabilities.TRANSPORT, pos, opposite);
        var container = level.getCapability(AspectCapabilities.CONTAINER, pos, opposite);
        if (transport == null && container == null) return 0;

        // Build 164: instrumentum repairs 8, fabrico repairs 5, ordo repairs 3.
        var aspects = java.util.List.of(TTAspects.INSTRUMENTUM, TTAspects.FABRICO, TTAspects.ORDO);
        var amounts = new int[]{8, 5, 3};
        for (int i = 0; i < aspects.size(); i++) {
            var key = aspects.get(i);
            var holder = Aspects.resolve(level, key);
            if (holder == null) continue;
            if (transport != null && transport.canOutputTo(opposite) && transport.getMinimumSuction() <= getSuctionAmount(face) && transport.takeEssentia(holder, 1, opposite) == 1) {
                return amounts[i];
            }
            if (container != null && container.holds(holder, 1) && container.drain(holder, 1)) {
                return amounts[i];
            }
        }
        return 0;
    }

    private static int repairValue(Holder<IAspect> type) {
        if (type.is(TTAspects.INSTRUMENTUM)) return 8;
        if (type.is(TTAspects.FABRICO)) return 5;
        if (type.is(TTAspects.ORDO)) return 3;
        return 0;
    }

    @Override
    public boolean isConnectable(Direction side) {
        return side == orientation();
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return side == orientation();
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> type, int count) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction side) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction side) {
        if (side != null && side != orientation()) return 0;
        var stack = inventory.getItem(0);
        return !stack.isEmpty() && stack.getDamageValue() > 0 ? 128 : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction side) {
        return null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction side) {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> type, int count, Direction side) {
        return 0;
    }

    @Override
    public int spaceFor(Holder<IAspect> type, Direction side) {
        if (side != orientation() || repairValue(type) <= 0) return 0;
        var stack = inventory.getItem(0);
        return !stack.isEmpty() && stack.getDamageValue() > 0 ? 1 : 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> type, int count, Direction side) {
        if (count <= 0 || spaceFor(type, side) <= 0) return 0;
        var stack = inventory.getItem(0);
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - repairValue(type)));
        repairing = true;
        changed();
        return 1;
    }

    public void dropContents(Level level, BlockPos pos) {
        if (level != null) Containers.dropContents(level, pos, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        var invTag = new CompoundTag();
        ContainerHelper.saveAllItems(invTag, inventory.getItems(), registries);
        tag.put("Inventory", invTag);
        tag.putBoolean("Repairing", repairing);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.clearContent();
        if (tag.contains("Inventory")) {
            ContainerHelper.loadAllItems(tag.getCompound("Inventory"), inventory.getItems(), registries);
        }
        repairing = tag.getBoolean("Repairing");
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
