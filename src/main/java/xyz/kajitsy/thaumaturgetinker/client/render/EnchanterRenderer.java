package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.block.EnchanterBlock;
import xyz.kajitsy.thaumaturgetinker.block.entity.EnchanterEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EnchanterRenderer implements BlockEntityRenderer<EnchanterEntity, EnchanterRenderer.State> {
    public static final class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public @Nullable ItemStackRenderState tool;
        public @Nullable ItemStackRenderState wand;
        public float ticks;
    }

    private final ItemModelResolver itemModelResolver;

    public EnchanterRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EnchanterEntity tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        var blockState = tile.getBlockState();
        state.facing = blockState.hasProperty(EnchanterBlock.FACING) ? blockState.getValue(EnchanterBlock.FACING) : Direction.NORTH;

        ItemStack toolStack = tile.inventory.getItem(0);
        if (toolStack.isEmpty()) {
            state.tool = null;
        } else {
            ItemStackRenderState toolState = state.tool == null ? new ItemStackRenderState() : state.tool;
            itemModelResolver.updateForTopItem(toolState, toolStack, ItemDisplayContext.FIXED, tile.getLevel(), null, 0);
            state.tool = toolState;
        }

        ItemStack wandStack = tile.inventory.getItem(1);
        if (wandStack.isEmpty()) {
            state.wand = null;
        } else {
            ItemStackRenderState wandState = state.wand == null ? new ItemStackRenderState() : state.wand;
            itemModelResolver.updateForTopItem(wandState, wandStack, ItemDisplayContext.FIXED, tile.getLevel(), null, 0);
            state.wand = wandState;
        }

        var level = Minecraft.getInstance().level;
        state.ticks = level == null ? partialTicks : (level.getGameTime() + partialTicks);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float yRot = switch (state.facing) {
            case EAST -> -90f;
            case SOUTH -> 180f;
            case WEST -> 90f;
            default -> 0f;
        };

        if (state.tool != null) {
            pose.pushPose();
            pose.translate(0.5, 0.76, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(yRot));
            pose.mulPose(Axis.XP.rotationDegrees(90f));
            pose.mulPose(Axis.ZP.rotationDegrees(30f));
            pose.scale(0.7f, 0.7f, 0.7f);
            state.tool.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }

        if (state.wand != null) {
            pose.pushPose();
            double bob = Math.sin(state.ticks / 10.0) * 0.05;
            pose.translate(0.5, 1.25 + bob, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(yRot));
            pose.mulPose(Axis.XP.rotationDegrees(90f));
            pose.scale(0.6f, 0.6f, 0.6f);
            pose.mulPose(Axis.ZP.rotationDegrees(-70f));
            pose.mulPose(Axis.XP.rotationDegrees((float) Math.cos(state.ticks / 5.0) * 5f));
            state.wand.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
