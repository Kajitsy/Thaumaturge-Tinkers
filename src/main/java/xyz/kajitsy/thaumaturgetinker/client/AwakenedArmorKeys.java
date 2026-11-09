package xyz.kajitsy.thaumaturgetinker.client;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedArmorItem;
import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;
import xyz.kajitsy.thaumaturgetinker.network.AwakenedArmorStatus;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "thaumaturgetinker", value = Dist.CLIENT)
public final class AwakenedArmorKeys {
    public static final String CATEGORY = "key.categories.thaumaturgetinker";
    public static final KeyMapping TOGGLE = new KeyMapping("ttmisc.toggleArmor", GLFW.GLFW_KEY_U, CATEGORY);
    public static final KeyMapping OPEN_POUCH = new KeyMapping("ttmisc.openPouch", GLFW.GLFW_KEY_B, CATEGORY);

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE);
        event.register(OPEN_POUCH);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        while (TOGGLE.consumeClick()) {
            if (mc.player == null || mc.screen != null) continue;
            boolean worn = false;
            for (var slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    if (mc.player.getItemBySlot(slot).getItem() instanceof AwakenedArmorItem) worn = true;
                }
            }
            if (!worn) continue;
            boolean next = !AwakenedArmorStatus.enabled(mc.player);
            AwakenedArmorStatus.set(mc.player, next);
            mc.player.sendOverlayMessage(Component.translatable(next ? "ttmisc.enableAllArmor" : "ttmisc.disableAllArmor"));
            PacketDistributor.sendToServer(new AwakenedArmorStatus.Payload(next));
        }
        while (OPEN_POUCH.consumeClick()) {
            if (mc.player == null || mc.screen != null || !KamiStartupConfig.enabled()) continue;
            if (IchorPouch.findSlot(mc.player) != -99) {
                PacketDistributor.sendToServer(new IchorPouch.OpenPayload());
            }
        }
    }
}
