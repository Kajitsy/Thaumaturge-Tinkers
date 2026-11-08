package xyz.kajitsy.thaumaturgetinker.focus;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class DislocationFocus extends TinkererFocus {
    public DislocationFocus() {
        super("focus_dislocation");
    }

    @Override
    public String getSortingHelperBase() {
        return "DISLOCATION";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 16757248;
    }

    @Override
    public int[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new int[]{2};
    }

    @Override
    public Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus) {
        return cost(null);
    }

    public static Map<ResourceKey<IAspect>, Integer> cost(BlockEntity tile) {
        int order = tile == null ? 500 : tile instanceof SpawnerBlockEntity ? 10000 : 2500;
        int earth = tile == null ? 100 : tile instanceof SpawnerBlockEntity ? 5000 : 500;
        return Map.of(TTAspects.PERDITIO, order, TTAspects.ORDO, order, TTAspects.TERRA, earth);
    }

    public static CompoundTag payload(ItemStack focus) {
        var data = focus.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    @Override
    public InteractionResult onCast(ItemStack wand, Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (hit.getType() == HitResult.Type.BLOCK) {
                useAt(wand, player, hit);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static BlockState orient(BlockState state, Player player, BlockPos pos) {
        var block = state.getBlock();
        if (block instanceof ChestBlock) {
            var context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, new ItemStack(block), new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            var placement = block.getStateForPlacement(context);
            if (placement != null) return placement;
        }
        if (block instanceof AbstractFurnaceBlock || block instanceof EnderChestBlock) {
            return state.setValue(HorizontalDirectionalBlock.FACING, player.getDirection().getOpposite());
        }
        if (block instanceof DispenserBlock || block instanceof PistonBaseBlock) {
            var facing = player.getDirection().getOpposite();
            if (Math.abs(player.getX() - pos.getX()) < 2 && Math.abs(player.getZ() - pos.getZ()) < 2) {
                if (player.getEyeY() - pos.getY() > 2) facing = Direction.UP;
                else if (pos.getY() > player.getEyeY()) facing = Direction.DOWN;
            }
            state = state.setValue(DirectionalBlock.FACING, facing);
            if (state.hasProperty(PistonBaseBlock.EXTENDED)) state = state.setValue(PistonBaseBlock.EXTENDED, false);
            if (state.hasProperty(DispenserBlock.TRIGGERED)) state = state.setValue(DispenserBlock.TRIGGERED, false);
        }
        return state;
    }

    boolean useAt(ItemStack wand, Player player, BlockHitResult hit) {
        if (!(player.level() instanceof ServerLevel level)) return false;
        ItemStack focus = wand.getItem() instanceof ItemWand itemWand ? itemWand.getFocusStack(wand) : wand;
        if (!(focus.getItem() instanceof DislocationFocus)) return false;
        var data = payload(focus);
        var at = hit.getBlockPos();
        boolean placing = DislocationStorage.available(data);
        var target = placing ? at.relative(hit.getDirection()) : at;
        if (!level.isLoaded(at) || !level.isLoaded(target) || !level.mayInteract(player, target) || !player.mayUseItemAt(target, hit.getDirection(), wand)) return false;
        var interact = new PlayerInteractEvent.RightClickBlock(player, player.getOffhandItem() == wand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, at, hit);
        NeoForge.EVENT_BUS.post(interact);
        if (interact.isCanceled() || interact.getUseItem() == TriState.FALSE) return false;
        var pickupSound = level.getBlockState(target).getSoundType(level, target, player).getBreakSound();
        if (placing) {
            var snapshot = BlockSnapshot.create(level.dimension(), level, target);
            if (!DislocationStorage.place(level, target, data, () -> {
                var event = new BlockEvent.EntityPlaceEvent(snapshot, level.getBlockState(at), player);
                NeoForge.EVENT_BUS.post(event);
                return !event.isCanceled();
            }, state -> orient(state, player, target), state -> state.getBlock().setPlacedBy(level, target, state, player, wand))) return false;
        } else {
            if (!DislocationStorage.movable(level, target)) return false;
            var event = new net.neoforged.neoforge.event.level.block.BreakBlockEvent(level, target, level.getBlockState(target), player);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) return false;
            var usage = cost(level.getBlockEntity(target));
            if (!player.isCreative() && wand.getItem() instanceof ItemWand) {
                if (!WandVisHelper.payCosts(wand, player, usage, true, false)) return false;
            }
            if (!DislocationStorage.pickup(level, target, data)) return false;
        }
        focus.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        if (wand.getItem() instanceof ItemWand itemWand) {
            itemWand.setFocus(wand, focus);
        }
        for (int i = 0; i < 8; i++) {
            double x = target.getX() + level.getRandom().nextFloat();
            double y = target.getY() + level.getRandom().nextFloat() + (placing ? 0.65 : 0);
            double z = target.getZ() + level.getRandom().nextFloat();
            level.sendParticles(ParticleTypes.PORTAL, x, y, z, 3, 0.1, 0.1, 0.1, 0.05);
        }
        if (!placing) level.playSound(null, target, pickupSound, SoundSource.BLOCKS, 1, 1);
        level.playSound(null, target, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5f, 1);
        return true;
    }
}
