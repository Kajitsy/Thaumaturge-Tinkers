package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.entity.RepairerEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class RepairerRenderer implements BlockEntityRenderer<RepairerEntity, RepairerRenderer.State> {
    public static final class State extends BlockEntityRenderState {
        public @Nullable ItemStackRenderState item;
        public float ticks;
    }

    private final ItemModelResolver itemModelResolver;

    public RepairerRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(RepairerEntity tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        ItemStack stack = tile.inventory.getItem(0);
        if (stack.isEmpty()) {
            state.item = null;
            return;
        }
        ItemStackRenderState item = state.item == null ? new ItemStackRenderState() : state.item;
        itemModelResolver.updateForTopItem(item, stack, ItemDisplayContext.FIXED, tile.getLevel(), null, 0);
        state.item = item;
        state.ticks = tile.ticks + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item == null) return;
        pose.pushPose();
        pose.translate(0.5, 0.75 + Math.sin(state.ticks / 10.0) * 0.05, 0.5);
        pose.scale(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(state.ticks * 0.75f));
        state.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
