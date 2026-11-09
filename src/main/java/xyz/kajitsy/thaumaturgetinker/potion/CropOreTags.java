package xyz.kajitsy.thaumaturgetinker.potion;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

/** Compatibility names for the removed OreDictionary. Individual tag matches remain distinct. */
public final class CropOreTags {
    public record Source(String name, List<ItemStack> items) {}

    public static String legacyName(ResourceLocation id) {
        var path = id.getPath();
        if (id.getNamespace().equals("thaumaturgetinker") && path.startsWith("crop_ores/")) {
            var words = path.substring("crop_ores/".length()).split("_");
            var name = new StringBuilder(words[0]);
            for (int i = 1; i < words.length; i++) {
                if (!words[i].isEmpty()) name.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
            }
            return name.toString();
        }
        if (id.getNamespace().equals("thaumaturge") && path.equals("shards")) return "shard";
        if (id.getNamespace().equals("minecraft")) return switch (path) {
            case "logs" -> "logWood";
            case "planks" -> "plankWood";
            case "saplings" -> "sapling";
            default -> null;
        };
        if (!id.getNamespace().equals("c") && !id.getNamespace().equals("forge") && !id.getNamespace().equals("neoforge")) return null;
        var parts = path.split("/");
        if (parts.length == 2) {
            String material = parts[1];
            String suffix = Character.toUpperCase(material.charAt(0)) + material.substring(1);
            return switch (parts[0]) {
                case "ingots" -> "ingot" + suffix;
                case "nuggets" -> "nugget" + suffix;
                case "ores" -> "ore" + suffix;
                case "storage_blocks" -> "block" + suffix;
                case "dusts" -> "dust" + suffix;
                case "gears" -> "gear" + suffix;
                case "buckets" -> "bucket" + suffix;
                case "rods" -> material.equals("wooden") ? "stickWood" : "rod" + suffix;
                default -> null;
            };
        }
        return switch (path) {
            case "slime_balls" -> "slimeball";
            case "buckets" -> "bucket";
            case "gears" -> "gear";
            case "saplings" -> "sapling";
            default -> null;
        };
    }

    public static List<Source> sources() {
        var result = new ArrayList<Source>();
        BuiltInRegistries.ITEM.getTags().sorted(Comparator.comparing(tag -> tag.getFirst().location().toString())).forEach(tag -> {
            var name = legacyName(tag.getFirst().location());
            if (name != null) {
                var stacks = new ArrayList<ItemStack>();
                for (var itemHolder : tag.getSecond()) {
                    stacks.add(new ItemStack(itemHolder.value()));
                }
                result.add(new Source(name, stacks));
            }
        });
        return result;
    }

    public static void expand(InfusedCropLoot loot, ResourceKey<IAspect> aspect, String target, int count, int repeats, List<Source> sources) {
        String capital = target.isEmpty() ? target : Character.toUpperCase(target.charAt(0)) + target.substring(1).toLowerCase(Locale.ROOT);
        for (int pass = 0; pass < repeats; pass++) {
            for (var source : sources) {
                if (source.name().contains(target) || source.name().contains(capital)) {
                    for (var item : source.items()) {
                        var stack = item.copy();
                        stack.setCount(count);
                        loot.add(aspect, stack, 1);
                    }
                }
            }
        }
    }

    public static void addAll(InfusedCropLoot loot) {
        var sources = sources();
        expand(loot, TTAspects.ALIENIS, "bucketEnder", 1, 2, sources);
        expand(loot, TTAspects.HERBA, "wood", 1, 2, sources);
        expand(loot, TTAspects.GELUM, "rodBlizz", 1, 2, sources);
        expand(loot, TTAspects.HERBA, "sapling", 1, 2, sources);
        expand(loot, TTAspects.AQUA, "slime", 1, 2, sources);
        expand(loot, TTAspects.MACHINA, "gear", 1, 2, sources);
        expand(loot, TTAspects.METALLUM, "iron", 1, 2, sources);
        expand(loot, TTAspects.PERMUTATIO, "ingotCopper", 4, 1, sources);
        expand(loot, TTAspects.PRAECANTATIO, "shard", 1, 2, sources);
        expand(loot, TTAspects.VOLATUS, "cloud", 100, 1, sources);
        expand(loot, TTAspects.VACUOS, "bucket", 1, 2, sources);
    }
}
