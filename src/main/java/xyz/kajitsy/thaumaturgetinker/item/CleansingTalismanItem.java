package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public final class CleansingTalismanItem extends Item {
    public CleansingTalismanItem() {
        super(ThaumicTinkererModern.itemProps("cleansing_talisman", new Properties().durability(100).rarity(Rarity.UNCOMMON)));
    }

    public static boolean enabled(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("enabled");
    }

    public static void setEnabled(ItemStack stack, boolean value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putBoolean("enabled", value));
    }

    public static void toggle(ItemStack stack) {
        setEnabled(stack, !enabled(stack));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            toggle(stack);
            if (!level.isClientSide()) {
                level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, .3f, .1f);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof LivingEntity living) onWornTick(stack, living);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable(enabled(stack) ? "ttmisc.active" : "ttmisc.inactive"));
    }

    public void onWornTick(ItemStack stack, LivingEntity entity) {
        if (entity.level().isClientSide() || !(entity instanceof Player player) || entity.tickCount % 20 != 0 || !enabled(stack)) return;
        boolean removed = false;
        int cost = 1;
        if (entity.isOnFire()) {
            entity.clearFire();
            removed = true;
        } else {
            for (var effect : List.copyOf(entity.getActiveEffects())) {
                if (effect.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) continue;
                var key = effect.getEffect().unwrapKey().orElse(null);
                if (key != null && key.location().getPath().equals("warp_ward")) continue;
                entity.removeEffect(effect.getEffect());
                removed = true;
                if (key != null && key.location().getNamespace().equals("thaumaturge")
                        && List.of("blurred_vision", "death_gaze", "infectious_vis_exhaust", "sun_scorned", "unnatural_hunger").contains(key.location().getPath())) {
                    cost = 10;
                }
                break;
            }
        }
        if (removed && !player.getAbilities().instabuild) {
            int damage = stack.getDamageValue() + cost;
            if (damage >= stack.getMaxDamage()) stack.shrink(1);
            else stack.setDamageValue(damage);
        }
    }
}
