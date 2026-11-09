package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.entity.RepairerEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class RepairerRenderer implements BlockEntityRenderer<RepairerEntity> {
    private final ItemRenderer itemRenderer;

    public RepairerRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RepairerEntity tile, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = tile.inventory.getItem(0);
        if (!stack.isEmpty()) {
            float ticks = tile.ticks + partialTicks;
            pose.pushPose();
            pose.translate(0.5, 0.75 + Math.sin(ticks / 10.0) * 0.05, 0.5);
            pose.scale(0.5f, 0.5f, 0.5f);
            pose.mulPose(Axis.YP.rotationDegrees(ticks * 0.75f));
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, tile.getLevel(), 0);
            pose.popPose();
        }
    }
}
