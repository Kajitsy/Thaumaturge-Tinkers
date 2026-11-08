package xyz.kajitsy.thaumaturgetinker.menu;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.entity.EnchanterEntity;
import xyz.kajitsy.thaumaturgetinker.enchanter.EnchanterCosts;


import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class EnchanterMenu extends AbstractContainerMenu {
    public static final List<String> IDS = List.copyOf(EnchanterCosts.ENTRIES.keySet());
    public static final int DATA_COUNT = 1 + IDS.size() + TTAspects.PRIMALS.size();
    final EnchanterEntity tile;
    private final ContainerData data;

    public EnchanterMenu(int id, Inventory inventory, EnchanterEntity tile) {
        super(ThaumicTinkererModern.ENCHANTER_MENU.get(), id);
        this.tile = tile;
        data = inventory.player.level().isClientSide() ? new SimpleContainerData(DATA_COUNT) : new ContainerData() {
            @Override
            public int getCount() {
                return DATA_COUNT;
            }

            @Override
            public int get(int index) {
                if (index == 0) return tile.process.working() ? 1 : 0;
                if (index <= IDS.size()) return tile.process.selection.entries().getOrDefault(IDS.get(index - 1), 0);
                int primalIndex = index - 1 - IDS.size();
                return (primalIndex >= 0 && primalIndex < TTAspects.PRIMALS.size()) ? tile.process.collected(TTAspects.PRIMALS.get(primalIndex)) : 0;
            }

            @Override
            public void set(int index, int value) {}
        };
        addDataSlots(data);
        addSlot(new Slot(tile.inventory, 0, 6, 6) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !working() && tile.inventory.canPlaceItem(0, stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPickup(Player player) {
                return !working();
            }
        });
        addSlot(new Slot(tile.inventory, 1, 6, 31) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return tile.inventory.canPlaceItem(1, stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    public boolean working() {
        return data.get(0) != 0;
    }

    public int rank(int index) {
        return index >= 0 && index < IDS.size() ? data.get(index + 1) : 0;
    }

    public int progress(int index) {
        return index >= 0 && index < TTAspects.PRIMALS.size() ? data.get(index + 1 + IDS.size()) : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return tile.getLevel() != null && tile.getLevel().getBlockEntity(tile.getBlockPos()) == tile && player.distanceToSqr(Vec3.atCenterOf(tile.getBlockPos())) <= 64;
    }

    public static Set<String> playerResearch(Player player) {
        var knowledge = KnowledgeAccess.of(player);
        var set = new HashSet<String>();
        for (var entry : EnchanterCosts.ENTRIES.values()) {
            if (!entry.research().isEmpty()) {
                var rl = Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, entry.research().toLowerCase(Locale.ROOT));
                if (knowledge.isResearchComplete(rl) || knowledge.isResearchKnown(rl)) {
                    set.add(entry.research());
                }
            }
        }
        return set;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!stillValid(player) || !(player.level() instanceof ServerLevel server) || working() || button < 0) return false;
        boolean changed;
        if (button == 0) {
            changed = !tile.inventory.getItem(0).isEmpty() && tile.process.start();
        } else {
            int index = (button - 1) / 3, action = (button - 1) % 3;
            if (index < 0 || index >= IDS.size()) return false;
            String key = IDS.get(index);
            int current = rank(index);
            int requested = action == 0 ? (current > 0 ? -1 : 1) : action == 1 ? current + 1 : current <= 1 ? -1 : current - 1;
            var research = player.hasInfiniteMaterials() ? EnchanterCosts.allResearch() : playerResearch(player);
            changed = tile.process.change(server, tile.inventory.getItem(0), research, key, requested);
        }
        if (changed) {
            tile.setChanged();
            server.sendBlockUpdated(tile.getBlockPos(), tile.getBlockState(), tile.getBlockState(), 3);
            broadcastChanges();
        }
        return changed;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        var slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        var stack = slot.getItem();
        var copy = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, 38, true)) return ItemStack.EMPTY;
        } else {
            int target = tile.inventory.canPlaceItem(1, stack) ? 1 : 0;
            if (!moveItemStackTo(stack, target, target + 1, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }
}
