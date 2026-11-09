package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.MagnetBlock;
import xyz.kajitsy.thaumaturgetinker.item.SoulMouldItem;


import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class MagnetEntity extends BlockEntity implements MovableTile {
    public static class MagnetContainer extends SimpleContainer {
        public MagnetContainer() { super(1); }
        public ItemStack getStackInSlot(int slot) { return getItem(slot); }
        @Override public int getMaxStackSize() { return 1; }
        @Override public boolean canPlaceItem(int slot, ItemStack stack) { return stack.getItem() instanceof SoulMouldItem; }
    }

    public boolean adult = true;
    public final MagnetContainer inventory = new MagnetContainer() {
        @Override public void setChanged() { super.setChanged(); changed(); }
    };

    public MagnetEntity(BlockPos pos, BlockState state) {
        super(ThaumicTinkererModern.MAGNET_ENTITY.get(), pos, state);
    }

    public boolean mobs() {
        return ((MagnetBlock) getBlockState().getBlock()).mobs;
    }

    public void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    boolean matches(Entity entity) {
        if (!entity.isAlive()) return false;
        if (!mobs()) {
            return entity instanceof ItemEntity || entity instanceof AbstractArrow;
        }
        if (!(entity instanceof LivingEntity) || entity instanceof Player || entity instanceof ArmorStand) return false;
        if (entity instanceof AgeableMob ageable && adult == ageable.isBaby()) return false;
        if (entity instanceof Zombie zombie && adult == zombie.isBaby()) return false;

        var filter = inventory.getStackInSlot(0);
        if (filter.isEmpty()) return true;
        String pat = SoulMouldItem.pattern(filter);
        if (pat.isEmpty()) return true;
        return pat.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MagnetEntity tile) {
        if (level.isClientSide()) return;

        int signal = Math.max(level.getBestNeighborSignal(pos), level.getDirectSignalTo(pos));
        double range = signal > 0 ? Math.max(1.5, signal / 2.0) : 8.0;

        var center = Vec3.atCenterOf(pos);
        boolean pull = state.getValue(MagnetBlock.PULLING);
        var bounds = AABB.ofSize(center, range * 2.0, range * 2.0, range * 2.0);

        for (var entity : level.getEntities((Entity) null, bounds, tile::matches)) {
            double distSqr = entity.position().distanceToSqr(center);
            if (pull && distSqr <= 1.0) continue;

            Vec3 entityCenter = entity.position().add(0, entity.getBbHeight() / 2.0, 0);
            Vec3 delta = center.subtract(entityCenter);

            if (delta.lengthSqr() > 1.0) {
                delta = delta.normalize();
            }

            double speed = pull ? 0.35 : -0.35;
            double vx = delta.x * speed;
            double vz = delta.z * speed;
            double vy = delta.y * speed;
            if (pull && entity.onGround() && vy < 0.1) {
                vy = 0.12;
            }

            entity.setDeltaMovement(new Vec3(vx, vy, vz));
            entity.needsSync = true;
            entity.hurtMarked = true;
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        if (level != null) net.minecraft.world.Containers.dropContents(level, pos, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Adult", adult);
        var invTag = new CompoundTag();
        ContainerHelper.saveAllItems(invTag, inventory.getItems(), registries);
        tag.put("Inventory", invTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        adult = !tag.contains("Adult") || tag.getBoolean("Adult");
        inventory.clearContent();
        if (tag.contains("Inventory")) {
            ContainerHelper.loadAllItems(tag.getCompound("Inventory"), inventory.getItems(), registries);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
