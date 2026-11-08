package xyz.kajitsy.thaumaturgetinker.mixin;

import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CasterManager.class)
public class CasterManagerMixin {
    @Inject(method = "isPouch", at = @At("HEAD"), cancellable = true)
    private static void thaumaturgetinker$isPouch(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty() && stack.getItem() instanceof IchorPouch) {
            cir.setReturnValue(true);
        }
    }
}
