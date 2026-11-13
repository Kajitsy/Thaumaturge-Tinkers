package xyz.kajitsy.thaumaturgetinker.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class IchorSpear extends Item {
    public IchorSpear() {
        this("ichor_spear");
    }

    public IchorSpear(String name) {
        super(KamiTools.properties(name).spear(
                KamiTools.TIER,
                1.25F, 1.25F, 0.35F,
                2.25F, 8.5F,
                5.0F, 5.1F,
                8.0F, 4.6F
        ));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
