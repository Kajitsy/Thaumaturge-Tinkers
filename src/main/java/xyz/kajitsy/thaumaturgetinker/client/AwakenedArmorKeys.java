package xyz.kajitsy.thaumaturgetinker.client;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedArmorItem;
import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;
import xyz.kajitsy.thaumaturgetinker.network.AwakenedArmorStatus;

@net.neoforged.fml.common.EventBusSubscriber(modid = "thaumaturgetinker", value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class AwakenedArmorKeys {
    public static final net.minecraft.client.KeyMapping.Category CATEGORY = new net.minecraft.client.KeyMapping.Category(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "keys"));
    public static final net.minecraft.client.KeyMapping TOGGLE = new net.minecraft.client.KeyMapping("ttmisc.toggleArmor", org.lwjgl.glfw.GLFW.GLFW_KEY_U, CATEGORY);
    public static final net.minecraft.client.KeyMapping OPEN_POUCH = new net.minecraft.client.KeyMapping("ttmisc.openPouch", org.lwjgl.glfw.GLFW.GLFW_KEY_B, CATEGORY);

    @net.neoforged.bus.api.SubscribeEvent
    public static void register(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(TOGGLE);
        event.register(OPEN_POUCH);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        while (TOGGLE.consumeClick()) {
            if (mc.player == null || mc.screen != null) continue;
            boolean worn = false;
            for (var slot : net.minecraft.world.entity.EquipmentSlotGroup.ARMOR) {
                if (mc.player.getItemBySlot(slot).getItem() instanceof AwakenedArmorItem) worn = true;
            }
            if (!worn) continue;
            boolean next = !AwakenedArmorStatus.enabled(mc.player);
            AwakenedArmorStatus.set(mc.player, next);
            mc.player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable(next ? "ttmisc.enableAllArmor" : "ttmisc.disableAllArmor"));
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new AwakenedArmorStatus.Payload(next));
        }
        while (OPEN_POUCH.consumeClick()) {
            if (mc.player == null || mc.screen != null || !KamiStartupConfig.enabled()) continue;
            if (IchorPouch.findSlot(mc.player) != -99) {
                net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new IchorPouch.OpenPayload());
            }
        }
    }
}
