package xyz.kajitsy.thaumaturgetinker.block;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.entity.TransvectorEntity;
import xyz.kajitsy.thaumaturgetinker.item.ConnectorItem;


import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TransvectorBlock extends BaseEntityBlock {
    public static final MapCodec<TransvectorBlock> CODEC=simpleCodec(p->new TransvectorBlock());
    public TransvectorBlock(){super(ThaumicTinkererModern.blockProps("transvector_interface",Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(3,10)));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override protected net.minecraft.world.InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,BlockState state,net.minecraft.world.level.Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand,net.minecraft.world.phys.BlockHitResult hit){
        if(stack.getItem() instanceof ConnectorItem)return net.minecraft.world.InteractionResult.TRY_WITH_EMPTY_HAND;
        if(!(stack.getItem() instanceof net.minecraft.world.item.BlockItem item)||item.getBlock() instanceof TransvectorBlock)return net.minecraft.world.InteractionResult.TRY_WITH_EMPTY_HAND;
        var camo=item.getBlock().defaultBlockState();
        if(camo.getRenderShape()!=RenderShape.MODEL||camo.hasBlockEntity()||!camo.isCollisionShapeFullBlock(level,pos))return net.minecraft.world.InteractionResult.TRY_WITH_EMPTY_HAND;
        if(!level.mayInteract(player,pos))return net.minecraft.world.InteractionResult.FAIL;
        if(!level.isClientSide()&&level.getBlockEntity(pos) instanceof TransvectorEntity tile)tile.camouflage(camo);
        return level.isClientSide()?net.minecraft.world.InteractionResult.SUCCESS:net.minecraft.world.InteractionResult.CONSUME;
    }
    @Override protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state,net.minecraft.world.level.Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.phys.BlockHitResult hit){
        if(!player.getMainHandItem().isEmpty()||!level.mayInteract(player,pos))return net.minecraft.world.InteractionResult.PASS;
        if(!level.isClientSide()&&level.getBlockEntity(pos) instanceof TransvectorEntity tile)tile.camouflage(null);
        return level.isClientSide()?net.minecraft.world.InteractionResult.SUCCESS:net.minecraft.world.InteractionResult.CONSUME;
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new TransvectorEntity(pos,state);}
}
