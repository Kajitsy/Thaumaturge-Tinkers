package xyz.kajitsy.thaumaturgetinker.network;
import net.minecraft.world.entity.player.Player;
@net.neoforged.fml.common.EventBusSubscriber(modid="thaumaturgetinker")
public final class AwakenedArmorStatus {
 public static boolean enabled(Player player){var tag=player.getPersistentData().getCompoundOrEmpty("ThaumicTinkerer");return tag.getBooleanOr("GemArmor",true);}
 public static void set(Player player,boolean value){var data=player.getPersistentData();var tag=data.getCompoundOrEmpty("ThaumicTinkerer");tag.putBoolean("GemArmor",value);data.put("ThaumicTinkerer",tag);}
 @net.neoforged.fml.common.EventBusSubscriber(modid="thaumaturgetinker")public static final class Login {@net.neoforged.bus.api.SubscribeEvent public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,new AwakenedArmorStatusPayload(enabled(player)));}}
 public record Payload(boolean enabled) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
  public static final Type<Payload> TYPE=new Type<>(net.minecraft.resources.Identifier.fromNamespaceAndPath("thaumaturgetinker","armor_status"));
  public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf,Payload> CODEC=net.minecraft.network.codec.StreamCodec.composite(net.minecraft.network.codec.ByteBufCodecs.BOOL,Payload::enabled,Payload::new);
  public Type<Payload> type(){return TYPE;}
 }
 @net.neoforged.bus.api.SubscribeEvent public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event){
  net.neoforged.neoforge.network.handling.IPayloadHandler<AwakenedArmorStatusPayload> handler = (packet,context)->context.enqueueWork(()->set(context.player(),packet.enabled()));
  event.registrar("1").playBidirectional(AwakenedArmorStatusPayload.TYPE,AwakenedArmorStatusPayload.CODEC,handler,handler);
 }
}
