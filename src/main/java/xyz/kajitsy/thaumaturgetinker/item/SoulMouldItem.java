package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public final class SoulMouldItem extends Item {
    public SoulMouldItem() {
        super(ThaumicTinkererModern.itemProps("soul_mould", new Properties().stacksTo(1)));
    }

    public static String pattern(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getStringOr("pattern", "");
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Player) && !player.level().isClientSide()) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString("pattern", BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && !level.isClientSide()) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove("pattern"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> builder, TooltipFlag flag) {
        var name = pattern(stack);
        var id = ResourceLocation.tryParse(name);
        var type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        builder.accept(type == null ? Component.translatable("ttmisc.soulMould.nonAssigned") : Component.translatable("ttmisc.soulMould.pattern", type.getDescription()));
    }
}
