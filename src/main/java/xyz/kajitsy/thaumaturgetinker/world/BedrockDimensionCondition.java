package xyz.kajitsy.thaumaturgetinker.world;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.TinkererConfig;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public record BedrockDimensionCondition() implements ICondition {
    public static final MapCodec<BedrockDimensionCondition> CODEC = MapCodec.unit(new BedrockDimensionCondition());
    private static final DeferredRegister<MapCodec<? extends ICondition>> REGISTER = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, "thaumaturgetinker");

    public record KamiEnabled() implements ICondition {
        public static final MapCodec<KamiEnabled> CODEC = MapCodec.unit(new KamiEnabled());
        @Override public boolean test(IContext context) { return KamiStartupConfig.enabled(); }
        @Override public MapCodec<? extends ICondition> codec() { return CODEC; }
    }

    static {
        REGISTER.register("bedrock_enabled", () -> CODEC);
        REGISTER.register("kami_enabled", () -> KamiEnabled.CODEC);
    }

    public static void init(IEventBus bus) {
        REGISTER.register(bus);
    }

    public static boolean enabled() {
        return TinkererConfig.bedrockEnabled();
    }

    @Override
    public boolean test(IContext context) {
        return enabled();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
