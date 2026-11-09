package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public final class RevealingHelmItem extends ArmorItem implements IVisDiscountGear {
    public RevealingHelmItem() {
        super(ThaumicTinkererModern.ARMOR_REVEALING, ArmorItem.Type.HELMET, ThaumicTinkererModern.itemProps("revealing_helm", new Properties().durability(500).rarity(Rarity.UNCOMMON).component(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE)));
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return 5;
    }
}
