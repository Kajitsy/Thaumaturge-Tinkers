package xyz.kajitsy.thaumaturgetinker.menu;

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
import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;

public final class IchorPouchMenu extends AbstractContainerMenu {
    public static final Supplier<MenuType<IchorPouchMenu>> TYPE = KamiRegistries.MENUS.register("ichor_pouch",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new IchorPouchMenu(id, inv, buf.readVarInt())));
    public final SimpleContainer contents = new SimpleContainer(IchorPouch.SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            persist();
        }
    };
    private final Inventory inventory;
    private final ItemStack pouch;
    private final int slotCode;

    public IchorPouchMenu(int id, Inventory inventory, boolean offhand) {
        this(id, inventory, offhand ? -1 : inventory.getSelectedSlot());
    }

    public IchorPouchMenu(int id, Inventory inventory, int slotCode) {
        super(TYPE.get(), id);
        this.inventory = inventory;
        this.slotCode = slotCode;
        pouch = IchorPouch.resolve(inventory.player, slotCode);
        var stored = IchorPouch.contents(pouch);
        for (int i = 0; i < IchorPouch.SLOTS; i++) contents.setItem(i, stored.get(i));
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
                int invIndex = 9 + row * 9 + col;
                addSlot(new Slot(inventory, invIndex, 48 + 18 * col, 177 + 18 * row) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return invIndex != slotCode;
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return invIndex != slotCode;
                    }
                });
            }
        }
        for (int col = 0; col < 9; col++) {
            int index = col;
            addSlot(new Slot(inventory, col, 48 + 18 * col, 235) {
                @Override
                public boolean mayPickup(Player player) {
                    return index != slotCode;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return index != slotCode;
                }
            });
        }
    }

    private void persist() {
        if (!inventory.player.level().isClientSide() && stillValid(inventory.player)) {
            var stored = new ArrayList<ItemStack>();
            for (int i = 0; i < IchorPouch.SLOTS; i++) stored.add(contents.getItem(i));
            IchorPouch.store(pouch, stored);
            inventory.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return IchorPouch.isPouch(pouch) && IchorPouch.resolve(player, slotCode) == pouch;
    }

    @Override
    public void clicked(int index, int button, ContainerInput type, Player player) {
        int lockedMenuSlot = slotCode >= 0 && slotCode < 9 ? IchorPouch.SLOTS + 27 + slotCode : (slotCode >= 9 && slotCode < 36 ? IchorPouch.SLOTS + slotCode - 9 : -99);
        if (!stillValid(player) || index == lockedMenuSlot || type == ContainerInput.SWAP && (button == slotCode || slotCode == -1 && button == 40)) {
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
        if (index < IchorPouch.SLOTS) {
            if (!moveItemStackTo(stack, IchorPouch.SLOTS, IchorPouch.SLOTS + 36, true)) return ItemStack.EMPTY;
        } else if (!IchorPouch.allowed(stack) || !moveItemStackTo(stack, 0, IchorPouch.SLOTS, false)) {
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
