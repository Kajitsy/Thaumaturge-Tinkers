package xyz.kajitsy.thaumaturgetinker.mixin;

import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.kajitsy.thaumaturgetinker.focus.TinkererFocus;

@Mixin(targets = "com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator$FocusSlot", remap = false)
public abstract class BlockEntityFocalManipulatorFocusSlotMixin {
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void thaumaturgetinker$disallowTinkererFocus(int index, ItemResource resource, CallbackInfoReturnable<Boolean> cir) {
        if (resource.getItem() instanceof TinkererFocus) {
            cir.setReturnValue(false);
        }
    }
}
