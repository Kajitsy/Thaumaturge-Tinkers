package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.enchanter.EnchanterProcess;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.misc.nitor.BlockNitor;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.world.level.block.Blocks;
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
            return slot == 1 ? (stack.getItem() instanceof ItemWand wand && !wand.isStaff(stack))
                : !stack.isEmpty() && !stack.is(net.minecraft.world.item.Items.BOOK) && !stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)
                && (stack.isEnchantable() || stack.isEnchanted() || stack.isDamageableItem() || net.minecraft.world.item.Equipable.get(stack) != null || stack.getMaxStackSize() == 1);
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

    /** Original six obsidian-totem pillars, each capped by nitor. Never loads chunks. */
    public static BlockPos cap(Level level, BlockPos base) {
        for (int height = 0; height <= 12; height++) {
            var pos = base.above(height);
            if (!level.isInWorldBounds(pos) || !level.isLoaded(pos)) return null;
            var state = level.getBlockState(pos);
            if (state.is(TTBlocks.OBSIDIAN_TOTEM.get()) || state.is(TTBlocks.OBSIDIAN_TOTEM_CHARGED.get()) || state.is(TTBlocks.OBSIDIAN_TILE.get()) || state.is(Blocks.OBSIDIAN)) continue;
            return height >= 2 && state.getBlock() instanceof BlockNitor ? pos : null;
        }
        return null;
    }

    public static List<BlockPos> findPillars(Level level, BlockPos center) {
        var result = new ArrayList<BlockPos>(6);
        for (int i = 0; i < 6; i++) {
            double angle = (i * 60) * Math.PI / 180;
            int x = (int) Math.round(center.getX() + Math.cos(angle) * 3);
            int z = (int) Math.round(center.getZ() + Math.sin(angle) * 3);
            var c = cap(level, new BlockPos(x, center.getY(), z));
            if (c != null) result.add(c);
        }
        if (result.size() >= 6) return List.copyOf(result.subList(0, 6));

        result.clear();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (dx == 0 && dz == 0) continue;
                var found = cap(level, center.offset(dx, 0, dz));
                if (found == null) found = cap(level, center.offset(dx, -1, dz));
                if (found == null) found = cap(level, center.offset(dx, 1, dz));
                if (found != null && !result.contains(found)) {
                    result.add(found);
                }
            }
        }
        return result.size() >= 6 ? List.copyOf(result.subList(0, 6)) : List.of();
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
        var pillars = wasWorking ? findPillars(level, pos) : List.<BlockPos>of();
        if (tile.process.tick(server, tile.inventory.getItem(0), tile.inventory.getItem(1), !wasWorking || !pillars.isEmpty())) {
            int after = 0;
            for (var a : TTAspects.PRIMALS) after += tile.process.collected(a);
            if (after > before && level.getRandom().nextBoolean() && !pillars.isEmpty()) {
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

    public void dropContents(Level level, BlockPos pos) {
        if (level != null) net.minecraft.world.Containers.dropContents(level, pos, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        var invTag = new CompoundTag();
        ContainerHelper.saveAllItems(invTag, inventory.getItems(), registries);
        tag.put("Inventory", invTag);
        tag.put("Process", pending == null ? process.save() : pending.copy());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.clearContent();
        if (tag.contains("Inventory")) {
            ContainerHelper.loadAllItems(tag.getCompound("Inventory"), inventory.getItems(), registries);
        }
        pending = tag.contains("Process") ? tag.getCompound("Process").copy() : null;
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
