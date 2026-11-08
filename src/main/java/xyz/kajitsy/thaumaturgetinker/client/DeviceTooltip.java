package xyz.kajitsy.thaumaturgetinker.client;

import xyz.kajitsy.thaumaturgetinker.block.MagnetBlock;
import xyz.kajitsy.thaumaturgetinker.block.entity.AnimationTabletEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.MagnetEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.RepairerEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.TransvectorEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.WarpGateEntity;
import xyz.kajitsy.thaumaturgetinker.item.SoulMouldItem;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
/** Original Waila information, generated from authoritative device state. */
public final class DeviceTooltip {
 public static Component text(String key,Object... args){return Component.translatable("ttwaila."+key,args);}
 public static java.util.List<Component> lines(BlockEntity tile){
  var lines=new java.util.ArrayList<Component>();
  if(tile instanceof AnimationTabletEntity tablet){
   var stack=tablet.inventory.getItem(0);lines.add(text("currentTool",stack.isEmpty()?text("nothing"):stack.getHoverName()));
   if(!stack.isEmpty()){lines.add(text(tablet.leftClick?"leftClick":"rightClick"));lines.add(text(tablet.redstone?"redstone":"autonomous"));}
  }else if(tile instanceof TransvectorEntity device){
   var level=device.getLevel();var pos=device.target();net.minecraft.world.level.block.state.BlockState target=null;
   if(level!=null&&pos!=null&&device.inRange(pos)&&level.isLoaded(pos)){var s=level.getBlockState(pos);if(!s.isAir())target=s;}
   lines.add(text("connected",target==null?text("nothing"):target.getBlock().getName()));
   if(target!=null)lines.add(Component.literal("x: "+pos.getX()+" y: "+pos.getY()+" z: "+pos.getZ()));
  }else if(tile instanceof RepairerEntity repairer){
   var stack=repairer.inventory.getItem(0);if(!stack.isEmpty())lines.add(text(stack.getDamageValue()>0?"repairing":"finishedRepairing",stack.getHoverName()));
  }else if(tile instanceof WarpGateEntity gate){
   // Original provider inverted these labels; the actual gate rejects incoming when locked.
   lines.add(text(gate.locked?"disallowIncoming":"allowIncoming"));
  }else if(tile instanceof MagnetEntity magnet){
   boolean pulling=magnet.getBlockState().getValue(MagnetBlock.PULLING);lines.add(text(pulling?"pullingMode":"pushingMode"));
   if(magnet.mobs()){
    String key=(pulling?"pulling":"pushing")+(magnet.adult?"Adult":"Child");var filter=magnet.inventory.getStackInSlot(0);
    if(filter.isEmpty())lines.add(text(key));
    else{var id=net.minecraft.resources.Identifier.tryParse(SoulMouldItem.pattern(filter));var type=id==null?null:net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);lines.add(text(key+"Type",type==null?Component.translatable("ttmisc.soulMould.nonAssigned"):type.getDescription()));}
   }
  }
  return java.util.List.copyOf(lines);
 }
}
