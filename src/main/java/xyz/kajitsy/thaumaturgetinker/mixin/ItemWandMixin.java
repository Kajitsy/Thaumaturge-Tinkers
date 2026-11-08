package xyz.kajitsy.thaumaturgetinker.mixin;

import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import xyz.kajitsy.thaumaturgetinker.focus.TinkererFocus;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemWand.class)
public abstract class ItemWandMixin {
    @Shadow
    public abstract ItemStack getFocusStack(ItemStack stack);

    @Inject(method = "onUseTick", at = @At("HEAD"), cancellable = true)
    private void thaumaturgetinker$skipRefineForTinkererFocus(Level level, LivingEntity user, ItemStack stack, int remaining, CallbackInfo ci) {
        ItemStack focus = getFocusStack(stack);
        if (!focus.isEmpty() && focus.getItem() instanceof TinkererFocus) {
            ci.cancel();
        }
    }
}
