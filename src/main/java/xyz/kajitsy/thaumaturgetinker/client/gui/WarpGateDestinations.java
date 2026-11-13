package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.SkyPearlData;
import xyz.kajitsy.thaumaturgetinker.block.entity.WarpGateEntity;
import xyz.kajitsy.thaumaturgetinker.item.SkyPearl;
import xyz.kajitsy.thaumaturgetinker.network.WarpGateNetwork;

import com.mojang.math.Axis;
import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Original one-world-block-per-screen-pixel destination map. */
public final class WarpGateDestinations extends Screen {
    final WarpGateEntity gate;
    double originX, originZ;
    int ticks;

    public WarpGateDestinations(WarpGateEntity gate) {
        super(Component.translatable("ttmisc.destinations"));
        this.gate = gate;
    }

    public static void tickGate(WarpGateEntity gate) {
        var mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null || !mc.player.isShiftKeyDown()) return;
        var p = gate.getBlockPos();
        if (mc.player.getBoundingBox().intersects(new AABB(p.getX(), p.getY() + 1, p.getZ(), p.getX() + 1, p.getY() + 1.5, p.getZ() + 1))) {
            mc.setScreen(new WarpGateDestinations(gate));
        }
    }

    public static void appendPearlInfo(ItemStack stack, Consumer<Component> builder) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;
        if (!SkyPearlData.dimension(stack).equals(player.level().dimension().location())) {
            builder.accept(Component.translatable("ttmisc.differentDim").withStyle(ChatFormatting.RED));
        } else {
            Vec3 origin = player.position().add(0, player.getBbHeight() / 2d, 0);
            boolean simple = false;
            if (mc.screen instanceof WarpGateScreen warpScreen) {
                var gate = warpScreen.getMenu().gate;
                for (int i = 0; i < gate.inventory.getContainerSize(); i++) {
                    if (gate.inventory.getItem(i) == stack) {
                        origin = Vec3.atLowerCornerOf(gate.getBlockPos());
                        simple = true;
                        break;
                    }
                }
            }
            builder.accept(Component.translatable("ttmisc.distance").append(": " + SkyPearlData.distance(stack, origin, simple) + "m").withStyle(ChatFormatting.BLUE));
        }
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
        return !pearl.isEmpty() && SkyPearlData.attuned(pearl) && gate.getLevel() != null && SkyPearlData.dimension(pearl).equals(gate.getLevel().dimension().location());
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (button == 0) {
            originX -= dx;
            originZ -= dy;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            center(gate.getBlockPos());
            return true;
        }
        int index = keyCode == GLFW.GLFW_KEY_0 ? 9 : keyCode - GLFW.GLFW_KEY_1;
        if (index >= 0 && index < 10) {
            var pearl = gate.inventory.getItem(index);
            if (visible(pearl)) center(SkyPearlData.position(pearl));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < 10; i++) {
                var pearl = gate.inventory.getItem(i);
                if (!visible(pearl)) continue;
                if (mouseX >= 3 && mouseX <= 220 && mouseY >= 52 + i * 11 && mouseY <= 64 + i * 11) {
                    PacketDistributor.sendToServer(new WarpGateNetwork.Payload(gate.getBlockPos(), i));
                    onClose();
                    return true;
                }
            }
            for (int i = 0; i < 10; i++) {
                var pearl = gate.inventory.getItem(i);
                if (!visible(pearl)) continue;
                var pos = SkyPearlData.position(pearl);
                double px = pos.getX() - originX, pz = pos.getZ() - originZ;
                double dx = mouseX - px;
                double dz = mouseY - pz;
                if (dx * dx + dz * dz <= 100) {
                    PacketDistributor.sendToServer(new WarpGateNetwork.Payload(gate.getBlockPos(), i));
                    onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        int rgb = Color.HSBtoRGB((float) (Math.sin(ticks / 150d) + .5), .5f, .4f);
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float gr = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        g.setColor(r, gr, b, 1.0f);
        g.blit(ResourceLocation.withDefaultNamespace("textures/entity/end_portal.png"), 0, 0, 0, 0, width, height, 256, 256);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        int gx = (int) (gate.getBlockPos().getX() - originX), gz = (int) (gate.getBlockPos().getZ() - originZ);
        int alpha = (int) (((Math.sin(ticks / 10d) + 1) / 4 + .25) * 255);
        for (int i = 0; i < 10; i++) {
            var pearl = gate.inventory.getItem(i);
            if (!visible(pearl)) continue;
            var p = SkyPearlData.position(pearl);
            int x = (int) (p.getX() - originX), z = (int) (p.getZ() - originZ);
            g.pose().pushPose();
            g.pose().translate(gx, gz, 0);
            g.pose().mulPose(Axis.ZP.rotation((float) Math.atan2(z - gz, x - gx)));
            g.fill(0, -1, (int) Math.hypot(x - gx, z - gz), 1, alpha << 24 | 0xFFFFFF);
            g.pose().popPose();
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
            g.drawString(font, Component.literal((i + 1) + ": ").append(name), 5, 54 + i * 11, -1, false);
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
        g.drawString(font, title.copy().withStyle(ChatFormatting.UNDERLINE), 3, 40, -1, false);
        g.drawCenteredString(font, Component.translatable("ttmisc.numberKeys"), width / 2, 5, -1);
        g.drawCenteredString(font, Component.translatable("ttmisc.spaceToReset"), width / 2, 16, -1);
        if (tooltip != null) g.renderComponentTooltip(font, tooltip, mx, my);
    }

    private static void icon(GuiGraphics g, ItemStack pearl, int x, int z) {
        g.pose().pushPose();
        g.pose().translate(x, z, 0);
        g.pose().scale(.5f, .5f, 1f);
        g.renderItem(new ItemStack(SkyPearl.ITEM.get()), -8, -8);
        g.pose().popPose();
    }
}
