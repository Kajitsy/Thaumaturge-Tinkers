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

    public AnimationTabletScreen(AnimationTabletMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new AnimationTabletModeButton(leftPos + 52, topPos + 15, 1, this));
        addRenderableWidget(new AnimationTabletModeButton(leftPos + 111, topPos + 15, 2, this));
        addRenderableWidget(new AnimationTabletModeButton(leftPos + 81, topPos + 60, 0, this));
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
