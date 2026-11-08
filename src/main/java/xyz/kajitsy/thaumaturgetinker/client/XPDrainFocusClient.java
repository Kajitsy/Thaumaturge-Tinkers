package xyz.kajitsy.thaumaturgetinker.client;

import java.awt.Color;
import net.minecraft.client.Minecraft;

public final class XPDrainFocusClient {
    public static int color() {
        var p = Minecraft.getInstance().player;
        return p == null ? 0xFFFFFFFF : Color.HSBtoRGB((p.tickCount * 2 % 360) / 360f, 1, 1);
    }
}
