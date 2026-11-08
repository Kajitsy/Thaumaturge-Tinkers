package xyz.kajitsy.thaumaturgetinker.item;

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
public record ToolModeProperty() implements SelectItemModelProperty<Integer> {
    public static final MapCodec<ToolModeProperty> MAP_CODEC = MapCodec.unit(new ToolModeProperty());
    public static final SelectItemModelProperty.Type<ToolModeProperty, Integer> TYPE =
            SelectItemModelProperty.Type.create(MAP_CODEC, Codec.INT);

    @Override
    public Integer get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        return AdvancedToolModes.get(stack);
    }

    @Override
    public SelectItemModelProperty.Type<ToolModeProperty, Integer> type() {
        return TYPE;
    }

    @Override
    public Codec<Integer> valueCodec() {
        return Codec.INT;
    }
}
