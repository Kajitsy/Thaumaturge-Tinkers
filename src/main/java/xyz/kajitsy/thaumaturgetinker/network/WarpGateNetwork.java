package xyz.kajitsy.thaumaturgetinker.network;

import xyz.kajitsy.thaumaturgetinker.block.entity.WarpGateEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@net.neoforged.fml.common.EventBusSubscriber(modid = "thaumaturgetinker")
public final class WarpGateNetwork {
    public record Payload(BlockPos gate, int index) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "warp_gate_teleport"));
        public static final StreamCodec<FriendlyByteBuf, Payload> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, Payload::gate, ByteBufCodecs.VAR_INT, Payload::index, Payload::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static boolean handle(net.minecraft.server.level.ServerPlayer player, Payload request) {
        if (request.index() < 0 || request.index() >= 10 || !player.level().isLoaded(request.gate())) return false;
        if (!(player.level().getBlockEntity(request.gate()) instanceof WarpGateEntity gate) || !gate.valid(player) || !player.level().mayInteract(player, request.gate())) return false;
        return gate.teleport(player, request.index());
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(Payload.TYPE, Payload.CODEC, (packet, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) handle(player, packet);
        }));
    }
}
