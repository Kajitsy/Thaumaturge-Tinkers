package xyz.kajitsy.thaumaturgetinker.block;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/** Recipe-driven Ordo conversion. No items are removed/created while resolving. */
public final class OrderFireResolver {
 private static final Map<ServerLevel,Map<BlockState,Optional<BlockState>>> CACHE=new WeakHashMap<>();
 public static void register(){NeoForge.EVENT_BUS.addListener(OrderFireResolver::reloadListeners);}
 private static void reloadListeners(AddReloadListenerEvent event){
  event.addListener((ResourceManagerReloadListener) resources -> clear());
 }
 public static void clear(){CACHE.clear();}
 public static boolean isResult(ServerLevel level,BlockState state){return CACHE.getOrDefault(level,Map.of()).values().stream().anyMatch(value->value.isPresent()&&value.get().getBlock()==state.getBlock());}
 public static BlockState result(ServerLevel level,BlockPos pos){
  if(!level.isLoaded(pos))return null;
  var state=level.getBlockState(pos);
  // Build 164 seeds these entries with null so they can never become targets.
  if(excluded(state))return null;
  var cache=CACHE.computeIfAbsent(level,ignored->new HashMap<>());
  return cache.computeIfAbsent(state,ignored->Optional.ofNullable(resolve(level,state,drops(level,pos,state)))).orElse(null);
 }
 private static boolean excluded(BlockState state){
  return state.is(Blocks.STONE)||state.is(Blocks.GRANITE)||state.is(Blocks.POLISHED_GRANITE)||state.is(Blocks.DIORITE)||state.is(Blocks.POLISHED_DIORITE)||state.is(Blocks.ANDESITE)||state.is(Blocks.POLISHED_ANDESITE)||state.is(Blocks.GLASS)||state.is(Blocks.SAND)||state.is(Blocks.RED_SAND)||state.is(Blocks.GRAVEL);
 }
 private static List<ItemStack> drops(ServerLevel level,BlockPos pos,BlockState state){
  return Block.getDrops(state,level,pos,level.getBlockEntity(pos));
 }
 public static int decayChance(ServerLevel level,BlockPos pos){return decayChance(drops(level,pos,level.getBlockState(pos)).size());}
 public static int decayChance(int dropEntries){return dropEntries==2?2:dropEntries>=3?1:3;}
 public static BlockState resolve(ServerLevel level,BlockState state,List<ItemStack> drops){
  if(excluded(state)||drops.isEmpty())return null;
  var first=drops.getFirst();
  if(first.isEmpty()||drops.stream().anyMatch(s->!ItemStack.isSameItem(first,s)))return null;
  // Before raw ores existed, vanilla ores dropped the ore block itself (or smeltable item).
  // Preserve that source conversion instead of inventing a raw-metal-block result.
  var input=normalizeRawOreInput(state,first);
  var output=craftNine(level,input);
  if(output.isEmpty()){
   var single=new SingleRecipeInput(input);
   var smelt=level.getRecipeManager().getRecipeFor(RecipeType.SMELTING,single,level);
   output=smelt.map(r->craftNine(level,r.value().assemble(single,level.registryAccess()))).orElse(ItemStack.EMPTY);
  }
  return output.getItem() instanceof BlockItem block?block.getBlock().defaultBlockState():null;
 }
 public static ItemStack normalizeRawOreInput(BlockState state,ItemStack drop){
  if((state.is(Blocks.IRON_ORE)||state.is(Blocks.DEEPSLATE_IRON_ORE))&&drop.is(Items.RAW_IRON))return new ItemStack(Items.IRON_ORE);
  if((state.is(Blocks.GOLD_ORE)||state.is(Blocks.DEEPSLATE_GOLD_ORE))&&drop.is(Items.RAW_GOLD))return new ItemStack(Items.GOLD_ORE);
  if((state.is(Blocks.COPPER_ORE)||state.is(Blocks.DEEPSLATE_COPPER_ORE))&&drop.is(Items.RAW_COPPER))return new ItemStack(Items.COPPER_ORE);
  return drop.copy();
 }
 public static ItemStack craftNine(ServerLevel level,ItemStack stack){
  if(stack.isEmpty())return ItemStack.EMPTY;
  var slots=new ArrayList<ItemStack>(9);for(int i=0;i<9;i++)slots.add(stack.copy());
  var input=CraftingInput.of(3,3,slots);
  return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,level).map(r->r.value().assemble(input,level.registryAccess())).orElse(ItemStack.EMPTY);
 }
 private OrderFireResolver(){}
}
