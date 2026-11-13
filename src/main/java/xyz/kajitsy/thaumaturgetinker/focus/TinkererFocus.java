package xyz.kajitsy.thaumaturgetinker.focus;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "thaumaturgetinker")
public abstract class TinkererFocus extends Item {
    public TinkererFocus(String name, Rarity rarity) {
        super(ThaumicTinkererModern.itemProps(name, new Properties().stacksTo(1).rarity(rarity).component(TTDataComponents.SPELL.get(), Spell.empty())));
    }

    public TinkererFocus(String name) {
        this(name, Rarity.COMMON);
    }

    public TinkererFocus() {
        this("focus_unknown");
    }

    public abstract Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus);

    public int getFocusColor(ItemStack focus) {
        return 0xFFFFFF;
    }

    public boolean isVisCostPerTick(ItemStack focus) {
        return false;
    }

    public String getSortingHelperBase() {
        return "TT_FOCUS";
    }

    public int[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new int[]{2, 0};
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public InteractionResult onCast(ItemStack wand, Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    public void onUsingFocusTick(ItemStack wand, Player player, int count) {}

    public void onPlayerStoppedUsing(ItemStack wand, Level level, LivingEntity entity, int timeLeft) {}

    public boolean allowsPotency() {
        return false;
    }

    public static int potencyLevel(ItemStack focus) {
        if (focus.isEmpty()) return 0;
        var spell = com.leclowndu93150.thaumaturge.api.spell.Spells.spellOf(focus);
        if (spell == null || spell.isEmpty()) return 0;
        int total = 0;
        for (var node : spell.nodes()) {
            if (com.leclowndu93150.thaumaturge.api.spell.TTSpellParts.AMPLIFY.equals(node.part())) {
                total += Math.clamp(node.settings().getOrDefault("level", 1), 1, 3);
            }
        }
        return Math.min(5, total);
    }

    public static boolean hasFrugal(ItemStack focus) {
        if (focus.isEmpty()) return false;
        var spell = com.leclowndu93150.thaumaturge.api.spell.Spells.spellOf(focus);
        if (spell == null || spell.isEmpty()) return false;
        for (var node : spell.nodes()) {
            if (com.leclowndu93150.thaumaturge.api.spell.TTSpellParts.EFFICIENT.equals(node.part())) {
                return true;
            }
        }
        return false;
    }

    public static java.util.Map<net.minecraft.resources.ResourceKey<com.leclowndu93150.thaumaturge.api.aspect.IAspect>, Integer> applyFrugal(
            java.util.Map<net.minecraft.resources.ResourceKey<com.leclowndu93150.thaumaturge.api.aspect.IAspect>, Integer> baseCost, boolean frugal) {
        if (!frugal || baseCost.isEmpty()) return baseCost;
        var scaled = new java.util.LinkedHashMap<net.minecraft.resources.ResourceKey<com.leclowndu93150.thaumaturge.api.aspect.IAspect>, Integer>(baseCost.size());
        for (var entry : baseCost.entrySet()) {
            int value = entry.getValue();
            scaled.put(entry.getKey(), value <= 0 ? 0 : Math.max(1, (int) ((value * 7L + 5L) / 10L)));
        }
        return scaled;
    }

    public static boolean isAllowedSpellPart(boolean allowsPotency,
            net.minecraft.resources.ResourceKey<com.leclowndu93150.thaumaturge.api.spell.part.SpellPart> part) {
        return com.leclowndu93150.thaumaturge.api.spell.Spell.ORIGIN.equals(part)
                || com.leclowndu93150.thaumaturge.api.spell.TTSpellParts.EFFICIENT.equals(part)
                || (allowsPotency && com.leclowndu93150.thaumaturge.api.spell.TTSpellParts.AMPLIFY.equals(part));
    }

    public static boolean canEditInFocalManipulator(ItemStack stack) {
        return !(stack.getItem() instanceof TinkererFocus);
    }

    public static void onSpellInscribe(com.leclowndu93150.thaumaturge.api.spell.event.SpellInscribeEvent event) {
        if (!canEditInFocalManipulator(event.focus())) {
            event.setCanceled(true);
        }
    }

    public ItemStack resolveFocus(ItemStack wand) {
        return wand.getItem() instanceof ItemWand itemWand ? itemWand.getFocusStack(wand) : wand;
    }

    public java.util.Map<net.minecraft.resources.ResourceKey<com.leclowndu93150.thaumaturge.api.aspect.IAspect>, Integer> effectiveVisCost(ItemStack wand) {
        ItemStack focus = resolveFocus(wand);
        return applyFrugal(getVisCost(focus), hasFrugal(focus));
    }

    @Override
    public void appendHoverText(ItemStack focus, Item.TooltipContext context,
            java.util.List<net.minecraft.network.chat.Component> tooltipComponents,
            net.minecraft.world.item.TooltipFlag flag) {
        int potency = potencyLevel(focus);
        boolean frugal = hasFrugal(focus);
        if (potency > 0) {
            tooltipComponents.add(com.leclowndu93150.thaumaturge.content.spell.SpellText.partName(com.leclowndu93150.thaumaturge.api.spell.TTSpellParts.AMPLIFY)
                    .append(" " + potency)
                    .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
        }
        if (frugal) {
            tooltipComponents.add(com.leclowndu93150.thaumaturge.content.spell.SpellText.partName(com.leclowndu93150.thaumaturge.api.spell.TTSpellParts.EFFICIENT)
                    .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
        }
        var visCost = applyFrugal(getVisCost(focus), frugal);
        if (!visCost.isEmpty()) {
            var line = net.minecraft.network.chat.Component.empty();
            boolean first = true;
            for (var entry : visCost.entrySet()) {
                if (!first) line.append(", ");
                first = false;
                float vis = entry.getValue() / 100.0f;
                String amount = vis == (int) vis ? Integer.toString((int) vis) : String.format(java.util.Locale.ROOT, "%.1f", vis);
                line.append(amount + " ").append(com.leclowndu93150.thaumaturge.content.spell.SpellText.aspectName(entry.getKey()));
            }
            tooltipComponents.add(com.leclowndu93150.thaumaturge.content.spell.SpellText.visCost(line));
        }
    }

    public static float auraCostVis(ItemStack wand, Player player, Map<ResourceKey<IAspect>, Integer> cost) {
        if (cost.isEmpty()) return 0.0F;
        double totalVis = 0.0;
        boolean hasWand = wand.getItem() instanceof ItemWand;
        float playerDiscount = player == null ? 0.0F : CasterManager.getTotalVisDiscount(player);
        for (var entry : cost.entrySet()) {
            int centivis = entry.getValue();
            if (centivis <= 0) continue;
            float modifier = hasWand
                    ? WandVisHelper.getConsumptionModifier(wand, player, entry.getKey(), false)
                    : Math.max(WandEconomy.MIN_CONSUMPTION_MODIFIER, 1.0F - playerDiscount);
            totalVis += (centivis * modifier) / WandEconomy.CENTIVIS_PER_VIS;
        }
        return (float) totalVis;
    }

    public static float auraCostVis(Map<ResourceKey<IAspect>, Integer> cost, float costFactor) {
        if (cost.isEmpty()) return 0.0F;
        double totalVis = 0.0;
        float clamped = Math.max(WandEconomy.MIN_CONSUMPTION_MODIFIER, costFactor);
        for (int centivis : cost.values()) {
            if (centivis > 0) {
                totalVis += (centivis * clamped) / WandEconomy.CENTIVIS_PER_VIS;
            }
        }
        return (float) totalVis;
    }

    private static boolean hasAura(Player player, float needed, boolean notify) {
        if (needed <= 0.0F) return true;
        if (player.level().isClientSide()) return !notify;
        boolean preserved = AuraHelper.shouldPreserveAura(player.level(), player, player.blockPosition());
        float available = AuraHelper.drainVis(player.level(), player.blockPosition(), needed, true);
        if (!preserved && available >= needed - 1.0E-4F) {
            return true;
        }
        if (notify && player instanceof ServerPlayer serverPlayer && (!player.isUsingItem() || serverPlayer.tickCount % 20 == 0)) {
            TTActionBar.sendPurple(serverPlayer, "message.thaumaturge.wand.no_aura");
        }
        return false;
    }

    public boolean hasVisCost(ItemStack wand, Player player, Map<ResourceKey<IAspect>, Integer> cost) {
        if (player.isCreative()) return true;
        float needed = auraCostVis(wand, player, cost);
        return hasAura(player, needed, false);
    }

    public boolean consumeVisCost(ItemStack wand, Player player, Map<ResourceKey<IAspect>, Integer> cost) {
        if (player.isCreative()) return true;
        float needed = auraCostVis(wand, player, cost);
        if (!hasAura(player, needed, true)) return false;
        return needed <= 0.0F || AuraHelper.drainVis(player.level(), player.blockPosition(), needed, false) >= needed - 1.0E-4F;
    }

    public boolean hasVis(ItemStack wand, Player player) {
        return hasVisCost(wand, player, effectiveVisCost(wand));
    }

    public boolean consumeVis(ItemStack wand, Player player) {
        return consumeVisCost(wand, player, effectiveVisCost(wand));
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        ItemStack held = event.getItemStack();
        if (held.getItem() instanceof ItemWand wand) {
            ItemStack focus = wand.getFocusStack(held);
            if (focus.getItem() instanceof TinkererFocus ttFocus) {
                InteractionResult result = ttFocus.onCast(held, event.getLevel(), event.getEntity(), event.getHand());
                if (result.consumesAction()) {
                    event.setCancellationResult(result);
                    event.setCanceled(true);
                }
            }
        } else if (held.getItem() instanceof TinkererFocus ttFocus) {
            InteractionResult result = ttFocus.onCast(held, event.getLevel(), event.getEntity(), event.getHand());
            if (result.consumesAction()) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.isUsingItem()) return;
        ItemStack using = player.getUseItem();
        if (using.getItem() instanceof ItemWand wand) {
            ItemStack focus = wand.getFocusStack(using);
            if (focus.getItem() instanceof TinkererFocus ttFocus) {
                ttFocus.onUsingFocusTick(using, player, player.getUseItemRemainingTicks());
            }
        } else if (using.getItem() instanceof TinkererFocus ttFocus) {
            ttFocus.onUsingFocusTick(using, player, player.getUseItemRemainingTicks());
        }
    }

    @SubscribeEvent
    public static void onStopUsing(net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Stop event) {
        ItemStack item = event.getItem();
        if (item.getItem() instanceof ItemWand wand) {
            ItemStack focus = wand.getFocusStack(item);
            if (focus.getItem() instanceof TinkererFocus ttFocus) {
                ttFocus.onPlayerStoppedUsing(item, event.getEntity().level(), event.getEntity(), event.getDuration());
            }
        } else if (item.getItem() instanceof TinkererFocus ttFocus) {
            ttFocus.onPlayerStoppedUsing(item, event.getEntity().level(), event.getEntity(), event.getDuration());
        }
    }

    @SubscribeEvent
    public static void onFinishUsing(net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish event) {
        ItemStack item = event.getItem();
        if (item.getItem() instanceof ItemWand wand) {
            ItemStack focus = wand.getFocusStack(item);
            if (focus.getItem() instanceof TinkererFocus ttFocus) {
                ttFocus.onPlayerStoppedUsing(item, event.getEntity().level(), event.getEntity(), 0);
            }
        } else if (item.getItem() instanceof TinkererFocus ttFocus) {
            ttFocus.onPlayerStoppedUsing(item, event.getEntity().level(), event.getEntity(), 0);
        }
    }
}
