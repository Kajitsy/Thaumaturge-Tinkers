package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.entity.WarpGateEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class WarpGateRenderer implements BlockEntityRenderer<WarpGateEntity, WarpGateRenderer.State> {
    public static final class State extends BlockEntityRenderState {
        public int playerTicks;
        public boolean occluded;
    }

    public WarpGateRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WarpGateEntity gate, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(gate, state, partialTicks, cameraPosition, breakProgress);
        var player = Minecraft.getInstance().player;
        state.playerTicks = player == null ? 0 : player.tickCount;
        var level = gate.getLevel();
        var above = gate.getBlockPos().above();
        state.occluded = level != null && !level.getBlockState(above).getCollisionShape(level, above).isEmpty();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.occluded) return;
        collector.submitCustomGeometry(pose, RenderTypes.debugQuads(), (p, out) -> {
            PoseStack local = new PoseStack();
            local.last().set(p);
            draw(local, out, state.playerTicks);
        });
    }

    public static void draw(PoseStack pose, VertexConsumer out, int playerTicks) {
        pose.pushPose();
        pose.translate(0.5, 1.25, 0.5);
        for (int repeat = 5; repeat >= 0; repeat--) {
            double ticks = playerTicks - 0.75 * (5 - repeat);
            pose.pushPose();
            pose.translate(-0.03125f, 0, -0.03125f);
            for (int i = 0; i < 12; i++) {
                float deg = (int) (ticks / 0.25 % 360 + 30 * i);
                float rad = deg * (float) Math.PI / 180f;
                float rx = (float) (0.7 + 0.1 * Math.sin(ticks / 6));
                float rz = (float) (0.7 + 0.1 * Math.cos(ticks / 6));
                float x = (float) (rx * Math.cos(rad));
                float z = (float) (rz * Math.sin(rad));
                float y = (float) Math.cos((ticks + 50 * i) / 5) / 10f;
                pose.pushPose();
                pose.translate(x, y, z);
                var axis = new org.joml.Vector3f((float) Math.sin(ticks * 0.25) / 2f, (float) Math.max(0.6, Math.sin(ticks * 0.1) / 2 + 0.5), (float) Math.cos(ticks * 0.25) / 2f).normalize();
                pose.mulPose(new org.joml.Quaternionf().rotationAxis(deg * (float) Math.PI / 180f, axis));
                cube(pose.last(), out, repeat == 5 ? 1f : 0.2f);
                pose.popPose();
            }
            pose.popPose();
        }
        pose.popPose();
    }

    private static void cube(PoseStack.Pose pose, VertexConsumer out, float alpha) {
        float s = 0.0625f;
        float[][] corners = {{0, 0, 0}, {s, 0, 0}, {s, s, 0}, {0, s, 0}, {0, 0, s}, {s, 0, s}, {s, s, s}, {0, s, s}};
        int[][] faces = {{0, 3, 2, 1}, {4, 5, 6, 7}, {0, 4, 7, 3}, {1, 2, 6, 5}, {0, 1, 5, 4}, {3, 7, 6, 2}};
        for (var face : faces) {
            for (int index : face) {
                var v = corners[index];
                out.addVertex(pose, v[0], v[1], v[2]).setColor(1f, 1f, 1f, alpha);
            }
        }
    }
}
