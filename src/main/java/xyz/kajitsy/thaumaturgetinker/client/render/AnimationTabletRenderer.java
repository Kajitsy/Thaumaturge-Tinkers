package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.entity.AnimationTabletEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class AnimationTabletRenderer implements BlockEntityRenderer<AnimationTabletEntity> {
    private final ItemRenderer itemRenderer;

    public AnimationTabletRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(AnimationTabletEntity tile, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Direction facing = tile.facing();
        int index = switch (facing) {
            case NORTH -> 0;
            case SOUTH -> 1;
            case WEST -> 2;
            default -> 3;
        };
        int rotation = new int[]{270, 90, 0, 180}[index];
        float ticks = tile.ticks + partialTicks;

        renderOverlay(ticks, pose, buffers, "atcenter", -1, 0.65f, 0.13f, 0);
        renderOverlay(ticks, pose, buffers, tile.leftClick ? "atleft" : "atright", 1, 1, tile.leftClick ? 0.13f : 0.131f, 0);
        renderOverlay(ticks, pose, buffers, "atfacingindent", 0, 0.5f, 0.13f, rotation + 90);

        ItemStack stack = tile.inventory.getItem(0);
        if (stack.isEmpty()) return;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        pose.translate(0.1, 0.2 + Math.cos(System.currentTimeMillis() / 600.0) / 18, 0.5);
        float[][] shifts = {{0, 0, -1}, {-1, 0, 0}, {0, 0, 0}, {-1, 0, -1}};
        pose.translate(shifts[index][0], 0, shifts[index][2]);
        pose.scale(0.8f, 0.8f, 0.8f);
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(tile.swingProgress));
        pose.translate(-0.5, 0, -0.5);
        pose.translate(-tile.swingProgress / 250f, tile.swingProgress / 1000f, 0);
        pose.mulPose(new org.joml.Quaternionf().rotationAxis((float) Math.toRadians(Math.cos(System.currentTimeMillis() / 400.0) * 5), new org.joml.Vector3f(1, 0, 1).normalize()));
        pose.translate(0.5, 0.55, 0);

        boolean isBlockItem = stack.getItem() instanceof BlockItem;
        float scale = isBlockItem ? 2.5f : 1.5f;
        pose.scale(scale, scale, scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, pose, buffers, tile.getLevel(), 0);
        pose.popPose();
    }

    private static void renderOverlay(float ticks, PoseStack pose, MultiBufferSource buffers, String texture, int rotation, float size, float height, float force) {
        pose.pushPose();
        pose.translate(0.5, height, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(rotation == 0 ? force : (ticks * rotation) % 360));
        var tex = ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "textures/misc/" + texture + ".png");
        VertexConsumer out = buffers.getBuffer(RenderType.entityTranslucent(tex));
        PoseStack.Pose p = pose.last();
        float h = size / 2;
        float[][] corners = {{-h, h, 0, 1}, {h, h, 1, 1}, {h, -h, 1, 0}, {-h, -h, 0, 0}};
        for (var c : corners) {
            out.addVertex(p.pose(), c[0], 0, c[1]).setColor(255, 255, 255, 255).setUv(c[2], c[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(p, 0, 1, 0);
        }
        pose.popPose();
    }
}
