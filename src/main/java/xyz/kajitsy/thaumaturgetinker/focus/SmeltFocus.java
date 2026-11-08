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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class SmeltFocus extends TinkererFocus {
    public SmeltFocus() {
        super("focus_smelt");
    }
    private final Map<UUID, SmeltProgress> progress = new HashMap<>();

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
        if (!(result.getItem() instanceof BlockItem block) || block.getBlock().defaultBlockState().hasBlockEntity()) {
            progress.remove(player.getUUID());
            return false;
        }
        var old = progress.get(player.getUUID());
        int ticks = 20;
        if (old != null && old.dimension() == level.dimension() && old.pos().equals(pos) && old.input() == state) {
            ticks = old.ticks() - 1;
        }
        if (ticks > 0) {
            progress.put(player.getUUID(), new SmeltProgress(level.dimension(), pos.immutable(), state, ticks));
            return false;
        }
        progress.remove(player.getUUID());
        var event = new net.neoforged.neoforge.event.level.block.BreakBlockEvent(level, pos, state, player);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return false;
        if (!consumeVis(wand, player)) return false;
        level.setBlock(pos, block.getBlock().defaultBlockState(), 3);
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6f, 1);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 25, 0.3, 0.3, 0.3, 0.02);
        }
        return true;
    }
}
