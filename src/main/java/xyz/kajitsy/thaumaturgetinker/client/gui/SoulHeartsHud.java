package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.network.SoulHearts;


import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = "thaumaturgetinker", value = Dist.CLIENT)
public final class SoulHeartsHud {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("thaumaturgetinker", "textures/gui/soulhearts.png");
    public static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath("thaumaturgetinker", "soul_hearts");

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.ARMOR_LEVEL, LAYER_ID, SoulHeartsHud::renderLayer);
    }

    public static void renderLayer(GuiGraphicsExtractor gui, DeltaTracker deltaTracker) {
        if (!KamiStartupConfig.enabled()) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !mc.gameMode.canHurtPlayer()) return;

        int hp = SoulHearts.get(mc.player);
        if (hp <= 0) return;

        draw(gui, mc.player, hp, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    }

    public static void draw(GuiGraphicsExtractor gui, Player player, int hp, int width, int height) {
        int left = width / 2 - 91;
        int top = height - 39;

        float maxHealth = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
        int absorption = Mth.ceil(player.getAbsorptionAmount());
        int healthRows = Mth.ceil((maxHealth + (float) absorption) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        int y = top - (healthRows - 1) * rowHeight - 10;
        if (player.getArmorValue() > 0) {
            y -= 10;
        }

        for (int i = 0, it = 0; i < hp; i++) {
            boolean half = (i == hp - 1 && hp % 2 != 0);
            if (!half && i % 2 != 0) continue;

            int row = it / 10;
            int col = it % 10;
            int heartX = left + col * 8;
            int heartY = y - row * 10;

            gui.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, heartX, heartY, half ? 9 : 0, 0, half ? 7 : 9, 9, 16, 16);
            it++;
        }
    }
}
