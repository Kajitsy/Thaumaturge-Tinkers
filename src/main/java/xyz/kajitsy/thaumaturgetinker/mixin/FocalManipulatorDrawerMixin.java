package xyz.kajitsy.thaumaturgetinker.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator$FocusSlot")
public class FocalManipulatorDrawerMixin {
    @Inject(method = "isValid(ILnet/neoforged/neoforge/transfer/item/ItemResource;)Z", at = @At("HEAD"), cancellable = true)
    private void thaumaturgetinker$noTtFoci(int slot, ItemResource resource, CallbackInfoReturnable<Boolean> cir) {
        if (!resource.isEmpty() && BuiltInRegistries.ITEM.getKey(resource.toStack(1).getItem()).getNamespace().equals("thaumaturgetinker")) {
            cir.setReturnValue(false);
        }
    }
}
