package xyz.kajitsy.thaumaturgetinker.item;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Original ToolHandler extra-block harvest, delegated to native permission/drop hooks. */
public final class AdvancedToolMining {
    private AdvancedToolMining() {}

    private static final Set<ServerPlayer> BREAKING = Collections.newSetFromMap(new WeakHashMap<>());

    /** Legacy material.isToolNotRequired plus each tool's material family. */
    public static boolean material(BlockState state, String type) {
        return switch (type) {
            case "pick" -> !state.requiresCorrectToolForDrops() || state.is(BlockTags.MINEABLE_WITH_PICKAXE);
            case "shovel" -> !state.requiresCorrectToolForDrops() || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
            case "axe" -> !state.requiresCorrectToolForDrops() || state.is(BlockTags.MINEABLE_WITH_AXE);
            default -> false;
        };
    }

    public static boolean breaking(ServerPlayer player) {
        return BREAKING.contains(player);
    }

    public static List<BlockPos> targets(Player player, BlockPos origin, Direction face, String type, int mode) {
        var level = player.level();
        var originState = level.getBlockState(origin);
        if (mode == 0 || !material(originState, type)) return List.of();
        if ("axe".equals(type) && mode == 2) {
            return treePositions(level, origin);
        }
        var bounds = "shovel".equals(type) ? AdvancedToolModes.shovel(mode, face) : AdvancedToolModes.pick(mode, face);
        float referenceStrength = originState.getDestroyProgress(player, level, origin);
        var result = new ArrayList<BlockPos>();
        for (var pos : bounds.positions(origin)) {
            if (!level.isLoaded(pos)) continue;
            var state = level.getBlockState(pos);
            if (state.isAir() || !material(state, type) || !state.canHarvestBlock(level, pos, player)) continue;
            float strength = state.getDestroyProgress(player, level, pos);
            if (strength <= 0 || referenceStrength / strength > 10) continue;
            result.add(pos);
        }
        return List.copyOf(result);
    }

    public static List<BlockPos> treePositions(Level level, BlockPos origin) {
        var originState = level.getBlockState(origin);
        if (!originState.is(BlockTags.LOGS)) return List.of();
        var block = originState.getBlock();
        var visited = new HashSet<BlockPos>();
        var queue = new ArrayDeque<BlockPos>();
        visited.add(origin.immutable());
        queue.add(origin.immutable());
        while (!queue.isEmpty()) {
            var current = queue.poll();
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        var next = current.offset(x, y, z);
                        int dx = next.getX() - origin.getX(), dy = next.getY() - origin.getY(), dz = next.getZ() - origin.getZ();
                        if (Math.abs(dx) > 24 || Math.abs(dy) > 48 || Math.abs(dz) > 24 || !level.isLoaded(next) || visited.contains(next)) continue;
                        var state = level.getBlockState(next);
                        if (state.is(block) && state.is(BlockTags.LOGS) && state.getDestroySpeed(level, next) >= 0) {
                            var immutable = next.immutable();
                            visited.add(immutable);
                            queue.add(immutable);
                        }
                    }
                }
            }
        }
        visited.remove(origin);
        return List.copyOf(visited);
    }

    public static void harvest(ServerPlayer player, BlockPos origin, AdvancedToolModes.Bounds bounds, Predicate<BlockState> material) {
        if (!BREAKING.add(player)) return;
        var level = player.level();
        try {
            var reference = level.getBlockState(origin);
            float referenceStrength = reference.getDestroyProgress(player, level, origin);
            for (var pos : bounds.positions(origin)) {
                if (!level.isLoaded(pos)) continue;
                var state = level.getBlockState(pos);
                if (state.isAir() || !material.test(state) || !state.canHarvestBlock(level, pos, player)) continue;
                float strength = state.getDestroyProgress(player, level, pos);
                if (referenceStrength / strength > 10) continue;
                player.gameMode.destroyBlock(pos);
            }
            var box = new AABB(origin.getX() + bounds.xs(), origin.getY() + bounds.ys(), origin.getZ() + bounds.zs(), origin.getX() + bounds.xe(), origin.getY() + bounds.ye(), origin.getZ() + bounds.ze());
            for (var item : level.getEntitiesOfClass(ItemEntity.class, box)) item.setPos(player.getX(), player.getY() + 1, player.getZ());
        } finally {
            BREAKING.remove(player);
        }
    }

    public static BlockPos furthest(Level level, BlockPos origin, Block block) {
        var last = origin;
        double distance = 0;
        search:
        while (true) {
            for (int x = -2; x <= 2; x++) {
                for (int y = 2; y >= -2; y--) {
                    for (int z = -2; z <= 2; z++) {
                        var next = last.offset(x, y, z);
                        int dx = next.getX() - origin.getX(), dy = next.getY() - origin.getY(), dz = next.getZ() - origin.getZ();
                        if (Math.abs(dx) > 24 || Math.abs(dy) > 48 || Math.abs(dz) > 24) return last;
                        if (!level.isLoaded(next)) continue;
                        var state = level.getBlockState(next);
                        double d = (double) dx * dx + (double) dy * dy + (double) dz * dz;
                        if (state.is(block) && state.is(BlockTags.LOGS) && state.getDestroySpeed(level, next) >=0 && d > distance) {
                            distance = d;
                            last = next;
                            continue search;
                        }
                    }
                }
            }
            return last;
        }
    }

    public static void fell(ServerPlayer player, BlockPos origin) {
        if (!BREAKING.add(player)) return;
        var level = player.level();
        try {
            if (!level.getBlockState(origin).is(BlockTags.LOGS)) return;
            while (!level.getBlockState(origin).isAir()) {
                var block = level.getBlockState(origin).getBlock();
                var target = furthest(level, origin, block);
                var before = level.getBlockState(target);
                if (!player.gameMode.destroyBlock(target) || level.getBlockState(target).equals(before)) break;
                for (int x = -3; x <= 3; x++) {
                    for (int y = -3; y <= 3; y++) {
                        for (int z = -3; z <= 3; z++) {
                            var at = target.offset(x, y, z);
                            if (level.isLoaded(at)) level.scheduleTick(at, level.getBlockState(at).getBlock(), 150 + level.getRandom().nextInt(150));
                        }
                    }
                }
            }
            for (var item : level.getEntitiesOfClass(ItemEntity.class, new AABB(origin.getX() - 5, origin.getY() - 1, origin.getZ() - 5, origin.getX() + 5, origin.getY() + 64, origin.getZ() + 5))) {
                item.setPos(Vec3.atCenterOf(origin));
                item.tickCount += 20;
            }
        } finally {
            BREAKING.remove(player);
        }
    }
}
