package xyz.kajitsy.thaumaturgetinker.block;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.entity.TransvectorEntity;
import xyz.kajitsy.thaumaturgetinker.item.ConnectorItem;


import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class TransvectorBlock extends BaseEntityBlock {
    public static final MapCodec<TransvectorBlock> CODEC=simpleCodec(p->new TransvectorBlock());
    private static final ThreadLocal<Set<BlockPos>> VISITING=ThreadLocal.withInitial(HashSet::new);
    public TransvectorBlock(){super(ThaumicTinkererModern.blockProps("transvector_interface",Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(3,10)));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    private static BlockPos linkedTarget(BlockGetter level,BlockPos pos){
        if(!(level.getBlockEntity(pos) instanceof TransvectorEntity tile))return null;
        var target=tile.resolveTarget();
        if(target==null||VISITING.get().contains(pos))return null;
        return target;
    }
    private static BlockHitResult retargetHit(BlockPos target,BlockHitResult hit){
        return new BlockHitResult(Vec3.atCenterOf(target),hit.getDirection(),target,hit.isInside());
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(stack.getItem() instanceof ConnectorItem)return InteractionResult.TRY_WITH_EMPTY_HAND;
        if(stack.getItem() instanceof BlockItem item&&!(item.getBlock() instanceof TransvectorBlock)){
            var camo=item.getBlock().defaultBlockState();
            if(camo.getRenderShape()==RenderShape.MODEL&&!camo.hasBlockEntity()&&camo.isCollisionShapeFullBlock(level,pos)){
                if(!level.mayInteract(player,pos))return InteractionResult.FAIL;
                if(!level.isClientSide()&&level.getBlockEntity(pos) instanceof TransvectorEntity tile)tile.camouflage(camo);
                return level.isClientSide()?InteractionResult.SUCCESS:InteractionResult.CONSUME;
            }
        }
        var target=linkedTarget(level,pos);
        if(target!=null&&level.mayInteract(player,target)){
            VISITING.get().add(pos);
            try{
                return level.getBlockState(target).useItemOn(stack,level,player,hand,retargetHit(target,hit));
            }finally{
                VISITING.get().remove(pos);
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(!level.mayInteract(player,pos))return InteractionResult.PASS;
        if(player.getMainHandItem().isEmpty()&&level.getBlockEntity(pos) instanceof TransvectorEntity tile&&tile.camouflage()!=null){
            if(!level.isClientSide())tile.camouflage(null);
            return level.isClientSide()?InteractionResult.SUCCESS:InteractionResult.CONSUME;
        }
        var target=linkedTarget(level,pos);
        if(target!=null&&level.mayInteract(player,target)){
            VISITING.get().add(pos);
            try{
                return level.getBlockState(target).useWithoutItem(level,player,retargetHit(target,hit));
            }finally{
                VISITING.get().remove(pos);
            }
        }
        return InteractionResult.PASS;
    }
    @Override protected boolean isSignalSource(BlockState state){return true;}
    @Override protected int getSignal(BlockState state,BlockGetter level,BlockPos pos,Direction direction){
        var target=linkedTarget(level,pos);
        if(target==null)return 0;
        VISITING.get().add(pos);
        try{return level.getBlockState(target).getSignal(level,target,direction);}finally{VISITING.get().remove(pos);}
    }
    @Override protected int getDirectSignal(BlockState state,BlockGetter level,BlockPos pos,Direction direction){
        var target=linkedTarget(level,pos);
        if(target==null)return 0;
        VISITING.get().add(pos);
        try{return level.getBlockState(target).getDirectSignal(level,target,direction);}finally{VISITING.get().remove(pos);}
    }
    @Override protected boolean hasAnalogOutputSignal(BlockState state){return true;}
    @Override protected int getAnalogOutputSignal(BlockState state,Level level,BlockPos pos,Direction direction){
        var target=linkedTarget(level,pos);
        if(target==null)return 0;
        VISITING.get().add(pos);
        try{
            var targetState=level.getBlockState(target);
            return targetState.hasAnalogOutputSignal()?targetState.getAnalogOutputSignal(level,target,direction):0;
        }finally{VISITING.get().remove(pos);}
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new TransvectorEntity(pos,state);}
}
