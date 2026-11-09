package xyz.kajitsy.thaumaturgetinker.client.render;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.item.BlockTalisman;
import xyz.kajitsy.thaumaturgetinker.item.PlacementMirror;

import net.minecraft.client.Minecraft;
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
        if (!KamiStartupConfig.enabled()) return;
        var mc = Minecraft.getInstance();
        rendered = 0;
        if (mc.level == null) return;
        var camera = event.getLevelRenderState().cameraRenderState.pos;
        var pose = event.getPoseStack();
        var collector = event.getSubmitNodeCollector();

        for (var player : mc.level.players()) {
            var mirror = player.getMainHandItem();
            if (!mirror.is(PlacementMirror.ITEM.get()) || BlockTalisman.block(mirror) == Blocks.AIR) continue;
            var positions = PlacementMirror.targets(mirror, player);
            if (!player.getAbilities().instabuild && PlacementMirror.available(mirror, player) < positions.size()) continue;

            var stack = new ItemStack(BlockTalisman.block(mirror));
            var renderState = new ItemStackRenderState();
            mc.getItemModelResolver().updateForTopItem(renderState, stack, ItemDisplayContext.FIXED, mc.level, player, 0);

            for (var at : positions) {
                pose.pushPose();
                try {
                    pose.translate(at.getX() + 0.5 - camera.x, at.getY() + 0.5 - camera.y, at.getZ() + 0.5 - camera.z);
                    renderState.submit(pose, collector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
                    rendered++;
                } finally {
                    pose.popPose();
                }
            }
        }
    }

    private PlacementMirrorPreview() {}
}
