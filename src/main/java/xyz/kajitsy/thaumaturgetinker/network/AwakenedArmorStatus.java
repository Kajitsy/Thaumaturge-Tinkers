package xyz.kajitsy.thaumaturgetinker.network;

import net.minecraft.world.entity.player.Player;

@net.neoforged.fml.common.EventBusSubscriber(modid = "thaumaturgetinker")
public final class AwakenedArmorStatus {
    public static boolean enabled(Player player) {
        var tag = player.getPersistentData().getCompoundOrEmpty("ThaumicTinkerer");
        return tag.getBooleanOr("GemArmor", true);
    }

    public static void set(Player player, boolean value) {
        var data = player.getPersistentData();
        var tag = data.getCompoundOrEmpty("ThaumicTinkerer");
        tag.putBoolean("GemArmor", value);
        data.put("ThaumicTinkerer", tag);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new Payload(enabled(player)));
        }
    }

    public record Payload(boolean enabled) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "armor_status"));
        public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Payload> CODEC = net.minecraft.network.codec.StreamCodec.composite(net.minecraft.network.codec.ByteBufCodecs.BOOL, Payload::enabled, Payload::new);
        @Override public Type<Payload> type() { return TYPE; }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        net.neoforged.neoforge.network.handling.IPayloadHandler<Payload> handler = (packet, context) -> context.enqueueWork(() -> set(context.player(), packet.enabled()));
        event.registrar("1").playBidirectional(Payload.TYPE, Payload.CODEC, handler, handler);
    }
}
