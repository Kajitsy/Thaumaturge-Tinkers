package xyz.kajitsy.thaumaturgetinker.enchantment;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber(modid = "thaumaturgetinker")
public final class HarvestSpeedEnchantments {
    public static final ResourceKey<Enchantment> TUNNEL = key("tunnel"), SHATTER = key("shatter");

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", name));
    }

    @SubscribeEvent
    public static void speed(PlayerEvent.BreakSpeed event) {
        if (!(event.getEntity() instanceof Player player)) return;
        var stack = player.getMainHandItem();
        var registry = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        int shatter = stack.getEnchantmentLevel(registry.getOrThrow(SHATTER));
        int tunnel = stack.getEnchantmentLevel(registry.getOrThrow(TUNNEL));
        float speed = event.getNewSpeed();
        if (shatter > 0) {
            speed = event.getState().getDestroySpeed(player.level(), event.getPosition().orElse(player.blockPosition())) > 20
                    ? speed * (3 * shatter)
                    : (float) (speed * .8);
        }
        var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tunnel > 0 && data.contains("TTEnchantTunnelDir")) {
            float difference = Math.abs(data.getFloatOr("TTEnchantTunnelDir", 0f) - player.getYRot());
            speed = (float) (speed * (difference < 50 ? 1 + .2 * tunnel : .3));
        }
        event.setNewSpeed(speed);
    }

    @SubscribeEvent
    public static void broken(BreakBlockEvent event) {
        var player = event.getPlayer();
        if (player.level().isClientSide()) return;
        var stack = player.getMainHandItem();
        var holder = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TUNNEL);
        if (stack.getEnchantmentLevel(holder) > 0) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putFloat("TTEnchantTunnelDir", player.getYRot()));
        }
    }

    private HarvestSpeedEnchantments() {}
}
