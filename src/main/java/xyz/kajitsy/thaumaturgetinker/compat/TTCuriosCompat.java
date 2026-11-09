package xyz.kajitsy.thaumaturgetinker.compat;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.item.BlockTalisman;
import xyz.kajitsy.thaumaturgetinker.item.CatAmulet;
import xyz.kajitsy.thaumaturgetinker.item.CleansingTalismanItem;
import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;
import xyz.kajitsy.thaumaturgetinker.item.XPTalismanItem;


import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public final class TTCuriosCompat {
    public static void init(IEventBus bus) {
        if (ModList.get().isLoaded("curios")) {
            bus.addListener(TTCuriosCompat::registerCapabilities);
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public void curioTick(SlotContext slotContext) {
                if (stack.getItem() instanceof CatAmulet cat) {
                    cat.onWornTick(stack, slotContext.entity());
                } else if (stack.getItem() instanceof BlockTalisman block) {
                    block.onWornTick(stack, slotContext.entity());
                } else if (stack.getItem() instanceof CleansingTalismanItem cl) {
                    cl.onWornTick(stack, slotContext.entity());
                } else if (stack.getItem() instanceof XPTalismanItem xp) {
                    xp.onWornTick(stack, slotContext.entity());
                }
            }
        }, CatAmulet.ITEM.get(), BlockTalisman.ITEM.get(), ThaumicTinkererModern.CLEANSING_TALISMAN.get(), ThaumicTinkererModern.XP_TALISMAN.get());

        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> (ICurio) () -> stack, IchorPouch.ITEM.get());
    }

    public static ItemStack findFirstCurio(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.Item item) {
        if (!ModList.get().isLoaded("curios")) return ItemStack.EMPTY;
        return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                .flatMap(h -> h.findFirstCurio(item))
                .map(top.theillusivec4.curios.api.SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }
}
