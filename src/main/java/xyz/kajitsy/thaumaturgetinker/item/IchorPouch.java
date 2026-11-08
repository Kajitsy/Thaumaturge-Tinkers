package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.menu.IchorPouchMenu;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;

/** Original 117-slot general inventory, not the ordinary 18-focus pouch. */
public final class IchorPouch extends Item {
    public static final DeferredItem<IchorPouch> ITEM = KamiRegistries.ITEMS.register("ichor_pouch", IchorPouch::new);

    public IchorPouch() {
        super(ThaumicTinkererModern.itemProps("ichor_pouch", new Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }

    public static void init() {
        IchorPouchMenu.init();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer server) {
            server.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new IchorPouchMenu(id, inv, hand == InteractionHand.OFF_HAND),
                getName(player.getItemInHand(hand))
            ), buf -> buf.writeBoolean(hand == InteractionHand.OFF_HAND));
        }
        return InteractionResult.SUCCESS;
    }

    public static List<ItemStack> contents(ItemStack pouch) {
        var result = new ArrayList<ItemStack>(Collections.nCopies(117, ItemStack.EMPTY));
        var stored = pouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        for (int i = 0; i < Math.min(117, stored.getSlots()); i++) {
            result.set(i, stored.getStackInSlot(i).copy());
        }
        return result;
    }

    public static boolean allowed(ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof IchorPouch) && !stack.is(TTItems.FOCUS_POUCH.get());
    }

    public static void store(ItemStack pouch, List<ItemStack> items) {
        if (items.size() != 117) throw new IllegalArgumentException("Ichor pouch requires 117 slots");
        for (var stack : items) {
            if (!stack.isEmpty() && !allowed(stack)) throw new IllegalArgumentException("Nested focus pouches are not allowed");
        }
        pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }
}
