package xyz.kajitsy.thaumaturgetinker.client.gui;

import net.minecraft.network.chat.Component;
import java.util.List;

public record EnchanterHit(int x, int y, int width, int height, int button, List<Component> tooltip) {}
