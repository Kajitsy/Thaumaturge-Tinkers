package xyz.kajitsy.thaumaturgetinker.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AwakenedArmorStatusPayload(boolean enabled) implements CustomPacketPayload {
    public static final Type<AwakenedArmorStatusPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("thaumaturgetinker", "armor_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AwakenedArmorStatusPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            AwakenedArmorStatusPayload::enabled,
            AwakenedArmorStatusPayload::new
    );

    @Override
    public Type<AwakenedArmorStatusPayload> type() {
        return TYPE;
    }
}
