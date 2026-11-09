package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.item.BlockTalisman;
import xyz.kajitsy.thaumaturgetinker.item.BloodSwordItem;
import xyz.kajitsy.thaumaturgetinker.item.CleansingTalismanItem;
import xyz.kajitsy.thaumaturgetinker.item.XPTalismanItem;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class ActiveProperty implements ClampedItemPropertyFunction {
    public static final ActiveProperty INSTANCE = new ActiveProperty();

    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        if (stack.getItem() instanceof CleansingTalismanItem) return CleansingTalismanItem.enabled(stack) ? 1.0F : 0.0F;
        if (stack.getItem() instanceof XPTalismanItem) return XPTalismanItem.enabled(stack) ? 1.0F : 0.0F;
        if (stack.getItem() instanceof BlockTalisman) return BlockTalisman.active(stack) ? 1.0F : 0.0F;
        if (stack.getItem() instanceof BloodSwordItem) return BloodSwordItem.active(stack) ? 1.0F : 0.0F;
        return 0.0F;
    }
}
