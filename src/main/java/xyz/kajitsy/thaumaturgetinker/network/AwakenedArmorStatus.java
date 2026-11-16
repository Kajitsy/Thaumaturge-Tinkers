package xyz.kajitsy.thaumaturgetinker.network;

import net.minecraft.world.entity.player.Player;

@net.neoforged.fml.common.EventBusSubscriber(modid = "thaumaturgetinker")
public final class AwakenedArmorStatus {
    public static boolean enabled(Player player) {
        var root = player.getPersistentData();
        var persisted = root.getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
        var tag = persisted.contains("ThaumicTinkerer")
                ? persisted.getCompoundOrEmpty("ThaumicTinkerer")
                : root.getCompoundOrEmpty("ThaumicTinkerer");
        return tag.getBooleanOr("GemArmor", true);
    }

    public static void set(Player player, boolean value) {
        var data = player.getPersistentData();
        var persisted = data.getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
        var tag = persisted.contains("ThaumicTinkerer")
                ? persisted.getCompoundOrEmpty("ThaumicTinkerer")
                : data.getCompoundOrEmpty("ThaumicTinkerer");
        tag.putBoolean("GemArmor", value);
        persisted.put("ThaumicTinkerer", tag);
        data.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static void sync(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer server && !(server instanceof net.neoforged.neoforge.common.util.FakePlayer)) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(server, new Payload(enabled(server)));
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        sync(event.getEntity());
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void changedDimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        sync(event.getEntity());
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        sync(event.getEntity());
    }

    public record Payload(boolean enabled) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(net.minecraft.resources.Identifier.fromNamespaceAndPath("thaumaturgetinker", "armor_status"));
        public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Payload> CODEC = net.minecraft.network.codec.StreamCodec.composite(net.minecraft.network.codec.ByteBufCodecs.BOOL, Payload::enabled, Payload::new);
        @Override public Type<Payload> type() { return TYPE; }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        net.neoforged.neoforge.network.handling.IPayloadHandler<Payload> handler = (packet, context) -> context.enqueueWork(() -> set(context.player(), packet.enabled()));
        event.registrar("1").playBidirectional(Payload.TYPE, Payload.CODEC, handler, handler);
    }
}
