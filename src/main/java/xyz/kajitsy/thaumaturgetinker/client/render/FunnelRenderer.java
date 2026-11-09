package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.entity.FunnelEntity;

import com.mojang.blaze3d.vertex.PoseStack;
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

public final class FunnelRenderer implements BlockEntityRenderer<FunnelEntity, FunnelRenderer.State> {
    public static final class State extends BlockEntityRenderState {
        public @Nullable ItemStackRenderState jar;
    }

    private final ItemModelResolver itemModelResolver;

    public FunnelRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FunnelEntity tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        ItemStack stack = tile.inventory.getItem(0);
        if (stack.isEmpty()) {
            state.jar = null;
            return;
        }
        ItemStackRenderState item = state.jar == null ? new ItemStackRenderState() : state.jar;
        itemModelResolver.updateForTopItem(item, stack, ItemDisplayContext.FIXED, tile.getLevel(), null, 0);
        state.jar = item;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.jar == null) return;
        pose.pushPose();
        pose.translate(0.5, 0.365, 0.5);
        state.jar.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
