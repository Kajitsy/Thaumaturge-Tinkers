package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class ImbuedPotionItem extends PotionItem {
    public final ResourceKey<IAspect> aspect;

    public ImbuedPotionItem(String name, ResourceKey<IAspect> aspect, Holder<MobEffect> effect) {
        this(name, aspect, effect, 0);
    }

    public ImbuedPotionItem(String name, ResourceKey<IAspect> aspect, Holder<MobEffect> effect, int color) {
        super(ThaumicTinkererModern.itemProps(name, new Properties()
                .stacksTo(1)
                .component(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(color), List.of(new MobEffectInstance(effect, 3600, 0)), Optional.empty()))
                .component(DataComponents.CONSUMABLE, net.minecraft.world.item.component.Consumables.DEFAULT_DRINK)
                .usingConvertsTo(net.minecraft.world.item.Items.GLASS_BOTTLE)));
        this.aspect = aspect;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("aspect." + aspect.identifier().getNamespace() + "." + aspect.identifier().getPath()));
    }
}
