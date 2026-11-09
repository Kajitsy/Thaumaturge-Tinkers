package xyz.kajitsy.thaumaturgetinker.mixin;

import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.leclowndu93150.thaumaturge.content.casters.CasterManager$PouchStore")
public abstract class CasterManagerPouchStoreMixin {
    @Shadow
    public abstract Supplier<ItemStack> source();

    @Inject(method = "size", at = @At("HEAD"), cancellable = true)
    private void thaumaturgetinker$pouchStoreSize(CallbackInfoReturnable<Integer> cir) {
        Supplier<ItemStack> src = this.source();
        if (src != null && IchorPouch.isPouch(src.get())) {
            cir.setReturnValue(IchorPouch.SLOTS);
        }
    }
}
