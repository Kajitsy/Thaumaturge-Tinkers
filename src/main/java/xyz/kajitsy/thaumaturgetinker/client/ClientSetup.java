package xyz.kajitsy.thaumaturgetinker.client;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.WarpGateBlock;
import xyz.kajitsy.thaumaturgetinker.client.gui.AdvancedToolHud;
import xyz.kajitsy.thaumaturgetinker.client.gui.AnimationTabletScreen;
import xyz.kajitsy.thaumaturgetinker.client.gui.EnchanterScreen;
import xyz.kajitsy.thaumaturgetinker.client.gui.IchorPouchScreen;
import xyz.kajitsy.thaumaturgetinker.client.gui.MagnetScreen;
import xyz.kajitsy.thaumaturgetinker.client.gui.WarpGateScreen;
import xyz.kajitsy.thaumaturgetinker.client.render.AnimationTabletRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.DislocatorRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.EnchanterRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.FunnelRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.PlatformRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.RepairerRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.TransvectorRenderer;
import xyz.kajitsy.thaumaturgetinker.client.render.WarpGateRenderer;
import xyz.kajitsy.thaumaturgetinker.item.ToolModeProperty;
import xyz.kajitsy.thaumaturgetinker.menu.IchorPouchMenu;
import xyz.kajitsy.thaumaturgetinker.menu.WarpGateMenu;


import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = ThaumicTinkererModern.ID, value = Dist.CLIENT)
public final class ClientSetup {
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
        event.registerBlockEntityRenderer(ThaumicTinkererModern.PLATFORM_ENTITY.get(), PlatformRenderer::new);
        event.registerBlockEntityRenderer(ThaumicTinkererModern.ANIMATION_TABLET_ENTITY.get(), AnimationTabletRenderer::new);
        event.registerBlockEntityRenderer(ThaumicTinkererModern.DISLOCATOR_ENTITY.get(), DislocatorRenderer::new);
        event.registerBlockEntityRenderer(ThaumicTinkererModern.TRANSVECTOR_ENTITY.get(), TransvectorRenderer::new);
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
        event.register(Identifier.fromNamespaceAndPath("thaumaturgetinker", "active"), xyz.kajitsy.thaumaturgetinker.item.ActiveProperty.TYPE);
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
    }
}
