package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class MobAspectItem extends Item {
    public final MobAspect aspect;
    public final int form;

    public MobAspectItem(MobAspect aspect, int form) {
        super(ThaumicTinkererModern.itemProps("mob_aspect_" + aspect.tag() + (form == 0 ? "" : form == 1 ? "_condensed" : "_infused"), new Properties().stacksTo(16)));
        this.aspect = aspect;
        this.form = form;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return form == 2;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.literal(aspect.displayName()));
    }
}
