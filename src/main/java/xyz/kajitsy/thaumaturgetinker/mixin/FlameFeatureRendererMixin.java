package xyz.kajitsy.thaumaturgetinker.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.resources.model.sprite.SpriteId;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireKind;
import xyz.kajitsy.thaumaturgetinker.client.render.InfusedFireClient;

@Mixin(FlameFeatureRenderer.class)
public class FlameFeatureRendererMixin {
    @Redirect(
        method = "renderFlame",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/sprite/AtlasManager;get(Lnet/minecraft/client/resources/model/sprite/SpriteId;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
        )
    )
    private TextureAtlasSprite thaumaturgetinker$useInfusedFireSprite(
        AtlasManager atlasManager,
        SpriteId spriteId,
        PoseStack.Pose pose,
        MultiBufferSource buffers,
        EntityRenderState state,
        Quaternionf rotation,
        AtlasManager enclosingAtlas
    ) {
        InfusedFireKind kind = InfusedFireClient.get(state);
        if (kind != null) {
            if (spriteId.equals(ModelBakery.FIRE_0)) {
                return atlasManager.get(InfusedFireClient.layer0(kind));
            }
            if (spriteId.equals(ModelBakery.FIRE_1)) {
                return atlasManager.get(InfusedFireClient.layer1(kind));
            }
        }
        return atlasManager.get(spriteId);
    }
}
