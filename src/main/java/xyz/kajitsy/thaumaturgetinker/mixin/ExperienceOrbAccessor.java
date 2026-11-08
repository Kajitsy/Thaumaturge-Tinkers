package xyz.kajitsy.thaumaturgetinker.mixin;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ExperienceOrb.class)
public interface ExperienceOrbAccessor {
    @Accessor("count") int thaumaturgetinker$count();
    @Accessor("count") void thaumaturgetinker$count(int count);
}
