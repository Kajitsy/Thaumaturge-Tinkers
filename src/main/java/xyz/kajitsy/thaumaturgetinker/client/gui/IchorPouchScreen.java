package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.menu.IchorPouchMenu;


import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class IchorPouchScreen extends AbstractContainerScreen<IchorPouchMenu> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "textures/gui/ichorpouch.png");

    public IchorPouchScreen(IchorPouchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 256, 256);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partial) {
        super.extractBackground(gui, mouseX, mouseY, partial);
        gui.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, 256, 256, 256, 256);
    }
}
