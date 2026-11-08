package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class ForcefieldEntity extends BlockEntity {
 private int ticks=60;
 public ForcefieldEntity(BlockPos pos,BlockState state){super(ThaumicTinkererModern.FORCEFIELD_ENTITY.get(),pos,state);}
 public static void tick(Level level,BlockPos pos,ForcefieldEntity tile){
  if(level.isClientSide()){level.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,pos.getX()+level.getRandom().nextFloat(),pos.getY()+level.getRandom().nextFloat(),pos.getZ()+level.getRandom().nextFloat(),0,0,0);return;}
  if(tile.ticks<0)level.removeBlock(pos,false);tile.ticks--;
 }
}
