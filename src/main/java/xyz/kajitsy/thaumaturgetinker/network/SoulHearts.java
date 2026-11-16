package xyz.kajitsy.thaumaturgetinker.network;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.item.IchorSword;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/** Original separate 0..20 soul-health reserve, never vanilla absorption. */
@net.neoforged.fml.common.EventBusSubscriber(modid = "thaumaturgetinker")
public final class SoulHearts {
    public record Payload(int hearts) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("thaumaturgetinker", "soul_hearts"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC = StreamCodec.composite(ByteBufCodecs.INT, Payload::hearts, Payload::new);
        @Override public Type<Payload> type() { return TYPE; }
    }

    private static net.minecraft.nbt.CompoundTag readTag(Player player) {
        var root = player.getPersistentData();
        var persisted = root.getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
        if (persisted.contains("ThaumicTinkerer")) {
            return persisted.getCompoundOrEmpty("ThaumicTinkerer");
        }
        return root.getCompoundOrEmpty("ThaumicTinkerer");
    }

    public static int get(Player player) {
        return readTag(player).getIntOr("soulHearts", 0);
    }

    public static void set(Player player, int value) {
        var data = player.getPersistentData();
        var persisted = data.getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
        var tag = persisted.contains("ThaumicTinkerer")
                ? persisted.getCompoundOrEmpty("ThaumicTinkerer")
                : data.getCompoundOrEmpty("ThaumicTinkerer");
        tag.putInt("soulHearts", Math.clamp(value, 0, 20));
        persisted.put("ThaumicTinkerer", tag);
        data.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static void add(Player player) {
        set(player, get(player) + 1);
        sync(player);
    }

    public static int remove(Player player, int amount) {
        int next = get(player) - amount;
        set(player, Math.max(0, next));
        return Math.max(0, -next);
    }

    public static void sync(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer server && !(server instanceof net.neoforged.neoforge.common.util.FakePlayer)) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(server, new Payload(get(player)));
        }
    }

    public record AbsorbResult(int remainingHearts, float remainingDamage) {}

    public static AbsorbResult absorbAmount(int hearts, float amount) {
        if (hearts <= 0 || amount <= 0.0f) return new AbsorbResult(Math.max(0, hearts), amount);
        int absorbed = Math.min(hearts, (int) Math.ceil(amount));
        return new AbsorbResult(hearts - absorbed, Math.max(0.0f, amount - absorbed));
    }

    public static float absorb(Player player, float amount) {
        if (!KamiStartupConfig.enabled() || amount <= 0) return amount;
        int hearts = get(player);
        if (hearts <= 0) return amount;
        AbsorbResult result = absorbAmount(hearts, amount);
        set(player, result.remainingHearts());
        sync(player);
        return result.remainingDamage();
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (KamiStartupConfig.enabled()) sync(event.getEntity());
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void changedDimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        if (KamiStartupConfig.enabled()) sync(event.getEntity());
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        if (KamiStartupConfig.enabled()) sync(event.getEntity());
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void damage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            float remaining = absorb(player, event.getNewDamage());
            event.setNewDamage(IchorSword.reduce(player, event.getSource(), remaining));
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Payload.TYPE, Payload.CODEC, (packet, context) -> context.enqueueWork(() -> set(context.player(), packet.hearts())));
    }
}
