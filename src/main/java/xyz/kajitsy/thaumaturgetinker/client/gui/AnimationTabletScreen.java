package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.menu.AnimationTabletMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class AnimationTabletScreen extends AbstractContainerScreen<AnimationTabletMenu> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "textures/gui/animationtablet.png");

    private final class ModeButton extends AbstractButton {
        private final int choice;

        ModeButton(int x, int y, int choice) {
            super(x, y, 13, 13, Component.translatable(choice == 0 ? "ttmisc.redstoneControl" : choice == 1 ? "ttmisc.leftClick" : "ttmisc.rightClick"));
            this.choice = choice;
        }

        @Override
        public void onPress() {
            select(choice);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mx, int my, float partial) {
            int u = choice == 0 ? (menu.tile.redstone ? 13 : 0) : (menu.tile.leftClick == (choice == 1) ? 13 : 0);
            int v = choice == 0 ? 0 : (choice == 1 ? 13 : 26);
            g.blit(TEXTURE, getX(), getY(), 176.0f + u, (float) v, 13, 13, 256, 256);
        }
    }

    public AnimationTabletScreen(AnimationTabletMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new ModeButton(leftPos + 52, topPos + 15, 1));
        addRenderableWidget(new ModeButton(leftPos + 111, topPos + 15, 2));
        addRenderableWidget(new ModeButton(leftPos + 81, topPos + 60, 0));
    }

    void select(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0.0f, 0.0f, imageWidth, imageHeight, 256, 256);
        var left = Component.translatable("ttmisc.leftClick");
        g.drawString(font, left, leftPos + 48 - font.width(left), topPos + 18, 0x999999, false);
        g.drawString(font, Component.translatable("ttmisc.rightClick"), leftPos + 128, topPos + 18, 0x999999, false);
        var redstone = Component.translatable("ttmisc.redstoneControl");
        g.drawString(font, redstone, leftPos + 88 - font.width(redstone) / 2, topPos + 50, 0x999999, false);
    }
}
