package xyz.kajitsy.thaumaturgetinker.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(net.minecraft.world.entity.monster.Creeper.class)
public interface CatCreeperAccessor {
 @Accessor("swell") void thaumaturgetinker$fuse(int value);
}
