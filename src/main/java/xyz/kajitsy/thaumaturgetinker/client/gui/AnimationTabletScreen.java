package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.menu.AnimationTabletMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class AnimationTabletScreen extends AbstractContainerScreen<AnimationTabletMenu> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("thaumaturgetinker", "textures/gui/animationtablet.png");

    private final class ModeButton extends AbstractButton {
        private final int choice;

        ModeButton(int x, int y, int choice) {
            super(x, y, 13, 13, Component.translatable(choice == 0 ? "ttmisc.redstoneControl" : choice == 1 ? "ttmisc.leftClick" : "ttmisc.rightClick"));
            this.choice = choice;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            select(choice);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float partial) {
            int u = choice == 0 ? (menu.tile.redstone ? 13 : 0) : (menu.tile.leftClick == (choice == 1) ? 13 : 0);
            int v = choice == 0 ? 0 : (choice == 1 ? 13 : 26);
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), 176.0f + u, v, 13, 13, 256, 256);
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
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0f, 0.0f, imageWidth, imageHeight, 256, 256);
        var left = Component.translatable("ttmisc.leftClick");
        g.text(font, left, leftPos + 48 - font.width(left), topPos + 18, 0x999999, false);
        g.text(font, Component.translatable("ttmisc.rightClick"), leftPos + 128, topPos + 18, 0x999999, false);
        var redstone = Component.translatable("ttmisc.redstoneControl");
        g.text(font, redstone, leftPos + 88 - font.width(redstone) / 2, topPos + 50, 0x999999, false);
    }
}
