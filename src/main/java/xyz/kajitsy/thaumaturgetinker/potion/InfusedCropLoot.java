package xyz.kajitsy.thaumaturgetinker.potion;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.item.LegacyCropEggItem;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

/** Build-164 item entries. Optional tag expansions and Thaumcraft variants are added separately. */
public final class InfusedCropLoot {
    public record Entry(ItemStack stack, int weight) {
        public Entry {
            stack = stack.copy();
            if (weight <= 0) throw new IllegalArgumentException("nonpositive crop loot weight");
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }

    private static final Map<ServerLevel, InfusedCropLoot> CACHE = new WeakHashMap<>();

    public static InfusedCropLoot get(ServerLevel level) {
        return CACHE.computeIfAbsent(level, l -> complete(l.registryAccess()));
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(InfusedCropLoot::reload);
    }

    private static void reload(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath("thaumaturgetinker", "crop_loot"), (ResourceManagerReloadListener) resources -> CACHE.clear());
    }

    private final Map<ResourceKey<IAspect>, List<Entry>> table = new LinkedHashMap<>();

    void add(ResourceKey<IAspect> aspect, ItemStack stack, int weight) {
        table.computeIfAbsent(aspect, a -> new ArrayList<>()).add(new Entry(stack, weight));
    }

    void add(ResourceKey<IAspect> aspect, ItemLike item, int count, int weight) {
        add(aspect, new ItemStack(item, count), weight);
    }

    List<Entry> entries(ResourceKey<IAspect> aspect) {
        return List.copyOf(table.getOrDefault(aspect, List.of()));
    }

    public ItemStack roll(ResourceKey<IAspect> aspect, RandomSource random) {
        var entries = table.get(aspect);
        if (entries == null || entries.isEmpty()) return ItemStack.EMPTY;
        int total = 0;
        for (var entry : entries) total = Math.addExact(total, entry.weight());
        int value = random.nextInt(total);
        for (var entry : entries) {
            value -= entry.weight();
            if (value <= 0) return entry.stack();
        }
        return ItemStack.EMPTY;
    }

    public static InfusedCropLoot fixed() {
        var loot = new InfusedCropLoot();
        loot.add(TTAspects.AER, ThaumicTinkererModern.INFUSED_GRAIN_AIR.get(), 1, 1);
        loot.add(TTAspects.IGNIS, ThaumicTinkererModern.INFUSED_GRAIN_FIRE.get(), 1, 1);
        loot.add(TTAspects.TERRA, ThaumicTinkererModern.INFUSED_GRAIN_EARTH.get(), 1, 1);
        loot.add(TTAspects.AQUA, ThaumicTinkererModern.INFUSED_GRAIN_WATER.get(), 1, 1);

        loot.add(TTAspects.ORDO, Items.GLASS, 64, 1);
        loot.add(TTAspects.PERDITIO, Items.SAND, 64, 1);

        loot.add(TTAspects.ALIENIS, Items.ENDER_PEARL, 4, 10);
        loot.add(TTAspects.ALIENIS, Items.ENDER_EYE, 4, 5);

        loot.add(TTAspects.HERBA, Items.OAK_LOG, 1, 1);

        loot.add(TTAspects.COGNITIO, Items.PAPER, 64, 15);
        loot.add(TTAspects.COGNITIO, Items.BOOK, 32, 10);
        loot.add(TTAspects.COGNITIO, Items.BOOKSHELF, 16, 5);

        loot.add(TTAspects.EXANIMIS, Items.ROTTEN_FLESH, 32, 1);
        loot.add(TTAspects.FABRICO, ThaumicTinkererModern.QUARTZ.get(), 32, 1);

        loot.add(TTAspects.DESIDERIUM, Items.NETHER_WART, 16, 1);
        loot.add(TTAspects.GELUM, Items.SNOWBALL, 16, 1);

        for (var sapling : List.of(Items.OAK_SAPLING, Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING, Items.JUNGLE_SAPLING, Items.ACACIA_SAPLING, Items.DARK_OAK_SAPLING)) {
            loot.add(TTAspects.HERBA, sapling, 1, 1);
        }
        for (var armor : List.of(Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET)) {
            loot.add(TTAspects.PRAEMUNIO, armor, 1, 1);
        }
        loot.add(TTAspects.AQUA, Items.SLIME_BALL, 16, 1);
        loot.add(TTAspects.DESIDERIUM, Items.GOLD_INGOT, 4, 1);

        loot.add(TTAspects.LUX, Items.GLOWSTONE_DUST, 16, 5);
        loot.add(TTAspects.LUX, ThaumicTinkererModern.BRIGHT_NITOR.get(), 1, 1);

        loot.add(TTAspects.MACHINA, Items.PISTON, 8, 1);
        loot.add(TTAspects.HERBA, Items.WHEAT, 32, 1);
        loot.add(TTAspects.METALLUM, Items.IRON_INGOT, 4, 100);

        loot.add(TTAspects.MORTUUS, Items.BONE, 32, 1);
        loot.add(TTAspects.MOTUS, Items.RAIL, 1, 10);
        loot.add(TTAspects.MOTUS, Items.ACTIVATOR_RAIL, 1, 1);

        loot.add(TTAspects.FABRICO, Items.WHITE_WOOL, 16, 30);
        loot.add(TTAspects.FABRICO, Items.STRING, 15, 10);
        for (var wool : List.of(Items.WHITE_WOOL, Items.ORANGE_WOOL, Items.MAGENTA_WOOL, Items.LIGHT_BLUE_WOOL, Items.YELLOW_WOOL, Items.LIME_WOOL, Items.PINK_WOOL, Items.GRAY_WOOL, Items.LIGHT_GRAY_WOOL, Items.CYAN_WOOL, Items.PURPLE_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.GREEN_WOOL, Items.RED_WOOL, Items.BLACK_WOOL)) {
            loot.add(TTAspects.FABRICO, wool, 4, 1);
        }

        loot.add(TTAspects.VICTUS, Items.GOLDEN_APPLE, 1, 1);
        loot.add(TTAspects.VICTUS, Items.CAKE, 1, 1);
        loot.add(TTAspects.SENSUS, Items.LAPIS_LAZULI, 20, 1);

        loot.add(TTAspects.SPIRITUS, Items.SOUL_SAND, 64, 2);
        loot.add(TTAspects.SPIRITUS, Items.NETHERRACK, 64, 2);
        loot.add(TTAspects.SPIRITUS, Items.NETHER_BRICKS, 1, 1);

        // Original WEATHER includes an AIR item: intentionally an empty result
        loot.add(TTAspects.VOLATUS, ItemStack.EMPTY, 1);
        loot.add(TTAspects.TENEBRAE, Items.OBSIDIAN, 10, 1);
        loot.add(TTAspects.VACUOS, Items.BUCKET, 1, 1);

        loot.add(TTAspects.VICTUS, Items.EGG, 8, 1);
        loot.add(TTAspects.VINCULUM, Items.COBWEB, 4, 1);
        loot.add(TTAspects.VITREUS, Items.DIAMOND, 1, 1);

        for (int id = 0; id < 24; id++) {
            loot.add(TTAspects.BESTIA, LegacyCropEggItem.stack(id), 1);
        }
        addThaumcraft(loot);
        return loot;
    }

