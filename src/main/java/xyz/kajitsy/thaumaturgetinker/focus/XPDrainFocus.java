package xyz.kajitsy.thaumaturgetinker.focus;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredItem;

public final class XPDrainFocus extends TinkererFocus {
    public static final DeferredItem<XPDrainFocus> ITEM = KamiRegistries.ITEMS.register("focus_xp_drain", XPDrainFocus::new);

    public XPDrainFocus() {
        super("focus_xp_drain", Rarity.EPIC);
    }

    private int lastGiven;

    @Override
    public Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus) {
        return Map.of();
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Override
    public String getSortingHelperBase() {
        return "XPDRAIN";
    }

    @Override
    public int[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new int[]{2};
    }

    public static int xpUse(ItemStack focus) {
        return hasFrugal(focus) ? Math.max(1, (int) ((15 * 7L + 5L) / 10L)) : 15;
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return FMLEnvironment.dist == Dist.CLIENT ? Color.HSBtoRGB((System.currentTimeMillis() / 25 % 360) / 360f, 1, 1) : 0xFFFFFF;
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int ticks) {
        if (player.level().isClientSide() || !(wand.getItem() instanceof ItemWand)) return;
        var primals = TTAspects.PRIMALS;
        ResourceKey<IAspect> selected = null;
        int capacity = WandVisHelper.getMaxVis(wand);
        for (int attempts = 0; selected == null && attempts < 7; attempts++) {
            lastGiven = lastGiven == 5 ? 0 : lastGiven + 1;
            var aspect = primals.get(lastGiven);
            if (WandVisHelper.getVis(wand, aspect) < capacity) {
                selected = aspect;
            }
        }
        int xpCost = xpUse(resolveFocus(wand));
        if (selected != null && player.totalExperience >= xpCost) {
            player.giveExperiencePoints(-xpCost);
            WandVisHelper.addRealVis(wand, selected, 500, true);
        }
    }

    @Override
    public void appendHoverText(ItemStack focus, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        super.appendHoverText(focus, context, tooltipComponents, flag);
        tooltipComponents.add(Component.translatable("ttmisc.experience").append(" x " + xpUse(focus)).withStyle(ChatFormatting.GREEN));
    }
}
