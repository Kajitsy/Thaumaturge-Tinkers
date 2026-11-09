package xyz.kajitsy.thaumaturgetinker.mixin;

import xyz.kajitsy.thaumaturgetinker.focus.TinkererFocus;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator$FocusSlot")
public class FocalManipulatorDrawerMixin {
    @Inject(method = "isValid(ILnet/neoforged/neoforge/transfer/item/ItemResource;)Z", at = @At("HEAD"), cancellable = true)
    private void thaumaturgetinker$noTtFoci(int slot, ItemResource resource, CallbackInfoReturnable<Boolean> cir) {
        if (!resource.isEmpty() && resource.getItem() instanceof TinkererFocus) {
            cir.setReturnValue(false);
        }
    }
}
