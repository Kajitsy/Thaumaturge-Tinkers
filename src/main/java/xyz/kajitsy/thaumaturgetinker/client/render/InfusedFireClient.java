package xyz.kajitsy.thaumaturgetinker.client.render;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireKind;

public final class InfusedFireClient {
    private static final Map<InfusedFireKind, Material> LAYER_0 = new EnumMap<>(InfusedFireKind.class);
    private static final Map<InfusedFireKind, Material> LAYER_1 = new EnumMap<>(InfusedFireKind.class);

    static {
        ResourceLocation atlas = ModelBakery.FIRE_0.atlasLocation();
        for (InfusedFireKind kind : InfusedFireKind.values()) {
            String prefix = kind.texturePrefix();
            LAYER_0.put(kind, new Material(atlas, ResourceLocation.fromNamespaceAndPath(ThaumicTinkererModern.ID, "blocks/" + prefix + "_layer_0")));
            LAYER_1.put(kind, new Material(atlas, ResourceLocation.fromNamespaceAndPath(ThaumicTinkererModern.ID, "blocks/" + prefix + "_layer_1")));
        }
    }

    private InfusedFireClient() {}

    public static Material layer0(InfusedFireKind kind) {
        return LAYER_0.get(kind);
    }

    public static Material layer1(InfusedFireKind kind) {
        return LAYER_1.get(kind);
    }
}
