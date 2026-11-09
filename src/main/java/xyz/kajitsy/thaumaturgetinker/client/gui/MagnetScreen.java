package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.menu.MagnetMenu;


import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class MagnetScreen extends AbstractContainerScreen<MagnetMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "textures/gui/mobmagnet.png");

    public MagnetScreen(MagnetMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("ttmisc.adult"), b -> select(0)).bounds(leftPos + 98, topPos + 24, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("ttmisc.child"), b -> select(1)).bounds(leftPos + 98, topPos + 46, 70, 20).build());
    }

    private void select(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        graphics.text(font, menu.adult() ? "●" : "○", leftPos + 87, topPos + 30, 0x404040, false);
        graphics.text(font, menu.adult() ? "○" : "●", leftPos + 87, topPos + 52, 0x404040, false);
    }
}
