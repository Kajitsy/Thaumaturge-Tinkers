package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.entity.EnchanterEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class EnchanterRenderer implements BlockEntityRenderer<EnchanterEntity> {
    private final ItemRenderer itemRenderer;

    public EnchanterRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(EnchanterEntity tile, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack toolStack = tile.inventory.getItem(0);
        if (!toolStack.isEmpty()) {
            pose.pushPose();
            pose.translate(0.5, 0.76, 0.5);
            pose.mulPose(Axis.XP.rotationDegrees(90f));
            pose.mulPose(Axis.ZP.rotationDegrees(30f));
            pose.scale(0.7f, 0.7f, 0.7f);
            itemRenderer.renderStatic(toolStack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, tile.getLevel(), 0);
            pose.popPose();
        }

        ItemStack wandStack = tile.inventory.getItem(1);
        if (!wandStack.isEmpty()) {
            var level = tile.getLevel();
            float ticks = level == null ? partialTicks : (level.getGameTime() + partialTicks);
            pose.pushPose();
            double bob = Math.sin(ticks / 10.0) * 0.05;
            pose.translate(0.5, 1.25 + bob, 0.5);
            pose.mulPose(Axis.XP.rotationDegrees(90f));
            pose.scale(0.6f, 0.6f, 0.6f);
            pose.mulPose(Axis.ZP.rotationDegrees(-70f));
            pose.mulPose(Axis.XP.rotationDegrees((float) Math.cos(ticks / 5.0) * 5f));
            itemRenderer.renderStatic(wandStack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, tile.getLevel(), 0);
            pose.popPose();
        }
    }
}
