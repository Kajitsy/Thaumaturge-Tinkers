package xyz.kajitsy.thaumaturgetinker.world;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.neoforged.neoforge.common.Tags;

/** Build 164 enum order, weights, deepslate preference, modded ore tag discovery, and blacklist semantics. */
public enum BedrockOres {
    ALUMINUM("oreAluminum", "aluminum", 617),
    AMBER("oreAmber", "amber", 161),
    APATITE("oreApatite", "apatite", 269),
    BLUE_TOPAZ("oreBlueTopaz", "blue_topaz", 238),
    CERTUS_QUARTZ("oreCertusQuartz", "certus_quartz", 234),
    CHIMERITE("oreChimerite", "chimerite", 270),
    CINNABAR("oreCinnabar", "cinnabar", 172),
    COAL("oreCoal", "coal", 2648),
    COPPER("oreCopper", "copper", 603),
    DIAMOND("oreDiamond", "diamond", 67),
    EMERALD("oreEmerald", "emerald", 48),
    DARK_IRON("oreFzDarkIron", "fz_dark_iron", 61),
    GOLD("oreGold", "gold", 164),
    AIR("oreInfusedAir", "infused_air", 94),
    EARTH("oreInfusedEarth", "infused_earth", 35),
    ENTROPY("oreInfusedEntropy", "infused_entropy", 53),
    FIRE("oreInfusedFire", "infused_fire", 42),
    ORDER("oreInfusedOrder", "infused_order", 31),
    WATER("oreInfusedWater", "infused_water", 27),
    IRON("oreIron", "iron", 1503),
    LAPIS("oreLapis", "lapis", 57),
    LEAD("oreLead", "lead", 335),
    NICKEL("oreNickel", "nickel", 72),
    PERIDOT("orePeridot", "peridot", 79),
    REDSTONE("oreRedstone", "redstone", 364),
    RUBY("oreRuby", "ruby", 57),
    SALT("oreSaltpeter", "saltpeter", 768),
    SAPPHIRE("oreSapphire", "sapphire", 70),
    SILVER("oreSilver", "silver", 416),
    SULFUR("oreSulfur", "sulfur", 105),
    TIN("oreTin", "tin", 507),
    URANIUM("oreUranium", "uranium", 112),
    VINETUM("oreVinteum", "vinteum", 392),
    QUARTZ("oreQuartz", "quartz", 160);

    public static final int DEFAULT_MODDED_WEIGHT = 150;
    private static final Map<Set<String>, List<Entry>> CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static void clearCache() {
        CACHE.clear();
    }

    public record Entry(BedrockOres kind, Block block, int weight) {
        public Entry(BedrockOres kind, Block block) {
            this(kind, block, kind != null ? kind.weight : DEFAULT_MODDED_WEIGHT);
        }
    }

    final String legacy, path;
    final int weight;

    BedrockOres(String legacy, String path, int weight) {
        this.legacy = legacy;
        this.path = path;
        this.weight = weight;
    }

    public static Block resolveFallback(BedrockOres kind) {
        var holder = switch (kind) {
            case AMBER -> com.leclowndu93150.thaumaturge.registry.TTBlocks.DEEPSLATE_ORE_AMBER;
            case CINNABAR -> com.leclowndu93150.thaumaturge.registry.TTBlocks.DEEPSLATE_ORE_CINNABAR;
            case QUARTZ -> com.leclowndu93150.thaumaturge.registry.TTBlocks.DEEPSLATE_ORE_QUARTZ;
            case AIR -> com.leclowndu93150.thaumaturge.registry.TTBlocks.CRYSTAL_AER;
            case EARTH -> com.leclowndu93150.thaumaturge.registry.TTBlocks.CRYSTAL_TERRA;
            case ENTROPY -> com.leclowndu93150.thaumaturge.registry.TTBlocks.CRYSTAL_PERDITIO;
            case FIRE -> com.leclowndu93150.thaumaturge.registry.TTBlocks.CRYSTAL_IGNIS;
            case ORDER -> com.leclowndu93150.thaumaturge.registry.TTBlocks.CRYSTAL_ORDO;
            case WATER -> com.leclowndu93150.thaumaturge.registry.TTBlocks.CRYSTAL_AQUA;
            default -> null;
        };
        if (holder != null && holder.isBound()) return holder.get();
        return switch (kind) {
            case COAL -> Blocks.DEEPSLATE_COAL_ORE;
            case COPPER -> Blocks.DEEPSLATE_COPPER_ORE;
            case DIAMOND -> Blocks.DEEPSLATE_DIAMOND_ORE;
            case EMERALD -> Blocks.DEEPSLATE_EMERALD_ORE;
            case GOLD -> Blocks.DEEPSLATE_GOLD_ORE;
            case IRON -> Blocks.DEEPSLATE_IRON_ORE;
            case LAPIS -> Blocks.DEEPSLATE_LAPIS_ORE;
            case REDSTONE -> Blocks.DEEPSLATE_REDSTONE_ORE;
            case QUARTZ -> Blocks.NETHER_QUARTZ_ORE;
            default -> null;
        };
    }

