package xyz.kajitsy.thaumaturgetinker.item;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;

public record ToolBounds(int xs, int ys, int zs, int xe, int ye, int ze) {
    public List<BlockPos> positions(BlockPos origin) {
        var list = new ArrayList<BlockPos>();
        for (int x = xs; x < xe; x++) {
            for (int y = ys; y < ye; y++) {
                for (int z = zs; z < ze; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        list.add(origin.offset(x, y, z));
                    }
                }
            }
        }
        return List.copyOf(list);
    }
}
