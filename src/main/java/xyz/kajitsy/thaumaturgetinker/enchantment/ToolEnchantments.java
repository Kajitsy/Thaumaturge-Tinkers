package xyz.kajitsy.thaumaturgetinker.enchantment;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid="thaumaturgetinker")
public final class ToolEnchantments {
    public static final ResourceKey<Enchantment> FLAME=key("auto_smelt"),SOFT=key("desintegrate");
    public static final TagKey<net.minecraft.world.level.block.Block> WOOD=TagKey.create(Registries.BLOCK,id("wood_material"));
    private static Identifier id(String name){return Identifier.fromNamespaceAndPath("thaumaturgetinker",name);}
    private static ResourceKey<Enchantment> key(String name){return ResourceKey.create(Registries.ENCHANTMENT,id(name));}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public static void speed(net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event){
        HarvestSpeedEnchantments.speed(event);
        var player=event.getEntity();var stack=player.getMainHandItem();var registry=player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var flame=registry.get(FLAME).orElse(null);var soft=registry.get(SOFT).orElse(null);
        boolean flaming=flame!=null&&stack.getEnchantmentLevel(flame)>0,disintegrating=soft!=null&&stack.getEnchantmentLevel(soft)>0;if(!flaming&&!disintegrating)return;
        var pos=event.getPosition().orElse(player.blockPosition());float hardness=event.getState().getDestroySpeed(player.level(),pos);
        if(hardness>=0&&(flaming&&event.getState().is(WOOD)||disintegrating&&hardness<=1.5f)){
            if(!player.level().isClientSide())stack.hurtAndBreak(1,player,net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            event.setNewSpeed(Float.MAX_VALUE);
        }else event.setCanceled(true);
    }
    private ToolEnchantments(){}
}