    public static InfusedCropLoot complete(HolderLookup.Provider registries) {
        var loot = fixed();
        loot.addBooks(registries);
        CropOreTags.addAll(loot);
        return loot;
    }

    public static final TagKey<Enchantment> BOOKS = TagKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "crop_books"));

    void addBooks(HolderLookup.Provider registries) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        for (var enchantment : enchantments.getOrThrow(BOOKS)) {
            add(TTAspects.COGNITIO, EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, 1)), 1);
        }
    }

    private static void addThaumcraft(InfusedCropLoot loot) {
        loot.add(TTAspects.EXANIMIS, TTItems.BRAIN.get(), 16, 4);
        loot.add(TTAspects.EXANIMIS, TTItems.BRAIN.get(), 16, 1);
        loot.add(TTAspects.FABRICO, TTItems.SMELTER_BASIC.get(), 16, 1);
        loot.add(TTAspects.INSTRUMENTUM, TTItems.THAUMIUM_PICKAXE.get(), 1, 1);
        loot.add(TTAspects.INSTRUMENTUM, TTItems.THAUMIUM_HOE.get(), 1, 1);
        loot.add(TTAspects.AVERSIO, TTItems.THAUMIUM_SWORD.get(), 1, 1);
        loot.add(TTAspects.INSTRUMENTUM, TTItems.THAUMIUM_SHOVEL.get(), 1, 1);
        loot.add(TTAspects.INSTRUMENTUM, TTItems.THAUMIUM_AXE.get(), 1, 1);
        loot.add(TTAspects.MOTUS, TTItems.STONE_ARCANE_BRICK.get(), 8, 1);
        loot.table.computeIfAbsent(TTAspects.LUX, a -> new ArrayList<>()).add(1, new Entry(new ItemStack(TTItems.NITORS.get(DyeColor.YELLOW).get(), 4), 1));
        loot.add(TTAspects.PERMUTATIO, TTItems.RAW_CINNABAR.get(), 4, 1);
        loot.add(TTAspects.POTENTIA, TTItems.ALUMENTUM.get(), 12, 1);
        loot.add(TTAspects.ALKIMIA, TTItems.QUICKSILVER.get(), 16, 1);
        loot.add(TTAspects.VITIUM, TTItems.TAINTED_GOO.get(), 4, 1);
        for (var seal : List.of(TTItems.SEAL_FILL.get(), TTItems.SEAL_EMPTY.get(), TTItems.SEAL_PICKUP.get(), TTItems.SEAL_HARVEST.get(),
                TTItems.SEAL_GUARD.get(), TTItems.SEAL_LUMBER.get(), TTItems.SEAL_USE.get(), TTItems.SEAL_BUTCHER.get())) {
            loot.add(TTAspects.HUMANUS, seal, 1, 1);
        }
        loot.add(TTAspects.AURAM, TTItems.PHIAL.get(), 2, 1);
    }
}
