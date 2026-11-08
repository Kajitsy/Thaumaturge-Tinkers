package xyz.kajitsy.thaumaturgetinker.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SoulHeartsPayload(int hearts) implements CustomPacketPayload {
    public static final Type<SoulHeartsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("thaumaturgetinker", "soul_hearts"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SoulHeartsPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SoulHeartsPayload::hearts,
            SoulHeartsPayload::new
    );

    @Override
    public Type<SoulHeartsPayload> type() {
        return TYPE;
    }
}
