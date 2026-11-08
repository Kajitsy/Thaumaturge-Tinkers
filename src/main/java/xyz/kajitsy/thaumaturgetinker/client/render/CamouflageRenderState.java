package xyz.kajitsy.thaumaturgetinker.client.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CamouflageRenderState extends BlockEntityRenderState {
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
