package xyz.kajitsy.thaumaturgetinker.mixin;

import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;
import com.leclowndu93150.thaumaturge.client.casters.RadialFocusOverlay;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RadialFocusOverlay.class)
public class RadialFocusOverlayMixin {
    @Inject(method = "isPouch", at = @At("HEAD"), cancellable = true)
    private static void thaumaturgetinker$isPouch(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (IchorPouch.isPouch(stack)) {
            cir.setReturnValue(true);
        }
    }
}
