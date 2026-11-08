package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.entity.DislocatorEntity;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class DislocatorRenderer implements BlockEntityRenderer<DislocatorEntity, CamouflageRenderState> {
    public DislocatorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public CamouflageRenderState createRenderState() {
        return new CamouflageRenderState();
    }

    @Override
    public void extractRenderState(DislocatorEntity tile, CamouflageRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.block = null;
        BlockState camo = tile.camouflage() == null ? tile.getBlockState() : tile.camouflage();
        if (tile.getLevel() instanceof ClientLevel level) {
            state.extract(level, tile.getBlockPos(), camo);
        }
    }

    @Override
    public void submit(CamouflageRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.block != null) {
            collector.submitMovingBlock(poseStack, state.block);
        }
    }
}
