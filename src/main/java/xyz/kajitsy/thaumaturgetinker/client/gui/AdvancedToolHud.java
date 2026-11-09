package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.item.AdvancedToolModes;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "thaumaturgetinker", value = Dist.CLIENT)
public final class AdvancedToolHud implements LayeredDraw.Layer {
    static {
        AdvancedToolModes.HUD_NOTIFICATION = AdvancedToolHud::show;
    }

    public static String text;
    public static int remaining;
    public static long elapsed;

    public static void show(String value) {
        if (!value.equals(text)) {
            text = value;
            remaining = 400;
        }
    }

    public static int color(int ticks, long time) {
        int opacity = Math.min(160, (int) (ticks * 256f / 10f));
        return java.awt.Color.HSBtoRGB((float) Math.cos(time / 250.0), .6f, 1f) + (opacity << 24);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        elapsed++;
        if (remaining > 0) remaining--;
    }

    @Override
    public void render(GuiGraphics gui, DeltaTracker deltaTracker) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (remaining > 0 && text != null && !text.isEmpty()) {
            int width = gui.guiWidth();
            int height = gui.guiHeight();
            gui.drawString(mc.font, text, (width - mc.font.width(text)) / 2, height - 72, color(remaining, elapsed), false);
        }
    }
}
