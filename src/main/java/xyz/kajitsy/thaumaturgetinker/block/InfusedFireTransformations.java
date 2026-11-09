package xyz.kajitsy.thaumaturgetinker.block;

import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** Build 164's fixed transformations. Dynamic Ordo recipes belong to the world resolver. */
public final class InfusedFireTransformations {
 private static final EnumMap<InfusedFireKind,Map<Block,Block>> TABLE = new EnumMap<>(InfusedFireKind.class);
 static {
  for(var kind:InfusedFireKind.values()) TABLE.put(kind,new LinkedHashMap<>());
  add(InfusedFireKind.AIR,Blocks.SAND,Blocks.OAK_LOG,Blocks.SPRUCE_LOG,Blocks.BIRCH_LOG,Blocks.JUNGLE_LOG,Blocks.ACACIA_LOG,Blocks.DARK_OAK_LOG,Blocks.DIRT,Blocks.COARSE_DIRT,Blocks.PODZOL,Blocks.GRASS_BLOCK);
  add(InfusedFireKind.AIR,Blocks.SANDSTONE,Blocks.OAK_LEAVES,Blocks.SPRUCE_LEAVES,Blocks.BIRCH_LEAVES,Blocks.JUNGLE_LEAVES,Blocks.ACACIA_LEAVES,Blocks.DARK_OAK_LEAVES);
  add(InfusedFireKind.AIR,Blocks.GLASS,Blocks.ICE);
  add(InfusedFireKind.WATER,Blocks.ICE,Blocks.SAND,Blocks.RED_SAND,Blocks.SOUL_SAND,Blocks.GLOWSTONE);
  add(InfusedFireKind.WATER,Blocks.SNOW_BLOCK,Blocks.NETHERRACK);
  add(InfusedFireKind.WATER,Blocks.OBSIDIAN,Blocks.LAVA);
  add(InfusedFireKind.FIRE,Blocks.NETHERRACK,Blocks.GRASS_BLOCK,Blocks.DIRT,Blocks.COARSE_DIRT,Blocks.PODZOL);
  add(InfusedFireKind.FIRE,Blocks.SOUL_SAND,Blocks.SAND,Blocks.RED_SAND,Blocks.GRAVEL);
  add(InfusedFireKind.FIRE,Blocks.GLOWSTONE,Blocks.CLAY);
  add(InfusedFireKind.FIRE,Blocks.NETHER_QUARTZ_ORE,Blocks.COAL_ORE,Blocks.IRON_ORE,Blocks.DIAMOND_ORE,Blocks.EMERALD_ORE,Blocks.GOLD_BLOCK,Blocks.LAPIS_ORE,Blocks.REDSTONE_ORE);
  add(InfusedFireKind.FIRE,Blocks.NETHER_WART,Blocks.WHEAT,Blocks.POTATOES,Blocks.CARROTS);
  add(InfusedFireKind.FIRE,Blocks.BROWN_MUSHROOM,Blocks.POPPY,Blocks.BLUE_ORCHID,Blocks.ALLIUM,Blocks.AZURE_BLUET,Blocks.RED_TULIP,Blocks.ORANGE_TULIP,Blocks.WHITE_TULIP,Blocks.PINK_TULIP,Blocks.OXEYE_DAISY);
  add(InfusedFireKind.FIRE,Blocks.DANDELION,Blocks.DANDELION);
  add(InfusedFireKind.EARTH,Blocks.DIRT,Blocks.SAND,Blocks.RED_SAND,Blocks.STONE,Blocks.GRANITE,Blocks.POLISHED_GRANITE,Blocks.DIORITE,Blocks.POLISHED_DIORITE,Blocks.ANDESITE,Blocks.POLISHED_ANDESITE,Blocks.COBBLESTONE,Blocks.GLASS,Blocks.OAK_LOG,Blocks.SPRUCE_LOG,Blocks.BIRCH_LOG,Blocks.JUNGLE_LOG,Blocks.ACACIA_LOG,Blocks.DARK_OAK_LOG,Blocks.OAK_LEAVES,Blocks.SPRUCE_LEAVES,Blocks.BIRCH_LEAVES,Blocks.JUNGLE_LEAVES,Blocks.ACACIA_LEAVES,Blocks.DARK_OAK_LEAVES,Blocks.OAK_PLANKS,Blocks.SPRUCE_PLANKS,Blocks.BIRCH_PLANKS,Blocks.JUNGLE_PLANKS,Blocks.ACACIA_PLANKS,Blocks.DARK_OAK_PLANKS);
  add(InfusedFireKind.EARTH,Blocks.CLAY,Blocks.GRAVEL);
  add(InfusedFireKind.EARTH,Blocks.OAK_PLANKS,Blocks.NETHER_BRICKS);
  add(InfusedFireKind.EARTH,Blocks.OAK_FENCE,Blocks.NETHER_BRICK_FENCE);
  add(InfusedFireKind.EARTH,Blocks.OAK_STAIRS,Blocks.NETHER_BRICK_STAIRS);
  add(InfusedFireKind.EARTH,Blocks.OAK_LOG,Blocks.CACTUS);
  // Old tallgrass metadata 0 is the dead shrub, not metadata 1's grass.
  add(InfusedFireKind.EARTH,Blocks.DEAD_BUSH,Blocks.SNOW);
  add(InfusedFireKind.EARTH,Blocks.IRON_BLOCK,Blocks.SPAWNER);
 }
 private static void add(InfusedFireKind kind,Block result,Block... inputs){for(var input:inputs)TABLE.get(kind).put(input,result);}
 public static BlockState fixed(InfusedFireKind kind,BlockState state,boolean cakeEnabled){
  if(state.is(Blocks.WATER)&&state.getFluidState().isSource()) {
   if(kind==InfusedFireKind.AIR&&cakeEnabled)return Blocks.CAKE.defaultBlockState();
   if(kind==InfusedFireKind.FIRE)return Blocks.LAVA.defaultBlockState();
  }
  var result=TABLE.get(kind).get(state.getBlock());
  return result==null?null:result.defaultBlockState();
 }
 public static boolean isResult(InfusedFireKind kind,BlockState state,boolean cakeEnabled){return TABLE.get(kind).containsValue(state.getBlock())||(kind==InfusedFireKind.AIR&&cakeEnabled&&state.is(Blocks.CAKE))||(kind==InfusedFireKind.FIRE&&state.is(Blocks.LAVA));}
 private InfusedFireTransformations(){}
}
