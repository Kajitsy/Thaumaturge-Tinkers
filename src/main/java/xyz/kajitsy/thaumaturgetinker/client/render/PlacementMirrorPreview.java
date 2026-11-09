package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.item.BlockTalisman;
import xyz.kajitsy.thaumaturgetinker.item.PlacementMirror;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = "thaumaturgetinker", value = Dist.CLIENT)
public final class PlacementMirrorPreview {
    public static int rendered;

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (!KamiStartupConfig.enabled()) return;
        var mc = Minecraft.getInstance();
        rendered = 0;
        if (mc.level == null) return;
        var camera = event.getCamera().getPosition();
        var pose = event.getPoseStack();
        var buffers = mc.renderBuffers().bufferSource();

        for (var player : mc.level.players()) {
            var mirror = player.getMainHandItem();
            if (!mirror.is(PlacementMirror.ITEM.get()) || BlockTalisman.block(mirror) == Blocks.AIR) continue;
            var positions = PlacementMirror.targets(mirror, player);
            if (!player.getAbilities().instabuild && PlacementMirror.available(mirror, player) < positions.size()) continue;

            var stack = new ItemStack(BlockTalisman.block(mirror));

            for (var at : positions) {
                pose.pushPose();
                try {
                    pose.translate(at.getX() + 0.5 - camera.x, at.getY() + 0.5 - camera.y, at.getZ() + 0.5 - camera.z);
                    mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, pose, buffers, mc.level, 0);
                    rendered++;
                } finally {
                    pose.popPose();
                }
            }
        }
    }

    private PlacementMirrorPreview() {}
}
