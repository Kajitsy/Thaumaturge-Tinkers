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
import xyz.kajitsy.thaumaturgetinker.item.AwakenedAxe;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedPick;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedShovel;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedSword;
import xyz.kajitsy.thaumaturgetinker.item.BlockTalisman;
import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;
import xyz.kajitsy.thaumaturgetinker.item.SkyPearl;
import xyz.kajitsy.thaumaturgetinker.item.StorageTooltip;
import xyz.kajitsy.thaumaturgetinker.menu.IchorPouchMenu;
import xyz.kajitsy.thaumaturgetinker.menu.WarpGateMenu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
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
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "advanced_tool_hud"), new AdvancedToolHud());
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            var activeId = ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "active");
            ItemProperties.registerGeneric(activeId, ActiveProperty.INSTANCE);
            ItemProperties.register(ThaumicTinkererModern.CLEANSING_TALISMAN.get(), activeId, ActiveProperty.INSTANCE);
            ItemProperties.register(ThaumicTinkererModern.XP_TALISMAN.get(), activeId, ActiveProperty.INSTANCE);
            ItemProperties.register(ThaumicTinkererModern.BLOOD_SWORD.get(), activeId, ActiveProperty.INSTANCE);
            if (KamiStartupConfig.enabled()) {
                ItemProperties.register(BlockTalisman.ITEM.get(), activeId, ActiveProperty.INSTANCE);
            }

            var toolModeId = ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "tool_mode");
            ItemProperties.registerGeneric(toolModeId, ToolModeProperty.INSTANCE);
            if (KamiStartupConfig.enabled()) {
                ItemProperties.register(AwakenedPick.ITEM.get(), toolModeId, ToolModeProperty.INSTANCE);
                ItemProperties.register(AwakenedShovel.ITEM.get(), toolModeId, ToolModeProperty.INSTANCE);
                ItemProperties.register(AwakenedAxe.ITEM.get(), toolModeId, ToolModeProperty.INSTANCE);
                ItemProperties.register(AwakenedSword.ITEM.get(), toolModeId, ToolModeProperty.INSTANCE);
            }
        });
    }

    private static final ResourceLocation PROGRESSBAR_BORDER_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/bundle_progressbar_border");
    private static final ResourceLocation PROGRESSBAR_FILL_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/bundle_progressbar_fill");
    private static final ResourceLocation PROGRESSBAR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/bundle_progressbar_full");
    private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot_highlight_back");
    private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot_highlight_front");
    private static final ResourceLocation SLOT_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot_background");
    private static final Component BUNDLE_FULL_TEXT = Component.translatable("item.minecraft.bundle.full");
    private static final Component BUNDLE_EMPTY_TEXT = Component.translatable("item.minecraft.bundle.empty");
    private static final Component BUNDLE_EMPTY_DESCRIPTION = Component.translatable("item.minecraft.bundle.empty.description");

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

        double deltaY = event.getScrollDeltaY();
        double deltaX = event.getScrollDeltaX();
        int wheel = (int) Math.signum(deltaY == 0 ? -deltaX : deltaY);
        if (wheel != 0) {
            int current = IchorPouch.getSelectedItem(pouch);
            int next;
            if (current < 0) {
                next = wheel > 0 ? 0 : shown - 1;
            } else {
                next = Math.floorMod(current - wheel, shown);
            }
            if (current != next) {
                IchorPouch.toggleSelectedItem(pouch, next);
                PacketDistributor.sendToServer(new IchorPouch.SelectPayload(slot.index, next));
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
                    PacketDistributor.sendToServer(new IchorPouch.SelectPayload(lastHoveredPouchSlot, -1));
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
            public int getHeight() {
                Font font = Minecraft.getInstance().font;
                if (tooltip.entries().isEmpty()) {
                    return (tooltip.showEmptyDescription() ? emptyDescriptionHeight(font) : 0) + 13 + 8;
                }
                return gridSizeY() * 24 + 13 + 8;
            }

            @Override
            public void renderImage(Font font, int x, int y, GuiGraphics g) {
                var weight = Fraction.getFraction(Math.min(tooltip.occupied(), tooltip.capacity()), Math.max(1, tooltip.capacity()));
                int width = getWidth(font);
                if (tooltip.entries().isEmpty()) {
                    int left = x + (width - 96) / 2;
                    int barY = y;
                    if (tooltip.showEmptyDescription()) {
                        g.drawWordWrap(font, BUNDLE_EMPTY_DESCRIPTION, left, y, 96, -5592406);
                        barY += emptyDescriptionHeight(font) + 4;
                    }
                    renderProgressbar(left, barY, font, g, Fraction.ZERO);
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
                                g.drawCenteredString(font, "+" + hidden, drawX + 12, drawY + 10, -1);
                            } else if (shownItems.size() >= slotNumber) {
                                int visualIdx = shownItems.size() - slotNumber;
                                boolean highlight = visualIdx == tooltip.selectedIndex();
                                var entry = shownItems.get(visualIdx);
                                g.blitSprite(highlight ? SLOT_HIGHLIGHT_BACK_SPRITE : SLOT_BACKGROUND_SPRITE, drawX, drawY, 24, 24);
                                g.renderItem(entry.stack(), drawX + 4, drawY + 4, slotNumber);
                                g.renderItemDecorations(font, entry.stack(), drawX + 4, drawY + 4, entry.countText());
                                if (highlight) {
                                    g.blitSprite(SLOT_HIGHLIGHT_FRONT_SPRITE, drawX, drawY, 24, 24);
                                }
                                ++slotNumber;
                            }
                        }
                    }

                    int sel = tooltip.selectedIndex();
                    if (sel >= 0 && sel < shownItems.size()) {
                        var selectedStack = shownItems.get(sel).stack();
                        var name = selectedStack.getHoverName();
                        int textWidth = font.width(name);
                        g.renderComponentTooltip(
                            font,
                            List.of(name),
                            (x + width / 2 - 12) - textWidth / 2,
                            y - 15
                        );
                    }

                    renderProgressbar(x + (width - 96) / 2, y + gridSizeY() * 24 + 4, font, g, weight);
                }
            }

            private static void renderProgressbar(int x, int y, Font font, GuiGraphics g, Fraction weight) {
                int fill = Mth.clamp(Mth.mulAndTruncate(weight, 94), 0, 94);
                var tex = weight.compareTo(Fraction.ONE) >= 0 ? PROGRESSBAR_FULL_SPRITE : PROGRESSBAR_FILL_SPRITE;
                g.blitSprite(tex, x + 1, y, fill, 13);
                g.blitSprite(PROGRESSBAR_BORDER_SPRITE, x, y, 96, 13);
                Component text = weight.compareTo(Fraction.ZERO) == 0 ? BUNDLE_EMPTY_TEXT : (weight.compareTo(Fraction.ONE) >= 0 ? BUNDLE_FULL_TEXT : null);
                if (text != null) {
                    g.drawCenteredString(font, text, x + 48, y + 3, -1);
                }
            }
        });
    }

    private record OutlineTarget(BlockPos pos, VoxelShape shape) {}

    @SubscribeEvent
    public static void blockOutline(RenderHighlightEvent.Block event) {
        if (!KamiStartupConfig.enabled()) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof AdvancedTool tool)) return;
        int mode = AdvancedToolModes.get(stack);
        if (mode == 0) return;

        var hit = event.getTarget();
        var blockPos = hit.getBlockPos();
        var level = player.level();
        var positions = AdvancedToolMining.targets(player, blockPos, hit.getDirection(), tool.toolType(), mode);
        if (positions.isEmpty()) return;

        var context = CollisionContext.of(player);
        var outlines = new ArrayList<OutlineTarget>(positions.size());
        for (var pos : positions) {
            var shape = level.getBlockState(pos).getShape(level, pos, context);
            if (!shape.isEmpty()) {
                outlines.add(new OutlineTarget(pos, shape));
            }
        }
        if (outlines.isEmpty()) return;

        var cam = event.getCamera().getPosition();
        var poseStack = event.getPoseStack();
        var bufferSource = event.getMultiBufferSource();
        var buffer = bufferSource.getBuffer(RenderType.lines());

        for (var target : outlines) {
            double dx = target.pos.getX() - cam.x, dy = target.pos.getY() - cam.y, dz = target.pos.getZ() - cam.z;
            LevelRenderer.renderShape(poseStack, buffer, target.shape, dx, dy, dz, 0.0F, 0.0F, 0.0F, 0.4F);
        }
    }
}
