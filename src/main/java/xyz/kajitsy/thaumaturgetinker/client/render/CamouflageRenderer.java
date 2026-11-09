package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.entity.CamouflagedTile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class CamouflageRenderer<T extends BlockEntity & CamouflagedTile> implements BlockEntityRenderer<T, CamouflageRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public @Nullable MovingBlockRenderState block;

        public void extract(ClientLevel level, BlockPos pos, BlockState state) {
            if (state.isAir()) {
                this.block = null;
                return;
            }
            MovingBlockRenderState moving = new MovingBlockRenderState();
            moving.randomSeedPos = pos;
            moving.blockPos = pos;
            moving.blockState = state;
            moving.biome = level.getBiome(pos);
            moving.cardinalLighting = level.cardinalLighting();
            moving.lightEngine = level.getLightEngine();
            this.block = moving;
        }
    }

    public CamouflageRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.block = null;
        BlockState camo = tile.camouflage() == null ? tile.getBlockState() : tile.camouflage();
        if (tile.getLevel() instanceof ClientLevel level) {
            state.extract(level, tile.getBlockPos(), camo);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.block != null) {
            collector.submitMovingBlock(poseStack, state.block);
        }
    }
}
