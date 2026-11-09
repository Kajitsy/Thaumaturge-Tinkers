package xyz.kajitsy.thaumaturgetinker.focus;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
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

    public boolean consumeVis(ItemStack wand, Player player) {
        if (player.isCreative()) return true;
        if (!(wand.getItem() instanceof ItemWand)) return true;
        return WandVisHelper.payCosts(wand, player, getVisCost(wand), true, false);
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
