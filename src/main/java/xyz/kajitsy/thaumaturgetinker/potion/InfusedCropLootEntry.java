package xyz.kajitsy.thaumaturgetinker.potion;

import net.minecraft.world.item.ItemStack;

public record InfusedCropLootEntry(ItemStack stack, int weight) {
    public InfusedCropLootEntry {
        stack = stack.copy();
        if (weight <= 0) throw new IllegalArgumentException("nonpositive crop loot weight");
    }

    @Override
    public ItemStack stack() {
        return stack.copy();
    }
}
