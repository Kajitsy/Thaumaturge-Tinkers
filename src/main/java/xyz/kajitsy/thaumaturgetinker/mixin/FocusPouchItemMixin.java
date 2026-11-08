package xyz.kajitsy.thaumaturgetinker.mixin;

import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;
import com.leclowndu93150.thaumaturge.content.casters.FocusPouchItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FocusPouchItem.class)
public class FocusPouchItemMixin {
    @Inject(method = "getInventory", at = @At("HEAD"), cancellable = true)
    private static void thaumaturgetinker$getIchorPouchInventory(ItemStack stack, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        if (!stack.isEmpty() && stack.getItem() instanceof IchorPouch) {
            var items = IchorPouch.contents(stack);
            NonNullList<ItemStack> list = NonNullList.withSize(117, ItemStack.EMPTY);
            for (int i = 0; i < items.size(); i++) {
                list.set(i, items.get(i));
            }
            cir.setReturnValue(list);
        }
    }

    @Inject(method = "setInventory", at = @At("HEAD"), cancellable = true)
    private static void thaumaturgetinker$setIchorPouchInventory(ItemStack stack, NonNullList<ItemStack> list, CallbackInfo ci) {
        if (!stack.isEmpty() && stack.getItem() instanceof IchorPouch) {
            List<ItemStack> items = new ArrayList<>(IchorPouch.contents(stack));
            for (int i = 0; i < Math.min(items.size(), list.size()); i++) {
                items.set(i, list.get(i));
            }
            IchorPouch.store(stack, items);
            ci.cancel();
        }
    }
}
