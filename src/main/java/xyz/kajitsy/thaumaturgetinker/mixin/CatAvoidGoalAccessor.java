package xyz.kajitsy.thaumaturgetinker.mixin;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(net.minecraft.world.entity.ai.goal.AvoidEntityGoal.class)
public interface CatAvoidGoalAccessor {
 @Accessor("avoidClass") Class<?> thaumaturgetinker$avoidClass();
 @Mutable @Accessor("avoidClass") void thaumaturgetinker$avoidClass(Class<?> type);
}
