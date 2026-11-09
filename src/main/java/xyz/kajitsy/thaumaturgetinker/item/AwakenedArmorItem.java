package xyz.kajitsy.thaumaturgetinker.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public abstract class AwakenedArmorItem extends KamiArmor {
    public AwakenedArmorItem(String name, ArmorItem.Type type) {
        super(name, type, GEM_MATERIAL);
    }

    public AwakenedArmorItem(String name, ArmorItem.Type type, Properties properties) {
        super(name, type, GEM_MATERIAL, properties);
    }

    public static boolean disabled(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("AwakenedDisabled");
    }

    public static void toggle(ItemStack stack) {
        boolean next = !disabled(stack);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putBoolean("AwakenedDisabled", next));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                toggle(stack);
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 0.1f);
                player.sendOverlayMessage(Component.translatable("ttmisc.awakenedArmor" + (disabled(stack) ? 1 : 0)));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipComponents, flag);
        if (disabled(stack)) {
            tooltipComponents.add(Component.translatable("ttmisc.awakenedArmor1"));
        }
    }
}
