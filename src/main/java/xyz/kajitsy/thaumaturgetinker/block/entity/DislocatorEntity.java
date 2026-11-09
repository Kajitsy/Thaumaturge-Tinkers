package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.DislocatorBlock;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
public final class DislocatorEntity extends BlockEntity implements MovableTile, TransvectorLink, CamouflagedTile {
    private BlockPos target;
    private BlockState camouflage;
    int cooldown;boolean pulseStored;
    public DislocatorEntity(BlockPos pos,BlockState state){super(ThaumicTinkererModern.DISLOCATOR_ENTITY.get(),pos,state);}
    public BlockPos target(){return target;}
    @Override public BlockState camouflage(){return camouflage;}
    public void camouflage(BlockState state){camouflage=state;changed();}
    void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    public boolean inRange(BlockPos pos){return !pos.equals(worldPosition)&&Math.abs((long)pos.getX()-worldPosition.getX())<=16&&Math.abs((long)pos.getY()-worldPosition.getY())<=16&&Math.abs((long)pos.getZ()-worldPosition.getZ())<=16;}
    public boolean link(BlockPos pos){if(level==null||!inRange(pos)||!level.isLoaded(pos)||level.getBlockEntity(pos) instanceof TransvectorLink)return false;target=pos.immutable();changed();return true;}
    public static boolean movable(Level level,BlockPos pos){var state=level.getBlockState(pos);return !state.is(Blocks.BEDROCK)&&state.getDestroySpeed(level,pos)>=0&&!state.is(net.minecraft.tags.BlockTags.create(net.minecraft.resources.Identifier.fromNamespaceAndPath("thaumaturgetinker","dislocator_immovable")));}
    public void pulse(){
        if(!(level instanceof ServerLevel server)||target==null||!inRange(target)||!server.isLoaded(target))return;
        var adjacent=worldPosition.relative(getBlockState().getValue(DislocatorBlock.FACING));
        if(target.equals(adjacent)||server.isOutsideBuildHeight(target)||server.isOutsideBuildHeight(adjacent)||!server.isLoaded(adjacent))return;
        if(cooldown>0){pulseStored=true;setChanged();return;}
        var atTarget=server.getEntities((Entity)null,new AABB(target));var atAdjacent=server.getEntities((Entity)null,new AABB(adjacent));
        if(movable(server,target)&&movable(server,adjacent)){
            var firstState=server.getBlockState(target);var firstTile=server.getBlockEntity(target);var firstData=firstTile==null?null:firstTile.saveWithFullMetadata(server.registryAccess());
            var secondState=server.getBlockState(adjacent);var secondTile=server.getBlockEntity(adjacent);var secondData=secondTile==null?null:secondTile.saveWithFullMetadata(server.registryAccess());
            // Remove tiles before changing states so vanilla onRemove cannot spill inventories.
            server.removeBlockEntity(target);server.removeBlockEntity(adjacent);
            MovableTile.install(server,target,secondState,secondData);MovableTile.install(server,adjacent,firstState,firstData);
            server.updateNeighborsAt(target,secondState.getBlock());server.updateNeighborsAt(adjacent,firstState.getBlock());
        }
        var delta=net.minecraft.world.phys.Vec3.atLowerCornerOf(adjacent.subtract(target));
        for(var entity:atTarget)move(entity,delta);
        for(var entity:atAdjacent)if(!atTarget.contains(entity))move(entity,delta.scale(-1));
        cooldown=10;setChanged();
    }
    private static void move(Entity entity,net.minecraft.world.phys.Vec3 delta){var end=entity.position().add(delta);if(entity instanceof ServerPlayer player)player.connection.teleport(end.x,end.y,end.z,player.getYRot(),player.getXRot());else entity.teleportTo(end.x,end.y,end.z);}
    public static void tick(Level level,BlockPos pos,BlockState state,DislocatorEntity tile){if(level.isClientSide())return;if(tile.cooldown>0){tile.cooldown--;tile.setChanged();}if(tile.cooldown==0&&tile.pulseStored){tile.pulseStored=false;tile.pulse();}}
    @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output){super.saveAdditional(output);if(target!=null)output.putLong("Target",target.asLong());if(camouflage!=null)output.store("Camouflage",BlockState.CODEC,camouflage);output.putInt("Cooldown",cooldown);output.putBoolean("PulseStored",pulseStored);}
    @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input){super.loadAdditional(input);target=input.getLong("Target").map(BlockPos::of).orElse(null);camouflage=input.read("Camouflage",BlockState.CODEC).orElse(null);cooldown=Math.max(0,input.getIntOr("Cooldown",0));pulseStored=input.getBooleanOr("PulseStored",false);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
}