    private static void addTagBlocks(String namespace, String subPath, List<Block> out) {
        var blockTag = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, "ores/" + subPath));
        for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag)) {
            out.add(holder.value());
        }
        var itemTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, "ores/" + subPath));
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(itemTag)) {
            if (holder.value() instanceof BlockItem blockItem) {
                out.add(blockItem.getBlock());
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean isDeepslateOre(Block block) {
        var holder = block.builtInRegistryHolder();
        if (holder.is(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE)) return true;
        if (block.asItem().builtInRegistryHolder().is(Tags.Items.ORES_IN_GROUND_DEEPSLATE)) return true;
        return BuiltInRegistries.BLOCK.getKey(block).getPath().contains("deepslate");
    }

    @SuppressWarnings("deprecation")
    private static boolean isOverworldStoneOre(Block block) {
        if (block.builtInRegistryHolder().is(Tags.Blocks.ORES_IN_GROUND_STONE)) return true;
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return !path.contains("nether") && !path.contains("end");
    }

    public static Block findDeepslateSibling(Block block) {
        var id = BuiltInRegistries.BLOCK.getKey(block);
        String ns = id.getNamespace();
        String path = id.getPath();
        if (path.contains("deepslate")) return block;
        List<String> candidates = new ArrayList<>();
        candidates.add("deepslate_" + path);
        candidates.add(path + "_deepslate");
        if (path.startsWith("stone_")) {
            candidates.add("deepslate_" + path.substring("stone_".length()));
        }
        if (path.endsWith("_stone")) {
            candidates.add(path.substring(0, path.length() - "_stone".length()) + "_deepslate");
        }
        for (String candidatePath : candidates) {
            var opt = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath(ns, candidatePath));
            if (opt.isPresent() && !opt.get().defaultBlockState().isAir()) {
                return opt.get();
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static Block preferDeepslate(List<Block> candidates) {
        var valid = new ArrayList<Block>();
        for (var b : candidates) {
            if (b != null && b != Blocks.AIR && !b.defaultBlockState().isAir() && !valid.contains(b)) {
                valid.add(b);
            }
        }
        if (valid.isEmpty()) return null;
        for (var b : valid) {
            if (b.builtInRegistryHolder().is(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE)
                    || b.asItem().builtInRegistryHolder().is(Tags.Items.ORES_IN_GROUND_DEEPSLATE)) {
                return b;
            }
        }
        for (var b : valid) {
            if (BuiltInRegistries.BLOCK.getKey(b).getPath().contains("deepslate")) {
                return b;
            }
        }
        for (var b : valid) {
            var sibling = findDeepslateSibling(b);
            if (sibling != null) return sibling;
        }
        for (var b : valid) {
            if (isOverworldStoneOre(b)) return b;
        }
        return valid.getFirst();
    }

    private static String stripDeepslate(String path) {
        String s = path;
        if (s.startsWith("deepslate_")) s = s.substring("deepslate_".length());
        if (s.endsWith("_deepslate")) s = s.substring(0, s.length() - "_deepslate".length());
        if (s.startsWith("stone_")) s = s.substring("stone_".length());
        if (s.endsWith("_stone")) s = s.substring(0, s.length() - "_stone".length());
        return s;
    }

    private static boolean isLikelyOrePath(String path) {
        return path.endsWith("_ore") || path.startsWith("ore_") || path.startsWith("deepslate_ore_") || path.endsWith("_ore_deepslate");
    }

    private static String toLegacyOreDict(String material) {
        var sb = new StringBuilder("ore");
        for (String part : material.split("[_/]")) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    private static boolean isBlockBlacklisted(Set<String> blacklist, Block block) {
        if (blacklist.isEmpty() || block == null) return false;
        var id = BuiltInRegistries.BLOCK.getKey(block);
        return blacklist.contains(id.toString())
                || blacklist.contains(id.getPath())
                || blacklist.contains(stripDeepslate(id.getPath()));
    }

    private static boolean isMaterialBlacklisted(Set<String> blacklist, String material, List<Block> blocks) {
        if (blacklist.isEmpty()) return false;
        if (blacklist.contains(material)
                || blacklist.contains("ores/" + material)
                || blacklist.contains("c:ores/" + material)
                || blacklist.contains(toLegacyOreDict(material))) {
            return true;
        }
        for (var b : blocks) {
            if (isBlockBlacklisted(blacklist, b)) return true;
        }
        return false;
    }

    public static List<Entry> entries(Set<String> blacklist) {
        boolean hasTags = BuiltInRegistries.BLOCK.getTags().findAny().isPresent();
        if (hasTags) {
            var cached = CACHE.get(blacklist);
            if (cached != null) return cached;
        }
        var result = new ArrayList<Entry>();
        var handledBlocks = new HashSet<Block>();
        var addedBlocks = new HashSet<Block>();
        var handledMaterials = new HashSet<String>();

        for (var kind : values()) {
            handledMaterials.add(kind.path);
            var candidates = new ArrayList<Block>();
            addTagBlocks("c", kind.path, candidates);
            addTagBlocks("thaumaturge", kind.path, candidates);
            addTagBlocks("forge", kind.path, candidates);
            var fallback = resolveFallback(kind);
            if (fallback != null) candidates.add(fallback);

            for (var c : candidates) {
                if (c != null) {
                    handledBlocks.add(c);
                    var sib = findDeepslateSibling(c);
                    if (sib != null) handledBlocks.add(sib);
                }
            }
            if (blacklist.contains(kind.legacy) || isMaterialBlacklisted(blacklist, kind.path, candidates)) continue;
            Block resolved = preferDeepslate(candidates);
            if (resolved != null && !isBlockBlacklisted(blacklist, resolved) && addedBlocks.add(resolved)) {
                handledBlocks.add(resolved);
                result.add(new Entry(kind, resolved, kind.weight));
            }
        }

        // Discover all modded ores from any `*:ores/<material>` block or item tag
        var extraByMaterial = new TreeMap<String, List<Block>>();
        BuiltInRegistries.BLOCK.getTags().forEach(pair -> {
            String tagPath = pair.getFirst().location().getPath();
            if (!tagPath.startsWith("ores/")) return;
            String material = tagPath.substring("ores/".length());
            if (material.isEmpty() || handledMaterials.contains(material)) return;
            var list = extraByMaterial.computeIfAbsent(material, k -> new ArrayList<>());
            for (var holder : pair.getSecond()) {
                list.add(holder.value());
            }
        });
        BuiltInRegistries.ITEM.getTags().forEach(pair -> {
            String tagPath = pair.getFirst().location().getPath();
            if (!tagPath.startsWith("ores/")) return;
            String material = tagPath.substring("ores/".length());
            if (material.isEmpty() || handledMaterials.contains(material)) return;
            var list = extraByMaterial.computeIfAbsent(material, k -> new ArrayList<>());
            for (var holder : pair.getSecond()) {
                if (holder.value() instanceof BlockItem blockItem) {
                    list.add(blockItem.getBlock());
                }
            }
        });
        for (var entry : extraByMaterial.entrySet()) {
            String material = entry.getKey();
            var group = entry.getValue();
            for (var b : group) {
                if (b != null) {
                    handledBlocks.add(b);
                    var sib = findDeepslateSibling(b);
                    if (sib != null) handledBlocks.add(sib);
                }
            }
            if (isMaterialBlacklisted(blacklist, material, group)) continue;
            Block chosen = preferDeepslate(group);
            if (chosen != null && !isBlockBlacklisted(blacklist, chosen) && addedBlocks.add(chosen)) {
                handledBlocks.add(chosen);
                result.add(new Entry(null, chosen, DEFAULT_MODDED_WEIGHT));
            }
        }

        // Discover any modded ores tagged directly in `c:ores` / `forge:ores` without a material sub-tag
        var directOres = new TreeMap<String, List<Block>>();
        var forgeBlockOres = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "ores"));
        var forgeItemOres = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", "ores"));
        for (var tag : List.of(Tags.Blocks.ORES, forgeBlockOres)) {
            for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                Block b = holder.value();
                if (handledBlocks.contains(b) || b == Blocks.AIR || b.defaultBlockState().isAir()) continue;
                var id = BuiltInRegistries.BLOCK.getKey(b);
                directOres.computeIfAbsent(id.getNamespace() + ":" + stripDeepslate(id.getPath()), k -> new ArrayList<>()).add(b);
            }
        }
        for (var tag : List.<TagKey<Item>>of(Tags.Items.ORES, forgeItemOres)) {
            for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                if (holder.value() instanceof BlockItem blockItem) {
                    Block b = blockItem.getBlock();
                    if (handledBlocks.contains(b) || b == Blocks.AIR || b.defaultBlockState().isAir()) continue;
                    var id = BuiltInRegistries.BLOCK.getKey(b);
                    directOres.computeIfAbsent(id.getNamespace() + ":" + stripDeepslate(id.getPath()), k -> new ArrayList<>()).add(b);
                }
            }
        }
        for (var group : directOres.values()) {
            for (var b : group) {
                handledBlocks.add(b);
                var sib = findDeepslateSibling(b);
                if (sib != null) handledBlocks.add(sib);
            }
            Block chosen = preferDeepslate(group);
            if (chosen != null && !isBlockBlacklisted(blacklist, chosen) && addedBlocks.add(chosen)) {
                handledBlocks.add(chosen);
                result.add(new Entry(null, chosen, DEFAULT_MODDED_WEIGHT));
            }
        }

        // Fallback scan for untagged mod ore blocks
        var untaggedOres = new TreeMap<String, List<Block>>();
        for (var b : BuiltInRegistries.BLOCK) {
            if (handledBlocks.contains(b) || b == Blocks.AIR || b.defaultBlockState().isAir() || b instanceof EntityBlock) continue;
            var id = BuiltInRegistries.BLOCK.getKey(b);
            if (id.getNamespace().equals("minecraft") || !isLikelyOrePath(id.getPath())) continue;
            untaggedOres.computeIfAbsent(id.getNamespace() + ":" + stripDeepslate(id.getPath()), k -> new ArrayList<>()).add(b);
        }
        for (var group : untaggedOres.values()) {
            Block chosen = preferDeepslate(group);
            if (chosen != null && !isBlockBlacklisted(blacklist, chosen) && addedBlocks.add(chosen)) {
                result.add(new Entry(null, chosen, DEFAULT_MODDED_WEIGHT));
            }
        }

        var immutable = List.copyOf(result);
        if (hasTags) CACHE.put(Set.copyOf(blacklist), immutable);
        return immutable;
    }

    public static int sum(List<Entry> entries) {
        return entries.stream().mapToInt(Entry::weight).sum();
    }

    public static Block choose(Random random, List<Entry> entries) {
        int total = sum(entries);
        if (total <= 0) return Blocks.GOLD_ORE;
        int value = random.nextInt(total);
        for (var entry : entries) {
            if ((value -= entry.weight()) < 0) return entry.block();
        }
        return Blocks.GOLD_ORE;
    }
}
