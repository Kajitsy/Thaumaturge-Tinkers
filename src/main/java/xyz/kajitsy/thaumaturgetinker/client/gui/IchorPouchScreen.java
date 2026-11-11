package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.menu.IchorPouchMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class IchorPouchScreen extends AbstractContainerScreen<IchorPouchMenu> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "textures/gui/ichorpouch.png");

    public IchorPouchScreen(IchorPouchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partial, int mouseX, int mouseY) {
        gui.blit(TEXTURE, leftPos, topPos, 0, 0, 256, 256, 256, 256);
    }
}
