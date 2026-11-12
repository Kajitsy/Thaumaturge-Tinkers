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
import xyz.kajitsy.thaumaturgetinker.client.render.AwakenedWings;
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
import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;
import xyz.kajitsy.thaumaturgetinker.item.SkyPearl;
import xyz.kajitsy.thaumaturgetinker.item.StorageTooltip;
import xyz.kajitsy.thaumaturgetinker.menu.IchorPouchMenu;
import xyz.kajitsy.thaumaturgetinker.menu.WarpGateMenu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.apache.commons.lang3.math.Fraction;

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
    public static void addPackFinders(AddPackFindersEvent event) {
        event.addPackFinders(
            Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "resourcepacks/classic_1_7_10"),
            PackType.CLIENT_RESOURCES,
            Component.translatable("pack.thaumaturgetinker.classic_1_7_10.title"),
            PackSource.BUILT_IN,
            false,
            Pack.Position.TOP
        );
    }

    @SubscribeEvent
    public static void addReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(
            Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "classic_models"),
            (ResourceManagerReloadListener) AwakenedWings::reloadClassicModelState
        );
    }

    @SubscribeEvent
    public static void modelProperties(net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath("thaumaturgetinker", "tool_mode"), ToolModeProperty.TYPE);
        event.register(Identifier.fromNamespaceAndPath("thaumaturgetinker", "active"), ActiveProperty.TYPE);
    }

    private static final Identifier PROGRESSBAR_BORDER_SPRITE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_border");
    private static final Identifier PROGRESSBAR_FILL_SPRITE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_fill");
    private static final Identifier PROGRESSBAR_FULL_SPRITE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_full");
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_front");
    private static final Identifier SLOT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_background");
    private static final Component BUNDLE_FULL_TEXT = Component.translatable("item.minecraft.bundle.full");
    private static final Component BUNDLE_EMPTY_TEXT = Component.translatable("item.minecraft.bundle.empty");
    private static final Component BUNDLE_EMPTY_DESCRIPTION = Component.translatable("item.minecraft.bundle.empty.description");

    private static final ScrollWheelHandler POUCH_SCROLL_HANDLER = new ScrollWheelHandler();
    private static int lastHoveredPouchSlot = -1;

    @SubscribeEvent
    public static void pouchScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (!KamiStartupConfig.enabled()) return;
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        var slot = screen.getSlotUnderMouse();
        if (slot == null || !IchorPouch.isPouch(slot.getItem())) return;
        var pouch = slot.getItem();
        int shown = IchorPouch.numberOfItemsToShow(pouch);
        if (shown == 0) return;

        var wheelXY = POUCH_SCROLL_HANDLER.onMouseScroll(event.getScrollDeltaX(), event.getScrollDeltaY());
        int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
        if (wheel != 0) {
            int current = IchorPouch.getSelectedItem(pouch);
            int next = ScrollWheelHandler.getNextScrollWheelSelection(wheel, current, shown);
            if (current != next) {
                IchorPouch.toggleSelectedItem(pouch, next);
                ClientPacketDistributor.sendToServer(new IchorPouch.SelectPayload(slot.index, next));
            }
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void pouchHoverCheck(ScreenEvent.Render.Pre event) {
        if (!KamiStartupConfig.enabled()) return;
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            lastHoveredPouchSlot = -1;
            return;
        }
        var slot = screen.getSlotUnderMouse();
        int currentSlot = (slot != null && IchorPouch.isPouch(slot.getItem())) ? slot.index : -1;
        if (lastHoveredPouchSlot != -1 && lastHoveredPouchSlot != currentSlot) {
            var menu = screen.getMenu();
            if (lastHoveredPouchSlot >= 0 && lastHoveredPouchSlot < menu.slots.size()) {
                var prevStack = menu.getSlot(lastHoveredPouchSlot).getItem();
                if (IchorPouch.isPouch(prevStack) && IchorPouch.getSelectedItem(prevStack) != -1) {
                    IchorPouch.toggleSelectedItem(prevStack, -1);
                    ClientPacketDistributor.sendToServer(new IchorPouch.SelectPayload(lastHoveredPouchSlot, -1));
                }
            }
        }
        lastHoveredPouchSlot = currentSlot;
    }

    @SubscribeEvent
    public static void tooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(StorageTooltip.class, tooltip -> new ClientTooltipComponent() {
            private int gridSizeY() {
                return Mth.positiveCeilDiv(Math.min(12, tooltip.entries().size()), 4);
            }

            private int emptyDescriptionHeight(Font font) {
                return font.split(BUNDLE_EMPTY_DESCRIPTION, 96).size() * 9;
            }

            @Override
            public int getWidth(Font font) {
                return 96;
            }

            @Override
            public int getHeight(Font font) {
                if (tooltip.entries().isEmpty()) {
                    return (tooltip.showEmptyDescription() ? emptyDescriptionHeight(font) : 0) + 13 + 8;
                }
                return gridSizeY() * 24 + 13 + 8;
            }

            @Override
            public boolean showTooltipWithItemInHand() {
                return true;
            }

            @Override
            public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor g) {
                var weight = Fraction.getFraction(Math.min(tooltip.occupied(), tooltip.capacity()), Math.max(1, tooltip.capacity()));
                if (tooltip.entries().isEmpty()) {
                    int left = x + (width - 96) / 2;
                    int barY = y;
                    if (tooltip.showEmptyDescription()) {
                        g.textWithWordWrap(font, BUNDLE_EMPTY_DESCRIPTION, left, y, 96, -5592406);
                        barY += emptyDescriptionHeight(font) + 4;
                    }
                    extractProgressbar(left, barY, font, g, Fraction.ZERO);
                } else {
                    boolean isOverflowing = tooltip.entries().size() > 12;
                    int shownCount = StorageTooltip.numberOfItemsToShow(tooltip.entries().size());
                    var shownItems = tooltip.entries().subList(0, shownCount);
                    int xStartPos = !tooltip.showEmptyDescription() && shownItems.size() == 1
                        ? x + (width - 24) / 2 + 24
                        : x + (width - 96) / 2 + 96;
                    int yStartPos = y + gridSizeY() * 24;
                    int slotNumber = 1;

                    for (int row = 1; row <= gridSizeY(); ++row) {
                        for (int col = 1; col <= 4; ++col) {
                            int drawX = xStartPos - col * 24;
                            int drawY = yStartPos - row * 24;
                            if (isOverflowing && col * row == 1) {
                                int hidden = tooltip.entries().size() - shownItems.size();
                                g.centeredText(font, "+" + hidden, drawX + 12, drawY + 10, -1);
                            } else if (shownItems.size() >= slotNumber) {
                                int visualIdx = shownItems.size() - slotNumber;
                                boolean highlight = visualIdx == tooltip.selectedIndex();
                                var entry = shownItems.get(visualIdx);
                                g.blitSprite(RenderPipelines.GUI_TEXTURED, highlight ? SLOT_HIGHLIGHT_BACK_SPRITE : SLOT_BACKGROUND_SPRITE, drawX, drawY, 24, 24);
                                g.item(entry.stack(), drawX + 4, drawY + 4, slotNumber);
                                g.itemDecorations(font, entry.stack(), drawX + 4, drawY + 4, entry.countText());
                                if (highlight) {
                                    g.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, drawX, drawY, 24, 24);
                                }
                                ++slotNumber;
                            }
                        }
                    }

                    int sel = tooltip.selectedIndex();
                    if (sel >= 0 && sel < shownItems.size()) {
                        var selectedStack = shownItems.get(sel).stack();
                        var name = selectedStack.getStyledHoverName();
                        int textWidth = font.width(name.getVisualOrderText());
                        g.tooltip(
                            font,
                            List.of(ClientTooltipComponent.create(name.getVisualOrderText())),
                            (x + width / 2 - 12) - textWidth / 2,
                            y - 15,
                            DefaultTooltipPositioner.INSTANCE,
                            selectedStack.get(DataComponents.TOOLTIP_STYLE)
                        );
                    }

                    extractProgressbar(x + (width - 96) / 2, y + gridSizeY() * 24 + 4, font, g, weight);
                }
            }

            private static void extractProgressbar(int x, int y, Font font, GuiGraphicsExtractor g, Fraction weight) {
                int fill = Mth.clamp(Mth.mulAndTruncate(weight, 94), 0, 94);
                var tex = weight.compareTo(Fraction.ONE) >= 0 ? PROGRESSBAR_FULL_SPRITE : PROGRESSBAR_FILL_SPRITE;
                g.blitSprite(RenderPipelines.GUI_TEXTURED, tex, x + 1, y, fill, 13);
                g.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESSBAR_BORDER_SPRITE, x, y, 96, 13);
                Component text = weight.compareTo(Fraction.ZERO) == 0 ? BUNDLE_EMPTY_TEXT : (weight.compareTo(Fraction.ONE) >= 0 ? BUNDLE_FULL_TEXT : null);
                if (text != null) {
                    g.centeredText(font, text, x + 48, y + 3, -1);
                }
            }
        });
    }

    @SubscribeEvent
    public static void itemTooltip(ItemTooltipEvent event) {
        TooltipIndicators.append(event.getItemStack(), event.getToolTip());
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
