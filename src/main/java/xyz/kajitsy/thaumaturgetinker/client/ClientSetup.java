package xyz.kajitsy.thaumaturgetinker.client;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.WarpGateBlock;
import xyz.kajitsy.thaumaturgetinker.block.entity.WarpGateEntity;
import xyz.kajitsy.thaumaturgetinker.client.fx.ShadowbeamFx;
import xyz.kajitsy.thaumaturgetinker.client.fx.WarpGateFx;
import xyz.kajitsy.thaumaturgetinker.client.gui.AdvancedToolHud;
import xyz.kajitsy.thaumaturgetinker.client.gui.AnimationTabletScreen;
import xyz.kajitsy.thaumaturgetinker.client.gui.EnchanterScreen;
import xyz.kajitsy.thaumaturgetinker.client.gui.IchorPouchScreen;
import xyz.kajitsy.thaumaturgetinker.client.gui.MagnetScreen;
import xyz.kajitsy.thaumaturgetinker.client.gui.WarpGateDestinations;
import xyz.kajitsy.thaumaturgetinker.client.gui.WarpGateScreen;
import xyz.kajitsy.thaumaturgetinker.client.render.ActiveProperty;
import xyz.kajitsy.thaumaturgetinker.client.render.AnimationTabletRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.CamouflageRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.EnchanterRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.FunnelRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.RepairerRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.ToolModeProperty;
import xyz.kajitsy.thaumaturgetinker.client.render.WarpGateRenderer;
import xyz.kajitsy.thaumaturgetinker.focus.ShadowbeamFocus;
import xyz.kajitsy.thaumaturgetinker.item.AdvancedTool;
import xyz.kajitsy.thaumaturgetinker.item.AdvancedToolMining;
import xyz.kajitsy.thaumaturgetinker.item.AdvancedToolModes;
import xyz.kajitsy.thaumaturgetinker.item.SkyPearl;
import xyz.kajitsy.thaumaturgetinker.menu.IchorPouchMenu;
import xyz.kajitsy.thaumaturgetinker.menu.WarpGateMenu;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = ThaumicTinkererModern.ID, value = Dist.CLIENT)
public final class ClientSetup {
    static {
        WarpGateBlock.CLIENT_TICKER = WarpGateDestinations::tickGate;
        WarpGateEntity.SPARKLE_EMITTER = WarpGateFx::emit;
        ShadowbeamFocus.SPARKLE_EMITTER = ShadowbeamFx::emit;
        SkyPearl.CLIENT_TOOLTIP = WarpGateDestinations::appendPearlInfo;
    }

    @SubscribeEvent
    public static void screens(RegisterMenuScreensEvent event) {
        if (KamiStartupConfig.enabled()) event.register(WarpGateMenu.TYPE.get(), WarpGateScreen::new);
        event.register(ThaumicTinkererModern.ENCHANTER_MENU.get(), EnchanterScreen::new);
        if (KamiStartupConfig.enabled()) event.register(IchorPouchMenu.TYPE.get(), IchorPouchScreen::new);
        event.register(ThaumicTinkererModern.MAGNET_MENU.get(), MagnetScreen::new);
        event.register(ThaumicTinkererModern.ANIMATION_TABLET_MENU.get(), AnimationTabletScreen::new);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        if (KamiStartupConfig.enabled()) event.registerBlockEntityRenderer(WarpGateBlock.ENTITY.get(), WarpGateRenderer::new);
        event.registerBlockEntityRenderer(ThaumicTinkererModern.PLATFORM_ENTITY.get(), CamouflageRenderer::new);
        event.registerBlockEntityRenderer(ThaumicTinkererModern.ANIMATION_TABLET_ENTITY.get(), AnimationTabletRenderer::new);
        event.registerBlockEntityRenderer(ThaumicTinkererModern.DISLOCATOR_ENTITY.get(), CamouflageRenderer::new);
        event.registerBlockEntityRenderer(ThaumicTinkererModern.TRANSVECTOR_ENTITY.get(), CamouflageRenderer::new);
        event.registerBlockEntityRenderer(ThaumicTinkererModern.FUNNEL_ENTITY.get(), FunnelRenderer::new);
        event.registerBlockEntityRenderer(ThaumicTinkererModern.REPAIRER_ENTITY.get(), RepairerRenderer::new);
        event.registerBlockEntityRenderer(ThaumicTinkererModern.ENCHANTER_ENTITY.get(), EnchanterRenderer::new);
    }

    @SubscribeEvent
    public static void guiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, Identifier.fromNamespaceAndPath("thaumaturgetinker", "advanced_tool_hud"), new AdvancedToolHud());
    }

    @SubscribeEvent
    public static void modelProperties(net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath("thaumaturgetinker", "tool_mode"), ToolModeProperty.TYPE);
        event.register(Identifier.fromNamespaceAndPath("thaumaturgetinker", "active"), ActiveProperty.TYPE);
    }

    private record OutlineTarget(BlockPos pos, VoxelShape shape) {}

    @SubscribeEvent
    public static void blockOutline(ExtractBlockOutlineRenderStateEvent event) {
        if (!KamiStartupConfig.enabled()) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof AdvancedTool tool)) return;
        int mode = AdvancedToolModes.get(stack);
        if (mode == 0) return;

        var level = event.getLevel();
        var positions = AdvancedToolMining.targets(player, event.getBlockPos(), event.getHitResult().getDirection(), tool.toolType(), mode);
        if (positions.isEmpty()) return;

        var outlines = new ArrayList<OutlineTarget>(positions.size());
        var context = event.getCollisionContext();
        for (var pos : positions) {
            var shape = level.getBlockState(pos).getShape(level, pos, context);
            if (!shape.isEmpty()) {
                outlines.add(new OutlineTarget(pos, shape));
            }
        }
        if (outlines.isEmpty()) return;

        float lineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
        event.addCustomRenderer((renderState, bufferSource, poseStack, translucentPass, levelRenderState) -> {
            if (translucentPass != renderState.isTranslucent()) return false;
            var cam = levelRenderState.cameraRenderState.pos;
            int color = renderState.highContrast() ? 0xFF57FFDE : ARGB.black(102);
            var primaryType = renderState.highContrast() ? RenderTypes.lines() : RenderTypes.linesTranslucent();
            for (var target : outlines) {
                double dx = target.pos.getX() - cam.x, dy = target.pos.getY() - cam.y, dz = target.pos.getZ() - cam.z;
                if (renderState.highContrast()) {
                    ShapeRenderer.renderShape(poseStack, bufferSource.getBuffer(RenderTypes.secondaryBlockOutline()), target.shape, dx, dy, dz, 0xFF000000, 7.0F);
                }
                ShapeRenderer.renderShape(poseStack, bufferSource.getBuffer(primaryType), target.shape, dx, dy, dz, color, lineWidth);
            }
            return false;
        });
    }
}
