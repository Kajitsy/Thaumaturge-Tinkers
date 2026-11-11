package xyz.kajitsy.thaumaturgetinker.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireAttachment;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireBlock;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {
    @Inject(method = "entityInside", at = @At("TAIL"))
    private void thaumaturgetinker$clearInfusedFireOnOtherFire(
        BlockState state,
        Level level,
        BlockPos pos,
        Entity entity,
        CallbackInfo ci
    ) {
        if (!level.isClientSide() && !((Object) this instanceof InfusedFireBlock)) {
            InfusedFireAttachment.clear(entity);
        }
    }
}
