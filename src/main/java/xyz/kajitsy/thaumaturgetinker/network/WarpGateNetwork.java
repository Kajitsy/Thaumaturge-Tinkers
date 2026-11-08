package xyz.kajitsy.thaumaturgetinker.network;

import xyz.kajitsy.thaumaturgetinker.block.entity.WarpGateEntity;

@net.neoforged.fml.common.EventBusSubscriber(modid="thaumaturgetinker")
public final class WarpGateNetwork {
 public static boolean handle(net.minecraft.server.level.ServerPlayer player,WarpGateTeleportPayload request){if(request.index()<0||request.index()>=10||!player.level().isLoaded(request.gate()))return false;if(!(player.level().getBlockEntity(request.gate()) instanceof WarpGateEntity gate)||!gate.valid(player)||!player.level().mayInteract(player,request.gate()))return false;return gate.teleport(player,request.index());}
 @net.neoforged.bus.api.SubscribeEvent public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event){event.registrar("1").playToServer(WarpGateTeleportPayload.TYPE,WarpGateTeleportPayload.CODEC,(packet,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer player)handle(player,packet);}));}
}
