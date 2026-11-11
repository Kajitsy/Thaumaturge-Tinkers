package xyz.kajitsy.thaumaturgetinker.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireAttachment;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireKind;
import xyz.kajitsy.thaumaturgetinker.client.render.InfusedFireClient;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
    @Redirect(
        method = "renderFire",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/Material;sprite()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
        )
    )
    private static TextureAtlasSprite thaumaturgetinker$useInfusedFireOverlaySprite(
        Material material,
        Minecraft minecraft,
        PoseStack poseStack
    ) {
        if (minecraft.player != null) {
            InfusedFireKind kind = InfusedFireAttachment.get(minecraft.player);
            if (kind != null) {
                return InfusedFireClient.layer1(kind).sprite();
            }
        }
        return material.sprite();
    }
}
