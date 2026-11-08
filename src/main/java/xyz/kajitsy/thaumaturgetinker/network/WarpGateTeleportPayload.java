package xyz.kajitsy.thaumaturgetinker.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record WarpGateTeleportPayload(BlockPos gate, int index) implements CustomPacketPayload {
    public static final Type<WarpGateTeleportPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("thaumaturgetinker", "warp_gate_teleport"));
    public static final StreamCodec<FriendlyByteBuf, WarpGateTeleportPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            WarpGateTeleportPayload::gate,
            ByteBufCodecs.VAR_INT,
            WarpGateTeleportPayload::index,
            WarpGateTeleportPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
