package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.SkyPearlData;
import xyz.kajitsy.thaumaturgetinker.block.WarpGateBlock;
import xyz.kajitsy.thaumaturgetinker.client.fx.WarpGateEffects;
import xyz.kajitsy.thaumaturgetinker.item.SkyPearl;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class WarpGateEntity extends BlockEntity {
 public boolean locked;private long lastTeleportTick=Long.MIN_VALUE;
 public final SimpleContainer inventory=new SimpleContainer(10){
  @Override public int getMaxStackSize(){return 1;}
  @Override public boolean canPlaceItem(int slot,ItemStack stack){return stack.is(SkyPearl.ITEM.get());}
  @Override public void setChanged(){super.setChanged();changed();}
 };
 public WarpGateEntity(BlockPos pos,BlockState state){super(WarpGateBlock.ENTITY.get(),pos,state);}
 public void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 public boolean valid(net.minecraft.world.entity.player.Player player){return level!=null&&level.getBlockEntity(worldPosition)==this&&player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition))<=64;}
 public boolean teleport(net.minecraft.server.level.ServerPlayer player,int index){
  if(level==null||level.isClientSide()||index<0||index>=10||lastTeleportTick==level.getGameTime())return false;
  var pearl=inventory.getItem(index);if(pearl.isEmpty()||!SkyPearlData.attuned(pearl))return false;
  var targetDim=SkyPearlData.dimension(pearl);
  var server=player.level().getServer();
  var targetLevel=server!=null?server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,targetDim)):null;
  if(targetLevel==null)targetLevel=player.level();
  if(!teleport(player,targetLevel,SkyPearlData.position(pearl)))return false;
  lastTeleportTick=level.getGameTime();
  return true;
 }
 public static boolean teleport(net.minecraft.server.level.ServerPlayer player,net.minecraft.server.level.ServerLevel targetLevel,BlockPos destination){
  if(!(targetLevel.getBlockEntity(destination) instanceof WarpGateEntity gate)){player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("ttmisc.noDest"));return false;}
  if(gate.locked){player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("ttmisc.noTeleport"));return false;}
  var sound=com.leclowndu93150.thaumaturge.registry.TTSounds.WAND.get();
  player.level().playSound(null,player.blockPosition(),sound,net.minecraft.sounds.SoundSource.PLAYERS,1,1);
  WarpGateEffects.sparkle(player);
  player.stopRiding();
  if(player.level()!=targetLevel){
   player.teleportTo(targetLevel,destination.getX()+.5,destination.getY()+1.0,destination.getZ()+.5,java.util.Set.of(),player.getYRot(),player.getXRot(),false);
  }else{
   player.teleportTo(destination.getX()+.5,destination.getY()+1.0,destination.getZ()+.5);
  }
  WarpGateEffects.sparkle(player);
  targetLevel.playSound(null,player.blockPosition(),sound,net.minecraft.sounds.SoundSource.PLAYERS,1,.1f);
  return true;
 }
 public static boolean teleport(net.minecraft.server.level.ServerPlayer player,BlockPos destination){
  return teleport(player,player.level(),destination);
 }
 @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){if(level!=null)net.minecraft.world.Containers.dropContents(level,pos,inventory);super.preRemoveSideEffects(pos,state);}
 @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output){super.saveAdditional(output);output.putBoolean("locked",locked);ContainerHelper.saveAllItems(output.child("Inventory"),inventory.getItems());}
 @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input){super.loadAdditional(input);locked=input.getBooleanOr("locked",false);inventory.clearContent();ContainerHelper.loadAllItems(input.childOrEmpty("Inventory"),inventory.getItems());lastTeleportTick=Long.MIN_VALUE;}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
 @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
}
