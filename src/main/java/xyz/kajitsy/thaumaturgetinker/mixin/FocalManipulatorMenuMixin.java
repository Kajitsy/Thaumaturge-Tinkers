package xyz.kajitsy.thaumaturgetinker.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.leclowndu93150.thaumaturge.content.spell.manipulator.MenuFocalManipulator$FocusSlot")
public class FocalManipulatorMenuMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void thaumaturgetinker$noTtFoci(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("thaumaturgetinker")) {
            cir.setReturnValue(false);
        }
    }
}
