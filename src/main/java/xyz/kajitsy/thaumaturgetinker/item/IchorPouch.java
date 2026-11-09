package xyz.kajitsy.thaumaturgetinker.item;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.compat.TTCuriosCompat;
import xyz.kajitsy.thaumaturgetinker.menu.IchorPouchMenu;

/** Original 117-slot general inventory, not the ordinary 18-focus pouch. */
@EventBusSubscriber(modid = ThaumicTinkererModern.ID)
public final class IchorPouch extends Item {
    public static final int SLOTS = 117;
    private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
    private static final int BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);
    public static final DeferredItem<IchorPouch> ITEM = KamiRegistries.ITEMS.register("ichor_pouch", IchorPouch::new);

    public record OpenPayload() implements CustomPacketPayload {
        public static final Type<OpenPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "open_pouch"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenPayload> CODEC = StreamCodec.unit(new OpenPayload());
        @Override public Type<OpenPayload> type() { return TYPE; }
    }

    public record SelectPayload(int slotId, int selectedIndex) implements CustomPacketPayload {
        public static final Type<SelectPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "select_pouch_item"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SelectPayload::slotId,
            ByteBufCodecs.VAR_INT, SelectPayload::selectedIndex,
            SelectPayload::new
        );
        @Override public Type<SelectPayload> type() { return TYPE; }
    }

    public record Group(ItemStack template, int totalCount) {}

    @SubscribeEvent
    public static void registerPayload(RegisterPayloadHandlersEvent event) {
        var reg = event.registrar("1");
        reg.playToServer(OpenPayload.TYPE, OpenPayload.CODEC, (packet, context) -> context.enqueueWork(() -> {
            if (KamiStartupConfig.enabled() && context.player() instanceof ServerPlayer server) {
                openFirst(server);
            }
        }));
        reg.playToServer(SelectPayload.TYPE, SelectPayload.CODEC, (packet, context) -> context.enqueueWork(() -> {
            if (KamiStartupConfig.enabled() && context.player() instanceof ServerPlayer server) {
                var menu = server.containerMenu;
                if (packet.slotId() >= 0 && packet.slotId() < menu.slots.size()) {
                    var stack = menu.getSlot(packet.slotId()).getItem();
                    if (isPouch(stack)) {
                        toggleSelectedItem(stack, packet.selectedIndex());
                    }
                }
            }
        }));
    }

    public IchorPouch() {
        super(ThaumicTinkererModern.itemProps("ichor_pouch", new Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }

    public static boolean isPouch(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof IchorPouch;
    }

    public static int getSelectedItem(ItemStack pouch) {
        return pouch.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("SelectedItem", -1);
    }

    public static void toggleSelectedItem(ItemStack pouch, int index) {
        CustomData.update(DataComponents.CUSTOM_DATA, pouch, tag -> {
            if (index < 0) {
                tag.remove("SelectedItem");
            } else {
                tag.putInt("SelectedItem", index);
            }
        });
    }

    public static List<Group> groups(ItemStack pouch) {
        record MutableGroup(ItemStack template, int[] count) {}
        var list = new ArrayList<MutableGroup>();
        for (var item : contents(pouch)) {
            if (item.isEmpty()) continue;
            MutableGroup matched = null;
            for (var g : list) {
                if (ItemStack.isSameItemSameComponents(g.template, item)) {
                    matched = g;
                    break;
                }
            }
            if (matched != null) {
                matched.count[0] += item.getCount();
            } else {
                list.add(new MutableGroup(item.copyWithCount(1), new int[]{item.getCount()}));
            }
        }
        var result = new ArrayList<Group>(list.size());
        for (var g : list) {
            result.add(new Group(g.template, g.count[0]));
        }
        return result;
    }

    public static int occupiedSlots(ItemStack pouch) {
        int occupied = 0;
        for (var item : contents(pouch)) {
            if (!item.isEmpty()) occupied++;
        }
        return occupied;
    }

    public static int numberOfItemsToShow(ItemStack pouch) {
        return StorageTooltip.numberOfItemsToShow(groups(pouch).size());
    }

    public static int findSlot(Player player) {
        var inv = player.getInventory();
        if (isPouch(inv.getItem(inv.getSelectedSlot()))) return inv.getSelectedSlot();
        if (isPouch(player.getOffhandItem())) return -1;
        if (!TTCuriosCompat.findFirstCurio(player, ITEM.get()).isEmpty()) return -2;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (isPouch(inv.getItem(i))) return i;
        }
        return -99;
    }

    public static ItemStack resolve(Player player, int slotCode) {
        if (slotCode == -1) return player.getOffhandItem();
        if (slotCode == -2) return TTCuriosCompat.findFirstCurio(player, ITEM.get());
        var inv = player.getInventory();
        return slotCode >= 0 && slotCode < inv.getContainerSize() ? inv.getItem(slotCode) : ItemStack.EMPTY;
    }

    public static void open(ServerPlayer server, int slotCode) {
        var stack = resolve(server, slotCode);
        if (!isPouch(stack)) return;
        server.openMenu(new SimpleMenuProvider(
            (id, inv, p) -> new IchorPouchMenu(id, inv, slotCode),
            stack.getHoverName()
        ), buf -> buf.writeVarInt(slotCode));
    }

    public static void openFirst(ServerPlayer server) {
        int slotCode = findSlot(server);
        if (slotCode != -99) open(server, slotCode);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer server) {
            int slotCode = hand == InteractionHand.OFF_HAND ? -1 : player.getInventory().getSelectedSlot();
            open(server, slotCode);
        }
        return InteractionResult.SUCCESS;
    }

    public static int insert(ItemStack pouch, ItemStack stack) {
        if (!allowed(stack)) return 0;
        var items = contents(pouch);
        int remaining = stack.getCount();
        int initial = remaining;
        for (int i = 0; i < SLOTS && remaining > 0; i++) {
            var existing = items.get(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
                int take = Math.min(remaining, existing.getMaxStackSize() - existing.getCount());
                existing.grow(take);
                remaining -= take;
            }
        }
        for (int i = 0; i < SLOTS && remaining > 0; i++) {
            if (items.get(i).isEmpty()) {
                int take = Math.min(remaining, stack.getMaxStackSize());
                items.set(i, stack.copyWithCount(take));
                remaining -= take;
            }
        }
        if (remaining < initial) {
            store(pouch, items);
        }
        return initial - remaining;
    }

    public static ItemStack removeSelected(ItemStack pouch) {
        var gList = groups(pouch);
        if (gList.isEmpty()) return ItemStack.EMPTY;
        int sel = getSelectedItem(pouch);
        int shown = StorageTooltip.numberOfItemsToShow(gList.size());
        int targetGroupIdx = (sel >= 0 && sel < shown) ? sel : 0;
        var targetTemplate = gList.get(targetGroupIdx).template();

        var items = contents(pouch);
        int maxStack = targetTemplate.getMaxStackSize();
        int extractedCount = 0;
        for (int i = SLOTS - 1; i >= 0 && extractedCount < maxStack; i--) {
            var existing = items.get(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, targetTemplate)) {
                int take = Math.min(maxStack - extractedCount, existing.getCount());
                extractedCount += take;
                existing.shrink(take);
                if (existing.isEmpty()) {
                    items.set(i, ItemStack.EMPTY);
                }
            }
        }
        if (extractedCount > 0) {
            store(pouch, items);
            toggleSelectedItem(pouch, -1);
            return targetTemplate.copyWithCount(extractedCount);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;
        var other = slot.getItem();
        if (other.isEmpty()) {
            var removed = removeSelected(self);
            if (removed.isEmpty()) return false;
            var remainder = slot.safeInsert(removed);
            if (!remainder.isEmpty()) insert(self, remainder);
            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
            return true;
        } else {
            if (!allowed(other)) return false;
            var taken = slot.safeTake(other.getCount(), other.getCount(), player);
            int inserted = insert(self, taken);
            if (inserted <= 0) {
                slot.safeInsert(taken);
                return false;
            }
            if (inserted < taken.getCount()) {
                slot.safeInsert(taken.copyWithCount(taken.getCount() - inserted));
            }
            toggleSelectedItem(self, -1);
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
            return true;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (!slot.allowModification(player)) return false;
        if (action == ClickAction.SECONDARY && other.isEmpty()) {
            var removed = removeSelected(self);
            if (removed.isEmpty()) return false;
            access.set(removed);
            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
            return true;
        }
        if (!other.isEmpty() && allowed(other)) {
            var toInsert = action == ClickAction.SECONDARY ? other.copyWithCount(1) : other;
            int inserted = insert(self, toInsert);
            if (inserted <= 0) return false;
            other.shrink(inserted);
            toggleSelectedItem(self, -1);
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
            return true;
        }
        toggleSelectedItem(self, -1);
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return occupiedSlots(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int occ = occupiedSlots(stack);
        return Math.min(1 + Mth.mulAndTruncate(org.apache.commons.lang3.math.Fraction.getFraction(occ, SLOTS), 12), 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return occupiedSlots(stack) >= SLOTS ? FULL_BAR_COLOR : BAR_COLOR;
    }

    public static String compactCount(int n) {
        if (n >= 1_000_000_000) return (n / 1_000_000_000) + "B";
        if (n >= 1_000_000) return (n / 1_000_000) + "M";
        if (n >= 10_000) return (n / 1_000) + "K";
        return String.valueOf(n);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var gList = groups(stack);
        int occupied = occupiedSlots(stack);
        var entries = new ArrayList<StorageTooltip.Entry>(gList.size());
        for (var g : gList) {
            int total = g.totalCount();
            entries.add(new StorageTooltip.Entry(g.template(), total > 1 ? compactCount(total) : "", total));
        }
        return Optional.of(new StorageTooltip(entries, getSelectedItem(stack), occupied, SLOTS, true));
    }

    public static List<ItemStack> contents(ItemStack pouch) {
        var result = new ArrayList<ItemStack>(Collections.nCopies(SLOTS, ItemStack.EMPTY));
        var stored = pouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        for (int i = 0; i < Math.min(SLOTS, stored.getSlots()); i++) {
            result.set(i, stored.getStackInSlot(i).copy());
        }
        return result;
    }

    public static boolean allowed(ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof IchorPouch) && !stack.is(TTItems.FOCUS_POUCH.get());
    }

    public static void store(ItemStack pouch, List<ItemStack> items) {
        if (items.size() != SLOTS) throw new IllegalArgumentException("Ichor pouch requires " + SLOTS + " slots");
        for (var stack : items) {
            if (!stack.isEmpty() && !allowed(stack)) throw new IllegalArgumentException("Nested focus pouches are not allowed");
        }
        pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }
}

