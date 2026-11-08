package xyz.kajitsy.thaumaturgetinker;

import xyz.kajitsy.thaumaturgetinker.item.KamiWands;


import com.leclowndu93150.thaumaturge.content.wands.ItemWandCap;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandRod;
import net.minecraft.world.item.*;

public final class KamiResources {

    public static final java.util.Map<KamiResourceKind, net.neoforged.neoforge.registries.DeferredItem<Item>> ITEMS = new java.util.EnumMap<>(KamiResourceKind.class);

    static {
        for (var kind : KamiResourceKind.values()) {
            ITEMS.put(kind, KamiRegistries.ITEMS.register(kind.id, () -> {
                var props = ThaumicTinkererModern.itemProps(kind.id, new Item.Properties().rarity(kind.ordinal() < 6 ? Rarity.EPIC : Rarity.COMMON));
                if (kind == KamiResourceKind.CAP) {
                    return new ItemWandCap(props, () -> KamiWands.CAP_ICHOR.get());
                } else if (kind == KamiResourceKind.ROD) {
                    return new ItemWandRod(props, () -> KamiWands.ROD_ICHORCLOTH.get());
                }
                return new Item(props);
            }));
        }
    }

    public static void init() {}
    public static Item item(KamiResourceKind kind) { return ITEMS.get(kind).get(); }
    public static void research() {}
}
