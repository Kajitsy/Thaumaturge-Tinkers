package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.SkyPearlData;
import xyz.kajitsy.thaumaturgetinker.block.entity.WarpGateEntity;
import xyz.kajitsy.thaumaturgetinker.item.SkyPearl;
import xyz.kajitsy.thaumaturgetinker.network.WarpGateTeleportPayload;


import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.List;

/** Original one-world-block-per-screen-pixel destination map. */
public final class WarpGateDestinations extends Screen {
    final WarpGateEntity gate;
    double originX, originZ;
    int ticks;

    public WarpGateDestinations(WarpGateEntity gate) {
        super(Component.translatable("ttmisc.destinations"));
        this.gate = gate;
    }

    @Override
    protected void init() {
        center(gate.getBlockPos());
    }

    void center(BlockPos pos) {
        originX = pos.getX() - width / 2.0;
        originZ = pos.getZ() - height / 2.0;
    }

    boolean visible(ItemStack pearl) {
        return !pearl.isEmpty() && SkyPearlData.attuned(pearl) && gate.getLevel() != null && SkyPearlData.dimension(pearl).equals(gate.getLevel().dimension().identifier());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        ticks++;
        if (gate.isRemoved()) onClose();
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (event.button() == 0) {
            originX -= dx;
            originZ -= dy;
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_SPACE) {
            center(gate.getBlockPos());
            return true;
        }
        int index = event.key() == GLFW.GLFW_KEY_0 ? 9 : event.key() - GLFW.GLFW_KEY_1;
        if (index >= 0 && index < 10) {
            var pearl = gate.inventory.getItem(index);
            if (visible(pearl)) center(SkyPearlData.position(pearl));
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (int i = 0; i < 10; i++) {
                var pearl = gate.inventory.getItem(i);
                if (!visible(pearl)) continue;
                if (event.x() >= 3 && event.x() <= 220 && event.y() >= 52 + i * 11 && event.y() <= 64 + i * 11) {
                    ClientPacketDistributor.sendToServer(new WarpGateTeleportPayload(gate.getBlockPos(), i));
                    onClose();
                    return true;
                }
            }
            for (int i = 0; i < 10; i++) {
                var pearl = gate.inventory.getItem(i);
                if (!visible(pearl)) continue;
                var pos = SkyPearlData.position(pearl);
                double px = pos.getX() - originX, pz = pos.getZ() - originZ;
                double dx = event.x() - px;
                double dz = event.y() - pz;
                if (dx * dx + dz * dz <= 100) {
                    ClientPacketDistributor.sendToServer(new WarpGateTeleportPayload(gate.getBlockPos(), i));
                    onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
        int rgb = Color.HSBtoRGB((float) (Math.sin(ticks / 150d) + .5), .5f, .4f);
        g.blit(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("textures/entity/end_portal/end_portal.png"), 0, 0, 0, 0, width, height, 256, 256, (0xFF << 24) | (rgb & 0xFFFFFF));
        int gx = (int) (gate.getBlockPos().getX() - originX), gz = (int) (gate.getBlockPos().getZ() - originZ);
        int alpha = (int) (((Math.sin(ticks / 10d) + 1) / 4 + .25) * 255);
        for (int i = 0; i < 10; i++) {
            var pearl = gate.inventory.getItem(i);
            if (!visible(pearl)) continue;
            var p = SkyPearlData.position(pearl);
            int x = (int) (p.getX() - originX), z = (int) (p.getZ() - originZ);
            g.pose().pushMatrix();
            g.pose().translate(gx, gz);
            g.pose().rotate((float) Math.atan2(z - gz, x - gx));
            g.fill(0, -1, (int) Math.hypot(x - gx, z - gz), 1, alpha << 24 | 0xFFFFFF);
            g.pose().popMatrix();
        }
        icon(g, new ItemStack(SkyPearl.ITEM.get()), gx, gz);
        double gdx = mx - gx;
        double gdz = my - gz;
        List<Component> tooltip = gdx * gdx + gdz * gdz <= 64
                ? List.of(Component.translatable("ttmisc.entrancePoint"), Component.literal("X: " + gate.getBlockPos().getX()), Component.literal("Z: " + gate.getBlockPos().getZ()))
                : null;
        for (int i = 0; i < 10; i++) {
            var pearl = gate.inventory.getItem(i);
            if (!visible(pearl)) continue;
            var p = SkyPearlData.position(pearl);
            int x = (int) (p.getX() - originX), z = (int) (p.getZ() - originZ);
            icon(g, pearl, x, z);
            var name = pearl.has(DataComponents.CUSTOM_NAME) ? pearl.getHoverName() : Component.translatable("ttmisc.destination");
            g.text(font, Component.literal((i + 1) + ": ").append(name), 5, 54 + i * 11, -1, false);
            double dx = mx - x;
            double dz = my - z;
            boolean hoveredNode = dx * dx + dz * dz <= 100;
            boolean hoveredList = mx >= 3 && mx <= 220 && my >= 52 + i * 11 && my <= 64 + i * 11;
            if (hoveredNode || hoveredList) {
                tooltip = List.of(name, Component.literal("X: " + p.getX()), Component.literal("Z: " + p.getZ()),
                        Component.translatable("ttmisc.distance").append(": " + SkyPearlData.distance(pearl, Vec3.atLowerCornerOf(gate.getBlockPos()), true) + "m"),
                        Component.translatable("ttmisc.clickToTeleport"));
            }
        }
        g.text(font, title.copy().withStyle(ChatFormatting.UNDERLINE), 3, 40, -1, false);
        g.centeredText(font, Component.translatable("ttmisc.numberKeys"), width / 2, 5, -1);
        g.centeredText(font, Component.translatable("ttmisc.spaceToReset"), width / 2, 16, -1);
        if (tooltip != null) g.setComponentTooltipForNextFrame(font, tooltip, mx, my);
    }

    private static void icon(GuiGraphicsExtractor g, ItemStack pearl, int x, int z) {
        g.pose().pushMatrix();
        g.pose().translate(x, z);
        g.pose().scale(.5f, .5f);
        g.item(new ItemStack(SkyPearl.ITEM.get()), -8, -8);
        g.pose().popMatrix();
    }
}
