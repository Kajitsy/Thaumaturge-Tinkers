package xyz.kajitsy.thaumaturgetinker.enchanter;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;

/** Exact build164 cost catalog. Amounts are whole vis; conversion to centivis is at consumption. */
public final class EnchanterCosts {
    public static final Map<String, EnchanterCostEntry> ENTRIES;
    static {
        var entries = new LinkedHashMap<String, EnchanterCostEntry>();
        add(entries, "minecraft:protection", "thaumaturgetinker:textures/enchants/protection.png", true, "", TTAspects.TERRA, 10, TTAspects.PERDITIO, 7);
        add(entries, "minecraft:fire_protection", "thaumaturgetinker:textures/enchants/fireprotection.png", true, "", TTAspects.IGNIS, 10, TTAspects.PERDITIO, 3, TTAspects.AQUA, 4);
        add(entries, "minecraft:feather_falling", "thaumaturgetinker:textures/enchants/featherfalling.png", true, "", TTAspects.AER, 16, TTAspects.ORDO, 5);
        add(entries, "minecraft:blast_protection", "thaumaturgetinker:textures/enchants/blastprotection.png", true, "", TTAspects.TERRA, 5, TTAspects.IGNIS, 5, TTAspects.PERDITIO, 8);
        add(entries, "minecraft:projectile_protection", "thaumaturgetinker:textures/enchants/projectileprotection.png", true, "", TTAspects.AER, 10, TTAspects.PERDITIO, 7);
        add(entries, "minecraft:respiration", "thaumaturgetinker:textures/enchants/respiration.png", true, "", TTAspects.AQUA, 10, TTAspects.AER, 8, TTAspects.ORDO, 5);
        add(entries, "minecraft:aqua_affinity", "thaumaturgetinker:textures/enchants/aquaaffinity.png", true, "", TTAspects.AQUA, 25, TTAspects.ORDO, 20, TTAspects.TERRA, 5);
        add(entries, "minecraft:thorns", "thaumaturgetinker:textures/enchants/thorns.png", true, "", TTAspects.TERRA, 10, TTAspects.PERDITIO, 12);
        add(entries, "minecraft:sharpness", "thaumaturgetinker:textures/enchants/sharpness.png", true, "", TTAspects.ORDO, 10);
        add(entries, "minecraft:smite", "thaumaturgetinker:textures/enchants/smite.png", true, "", TTAspects.ORDO, 5, TTAspects.AER, 5);
        add(entries, "minecraft:bane_of_arthropods", "thaumaturgetinker:textures/enchants/baneofawfulturds.png", true, "", TTAspects.ORDO, 5, TTAspects.IGNIS, 5);
        add(entries, "minecraft:knockback", "thaumaturgetinker:textures/enchants/knockback.png", true, "", TTAspects.PERDITIO, 5, TTAspects.AER, 10);
        add(entries, "minecraft:fire_aspect", "thaumaturgetinker:textures/enchants/fireaspect.png", true, "", TTAspects.IGNIS, 15, TTAspects.TERRA, 4);
        add(entries, "minecraft:looting", "thaumaturgetinker:textures/enchants/looting.png", true, "", TTAspects.AER, 10, TTAspects.IGNIS, 10, TTAspects.AQUA, 10, TTAspects.TERRA, 10, TTAspects.ORDO, 15, TTAspects.PERDITIO, 15);
        add(entries, "minecraft:efficiency", "thaumaturgetinker:textures/enchants/efficiency.png", true, "", TTAspects.PERDITIO, 12, TTAspects.TERRA, 4);
        add(entries, "minecraft:silk_touch", "thaumaturgetinker:textures/enchants/silktouch.png", true, "", TTAspects.ORDO, 50, TTAspects.TERRA, 10, TTAspects.PERDITIO, 10);
        add(entries, "minecraft:unbreaking", "thaumaturgetinker:textures/enchants/unbreaking.png", true, "", TTAspects.ORDO, 15, TTAspects.AQUA, 8, TTAspects.TERRA, 8);
        add(entries, "minecraft:fortune", "thaumaturgetinker:textures/enchants/fortune.png", true, "", TTAspects.AER, 10, TTAspects.IGNIS, 10, TTAspects.AQUA, 10, TTAspects.TERRA, 10, TTAspects.ORDO, 15, TTAspects.PERDITIO, 15);
        add(entries, "minecraft:power", "thaumaturgetinker:textures/enchants/power.png", true, "", TTAspects.TERRA, 5, TTAspects.ORDO, 10);
        add(entries, "minecraft:punch", "thaumaturgetinker:textures/enchants/punch.png", true, "", TTAspects.AER, 4, TTAspects.TERRA, 10, TTAspects.PERDITIO, 5);
        add(entries, "minecraft:flame", "thaumaturgetinker:textures/enchants/flame.png", true, "", TTAspects.PERDITIO, 5, TTAspects.IGNIS, 20, TTAspects.TERRA, 5);
        add(entries, "minecraft:infinity", "thaumaturgetinker:textures/enchants/infinity.png", true, "", TTAspects.PERDITIO, 40, TTAspects.ORDO, 40, TTAspects.TERRA, 10);
        add(entries, "minecraft:lure", "thaumaturgetinker:textures/enchants/lure.png", true, "", TTAspects.AQUA, 20, TTAspects.AER, 20);
        add(entries, "minecraft:luck_of_the_sea", "thaumaturgetinker:textures/enchants/luckofthesea.png", true, "", TTAspects.PERDITIO, 20, TTAspects.AQUA, 20);
        add(entries, "thaumaturgetinker:ascent_boost", "thaumaturgetinker:textures/enchants/ascentboost.png", false, "TTENCH_ASCENT_BOOST", TTAspects.PERDITIO, 8, TTAspects.AER, 10);
        add(entries, "thaumaturgetinker:slow_fall", "thaumaturgetinker:textures/enchants/slowfall.png", false, "TTENCH_SLOW_FALL", TTAspects.ORDO, 8, TTAspects.AER, 10);
        add(entries, "thaumaturgetinker:auto_smelt", "thaumaturgetinker:textures/enchants/autosmelt.png", false, "TTENCH_AUTO_SMELT", TTAspects.PERDITIO, 20, TTAspects.IGNIS, 30);
        add(entries, "thaumaturgetinker:desintegrate", "thaumaturgetinker:textures/enchants/desintegrate.png", false, "TTENCH_DESINTEGRATE", TTAspects.PERDITIO, 25, TTAspects.AER, 10, TTAspects.TERRA, 10);
        add(entries, "thaumaturgetinker:quick_draw", "thaumaturgetinker:textures/enchants/quickdraw.png", false, "TTENCH_QUICK_DRAW", TTAspects.ORDO, 10, TTAspects.AER, 10, TTAspects.AQUA, 5);
        add(entries, "thaumaturgetinker:vampirism", "thaumaturgetinker:textures/enchants/vamprisim.png", false, "TTENCH_VAMPIRISM", TTAspects.PERDITIO, 8, TTAspects.IGNIS, 10, TTAspects.AQUA, 10);
        add(entries, "thaumaturgetinker:focused_strike", "thaumaturgetinker:textures/enchants/focusedstrikes.png", false, "TTENCH_FOCUSED", TTAspects.ORDO, 12, TTAspects.AER, 10, TTAspects.AQUA, 10);
        add(entries, "thaumaturgetinker:dispersed_strikes", "thaumaturgetinker:textures/enchants/dispersedstrikes.png", false, "TTENCH_DISPERSED", TTAspects.PERDITIO, 12, TTAspects.IGNIS, 10, TTAspects.TERRA, 10);
        add(entries, "thaumaturgetinker:valiance", "thaumaturgetinker:textures/enchants/valiance.png", false, "TTENCH_VALIANCE", TTAspects.ORDO, 12, TTAspects.IGNIS, 10, TTAspects.TERRA, 10);
        add(entries, "thaumaturgetinker:final_strike", "thaumaturgetinker:textures/enchants/finalstrike.png", false, "TTENCH_FINAL", TTAspects.PERDITIO, 16, TTAspects.IGNIS, 16);
        add(entries, "thaumaturgetinker:tunnel", "thaumaturgetinker:textures/enchants/tunnel.png", false, "TTENCH_TUNNEL", TTAspects.TERRA, 16, TTAspects.ORDO, 16);
        add(entries, "thaumaturgetinker:shatter", "thaumaturgetinker:textures/enchants/shatter.png", false, "TTENCH_SHATTER", TTAspects.TERRA, 16, TTAspects.PERDITIO, 16);
        add(entries, "thaumaturgetinker:shockwave", "thaumaturgetinker:textures/enchants/shockwave.png", false, "TTENCH_SHOCKWAVE", TTAspects.TERRA, 16, TTAspects.AER, 16);
        add(entries, "thaumaturgetinker:pounce", "thaumaturgetinker:textures/enchants/pounce.png", false, "TTENCH_POUNCE", TTAspects.TERRA, 16, TTAspects.AER, 16);
        ENTRIES = Collections.unmodifiableMap(entries);
    }
    @SuppressWarnings("unchecked")
    private static void add(Map<String, EnchanterCostEntry> entries, String key, String texture, boolean vanilla, String research, Object... pairs) {
        var base = new LinkedHashMap<ResourceKey<IAspect>, Integer>();
        for (int i = 0; i < pairs.length; i += 2) base.put((ResourceKey<IAspect>) pairs[i], (Integer) pairs[i + 1]);
        if (entries.put(key, new EnchanterCostEntry(texture, vanilla, research, Collections.unmodifiableMap(base))) != null)
            throw new IllegalStateException("Duplicate enchanter cost: " + key);
    }
    public static java.util.Set<String> allResearch() {
        var set = new java.util.HashSet<String>();
        for (var entry : ENTRIES.values()) {
            if (!entry.research().isEmpty()) {
                set.add(entry.research());
            }
        }
        return Collections.unmodifiableSet(set);
    }

    private EnchanterCosts() {}
}
