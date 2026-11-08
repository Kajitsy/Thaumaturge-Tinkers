package xyz.kajitsy.thaumaturgetinker.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import java.util.function.BooleanSupplier;

public final class WarpGateLockButton extends AbstractButton {
    public final BooleanSupplier locked;
    public final Runnable action;

    public WarpGateLockButton(int x, int y, Runnable action, BooleanSupplier locked) {
        super(x, y, 13, 13, Component.translatable("ttmisc.lockedGate"));
        this.action = action;
        this.locked = locked;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float partial) {
        if (locked.getAsBoolean()) {
            g.blit(RenderPipelines.GUI_TEXTURED, WarpGateScreen.TEXTURE, getX(), getY(), 176.0f, 0.0f, 13, 13, 256, 256);
        }
    }
}
