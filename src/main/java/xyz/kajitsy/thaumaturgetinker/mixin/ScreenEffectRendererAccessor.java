package xyz.kajitsy.thaumaturgetinker.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScreenEffectRenderer.class)
public interface ScreenEffectRendererAccessor {
    @Invoker("renderFire")
    static void thaumaturgetinker$renderFire(PoseStack poseStack, MultiBufferSource bufferSource, TextureAtlasSprite sprite) {
        throw new AssertionError();
    }
}
