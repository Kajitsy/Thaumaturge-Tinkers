package xyz.kajitsy.thaumaturgetinker.menu;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;


import java.util.ArrayList;
import java.util.function.Supplier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public final class IchorPouchMenu extends AbstractContainerMenu {
    public static final Supplier<MenuType<IchorPouchMenu>> TYPE = KamiRegistries.MENUS.register("ichor_pouch",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new IchorPouchMenu(id, inv, buf.readBoolean())));
    public final SimpleContainer contents = new SimpleContainer(117) {
        @Override
        public void setChanged() {
            super.setChanged();
            persist();
        }
    };
    private final Inventory inventory;
    private final ItemStack pouch;
    private final boolean offhand;
    private final int held;

    public static void init() {}

    public IchorPouchMenu(int id, Inventory inventory, boolean offhand) {
        super(TYPE.get(), id);
        this.inventory = inventory;
        this.offhand = offhand;
        held = inventory.getSelectedSlot();
        pouch = offhand ? inventory.player.getOffhandItem() : inventory.getItem(held);
        var stored = IchorPouch.contents(pouch);
        for (int i = 0; i < 117; i++) contents.setItem(i, stored.get(i));
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 13; col++) {
                addSlot(new Slot(contents, row * 13 + col, 12 + col * 18, 8 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return IchorPouch.allowed(stack);
                    }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, 9 + row * 9 + col, 48 + 18 * col, 177 + 18 * row));
            }
        }
        for (int col = 0; col < 9; col++) {
            int index = col;
            addSlot(new Slot(inventory, col, 48 + 18 * col, 235) {
                @Override
                public boolean mayPickup(Player player) {
                    return offhand || index != held;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return offhand || index != held;
                }
            });
        }
    }

    private void persist() {
        if (!inventory.player.level().isClientSide() && stillValid(inventory.player)) {
            var stored = new ArrayList<ItemStack>();
            for (int i = 0; i < 117; i++) stored.add(contents.getItem(i));
            IchorPouch.store(pouch, stored);
            inventory.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return pouch.getItem() instanceof IchorPouch && (offhand ? player.getOffhandItem() : inventory.getItem(held)) == pouch;
    }

    @Override
    public void clicked(int index, int button, ContainerInput type, Player player) {
        if (!stillValid(player) || !offhand && index == 144 + held || type == ContainerInput.SWAP && (!offhand && button == held || offhand && button == 40)) {
            return;
        }
        super.clicked(index, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        var slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        var stack = slot.getItem();
        var copy = stack.copy();
        if (index < 117) {
            if (!moveItemStackTo(stack, 117, 153, true)) return ItemStack.EMPTY;
        } else if (!IchorPouch.allowed(stack) || !moveItemStackTo(stack, 0, 117, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        persist();
        return copy;
    }

    @Override
    public void removed(Player player) {
        persist();
        super.removed(player);
    }
}
