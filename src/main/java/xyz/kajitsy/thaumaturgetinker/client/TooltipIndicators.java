package xyz.kajitsy.thaumaturgetinker.client;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.TinkererConfig;

public final class TooltipIndicators {
    public static final Component INDICATOR = Component.literal("[TT]").withStyle(ChatFormatting.DARK_GRAY);

    private TooltipIndicators() {}

    public static void append(ItemStack stack, List<Component> tooltip) {
        append(stack, tooltip, TinkererConfig.tooltipIndicators());
    }

    public static void append(ItemStack stack, List<Component> tooltip, boolean enabled) {
        if (!enabled || stack == null || stack.isEmpty()) return;
        append(BuiltInRegistries.ITEM.getKey(stack.getItem()), tooltip, true);
    }

    public static void append(Identifier itemId, List<Component> tooltip, boolean enabled) {
        if (!enabled || itemId == null || tooltip == null) return;
        if (!ThaumicTinkererModern.ID.equals(itemId.getNamespace())) return;
        if (tooltip.stream().anyMatch(c -> "[TT]".equals(c.getString()))) return;
        tooltip.add(INDICATOR);
    }
}
