package xyz.kajitsy.thaumaturgetinker.block.entity;
import net.minecraft.core.BlockPos;
/** Shared binder contract; the dislocator accepts ordinary blocks as well as tiles. */
public interface TransvectorLink{
    boolean inRange(BlockPos pos);
    boolean link(BlockPos pos);
}
