package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.menu.WarpGateMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class WarpGateScreen extends AbstractContainerScreen<WarpGateMenu> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "textures/gui/warpgate.png");

    private final class LockButton extends AbstractButton {
        LockButton(int x, int y) {
            super(x, y, 13, 13, Component.translatable("ttmisc.lockedGate"));
        }

        @Override
        public void onPress() {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mx, int my, float partial) {
            if (menu.gate.locked) {
                g.blit(TEXTURE, getX(), getY(), 176.0f, 0.0f, 13, 13, 256, 256);
            }
        }
    }

    public WarpGateScreen(WarpGateMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new LockButton(leftPos + 5, topPos + 5));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        super.render(g, mx, my, partialTick);
        renderTooltip(g, mx, my);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        g.drawString(font, Component.translatable("ttmisc.lockedGate"), leftPos + 20, topPos + 7, 0xFF999999, false);
    }
}
