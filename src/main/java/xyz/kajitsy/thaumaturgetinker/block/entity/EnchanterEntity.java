package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.enchanter.EnchanterProcess;
import xyz.kajitsy.thaumaturgetinker.enchanter.EnchanterStructure;


import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class EnchanterEntity extends BlockEntity implements MovableTile {
    public final EnchanterProcess process = new EnchanterProcess();
    private CompoundTag pending;
    public final SimpleContainer inventory = new SimpleContainer(2) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return slot == 1 ? (stack.getItem() instanceof ItemWand wand && !wand.usesStaffRod(stack))
                : !stack.isEmpty() && !stack.is(net.minecraft.world.item.Items.BOOK) && !stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)
                && (stack.isEnchantable() || stack.isEnchanted() || stack.isDamageableItem() || stack.has(net.minecraft.core.component.DataComponents.EQUIPPABLE) || stack.getMaxStackSize() == 1);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            super.setItem(slot, stack);
            if (slot == 0 && !process.working()) process.selection.clear();
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            var res = super.removeItem(slot, amount);
            if (slot == 0 && !process.working()) process.selection.clear();
            return res;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            EnchanterEntity.this.setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    public EnchanterEntity(BlockPos pos, BlockState state) {
        super(ThaumicTinkererModern.ENCHANTER_ENTITY.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        restore();
    }

    private void restore() {
        if (pending != null && level instanceof ServerLevel server) {
            process.load(server, inventory.getItem(0), pending);
            pending = null;
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnchanterEntity tile) {
        if (!(level instanceof ServerLevel server)) return;
        tile.restore();
        boolean wasWorking = tile.process.working();
        int before = 0;
        for (var a : TTAspects.PRIMALS) before += tile.process.collected(a);
        var pillars = wasWorking ? EnchanterStructure.find(level, pos) : java.util.List.<BlockPos>of();
        if (tile.process.tick(server, tile.inventory.getItem(0), tile.inventory.getItem(1), !wasWorking || (pillars != null && !pillars.isEmpty()))) {
            int after = 0;
            for (var a : TTAspects.PRIMALS) after += tile.process.collected(a);
            if (after > before && level.getRandom().nextBoolean() && pillars != null && !pillars.isEmpty()) {
                var cap = pillars.get(Math.min(tile.process.lastDrainIndex, pillars.size() - 1));
                if (level.getRandom().nextInt(5) == 0) {
                    level.playSound(null, new BlockPos(cap.getX(), pos.getY(), cap.getZ()), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.5f, 1.0f);
                }
            }
            if (wasWorking && !tile.process.working() && tile.inventory.getItem(0).isEnchanted()) {
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            tile.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
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
        output.store("Process", CompoundTag.CODEC, pending == null ? process.save() : pending.copy());
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        inventory.clearContent();
        ContainerHelper.loadAllItems(input.childOrEmpty("Inventory"), inventory.getItems());
        pending = input.read("Process", CompoundTag.CODEC).map(CompoundTag::copy).orElse(null);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider access) {
        return saveWithoutMetadata(access);
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
