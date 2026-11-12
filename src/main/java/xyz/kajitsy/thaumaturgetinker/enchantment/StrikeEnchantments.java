package xyz.kajitsy.thaumaturgetinker.enchantment;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid="thaumaturgetinker")
public final class StrikeEnchantments {
 public static final ResourceKey<Enchantment> FOCUSED=key("focused_strike"),DISPERSED=key("dispersed_strikes"),VALIANCE=key("valiance"),FINAL=key("final_strike");
 private static ResourceKey<Enchantment> key(String name){return ResourceKey.create(Registries.ENCHANTMENT,Identifier.fromNamespaceAndPath("thaumaturgetinker",name));}
 public static float criticalDamage(float input,int finalRank,int valianceRank,float health,float maxHealth,java.util.function.IntUnaryOperator random){
  if(finalRank>0&&random.applyAsInt(20-finalRank)==0)input*=3;
  if(valianceRank>0&&health/maxHealth<.5f)input=(float)(input*(1+.1*valianceRank));
  return input;
 }
 public static float strikeDamage(float baseDamage,int focused,int dispersed,int successive){
  float damage=baseDamage;
  if(focused>0){damage/=2;damage=(float)(damage+0.5*successive*damage*focused);}
  if(dispersed>0){damage*=1+successive/5.0;damage/=1+successive*2;}
  return damage;
 }
 @SubscribeEvent public static void incoming(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event){
  if(event.getEntity().level().isClientSide()||!(event.getSource().getEntity() instanceof LivingEntity attacker))return;
  var stack=attacker.getMainHandItem();var registry=attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
  FallCombatEnchantments.pounce(event,attacker);
  int finalRank=stack.getEnchantmentLevel(registry.getOrThrow(FINAL)),valianceRank=stack.getEnchantmentLevel(registry.getOrThrow(VALIANCE));
  if(finalRank>0||valianceRank>0)event.setAmount(criticalDamage(event.getAmount(),finalRank,valianceRank,attacker.getHealth(),attacker.getMaxHealth(),bound->new java.util.Random().nextInt(bound)));
  int focused=stack.getEnchantmentLevel(registry.getOrThrow(FOCUSED)),dispersed=stack.getEnchantmentLevel(registry.getOrThrow(DISPERSED));if(focused<=0&&dispersed<=0)return;
  var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();int target=event.getEntity().getId(),last=data.getIntOr("TTEnchantLastTarget",0),successive=data.getIntOr("TTEnchantSuccessiveStrike",0);
  if(last!=target){successive=0;}else{successive=successive+1;}
  data.putInt("TTEnchantSuccessiveStrike",successive);
  float damage=strikeDamage(event.getAmount(),focused,dispersed,successive);
  data.putInt("TTEnchantLastTarget",target);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));event.setAmount(damage);
 }
 private StrikeEnchantments(){}
}
