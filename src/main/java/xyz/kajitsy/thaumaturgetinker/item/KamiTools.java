package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.registries.DeferredItem;

public final class KamiTools {
    public static final ToolMaterial TIER = new ToolMaterial(
        BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
        1,
        10.0F,
        5.0F,
        25,
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "ichorium"))
    );

    public static Item.Properties properties(String name) {
        return ThaumicTinkererModern.itemProps(name, new Item.Properties().rarity(Rarity.EPIC).component(DataComponents.UNBREAKABLE, Unit.INSTANCE));
    }

    public static final DeferredItem<Item> PICK = KamiRegistries.ITEMS.register("ichor_pick", () -> new Item(properties("ichor_pick").pickaxe(TIER, 2, -2.8f)) {
        @Override public boolean isFoil(ItemStack stack) { return true; }
    });

    public static final DeferredItem<ShovelItem> SHOVEL = KamiRegistries.ITEMS.register("ichor_shovel", () -> new ShovelItem(TIER, 1, -3.0f, properties("ichor_shovel")) {
        @Override public boolean isFoil(ItemStack stack) { return true; }
    });

    public static final DeferredItem<AxeItem> AXE = KamiRegistries.ITEMS.register("ichor_axe", () -> new AxeItem(TIER, 3, -3.0f, properties("ichor_axe")) {
        @Override public boolean isFoil(ItemStack stack) { return true; }
    });

    public static final DeferredItem<IchorSword> SWORD = KamiRegistries.ITEMS.register("ichor_sword", () -> new IchorSword("ichor_sword"));
}
