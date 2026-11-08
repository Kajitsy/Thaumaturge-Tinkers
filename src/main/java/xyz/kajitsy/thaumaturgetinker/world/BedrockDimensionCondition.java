package xyz.kajitsy.thaumaturgetinker.world;

import xyz.kajitsy.thaumaturgetinker.KamiEnabledCondition;
import xyz.kajitsy.thaumaturgetinker.TinkererConfig;

public record BedrockDimensionCondition() implements net.neoforged.neoforge.common.conditions.ICondition {
 public static final com.mojang.serialization.MapCodec<BedrockDimensionCondition> CODEC=com.mojang.serialization.MapCodec.unit(new BedrockDimensionCondition());
 private static final net.neoforged.neoforge.registries.DeferredRegister<com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.conditions.ICondition>> REGISTER=net.neoforged.neoforge.registries.DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.CONDITION_SERIALIZERS,"thaumaturgetinker");
 static {REGISTER.register("bedrock_enabled",()->CODEC);REGISTER.register("kami_enabled",()->KamiEnabledCondition.CODEC);}
 public static void init(net.neoforged.bus.api.IEventBus bus){REGISTER.register(bus);}
 public static boolean enabled(){return TinkererConfig.bedrockEnabled();}
 @Override public boolean test(IContext context){return enabled();}
 @Override public com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.conditions.ICondition> codec(){return CODEC;}
}
