package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.entity.CamouflagedTile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CamouflageRenderer<T extends BlockEntity & CamouflagedTile> implements BlockEntityRenderer<T> {
    private final BlockRenderDispatcher blockRenderer;

    public CamouflageRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(T tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BlockState camo = tile.camouflage() == null ? tile.getBlockState() : tile.camouflage();
        if (camo != null && !camo.isAir()) {
            blockRenderer.renderSingleBlock(camo, poseStack, buffers, light, overlay);
        }
    }
}
