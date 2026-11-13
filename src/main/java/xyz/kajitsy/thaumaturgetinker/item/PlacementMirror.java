package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.registries.DeferredItem;

public final class PlacementMirror extends Item {
    public static final DeferredItem<PlacementMirror> ITEM = KamiRegistries.ITEMS.register("placement_mirror", PlacementMirror::new);

    public PlacementMirror() {
        super(ThaumicTinkererModern.itemProps("placement_mirror", new Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }

    public static int size(ItemStack s) {
        var tag = BlockTalisman.data(s);
        int n = tag.contains("size") ? tag.getInt("size") : 3;
        return n >= 3 && n <= 11 ? n | 1 : 3;
    }

    public static void bind(ItemStack s, BlockState state) {
        CustomData.update(DataComponents.CUSTOM_DATA, s, t -> {
            t.putString("blockName", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
            t.put("blockState", NbtUtils.writeBlockState(state));
        });
    }

    /** Original mirror builds upward on walls; looking almost straight down changes it to a floor square. */
    public static List<BlockPos> positions(BlockPos hit, Direction side, int size, float yaw, float pitch) {
        int range = ((size | 1) ^ 1) / 2, rotation = Mth.floor(yaw * 4f / 360f + .5) & 3;
        boolean horizontal = side.getAxis() == Direction.Axis.Y, down = pitch > 75f;
        int x = side.getAxis() == Direction.Axis.X ? 0 : horizontal ? (down || (rotation & 1) == 0 ? range : 0) : range;
        int y = horizontal ? (down ? 0 : range) : range;
        int z = side.getAxis() == Direction.Axis.Z ? 0 : horizontal ? (down || (rotation & 1) == 1 ? range : 0) : range;
        var result = new ArrayList<BlockPos>();
        for (int dx = -x; dx <= x; dx++)
            for (int dy = 0; dy <= 2 * y; dy++)
                for (int dz = -z; dz <= z; dz++)
                    result.add(hit.offset(dx, dy, dz).relative(side));
        return result;
    }

    public static List<BlockPos> targets(ItemStack s, Player p) {
        var start = p.getEyePosition();
        var hit = p.level().clip(new ClipContext(start, start.add(p.getViewVector(1).scale(5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, p));
        if (hit.getType() != HitResult.Type.BLOCK) return List.of();
        var pos = hit.getBlockPos();
        if (p.level().getBlockState(pos).canBeReplaced()) pos = pos.below();
        return positions(pos, hit.getDirection(), size(s), p.getYRot(), p.getXRot()).stream()
            .filter(at -> p.level().getBlockState(at).canBeReplaced())
            .toList();
    }

    public static long available(ItemStack mirror, Player p) {
        var block = BlockTalisman.block(mirror);
        long total = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            var stack = p.getInventory().getItem(i);
            if (stack.is(block.asItem())) total += stack.getCount();
            else if (stack.is(BlockTalisman.ITEM.get()) && BlockTalisman.block(stack) == block) total += BlockTalisman.count(stack);
        }
        return total;
    }

    public static void consume(ItemStack mirror, Player p) {
        var item = BlockTalisman.block(mirror).asItem();
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            var stack = p.getInventory().getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                p.getInventory().setChanged();
                return;
            }
        }
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            var stack = p.getInventory().getItem(i);
            if (stack.is(BlockTalisman.ITEM.get()) && BlockTalisman.block(stack).asItem() == item && BlockTalisman.count(stack) > 0) {
                BlockTalisman.remove(stack, 1);
                p.getInventory().setChanged();
                return;
            }
        }
    }

    public static int place(ItemStack mirror, Player p, List<BlockPos> positions) {
        var level = p.level();
        var block = BlockTalisman.block(mirror);
        boolean creative = p.getAbilities().instabuild;
        long remaining = creative ? positions.size() : available(mirror, p);
        if (level.isClientSide() || block.asItem() == Items.AIR || remaining <= 0) return 0;
        var data = BlockTalisman.data(mirror);
        if (!data.contains("blockState")) return 0;
        var stateTag = data.getCompound("blockState");
        var state = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), stateTag);
        if (state.isAir()) return 0;
        int placed = 0;
        for (var at : positions) {
            if (!creative && remaining <= 0) break;
            if (!level.getBlockState(at).canBeReplaced() || !level.mayInteract(p, at) || !p.mayUseItemAt(at, Direction.UP, mirror)) continue;
            var snapshot = BlockSnapshot.create(level.dimension(), level, at);
            boolean outerCapture = level.captureBlockSnapshots;
            int previousSnapshots = level.capturedBlockSnapshots.size();
            boolean changed;
            level.captureBlockSnapshots = true;
            try {
                changed = level.setBlock(at, state, 3);
            } finally {
                while (level.capturedBlockSnapshots.size() > previousSnapshots) level.capturedBlockSnapshots.removeLast();
                level.captureBlockSnapshots = outerCapture;
            }
            if (!changed) continue;
            if (EventHooks.onBlockPlace(p, snapshot, Direction.UP)) {
                level.restoringBlockSnapshots = true;
                try {
                    snapshot.restore(3);
                } finally {
                    level.restoringBlockSnapshots = false;
                }
                continue;
            }
            state.onPlace(level, at, snapshot.getState(), false);
            level.markAndNotifyBlock(at, level.getChunkAt(at), snapshot.getState(), state, 3, 512);
            if (!creative) {
                consume(mirror, p);
                remaining--;
            }
            placed++;
        }
        return placed;
    }

    @Override
    public InteractionResult useOn(UseOnContext c) {
        var p = c.getPlayer();
        if (p == null) return InteractionResult.PASS;
        if (p.isShiftKeyDown()) {
            var state = c.getLevel().getBlockState(c.getClickedPos());
            if (state.isCollisionShapeFullBlock(c.getLevel(), c.getClickedPos()) && state.getRenderShape() == RenderShape.MODEL) {
                bind(c.getItemInHand(), state);
            }
        } else {
            var positions = targets(c.getItemInHand(), p);
            if (place(c.getItemInHand(), p, positions) > 0 && !c.getLevel().isClientSide()) {
                c.getLevel().playSound(null, p.blockPosition(), TTSounds.WAND.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        var s = p.getItemInHand(hand);
        if (p.isShiftKeyDown()) {
            int n = size(s);
            CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.putInt("size", n == 11 ? 3 : n + 2));
            if (level.isClientSide()) AdvancedToolModes.notifyHud(size(s) + " x " + size(s));
            p.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.3f, 0.1f);
            return InteractionResultHolder.sidedSuccess(s, level.isClientSide());
        }
        return InteractionResultHolder.pass(s);
    }

    @Override
    public void appendHoverText(ItemStack s, Item.TooltipContext c, List<Component> tooltipComponents, TooltipFlag f) {
        tooltipComponents.add(Component.literal(size(s) + " x " + size(s)));
        if (BlockTalisman.block(s) != net.minecraft.world.level.block.Blocks.AIR) {
            tooltipComponents.add(Component.translatable(BlockTalisman.block(s).getDescriptionId()));
        }
    }
}
