package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * ItemInfusedInkwell (build 164, named "(NYI)" by the original): 800-use scribing tools with the TC4 inkwell icon and a glint,
 * accepted wherever thaumaturge takes scribing tools via #thaumaturge:scribing_tools tag;
 * hidden from the creative tab and without its own research, like the original.
 */
public final class InfusedInkwellItem extends Item {
    public InfusedInkwellItem() {
        super(ThaumicTinkererModern.itemProps("infused_inkwell", new Properties().durability(800)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
