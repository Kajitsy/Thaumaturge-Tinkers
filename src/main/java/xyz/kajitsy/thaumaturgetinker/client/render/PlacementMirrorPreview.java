package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.TinkererConfig;
import xyz.kajitsy.thaumaturgetinker.item.BlockTalisman;
import xyz.kajitsy.thaumaturgetinker.item.PlacementMirror;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

@EventBusSubscriber(modid = "thaumaturgetinker", value = Dist.CLIENT)
public final class PlacementMirrorPreview {
    public static int rendered;

    @SubscribeEvent
    public static void render(SubmitCustomGeometryEvent event) {
        rendered = 0;
        if (!KamiStartupConfig.enabled() || !TinkererConfig.showPlacementMirrorBlocks()) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var camera = event.getLevelRenderState().cameraRenderState.pos;
        var pose = event.getPoseStack();
        var collector = event.getSubmitNodeCollector();

        for (var player : mc.level.players()) {
            var mirror = player.getMainHandItem();
            if (!mirror.is(PlacementMirror.ITEM.get())) mirror = player.getOffhandItem();
            if (!mirror.is(PlacementMirror.ITEM.get()) || BlockTalisman.block(mirror) == Blocks.AIR) continue;
            var positions = PlacementMirror.targets(mirror, player);
            long maxBlocks = player.getAbilities().instabuild ? positions.size() : PlacementMirror.available(mirror, player);
            if (maxBlocks <= 0 || positions.isEmpty()) continue;
            int limit = (int) Math.min(positions.size(), maxBlocks);

            var stack = new ItemStack(BlockTalisman.block(mirror));
            var renderState = new ItemStackRenderState();
            mc.getItemModelResolver().updateForTopItem(renderState, stack, ItemDisplayContext.FIXED, mc.level, player, 0);

            for (int i = 0; i < limit; i++) {
                var at = positions.get(i);
                pose.pushPose();
                try {
                    pose.translate(at.getX() + 0.5 - camera.x, at.getY() + 0.5 - camera.y, at.getZ() + 0.5 - camera.z);
                    renderState.submit(pose, collector, LevelRenderer.getLightCoords(mc.level, at), OverlayTexture.NO_OVERLAY, 0);
                    rendered++;
                } finally {
                    pose.popPose();
                }
            }
        }
    }

    private PlacementMirrorPreview() {}
}
