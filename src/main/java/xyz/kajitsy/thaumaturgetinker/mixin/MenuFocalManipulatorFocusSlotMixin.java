package xyz.kajitsy.thaumaturgetinker.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kajitsy.thaumaturgetinker.focus.TinkererFocus;

@Mixin(targets = "com.leclowndu93150.thaumaturge.content.spell.manipulator.MenuFocalManipulator$FocusSlot", remap = false)
public abstract class MenuFocalManipulatorFocusSlotMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void thaumaturgetinker$disallowTinkererFocus(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!TinkererFocus.canEditInFocalManipulator(stack)) {
            cir.setReturnValue(false);
        }
    }
}
