package xyz.kajitsy.thaumaturgetinker.enchanter;

import com.leclowndu93150.thaumaturge.content.misc.nitor.BlockNitor;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** Original six obsidian-totem pillars, each capped by nitor. Never loads chunks. */
public final class EnchanterStructure {
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

    public static List<BlockPos> find(Level level, BlockPos center) {
        var result = new ArrayList<BlockPos>(6);
        for (int i = 0; i < 6; i++) {
            double angle = (i * 60) * Math.PI / 180;
            int x = (int) Math.round(center.getX() + Math.cos(angle) * 3);
            int z = (int) Math.round(center.getZ() + Math.sin(angle) * 3);
            var cap = cap(level, new BlockPos(x, center.getY(), z));
            if (cap != null) {
                result.add(cap);
            }
        }
        if (result.size() == 6) return List.copyOf(result);
        if (result.size() > 6) return List.copyOf(result.subList(0, 6));

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
        if (result.size() == 6) return List.copyOf(result);
        if (result.size() > 6) return List.copyOf(result.subList(0, 6));

        var cap = (BlockPos) null;
        if (cap == null) return List.of();
        return result;
    }
}
