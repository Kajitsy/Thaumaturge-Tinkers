package xyz.kajitsy.thaumaturgetinker;

import com.leclowndu93150.thaumaturge.content.wands.ItemWandCap;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandRod;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import xyz.kajitsy.thaumaturgetinker.item.KamiWands;

public final class KamiResources {
    public enum Kind {
        ICHOR("ichor", Rarity.EPIC),
        CLOTH("ichor_cloth", Rarity.EPIC),
        ICHORIUM("ichorium", Rarity.EPIC),
        NUGGET("ichor_nugget", Rarity.EPIC),
        CAP("ichor_cap", Rarity.EPIC),
        ROD("ichorcloth_rod", Rarity.EPIC),
        NETHER_SHARD("nether_shard", Rarity.COMMON),
        ENDER_SHARD("ender_shard", Rarity.COMMON);

        public final String id;
        public final Rarity rarity;

        Kind(String id, Rarity rarity) {
            this.id = id;
            this.rarity = rarity;
        }
    }

    public static final Map<Kind, DeferredItem<Item>> ITEMS = new EnumMap<>(Kind.class);

    static {
        for (var kind : Kind.values()) {
            ITEMS.put(kind, KamiRegistries.ITEMS.register(kind.id, () -> {
                var props = ThaumicTinkererModern.itemProps(kind.id, new Item.Properties().rarity(kind.rarity));
                if (kind == Kind.CAP) {
                    return new ItemWandCap(props, KamiWands.CAP_ICHOR::get);
                } else if (kind == Kind.ROD) {
                    return new ItemWandRod(props, KamiWands.ROD_ICHORCLOTH::get);
                }
                return new Item(props);
            }));
        }
    }

    public static Item item(Kind kind) {
        return ITEMS.get(kind).get();
    }
}
