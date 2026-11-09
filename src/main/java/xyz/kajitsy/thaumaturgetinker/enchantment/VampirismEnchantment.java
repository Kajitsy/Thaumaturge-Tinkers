package xyz.kajitsy.thaumaturgetinker.enchantment;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid="thaumaturgetinker")
public final class VampirismEnchantment {
 public static final ResourceKey<Enchantment> KEY=ResourceKey.create(Registries.ENCHANTMENT,ResourceLocation.parse("thaumaturgetinker:vampirism"));
 @SubscribeEvent public static void incoming(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event){
  if(event.getEntity().level().isClientSide()||!(event.getSource().getEntity() instanceof LivingEntity attacker))return;
  var holder=attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(KEY).orElse(null);if(holder==null)return;
  int rank=attacker.getMainHandItem().getEnchantmentLevel(holder);if(rank<=0)return;
  attacker.heal(rank);var victim=event.getEntity();victim.level().playSound(null,victim.getX(),victim.getY(),victim.getZ(),com.leclowndu93150.thaumaturge.registry.TTSounds.ZAP.get(),net.minecraft.sounds.SoundSource.PLAYERS,.6f,1);
 }
 private VampirismEnchantment(){}
}
