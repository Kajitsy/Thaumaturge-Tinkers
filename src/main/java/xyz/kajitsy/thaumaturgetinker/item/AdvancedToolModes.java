package xyz.kajitsy.thaumaturgetinker.item;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
/** Original ToolHandler metadata modes and exclusive iteration bounds. */
public final class AdvancedToolModes {
 private AdvancedToolModes(){}
 public static int get(ItemStack stack){int mode=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr("ToolMode",0);return mode>=0&&mode<=2?mode:0;}
 public static int change(ItemStack stack){int mode=(get(stack)+1)%3;CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putInt("ToolMode",mode));return mode;}
 public static net.minecraft.network.chat.Component description(String type,ItemStack stack){return net.minecraft.network.chat.Component.translatable("ttmisc.mode."+type+"."+get(stack));}
 public static ToolBounds shovel(int mode,Direction face){return mode==2?new ToolBounds(0,-8,0,1,8,1):pick(mode,face);}
 public static ToolBounds pick(int mode,Direction face){if(mode==0)return new ToolBounds(0,0,0,1,1,1);if(mode==1){boolean x=face.getStepX()==0,y=face.getStepY()==0,z=face.getStepZ()==0;return new ToolBounds(x?-2:0,y?-1:0,z?-2:0,x?3:1,y?4:1,z?3:1);}if(mode==2){int x=-face.getStepX(),y=-face.getStepY(),z=-face.getStepZ();return new ToolBounds(x>=0?0:-10,y>=0?0:-10,z>=0?0:-10,x>0?10:1,y>0?10:1,z>0?10:1);}throw new IllegalArgumentException("Unknown advanced pick mode "+mode);}
}
