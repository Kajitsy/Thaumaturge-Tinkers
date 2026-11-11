package xyz.kajitsy.thaumaturgetinker.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireAttachment;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireKind;
import xyz.kajitsy.thaumaturgetinker.client.render.InfusedFireClient;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Redirect(
        method = "renderFlame",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/Material;sprite()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
        )
    )
    private TextureAtlasSprite thaumaturgetinker$useInfusedFireSprite(
        Material material,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        Entity entity,
        Quaternionf quaternion
    ) {
        InfusedFireKind kind = InfusedFireAttachment.get(entity);
        if (kind != null) {
            if (material.equals(ModelBakery.FIRE_0)) {
                return InfusedFireClient.layer0(kind).sprite();
            }
            if (material.equals(ModelBakery.FIRE_1)) {
                return InfusedFireClient.layer1(kind).sprite();
            }
        }
        return material.sprite();
    }
}
