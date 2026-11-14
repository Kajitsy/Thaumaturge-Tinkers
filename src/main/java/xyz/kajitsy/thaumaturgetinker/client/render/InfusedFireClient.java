package xyz.kajitsy.thaumaturgetinker.client.render;

import com.google.common.reflect.TypeToken;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.jspecify.annotations.Nullable;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireAttachment;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireKind;
import xyz.kajitsy.thaumaturgetinker.mixin.ScreenEffectRendererAccessor;

@EventBusSubscriber(modid = ThaumicTinkererModern.ID, value = Dist.CLIENT)
public final class InfusedFireClient {
    private static final ContextKey<InfusedFireKind> RENDER_STATE_KEY =
        new ContextKey<>(Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "infused_fire"));

    private static final Map<InfusedFireKind, SpriteId> LAYER_0 = new EnumMap<>(InfusedFireKind.class);
    private static final Map<InfusedFireKind, SpriteId> LAYER_1 = new EnumMap<>(InfusedFireKind.class);

    static {
        Identifier atlas = TextureAtlas.LOCATION_BLOCKS;
        for (InfusedFireKind kind : InfusedFireKind.values()) {
            String prefix = kind.texturePrefix();
            LAYER_0.put(kind, new SpriteId(atlas, Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "blocks/" + prefix + "_layer_0")));
            LAYER_1.put(kind, new SpriteId(atlas, Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "blocks/" + prefix + "_layer_1")));
        }
    }

    public static SpriteId layer0(InfusedFireKind kind) {
        return LAYER_0.get(kind);
    }

    public static SpriteId layer1(InfusedFireKind kind) {
        return LAYER_1.get(kind);
    }

    public static @Nullable InfusedFireKind get(EntityRenderState state) {
        return state.getRenderData(RENDER_STATE_KEY);
    }

    @SubscribeEvent
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<EntityRenderer<Entity, EntityRenderState>>() {}, (entity, state) -> {
            InfusedFireKind kind = InfusedFireAttachment.get(entity);
            if (kind != null) {
                state.setRenderData(RENDER_STATE_KEY, kind);
            }
        });
    }

    @SubscribeEvent
    public static void onRenderFireOverlay(RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() != RenderBlockScreenEffectEvent.OverlayType.FIRE) return;
        InfusedFireKind kind = InfusedFireAttachment.get(event.getPlayer());
        if (kind == null) return;
        TextureAtlasSprite sprite = event.getSprites().get(layer1(kind));
        ScreenEffectRendererAccessor.thaumaturgetinker$renderFire(event.getPoseStack(), event.getBufferSource(), sprite);
        event.setCanceled(true);
    }
}
