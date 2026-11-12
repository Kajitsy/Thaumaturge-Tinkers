package xyz.kajitsy.thaumaturgetinker.focus;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.SkyPearlData;
import xyz.kajitsy.thaumaturgetinker.block.entity.WarpGateEntity;
import xyz.kajitsy.thaumaturgetinker.compat.TTCuriosCompat;
import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;
import xyz.kajitsy.thaumaturgetinker.item.SkyPearl;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;

/** Recalls the player within the current dimension using the first matching attuned Sky Pearl in inventory or an Ichor Pouch. */
public final class RecallFocus extends TinkererFocus {
    public static final DeferredItem<RecallFocus> ITEM = KamiRegistries.ITEMS.register("focus_recall", RecallFocus::new);

    public RecallFocus() {
        super("focus_recall", Rarity.EPIC);
    }

    @Override
    public Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus) {
        return Map.of(TTAspects.AER, 4000, TTAspects.TERRA, 4000, TTAspects.ORDO, 4000);
    }

    @Override
    public String getSortingHelperBase() {
        return "RECALL";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 10287359;
    }

    @Override
    public int[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new int[0];
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int remaining) {
        if (72000 - remaining <= 60) return;
        if (player instanceof ServerPlayer server) {
            var inv = player.getInventory();
            List<ItemStack> inventoryStacks = new ArrayList<>(inv.getContainerSize());
            for (int i = 0; i < inv.getContainerSize(); i++) {
                inventoryStacks.add(inv.getItem(i));
            }
            var curioPouch = TTCuriosCompat.findFirstCurio(player, IchorPouch.ITEM.get());
            var pearl = findRecallPearl(inventoryStacks, curioPouch, server.level().dimension().identifier());
            if (!pearl.isEmpty() && consumeVis(wand, player)) {
                WarpGateEntity.teleport(server, server.level(), SkyPearlData.position(pearl));
            }
        }
        player.stopUsingItem();
    }

    public static ItemStack findRecallPearl(List<ItemStack> inventoryStacks, ItemStack curioPouch, Identifier currentDimension) {
        return findRecallPearl(
                inventoryStacks,
                curioPouch,
                currentDimension,
                stack -> stack.is(SkyPearl.ITEM.get()),
                stack -> IchorPouch.isPouch(stack) ? IchorPouch.contents(stack) : List.of());
    }

    public static ItemStack findRecallPearl(
            List<ItemStack> inventoryStacks,
            ItemStack curioPouch,
            Identifier currentDimension,
            java.util.function.Predicate<ItemStack> isPearl,
            java.util.function.Function<ItemStack, Iterable<ItemStack>> pouchContents) {
        for (var candidate : inventoryStacks) {
            if (isMatchingPearl(candidate, currentDimension, isPearl)) return candidate;
        }
        for (var candidate : inventoryStacks) {
            var inPouch = findInPouch(candidate, currentDimension, isPearl, pouchContents);
            if (!inPouch.isEmpty()) return inPouch;
        }
        return findInPouch(curioPouch, currentDimension, isPearl, pouchContents);
    }

    private static ItemStack findInPouch(
            ItemStack pouch,
            Identifier currentDimension,
            java.util.function.Predicate<ItemStack> isPearl,
            java.util.function.Function<ItemStack, Iterable<ItemStack>> pouchContents) {
        if (pouch.isEmpty()) return ItemStack.EMPTY;
        for (var candidate : pouchContents.apply(pouch)) {
            if (isMatchingPearl(candidate, currentDimension, isPearl)) return candidate;
        }
        return ItemStack.EMPTY;
    }

    private static boolean isMatchingPearl(
            ItemStack candidate,
            Identifier currentDimension,
            java.util.function.Predicate<ItemStack> isPearl) {
        return !candidate.isEmpty()
                && isPearl.test(candidate)
                && SkyPearlData.attuned(candidate)
                && currentDimension.equals(SkyPearlData.dimension(candidate));
    }
}
