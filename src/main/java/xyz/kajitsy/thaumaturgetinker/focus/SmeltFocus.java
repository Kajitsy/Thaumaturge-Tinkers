package xyz.kajitsy.thaumaturgetinker.focus;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;

public final class SmeltFocus extends TinkererFocus {
    private record Progress(ResourceKey<Level> dimension, BlockPos pos, BlockState input, int ticks) {}

    public SmeltFocus() {
        super("focus_smelt");
    }

    private final Map<UUID, Progress> progress = new HashMap<>();

    @Override
    public Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus) {
        return Map.of(TTAspects.IGNIS, 45, TTAspects.PERDITIO, 12);
    }

    @Override
    public String getSortingHelperBase() {
        return "SMELT";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0xFF0000;
    }

    @Override
    public boolean allowsPotency() {
        return true;
    }

    public static int smeltDurationTicks(int potency) {
        return Math.max(5, 20 - Math.min(3, Math.max(0, potency)) * 5);
    }

    public static boolean isInPlaceBlockResult(ItemStack result) {
        return result.getItem() instanceof BlockItem block && !block.getBlock().defaultBlockState().hasBlockEntity();
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack wand, Level level, LivingEntity entity, int timeLeft) {
        progress.remove(entity.getUUID());
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        if (player.level().isClientSide()) return;
        var eye = player.getEyePosition();
        var hit = player.level().clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(10)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            progress.remove(player.getUUID());
            return;
        }
        smelt(wand, player, hit.getBlockPos());
    }

    boolean smelt(ItemStack wand, Player player, BlockPos pos) {
        if (!(player.level() instanceof ServerLevel level)) return false;
        if (!level.isLoaded(pos) || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, wand) || level.getBlockEntity(pos) != null) {
            progress.remove(player.getUUID());
            return false;
        }
        var state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0) {
            progress.remove(player.getUUID());
            return false;
        }
        var input = new ItemStack(state.getBlock());
        if (input.isEmpty()) {
            progress.remove(player.getUUID());
            return false;
        }
        var recipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
        var result = recipe.map(r -> r.value().assemble(new SingleRecipeInput(input))).orElse(ItemStack.EMPTY);
        if (result.isEmpty()) {
            if (state.is(Blocks.RAW_IRON_BLOCK)) result = new ItemStack(Items.IRON_BLOCK);
            else if (state.is(Blocks.RAW_GOLD_BLOCK)) result = new ItemStack(Items.GOLD_BLOCK);
            else if (state.is(Blocks.RAW_COPPER_BLOCK)) result = new ItemStack(Items.COPPER_BLOCK);
        }
        if (result.isEmpty()) {
            progress.remove(player.getUUID());
            return false;
        }
        var old = progress.get(player.getUUID());
        if (old == null && !hasVis(wand, player)) return false;
        int ticks = smeltDurationTicks(potencyLevel(resolveFocus(wand)));
        if (old != null && old.dimension() == level.dimension() && old.pos().equals(pos) && old.input() == state) {
            ticks = old.ticks() - 1;
        }
        if (ticks > 0) {
            progress.put(player.getUUID(), new Progress(level.dimension(), pos.immutable(), state, ticks));
            if (ticks % 4 == 0) {
                level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.35, 0.35, 0.35, 0.01);
            }
            return false;
        }
        progress.remove(player.getUUID());
        var event = new net.neoforged.neoforge.event.level.block.BreakBlockEvent(level, pos, state, player);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return false;
        if (!consumeVis(wand, player)) return false;
        if (isInPlaceBlockResult(result)) {
            level.setBlock(pos, ((BlockItem) result.getItem()).getBlock().defaultBlockState(), 3);
        } else {
            level.destroyBlock(pos, false, player);
            Block.popResource(level, pos, result.copy());
        }
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6f, 1);
        level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 25, 0.3, 0.3, 0.3, 0.02);
        return true;
    }
}
