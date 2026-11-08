package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class LocomotiveEntity extends BlockEntity{
    boolean linked;BlockPos first,second;Direction direction=Direction.NORTH;long lastMove=Long.MIN_VALUE;
    public LocomotiveEntity(BlockPos pos,BlockState state){super(ThaumicTinkererModern.LOCOMOTIVE_ENTITY.get(),pos,state);}
    boolean verify(){if(!linked||first==null||second==null)return false;if(!level.isLoaded(first)||!level.isLoaded(second))return false;if(!(level.getBlockEntity(first) instanceof LocomotiveRelayEntity relay)||!relay.verify()||!second.equals(relay.partner)){linked=false;setChanged();return false;}return true;}
    public static void tick(Level level,BlockPos pos,BlockState state,LocomotiveEntity tile){
        if(!(level instanceof ServerLevel server)||!tile.verify()||level.hasNeighborSignal(pos))return;
        long time=level.getGameTime();
        if(time%20!=0||tile.lastMove==time||!state.is(ThaumicTinkererModern.LOCOMOTIVE.get()))return;
        var above=pos.above();
        var passenger=level.getBlockState(above);
        boolean hasPassenger=!passenger.isAir();
        var target=pos.relative(tile.direction);
        if(!level.isLoaded(target)||level.isOutsideBuildHeight(target.above()))return;
        if(!level.isEmptyBlock(target)||(hasPassenger&&!level.isEmptyBlock(target.above()))){
            tile.direction=tile.direction.getOpposite();
            tile.setChanged();
            target=pos.relative(tile.direction);
            if(!level.isLoaded(target)||!level.isEmptyBlock(target)||(hasPassenger&&!level.isEmptyBlock(target.above())))return;
        }
        var passengerTile=level.getBlockEntity(above);
        boolean movePassenger=hasPassenger&&passenger.getDestroySpeed(level,above)>=0&&!passenger.is(Blocks.PISTON_HEAD)&&passenger.canSurvive(level,target.above())&&(passengerTile==null||net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(passenger.getBlock()).getNamespace().equals("minecraft")||passengerTile instanceof MovableTile);
        if(movePassenger&&passengerTile!=null){
            if(passengerTile instanceof MovableTile movable)movable.prepareToMove();
        }
        CompoundTag passengerData=movePassenger&&passengerTile!=null?passengerTile.saveWithFullMetadata(level.registryAccess()):null;
        tile.lastMove=time;var data=tile.saveWithFullMetadata(level.registryAccess());
        // Detach before state replacement: inventory blocks must never spill or duplicate contents.
        level.removeBlockEntity(pos);if(movePassenger)level.removeBlockEntity(above);
        level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        install(server,target,state,data);
        if(movePassenger){
            level.setBlock(above,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
            boolean completed=false;
            try{
                install(server,target.above(),passenger,passengerData);
                if(level.getBlockEntity(target.above()) instanceof MovableTile moved)moved.doneMoving();
                completed=true;
            }catch(RuntimeException error){org.slf4j.LoggerFactory.getLogger(LocomotiveEntity.class).warn("Locomotive passenger transfer failed at {}; restoring source",above,error);}
            if(!completed){
                // A failed foreign handler may already have installed a partial destination entity.
                for(var clear:java.util.List.of(target.above(),target)){level.removeBlockEntity(clear);level.setBlock(clear,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);level.invalidateCapabilities(clear);}
                install(server,pos,state,data);install(server,above,passenger,passengerData);
                for(var at:java.util.List.of(pos,above,target,target.above()))level.updateNeighborsAt(at,level.getBlockState(at).getBlock());
                return;
            }
        }
        for(var at:java.util.List.of(pos,above,target,target.above()))level.updateNeighborsAt(at,level.getBlockState(at).getBlock());
    }
    public static void install(ServerLevel level,BlockPos pos,BlockState state,CompoundTag data){level.setBlock(pos,state,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);if(data!=null){data=data.copy();data.putInt("x",pos.getX());data.putInt("y",pos.getY());data.putInt("z",pos.getZ());var tile=BlockEntity.loadStatic(pos,state,data,level.registryAccess());if(tile==null)throw new IllegalStateException("Cannot restore locomotive passenger at "+pos);level.setBlockEntity(tile);tile.setChanged();}level.invalidateCapabilities(pos);level.sendBlockUpdated(pos,state,state,3);}
    @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output){super.saveAdditional(output);output.putBoolean("Linked",linked);if(first!=null)output.putLong("First",first.asLong());if(second!=null)output.putLong("Second",second.asLong());output.putString("Direction",direction.getName());output.putLong("LastMove",lastMove);}
    @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input){super.loadAdditional(input);linked=input.getBooleanOr("Linked",false);first=input.getLong("First").map(BlockPos::of).orElse(null);second=input.getLong("Second").map(BlockPos::of).orElse(null);var d=Direction.byName(input.getStringOr("Direction","north"));direction=d!=null&&d.getAxis().isHorizontal()?d:Direction.NORTH;lastMove=input.getLongOr("LastMove",Long.MIN_VALUE);}
}
