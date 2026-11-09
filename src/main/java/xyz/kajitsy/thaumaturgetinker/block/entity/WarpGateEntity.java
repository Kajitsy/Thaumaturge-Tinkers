package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.SkyPearlData;
import xyz.kajitsy.thaumaturgetinker.block.WarpGateBlock;
import xyz.kajitsy.thaumaturgetinker.item.SkyPearl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class WarpGateEntity extends BlockEntity {
    public static BiConsumer<ResourceKey<Level>, List<Vec3>> SPARKLE_EMITTER = (dim, pts) -> {};

    public boolean locked;
    private long lastTeleportTick = Long.MIN_VALUE;

    public final SimpleContainer inventory = new SimpleContainer(10) {
        @Override public int getMaxStackSize() { return 1; }
        @Override public boolean canPlaceItem(int slot, ItemStack stack) { return stack.is(SkyPearl.ITEM.get()); }
        @Override public void setChanged() { super.setChanged(); changed(); }
    };

    public WarpGateEntity(BlockPos pos, BlockState state) {
        super(WarpGateBlock.ENTITY.get(), pos, state);
    }

    public void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean valid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 64;
    }

    public boolean teleport(ServerPlayer player, int index) {
        if (level == null || level.isClientSide() || index < 0 || index >= 10 || lastTeleportTick == level.getGameTime()) return false;
        var pearl = inventory.getItem(index);
        if (pearl.isEmpty() || !SkyPearlData.attuned(pearl)) return false;
        var targetLevel = SkyPearlData.targetLevel(player, pearl, true);
        if (!teleport(player, targetLevel, SkyPearlData.position(pearl))) return false;
        lastTeleportTick = level.getGameTime();
        return true;
    }

    /** Original proxy calls consume world RNG even when the dedicated proxy has no renderer. */
    public static void sparkle(ServerPlayer player) {
        var points = new ArrayList<Vec3>(20);
        var random = player.level().getRandom();
        for (int i = 0; i < 20; i++) {
            points.add(new Vec3((float) player.getX() + random.nextFloat() - .5f, (float) player.getY() + random.nextFloat(), (float) player.getZ() + random.nextFloat() - .5f));
        }
        SPARKLE_EMITTER.accept(player.level().dimension(), points);
    }

    public static boolean teleport(ServerPlayer player, ServerLevel targetLevel, BlockPos destination) {
        if (!(targetLevel.getBlockEntity(destination) instanceof WarpGateEntity gate)) {
            player.sendSystemMessage(Component.translatable("ttmisc.noDest"));
            return false;
        }
        if (gate.locked) {
            player.sendSystemMessage(Component.translatable("ttmisc.noTeleport"));
            return false;
        }
        var sound = com.leclowndu93150.thaumaturge.registry.TTSounds.WAND.get();
        player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 1, 1);
        sparkle(player);
        player.stopRiding();
        if (player.level() != targetLevel) {
            player.teleportTo(targetLevel, destination.getX() + .5, destination.getY() + 1.0, destination.getZ() + .5, Set.of(), player.getYRot(), player.getXRot(), false);
        } else {
            player.teleportTo(destination.getX() + .5, destination.getY() + 1.0, destination.getZ() + .5);
        }
        sparkle(player);
        targetLevel.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 1, .1f);
        return true;
    }

    public static boolean teleport(ServerPlayer player, BlockPos destination) {
        return teleport(player, player.level(), destination);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) Containers.dropContents(level, pos, inventory);
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("locked", locked);
        ContainerHelper.saveAllItems(output.child("Inventory"), inventory.getItems());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        locked = input.getBooleanOr("locked", false);
        inventory.clearContent();
        ContainerHelper.loadAllItems(input.childOrEmpty("Inventory"), inventory.getItems());
        lastTeleportTick = Long.MIN_VALUE;
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
