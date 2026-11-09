package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.entity.FunnelEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class FunnelRenderer implements BlockEntityRenderer<FunnelEntity> {
    private final ItemRenderer itemRenderer;

    public FunnelRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FunnelEntity tile, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = tile.inventory.getItem(0);
        if (!stack.isEmpty()) {
            pose.pushPose();
            pose.translate(0.5, 0.365, 0.5);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, tile.getLevel(), 0);
            pose.popPose();
        }
    }
}
