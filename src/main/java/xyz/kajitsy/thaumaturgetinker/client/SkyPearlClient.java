package xyz.kajitsy.thaumaturgetinker.client;

import xyz.kajitsy.thaumaturgetinker.SkyPearlData;
import xyz.kajitsy.thaumaturgetinker.client.gui.WarpGateScreen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.function.Consumer;

public final class SkyPearlClient {
    public static void info(ItemStack stack, Consumer<Component> builder) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;
        if (!SkyPearlData.dimension(stack).equals(player.level().dimension().identifier())) {
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
}
