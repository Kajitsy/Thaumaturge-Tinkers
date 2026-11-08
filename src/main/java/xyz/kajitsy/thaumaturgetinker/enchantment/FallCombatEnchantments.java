package xyz.kajitsy.thaumaturgetinker.enchantment;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid="thaumaturgetinker")
public final class FallCombatEnchantments {
 public static final ResourceKey<Enchantment> POUNCE=key("pounce"),SHOCKWAVE=key("shockwave");
 private static ResourceKey<Enchantment> key(String name){return ResourceKey.create(Registries.ENCHANTMENT,Identifier.fromNamespaceAndPath("thaumaturgetinker",name));}
 public static void pounce(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event,LivingEntity attacker){
  if(!(attacker instanceof Player)||attacker.getMainHandItem().isEmpty())return;
  var enchant=attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(POUNCE);int rank=attacker.getItemBySlot(EquipmentSlot.LEGS).getEnchantmentLevel(enchant);
  if(rank>0&&attacker.level().getBlockState(attacker.blockPosition().below()).isAir())event.setAmount((float)(event.getAmount()*(1+.25*rank)));
 }
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOW) public static void fall(net.neoforged.neoforge.event.entity.living.LivingFallEvent event){
  if(!(event.getEntity() instanceof Player player)||!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)||event.getDistance()<=3)return;
  var enchant=player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SHOCKWAVE);int rank=player.getItemBySlot(EquipmentSlot.FEET).getEnchantmentLevel(enchant);if(rank<=0)return;
  var area=new net.minecraft.world.phys.AABB(player.getX()-10,player.getY()-10,player.getZ()-10,player.getX()+10,player.getY()+10,player.getZ()+10);
  for(var target:serverLevel.getEntitiesOfClass(LivingEntity.class,area))if(target!=player)target.hurtServer(serverLevel,player.damageSources().fall(),(float)(.1f*rank*event.getDistance()));
 }
 private FallCombatEnchantments(){}
}
