package xyz.kajitsy.thaumaturgetinker.item;

import java.util.List;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record StorageTooltip(
    List<Entry> entries,
    int selectedIndex,
    int occupied,
    int capacity,
    boolean showEmptyDescription
) implements TooltipComponent {
    public record Entry(ItemStack stack, String countText, int totalCount) {}

    public static int numberOfItemsToShow(int totalGroups) {
        int available = totalGroups > 12 ? 11 : 12;
        int onNonFullRow = totalGroups % 4;
        int emptySpace = onNonFullRow == 0 ? 0 : 4 - onNonFullRow;
        return Math.min(totalGroups, available - emptySpace);
    }
}

