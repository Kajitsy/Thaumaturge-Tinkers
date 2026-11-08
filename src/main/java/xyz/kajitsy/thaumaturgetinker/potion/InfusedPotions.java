package xyz.kajitsy.thaumaturgetinker.potion;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.item.ImbuedPotionItem;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class InfusedPotions {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ThaumicTinkererModern.ID);
    public static final DeferredHolder<MobEffect, MobEffect>
        AIR = effect("air", 0xFFFF7E),
        FIRE = effect("fire", 0xFF5A00),
        EARTH = effect("earth", 0x56C000),
        WATER = effect("water", 0x3CD4FC);

    private static DeferredHolder<MobEffect, MobEffect> effect(String name, int color) {
        return EFFECTS.register(name + "_imbued", () -> new ImbuedEffect(color));
    }

    public static final DeferredItem<ImbuedPotionItem>
        POTION_AIR = item("air", TTAspects.AER, AIR, 0xFFFF7E),
        POTION_FIRE = item("fire", TTAspects.IGNIS, FIRE, 0xFF5A00),
        POTION_EARTH = item("earth", TTAspects.TERRA, EARTH, 0x56C000),
        POTION_WATER = item("water", TTAspects.AQUA, WATER, 0x3CD4FC);

    private static DeferredItem<ImbuedPotionItem> item(String name, ResourceKey<IAspect> aspect, Holder<MobEffect> effect, int color) {
        return ThaumicTinkererModern.ITEMS.register("infused_potion_" + name, () -> new ImbuedPotionItem("infused_potion_" + name, aspect, effect, color));
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
