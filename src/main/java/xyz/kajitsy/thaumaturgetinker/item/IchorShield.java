package xyz.kajitsy.thaumaturgetinker.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

public class IchorShield extends ShieldItem {
    public IchorShield() {
        this("ichor_shield");
    }

    public IchorShield(String name) {
        super(KamiTools.properties(name).stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
