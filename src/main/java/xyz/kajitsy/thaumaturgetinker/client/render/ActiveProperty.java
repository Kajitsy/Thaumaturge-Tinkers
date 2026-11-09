package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.item.BlockTalisman;
import xyz.kajitsy.thaumaturgetinker.item.BloodSwordItem;
import xyz.kajitsy.thaumaturgetinker.item.CleansingTalismanItem;
import xyz.kajitsy.thaumaturgetinker.item.XPTalismanItem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ActiveProperty() implements SelectItemModelProperty<Boolean> {
    public static final MapCodec<ActiveProperty> MAP_CODEC = MapCodec.unit(new ActiveProperty());
    public static final SelectItemModelProperty.Type<ActiveProperty, Boolean> TYPE =
            SelectItemModelProperty.Type.create(MAP_CODEC, Codec.BOOL);

    @Override
    public Boolean get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        if (stack.getItem() instanceof CleansingTalismanItem) return CleansingTalismanItem.enabled(stack);
        if (stack.getItem() instanceof XPTalismanItem) return XPTalismanItem.enabled(stack);
        if (stack.getItem() instanceof BlockTalisman) return BlockTalisman.active(stack);
        if (stack.getItem() instanceof BloodSwordItem) return BloodSwordItem.active(stack);
        return false;
    }

    @Override
    public SelectItemModelProperty.Type<ActiveProperty, Boolean> type() {
        return TYPE;
    }

    @Override
    public Codec<Boolean> valueCodec() {
        return Codec.BOOL;
    }
}
