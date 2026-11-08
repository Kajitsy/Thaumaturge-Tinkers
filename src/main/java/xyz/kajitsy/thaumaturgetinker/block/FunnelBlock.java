package xyz.kajitsy.thaumaturgetinker.block;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.entity.FunnelEntity;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
public final class FunnelBlock extends BaseEntityBlock {
    public static final MapCodec<FunnelBlock> CODEC=simpleCodec(p->new FunnelBlock());
    public FunnelBlock(){super(ThaumicTinkererModern.blockProps("funnel",Properties.ofFullCopy(Blocks.STONE).strength(3,8).noOcclusion()));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Block.box(0,0,0,16,2,16);}
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Shapes.block();}
    @Override public boolean canSurvive(BlockState state,LevelReader level,BlockPos pos){return level.getBlockState(pos.below()).is(Blocks.HOPPER);}
    @Override protected BlockState updateShape(BlockState state,LevelReader level,net.minecraft.world.level.ScheduledTickAccess ticks,BlockPos pos,Direction side,BlockPos otherPos,BlockState other,net.minecraft.util.RandomSource random){
        if(side==Direction.DOWN&&!canSurvive(state,level,pos))return Blocks.AIR.defaultBlockState();
        return state;
    }
    private static boolean interact(Level level,BlockPos pos,Player player,ItemStack held){
        if(!level.mayInteract(player,pos)||!(level.getBlockEntity(pos) instanceof FunnelEntity tile))return false;
        var stored=tile.inventory.getItem(0);
        if(stored.isEmpty()){
            if(!tile.inventory.canPlaceItem(0,held))return false;
            if(!level.isClientSide())tile.inventory.setItem(0,held.split(1));
        }else if(!level.isClientSide()){var jar=tile.inventory.removeItem(0,1);player.getInventory().placeItemBackInInventory(jar);}
        return true;
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){return interact(level,pos,player,stack)?(level.isClientSide()?InteractionResult.SUCCESS:InteractionResult.CONSUME):InteractionResult.TRY_WITH_EMPTY_HAND;}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){return interact(level,pos,player,ItemStack.EMPTY)?(level.isClientSide()?InteractionResult.SUCCESS:InteractionResult.CONSUME):InteractionResult.PASS;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new FunnelEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide()?null:createTickerHelper(type,ThaumicTinkererModern.FUNNEL_ENTITY.get(),FunnelEntity::tick);}
}
