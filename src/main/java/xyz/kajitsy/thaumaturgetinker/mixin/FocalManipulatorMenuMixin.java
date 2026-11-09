package xyz.kajitsy.thaumaturgetinker.mixin;

import xyz.kajitsy.thaumaturgetinker.focus.TinkererFocus;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.leclowndu93150.thaumaturge.content.spell.manipulator.MenuFocalManipulator$FocusSlot")
public class FocalManipulatorMenuMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void thaumaturgetinker$noTtFoci(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty() && stack.getItem() instanceof TinkererFocus) {
            cir.setReturnValue(false);
        }
    }
}
