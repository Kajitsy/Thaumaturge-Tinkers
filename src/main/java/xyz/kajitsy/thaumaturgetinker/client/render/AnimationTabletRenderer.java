package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.entity.AnimationTabletEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class AnimationTabletRenderer implements BlockEntityRenderer<AnimationTabletEntity, AnimationTabletRenderer.State> {
    public static final class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public boolean leftClick;
        public float swingProgress;
        public float ticks;
        public @Nullable ItemStackRenderState item;
        public boolean isBlockItem;
    }

    private final ItemModelResolver itemModelResolver;

    public AnimationTabletRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AnimationTabletEntity tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.facing = tile.facing();
        state.leftClick = tile.leftClick;
        state.swingProgress = tile.swingProgress;
        state.ticks = tile.ticks + partialTicks;

        ItemStack stack = tile.inventory.getItem(0);
        if (stack.isEmpty()) {
            state.item = null;
            return;
        }
        ItemStackRenderState item = state.item == null ? new ItemStackRenderState() : state.item;
        itemModelResolver.updateForTopItem(item, stack.copyWithCount(1), ItemDisplayContext.GROUND, tile.getLevel(), null, 0);
        state.item = item;
        state.isBlockItem = stack.getItem() instanceof BlockItem;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int index = switch (state.facing) {
            case NORTH -> 0;
            case SOUTH -> 1;
            case WEST -> 2;
            default -> 3;
        };
        int rotation = new int[]{270, 90, 0, 180}[index];

        overlay(state, pose, collector, "atcenter", -1, 0.65f, 0.13f, 0);
        overlay(state, pose, collector, state.leftClick ? "atleft" : "atright", 1, 1, state.leftClick ? 0.13f : 0.131f, 0);
        overlay(state, pose, collector, "atfacingindent", 0, 0.5f, 0.13f, rotation + 90);

        if (state.item == null) return;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        pose.translate(0.1, 0.2 + Math.cos(System.currentTimeMillis() / 600.0) / 18, 0.5);
        float[][] shifts = {{0, 0, -1}, {-1, 0, 0}, {0, 0, 0}, {-1, 0, -1}};
        pose.translate(shifts[index][0], 0, shifts[index][2]);
        pose.scale(0.8f, 0.8f, 0.8f);
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(state.swingProgress));
        pose.translate(-0.5, 0, -0.5);
        pose.translate(-state.swingProgress / 250f, state.swingProgress / 1000f, 0);
        pose.mulPose(new org.joml.Quaternionf().rotationAxis((float) Math.toRadians(Math.cos(System.currentTimeMillis() / 400.0) * 5), new org.joml.Vector3f(1, 0, 1).normalize()));
        pose.translate(0.5, 0.55, 0);

        float scale = state.isBlockItem ? 2.5f : 1.5f;
        pose.scale(scale, scale, scale);
        state.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }

    private static void overlay(State state, PoseStack pose, SubmitNodeCollector collector, String texture, int rotation, float size, float height, float force) {
        pose.pushPose();
        pose.translate(0.5, height, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(rotation == 0 ? force : (state.ticks * rotation) % 360));
        var tex = ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "textures/misc/" + texture + ".png");
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(tex), (p, out) -> {
            float h = size / 2;
            float[][] corners = {{-h, h, 0, 1}, {h, h, 1, 1}, {h, -h, 1, 0}, {-h, -h, 0, 0}};
            for (var c : corners) {
                out.addVertex(p.pose(), c[0], 0, c[1]).setColor(255, 255, 255, 255).setUv(c[2], c[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(p, 0, 1, 0);
            }
        });
        pose.popPose();
    }
}
