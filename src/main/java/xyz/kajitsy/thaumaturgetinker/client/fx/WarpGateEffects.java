package xyz.kajitsy.thaumaturgetinker.client.fx;
/** Original proxy calls consume world RNG even when the dedicated proxy has no renderer. */
public final class WarpGateEffects {
 public static void sparkle(net.minecraft.server.level.ServerPlayer player){
  var points=new java.util.ArrayList<net.minecraft.world.phys.Vec3>(20);var random=player.level().getRandom();
  for(int i=0;i<20;i++)points.add(new net.minecraft.world.phys.Vec3((float)player.getX()+random.nextFloat()-.5f,(float)player.getY()+random.nextFloat(),(float)player.getZ()+random.nextFloat()-.5f));
  if(net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())WarpGateFx.emit(player.level().dimension(),points);
 }
}
