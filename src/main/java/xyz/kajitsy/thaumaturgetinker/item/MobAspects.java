package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

/** Exact build-164 NumericAspectHelper and EnumMobAspect mappings (17 real aspects). */
public final class MobAspects {
    public static final List<MobAspect> ASPECTS = List.of(
            MobAspect.AQUA, MobAspect.HUMANUS, MobAspect.AER, MobAspect.VOLATUS, MobAspect.IGNIS,
            MobAspect.PRAECANTATIO, MobAspect.EXANIMIS, MobAspect.CORPUS, MobAspect.BESTIA,
            MobAspect.VENENUM, MobAspect.TERRA, MobAspect.ALIENIS, MobAspect.ITER, MobAspect.METALLUM,
            MobAspect.LIMUS, MobAspect.PANNUS, MobAspect.MESSIS);

    public static final Map<MobAspect, DeferredItem<MobAspectItem>[]> ITEMS = new LinkedHashMap<>();

    public static void register() {
        for (var aspect : ASPECTS) {
            @SuppressWarnings("unchecked")
            var variants = (DeferredItem<MobAspectItem>[]) new DeferredItem<?>[3];
            for (int i = 0; i < 3; i++) {
                final int form = i;
                variants[i] = ThaumicTinkererModern.ITEMS.register(
                        "mob_aspect_" + aspect.tag() + (i == 0 ? "" : i == 1 ? "_condensed" : "_infused"),
                        () -> new MobAspectItem(aspect, form));
            }
            ITEMS.put(aspect, variants);
        }
    }

    public static ItemStack stack(MobAspect aspect, int form) {
        return new ItemStack(ITEMS.get(aspect)[form].get());
    }

    public static final List<String> SUMMON_ORDER = List.of(
            "minecraft:snow_golem", "minecraft:bat", "minecraft:blaze", "thaumaturge:brainy_zombie", "thaumaturge:firebat",
            "minecraft:cave_spider", "minecraft:chicken", "minecraft:cow", "minecraft:creeper", "minecraft:enderman",
            "minecraft:ghast", "minecraft:horse", "minecraft:iron_golem", "minecraft:magma_cube", "minecraft:mooshroom",
            "minecraft:ocelot", "minecraft:pig", "minecraft:zombified_piglin", "minecraft:sheep", "minecraft:silverfish",
            "minecraft:skeleton", "minecraft:slime", "minecraft:spider", "minecraft:squid", "minecraft:villager",
            "thaumaturge:wisp", "minecraft:witch", "minecraft:wolf", "minecraft:zombie");

    public static final Map<String, List<MobAspect>> MOBS = mobTable();

    public static Map<String, List<MobAspect>> mobTable() {
        var table = new HashMap<String, List<MobAspect>>(Map.ofEntries(
                Map.entry("minecraft:snow_golem", List.of(MobAspect.AQUA, MobAspect.AQUA, MobAspect.HUMANUS)),
                Map.entry("minecraft:bat", List.of(MobAspect.AER, MobAspect.AER, MobAspect.VOLATUS)),
                Map.entry("minecraft:blaze", List.of(MobAspect.IGNIS, MobAspect.IGNIS, MobAspect.IGNIS)),
                Map.entry("thaumaturge:brainy_zombie", List.of(MobAspect.PRAECANTATIO, MobAspect.EXANIMIS, MobAspect.CORPUS)),
                Map.entry("thaumaturge:firebat", List.of(MobAspect.VOLATUS, MobAspect.IGNIS, MobAspect.PRAECANTATIO)),
                Map.entry("minecraft:cave_spider", List.of(MobAspect.BESTIA, MobAspect.VENENUM, MobAspect.VENENUM)),
                Map.entry("minecraft:chicken", List.of(MobAspect.MESSIS, MobAspect.VOLATUS, MobAspect.BESTIA)),
                Map.entry("minecraft:cow", List.of(MobAspect.BESTIA, MobAspect.TERRA, MobAspect.BESTIA)),
                Map.entry("minecraft:creeper", List.of(MobAspect.PRAECANTATIO, MobAspect.BESTIA, MobAspect.ALIENIS)),
                Map.entry("minecraft:enderman", List.of(MobAspect.ALIENIS, MobAspect.ALIENIS, MobAspect.HUMANUS)),
                Map.entry("minecraft:ghast", List.of(MobAspect.IGNIS, MobAspect.VOLATUS, MobAspect.VOLATUS)),
                Map.entry("minecraft:horse", List.of(MobAspect.BESTIA, MobAspect.BESTIA, MobAspect.ITER)),
                Map.entry("minecraft:iron_golem", List.of(MobAspect.METALLUM, MobAspect.METALLUM, MobAspect.HUMANUS)),
                Map.entry("minecraft:magma_cube", List.of(MobAspect.IGNIS, MobAspect.LIMUS, MobAspect.LIMUS)),
                Map.entry("minecraft:mooshroom", List.of(MobAspect.BESTIA, MobAspect.TERRA, MobAspect.MESSIS)),
                Map.entry("minecraft:ocelot", List.of(MobAspect.BESTIA, MobAspect.TERRA, MobAspect.ALIENIS)),
                Map.entry("minecraft:pig", List.of(MobAspect.BESTIA, MobAspect.TERRA, MobAspect.ITER)),
                Map.entry("minecraft:zombified_piglin", List.of(MobAspect.EXANIMIS, MobAspect.CORPUS, MobAspect.IGNIS)),
                Map.entry("minecraft:sheep", List.of(MobAspect.TERRA, MobAspect.PANNUS, MobAspect.BESTIA)),
                Map.entry("minecraft:silverfish", List.of(MobAspect.METALLUM, MobAspect.METALLUM, MobAspect.TERRA)),
                Map.entry("minecraft:skeleton", List.of(MobAspect.EXANIMIS, MobAspect.HUMANUS, MobAspect.EXANIMIS)),
                Map.entry("minecraft:slime", List.of(MobAspect.LIMUS, MobAspect.LIMUS, MobAspect.BESTIA)),
                Map.entry("minecraft:spider", List.of(MobAspect.BESTIA, MobAspect.EXANIMIS, MobAspect.EXANIMIS)),
                Map.entry("minecraft:squid", List.of(MobAspect.AQUA, MobAspect.AQUA, MobAspect.AQUA)),
                Map.entry("minecraft:villager", List.of(MobAspect.HUMANUS, MobAspect.HUMANUS, MobAspect.HUMANUS)),
                Map.entry("thaumaturge:wisp", List.of(MobAspect.AER, MobAspect.PRAECANTATIO, MobAspect.PRAECANTATIO)),
                Map.entry("minecraft:witch", List.of(MobAspect.PRAECANTATIO, MobAspect.EXANIMIS, MobAspect.ALIENIS)),
                Map.entry("minecraft:wolf", List.of(MobAspect.BESTIA, MobAspect.BESTIA, MobAspect.BESTIA)),
                Map.entry("minecraft:zombie", List.of(MobAspect.CORPUS, MobAspect.CORPUS, MobAspect.EXANIMIS))));

        table.put("thaumaturge:giant_brainy_zombie", table.get("thaumaturge:brainy_zombie"));
        table.put("minecraft:wither_skeleton", table.get("minecraft:skeleton"));
        table.put("minecraft:zombie_villager", table.get("minecraft:zombie"));
        table.put("minecraft:cat", table.get("minecraft:ocelot"));
        for (var horse : List.of("donkey", "mule", "skeleton_horse", "zombie_horse"))
            table.put("minecraft:" + horse, table.get("minecraft:horse"));
        return Map.copyOf(table);
    }
}
