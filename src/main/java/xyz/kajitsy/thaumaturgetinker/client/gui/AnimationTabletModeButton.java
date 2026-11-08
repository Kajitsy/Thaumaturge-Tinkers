package xyz.kajitsy.thaumaturgetinker.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public final class AnimationTabletModeButton extends AbstractButton {
    public final int choice;
    private final AnimationTabletScreen screen;

    public AnimationTabletModeButton(int x, int y, int choice, AnimationTabletScreen screen) {
        super(x, y, 13, 13, Component.translatable(choice == 0 ? "ttmisc.redstoneControl" : choice == 1 ? "ttmisc.leftClick" : "ttmisc.rightClick"));
        this.choice = choice;
        this.screen = screen;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        screen.select(choice);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float partial) {
        int u = choice == 0 ? (screen.getMenu().tile.redstone ? 13 : 0) : (screen.getMenu().tile.leftClick == (choice == 1) ? 13 : 0);
        int v = choice == 0 ? 0 : (choice == 1 ? 13 : 26);
        g.blit(RenderPipelines.GUI_TEXTURED, AnimationTabletScreen.TEXTURE, getX(), getY(), 176.0f + u, v, 13, 13, 256, 256);
    }
}
