package xyz.kajitsy.thaumaturgetinker;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.TTSpellParts;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireKind;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireTransformations;
import xyz.kajitsy.thaumaturgetinker.block.OrderFireResolver;
import xyz.kajitsy.thaumaturgetinker.enchantment.StrikeEnchantments;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedHelm;
import xyz.kajitsy.thaumaturgetinker.focus.FlightFocus;
import xyz.kajitsy.thaumaturgetinker.focus.HealFocus;
import xyz.kajitsy.thaumaturgetinker.focus.RecallFocus;
import xyz.kajitsy.thaumaturgetinker.focus.ShadowbeamTrace;
import xyz.kajitsy.thaumaturgetinker.focus.SmeltFocus;
import xyz.kajitsy.thaumaturgetinker.focus.TelekinesisFocus;
import xyz.kajitsy.thaumaturgetinker.focus.TinkererFocus;
import xyz.kajitsy.thaumaturgetinker.world.BedrockOres;

import static org.junit.jupiter.api.Assertions.*;

class PortFixesTest {
    private static final Identifier OVERWORLD = Identifier.withDefaultNamespace("overworld");
    private static final Identifier NETHER = Identifier.withDefaultNamespace("the_nether");

    @BeforeAll
    static void bindDataComponents() {
        var defaultComponents = DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build();
        BuiltInRegistries.ITEM.listElements().forEach(holder -> {
            if (!holder.areComponentsBound()) {
                holder.bindComponents(defaultComponents);
            }
        });
    }

    private static ItemStack pearlAt(Identifier dim, BlockPos pos) {
        var stack = new ItemStack(Items.ENDER_PEARL);
        SkyPearlData.bind(stack, pos, dim);
        return stack;
    }

    @Test
    void recallFocusSameDimensionAndPouchScanning() {
        var wrongDimPearl = pearlAt(NETHER, new BlockPos(1, 2, 3));
        var unattuned = new ItemStack(Items.ENDER_PEARL);
        var invPouch = new ItemStack(Items.BUNDLE);
        var invPouchPearl = pearlAt(OVERWORLD, new BlockPos(10, 64, 10));
        var curioPouch = new ItemStack(Items.CHEST);
        var curioPearl = pearlAt(OVERWORLD, new BlockPos(99, 70, 99));

        Map<ItemStack, List<ItemStack>> pouches = Map.of(
                invPouch, List.of(wrongDimPearl, invPouchPearl),
                curioPouch, List.of(curioPearl)
        );

        // Skips wrong-dim & unattuned pearls in inventory and picks same-dim pearl inside inventory pouch
        var foundInInvPouch = RecallFocus.findRecallPearl(
                List.of(wrongDimPearl, unattuned, invPouch),
                curioPouch,
                OVERWORLD,
                s -> s.is(Items.ENDER_PEARL),
                s -> pouches.getOrDefault(s, List.of())
        );
        assertSame(invPouchPearl, foundInInvPouch);

        // Falls back to curio pouch when inventory and inventory pouches lack a same-dimension pearl
        var foundInCurio = RecallFocus.findRecallPearl(
                List.of(wrongDimPearl, unattuned),
                curioPouch,
                OVERWORLD,
                s -> s.is(Items.ENDER_PEARL),
                s -> pouches.getOrDefault(s, List.of())
        );
        assertSame(curioPearl, foundInCurio);

        // Returns empty when only another-dimension pearl exists everywhere (no cross-dim teleport)
        var noneInEnd = RecallFocus.findRecallPearl(
                List.of(wrongDimPearl, invPouch),
                curioPouch,
                Identifier.withDefaultNamespace("the_end"),
                s -> s.is(Items.ENDER_PEARL),
                s -> pouches.getOrDefault(s, List.of())
        );
        assertTrue(noneInEnd.isEmpty());
    }

    @Test
    void focusUpgradesAllowlistPotencyAndFrugal() {
        assertTrue(TinkererFocus.isAllowedSpellPart(false, Spell.ORIGIN));
        assertTrue(TinkererFocus.isAllowedSpellPart(false, TTSpellParts.EFFICIENT));
        assertFalse(TinkererFocus.isAllowedSpellPart(false, TTSpellParts.AMPLIFY));
        assertFalse(TinkererFocus.isAllowedSpellPart(true, TTSpellParts.FIRE));
        assertFalse(TinkererFocus.isAllowedSpellPart(true, TTSpellParts.BOLT));
        assertTrue(TinkererFocus.isAllowedSpellPart(true, TTSpellParts.AMPLIFY));

        var stack = new ItemStack(Items.STICK);
        var root = SpellNode.of(Spell.ORIGIN)
                .then(SpellNode.of(TTSpellParts.EFFICIENT))
                .then(SpellNode.of(TTSpellParts.AMPLIFY).withSetting("level", 3));
        Spells.setSpell(stack, Spell.empty().withRoot(root));

        assertTrue(TinkererFocus.hasFrugal(stack));
        assertEquals(3, TinkererFocus.potencyLevel(stack));

        var scaled = TinkererFocus.applyFrugal(Map.of(TTAspects.IGNIS, 45, TTAspects.PERDITIO, 1), true);
        assertEquals(32, scaled.get(TTAspects.IGNIS));
        assertEquals(1, scaled.get(TTAspects.PERDITIO));
        assertEquals(45, TinkererFocus.applyFrugal(Map.of(TTAspects.IGNIS, 45), false).get(TTAspects.IGNIS));

        assertTrue(FlightFocus.flightForce(3) > FlightFocus.flightForce(0));
        assertEquals(30, HealFocus.healIntervalTicks(0));
        assertEquals(20, HealFocus.healIntervalTicks(3));
        assertEquals(5, HealFocus.healIntervalTicks(99));
        assertEquals(20, SmeltFocus.smeltDurationTicks(0));
        assertEquals(5, SmeltFocus.smeltDurationTicks(3));
        assertEquals(9, TelekinesisFocus.telekinesisRange(3));
        assertEquals(11f, ShadowbeamTrace.damageForPotency(3));

        var xpDrain = new ItemStack(Items.STICK);
        assertFalse(TinkererFocus.hasFrugal(xpDrain));
        assertEquals(0, TinkererFocus.potencyLevel(xpDrain));
        assertEquals(15, xyz.kajitsy.thaumaturgetinker.focus.XPDrainFocus.xpUse(xpDrain));
        Spells.setSpell(xpDrain, Spell.empty().withRoot(SpellNode.of(Spell.ORIGIN).then(SpellNode.of(TTSpellParts.EFFICIENT))));
        assertEquals(11, xyz.kajitsy.thaumaturgetinker.focus.XPDrainFocus.xpUse(xpDrain));
    }

    @Test
    void tinkererFociBlockedInFocalManipulator() {
        var foci = BuiltInRegistries.ITEM.stream()
                .filter(xyz.kajitsy.thaumaturgetinker.focus.TinkererFocus.class::isInstance)
                .toList();
        assertFalse(foci.isEmpty(), "Expected TinkererFocus items in registry");
        var spell = Spell.empty().withRoot(SpellNode.of(Spell.ORIGIN).then(SpellNode.of(TTSpellParts.EFFICIENT)));
        for (var item : foci) {
            var focus = new ItemStack(item);
            assertFalse(TinkererFocus.canEditInFocalManipulator(focus));
            var focusEvent = new com.leclowndu93150.thaumaturge.api.spell.event.SpellInscribeEvent(null, BlockPos.ZERO, focus, spell, null);
            TinkererFocus.onSpellInscribe(focusEvent);
            assertTrue(focusEvent.isCanceled());
        }

        var other = new ItemStack(Items.STICK);
        assertTrue(TinkererFocus.canEditInFocalManipulator(other));
        var otherEvent = new com.leclowndu93150.thaumaturge.api.spell.event.SpellInscribeEvent(null, BlockPos.ZERO, other, spell, null);
        TinkererFocus.onSpellInscribe(otherEvent);
        assertFalse(otherEvent.isCanceled());
    }

    @Test
    void ichorSpearsAndShieldsAreRegisteredWithExpectedTypesAndModes() {
        var spear = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "ichor_spear"));
        var awakenedSpear = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "ichor_spear_gem"));
        var shield = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "ichor_shield"));
        var awakenedShield = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ThaumicTinkererModern.ID, "ichor_shield_gem"));

        var spearImpl = assertInstanceOf(xyz.kajitsy.thaumaturgetinker.item.IchorSpear.class, spear);
        var awakenedSpearImpl = assertInstanceOf(xyz.kajitsy.thaumaturgetinker.item.AwakenedSpear.class, awakenedSpear);
        var shieldImpl = assertInstanceOf(xyz.kajitsy.thaumaturgetinker.item.IchorShield.class, shield);
        var awakenedShieldImpl = assertInstanceOf(xyz.kajitsy.thaumaturgetinker.item.AwakenedShield.class, awakenedShield);

        assertTrue(spearImpl.isFoil(new ItemStack(spear)));
        assertTrue(awakenedSpearImpl.isFoil(new ItemStack(awakenedSpear)));
        assertTrue(shieldImpl.isFoil(new ItemStack(shield)));
        assertTrue(awakenedShieldImpl.isFoil(new ItemStack(awakenedShield)));
        assertEquals("spear", awakenedSpearImpl.toolType());
        assertEquals("shield", awakenedShieldImpl.toolType());

        var stack = new ItemStack(awakenedSpear);
        assertEquals(0, xyz.kajitsy.thaumaturgetinker.item.AdvancedToolModes.get(stack));
        assertEquals(1, xyz.kajitsy.thaumaturgetinker.item.AdvancedToolModes.change(stack));
        assertEquals(2, xyz.kajitsy.thaumaturgetinker.item.AdvancedToolModes.change(stack));
        assertEquals(0, xyz.kajitsy.thaumaturgetinker.item.AdvancedToolModes.change(stack));
    }

    @Test
    void strikeEnchantmentsAccumulateAndUseFloatDivision() {
        // Successive hits with Focused Strike must scale beyond 1
        float hit0 = StrikeEnchantments.strikeDamage(10f, 1, 0, 0);
        float hit1 = StrikeEnchantments.strikeDamage(10f, 1, 0, 1);
        float hit2 = StrikeEnchantments.strikeDamage(10f, 1, 0, 2);
        assertEquals(5f, hit0, 0.001f);
        assertEquals(7.5f, hit1, 0.001f);
        assertEquals(10f, hit2, 0.001f);

        // Dispersed Strike uses fractional 1 + successive / 5.0 (not integer floor)
        float disp1 = StrikeEnchantments.strikeDamage(10f, 0, 1, 1);
        assertEquals(10f * 1.2f / 3f, disp1, 0.001f);
    }

    @Test
    void infusedFiresAndOrderFireRawOres() {
        assertTrue(OrderFireResolver.normalizeRawOreInput(Blocks.IRON_ORE.defaultBlockState(), new ItemStack(Items.RAW_IRON)).is(Items.IRON_ORE));
        assertTrue(OrderFireResolver.normalizeRawOreInput(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), new ItemStack(Items.RAW_IRON)).is(Items.IRON_ORE));
        assertTrue(OrderFireResolver.normalizeRawOreInput(Blocks.GOLD_ORE.defaultBlockState(), new ItemStack(Items.RAW_GOLD)).is(Items.GOLD_ORE));
        assertTrue(OrderFireResolver.normalizeRawOreInput(Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState(), new ItemStack(Items.RAW_GOLD)).is(Items.GOLD_ORE));
        assertTrue(OrderFireResolver.normalizeRawOreInput(Blocks.COPPER_ORE.defaultBlockState(), new ItemStack(Items.RAW_COPPER)).is(Items.COPPER_ORE));
        assertTrue(OrderFireResolver.normalizeRawOreInput(Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState(), new ItemStack(Items.RAW_COPPER)).is(Items.COPPER_ORE));
        assertTrue(OrderFireResolver.normalizeRawOreInput(Blocks.COAL_ORE.defaultBlockState(), new ItemStack(Items.COAL)).is(Items.COAL));

        assertEquals(Blocks.NETHER_QUARTZ_ORE.defaultBlockState(), InfusedFireTransformations.fixed(InfusedFireKind.FIRE, Blocks.GOLD_ORE.defaultBlockState(), true));
        assertEquals(Blocks.NETHER_QUARTZ_ORE.defaultBlockState(), InfusedFireTransformations.fixed(InfusedFireKind.FIRE, Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState(), true));
        assertEquals(Blocks.NETHER_QUARTZ_ORE.defaultBlockState(), InfusedFireTransformations.fixed(InfusedFireKind.FIRE, Blocks.COPPER_ORE.defaultBlockState(), true));
        assertEquals(Blocks.NETHER_QUARTZ_ORE.defaultBlockState(), InfusedFireTransformations.fixed(InfusedFireKind.FIRE, Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState(), true));
        assertNull(InfusedFireTransformations.fixed(InfusedFireKind.FIRE, Blocks.GOLD_BLOCK.defaultBlockState(), true));
        assertEquals(Blocks.BROWN_MUSHROOM.defaultBlockState(), InfusedFireTransformations.fixed(InfusedFireKind.FIRE, Blocks.DANDELION.defaultBlockState(), true));
    }

    @Test
    void kamiUnlockResearchFilterPredicate() {
        assertTrue(ThaumicTinkererModern.requiresEntryForKami(Identifier.fromNamespaceAndPath("thaumaturge", "base_eldritch"), true, false));
        assertTrue(ThaumicTinkererModern.requiresEntryForKami(Identifier.fromNamespaceAndPath("thaumaturgetinker", "enchanter"), true, false));

        assertFalse(ThaumicTinkererModern.requiresEntryForKami(Identifier.fromNamespaceAndPath("thaumaturgetinker", "ichor"), true, false));
        assertFalse(ThaumicTinkererModern.requiresEntryForKami(Identifier.fromNamespaceAndPath("thaumaturgetinker", "ichorium"), true, false));
        assertFalse(ThaumicTinkererModern.requiresEntryForKami(Identifier.fromNamespaceAndPath("thaumaturge", "scanned/entity/thaumaturge/cultist"), false, false));
        assertFalse(ThaumicTinkererModern.requiresEntryForKami(Identifier.fromNamespaceAndPath("thaumaturge", "scanned/foo"), true, false));
        assertFalse(ThaumicTinkererModern.requiresEntryForKami(Identifier.fromNamespaceAndPath("thaumaturgetinker", "dark_quartz"), true, true));
        assertFalse(ThaumicTinkererModern.requiresEntryForKami(Identifier.fromNamespaceAndPath("thaumaturge", "flux"), false, false));
        assertFalse(ThaumicTinkererModern.requiresEntryForKami(Identifier.fromNamespaceAndPath("other_mod", "entry"), true, false));
    }

    @Test
    void noKamiDescendantCreatesResearchDeadlock() throws Exception {
        var resource = getClass().getResource("/data/thaumaturgetinker/thaumaturge/research_entry/ichor.json");
        assertNotNull(resource);
        var uri = resource.toURI();
        var parentsByPath = new java.util.HashMap<String, List<String>>();
        var autoByPath = new java.util.HashMap<String, Boolean>();
        if ("jar".equals(uri.getScheme())) {
            try (var fs = java.nio.file.FileSystems.newFileSystem(uri, Map.of())) {
                readResearchEntries(fs.getPath("/data/thaumaturgetinker/thaumaturge/research_entry"), parentsByPath, autoByPath);
            }
        } else {
            readResearchEntries(java.nio.file.Path.of(uri).getParent(), parentsByPath, autoByPath);
        }
        assertFalse(parentsByPath.isEmpty());
        // Any TT entry whose parent chain reaches thaumaturgetinker:ichor must NOT be required to unlock ichor!
        for (var path : parentsByPath.keySet()) {
            boolean reachesIchor = reachesIchor(path, parentsByPath, new java.util.HashSet<>());
            if (reachesIchor) {
                assertFalse(
                        ThaumicTinkererModern.requiresEntryForKami(
                                Identifier.fromNamespaceAndPath("thaumaturgetinker", path),
                                !parentsByPath.get(path).isEmpty(),
                                autoByPath.getOrDefault(path, false)),
                        "KAMI entry '" + path + "' depends on ichor and would deadlock KAMI unlock");
            }
        }
    }

    private static void readResearchEntries(
            java.nio.file.Path dir,
            Map<String, List<String>> parentsByPath,
            Map<String, Boolean> autoByPath) throws Exception {
        try (var stream = java.nio.file.Files.list(dir)) {
            for (var file : stream.filter(p -> p.toString().endsWith(".json")).toList()) {
                String path = file.getFileName().toString().replace(".json", "");
                var obj = com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(file)).getAsJsonObject();
                var parents = new java.util.ArrayList<String>();
                if (obj.has("parents")) {
                    for (var el : obj.getAsJsonArray("parents")) {
                        parents.add(el.getAsString().replaceFirst("^~", ""));
                    }
                }
                parentsByPath.put(path, parents);
                boolean auto = obj.has("meta") && obj.getAsJsonArray("meta").asList().stream().anyMatch(m -> "autounlock".equals(m.getAsString()));
                autoByPath.put(path, auto);
            }
        }
    }

    private static boolean reachesIchor(String path, Map<String, List<String>> parentsByPath, Set<String> visited) {
        if ("ichor".equals(path)) return true;
        if (!visited.add(path)) return false;
        for (var parent : parentsByPath.getOrDefault(path, List.of())) {
            if (parent.startsWith("thaumaturgetinker:") && reachesIchor(parent.substring("thaumaturgetinker:".length()), parentsByPath, visited)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void bedrockOresSelectionRespectsBlacklistAndEmptyFallback() {
        assertEquals(Blocks.GOLD_ORE, BedrockOres.choose(new Random(0), List.of()));
        var onlyIron = List.of(new BedrockOres.Entry(BedrockOres.IRON, Blocks.IRON_ORE));
        assertEquals(Blocks.IRON_ORE, BedrockOres.choose(new Random(42), onlyIron));
        assertTrue(BedrockOres.entries(Set.of("oreFirestone")).stream().noneMatch(e -> e.block() == null));
    }

    @Test
    void localizationAndBedrockDimensionResourcesMatchExpectedSchema() throws Exception {
        var en = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(
                getClass().getResourceAsStream("/assets/thaumaturgetinker/lang/en_us.json"))).getAsJsonObject();
        var ru = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(
                getClass().getResourceAsStream("/assets/thaumaturgetinker/lang/ru_ru.json"))).getAsJsonObject();

        assertEquals(en.keySet(), ru.keySet());
        assertTrue(ru.has("item.darkQuartzItem.name"));
        assertTrue(ru.has("item.gaseousLightItem.name"));
        assertTrue(ru.has("item.gaseousShadowItem.name"));
        assertTrue(ru.has("tile.bedrockPortal.name"));
        assertTrue(ru.has("ttmisc.kami.locked"));
        assertTrue(ru.has("tooltip.thaumaturge.spell.vis_cost"));
        assertTrue(ru.has("spell.thaumaturge.amplify.name"));
        assertTrue(ru.has("spell.thaumaturge.efficient.name"));

        var bedrockDim = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(
                getClass().getResourceAsStream("/data/thaumaturgetinker/dimension_type/bedrock.json"))).getAsJsonObject();
        assertTrue(bedrockDim.get("has_skylight").getAsBoolean());
        assertEquals("overworld", bedrockDim.get("skybox").getAsString());
        assertEquals(1.0, bedrockDim.get("ambient_light").getAsDouble(), 0.001);
        var attrs = bedrockDim.getAsJsonObject("attributes");
        assertEquals("#a080a0", attrs.get("minecraft:visual/fog_color").getAsString());
        assertEquals(1.0, attrs.get("minecraft:visual/sky_light_factor").getAsDouble(), 0.001);
    }

    @Test
    void awakenedHelmNightVisionStaysAboveFlickerThreshold() {
        assertTrue(AwakenedHelm.NIGHT_VISION_DURATION > 200);
        assertTrue(AwakenedHelm.NIGHT_VISION_REFRESH_THRESHOLD > 200);
    }

    @Test
    void ichorShieldsAreNonStackable() throws Exception {
        assertEquals(1, builtMaxStackSize(xyz.kajitsy.thaumaturgetinker.item.KamiTools.SHIELD.getKey()));
        assertEquals(1, builtMaxStackSize(xyz.kajitsy.thaumaturgetinker.item.AwakenedShield.ITEM.getKey()));
    }

    @SuppressWarnings("unchecked")
    private static int builtMaxStackSize(net.minecraft.resources.ResourceKey<net.minecraft.world.item.Item> key) throws Exception {
        var field = net.minecraft.core.component.DataComponentInitializers.class.getDeclaredField("initializers");
        field.setAccessible(true);
        var entries = (List<?>) field.get(net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_INITIALIZERS);
        var keyMethod = entries.getFirst().getClass().getDeclaredMethod("key");
        var initMethod = entries.getFirst().getClass().getDeclaredMethod("initializer");
        keyMethod.setAccessible(true);
        initMethod.setAccessible(true);
        for (Object entry : entries) {
            if (key.equals(keyMethod.invoke(entry))) {
                var builder = DataComponentMap.builder();
                var init = (net.minecraft.core.component.DataComponentInitializers.Initializer<net.minecraft.world.item.Item>) initMethod.invoke(entry);
                try {
                    init.run(builder, net.minecraft.core.RegistryAccess.EMPTY, key);
                } catch (IllegalStateException ignored) {
                    // Delayed components (e.g. BLOCKS_ATTACKS) require full world tag lookups after MAX_STACK_SIZE is set.
                }
                return builder.build().getOrDefault(DataComponents.MAX_STACK_SIZE, 64);
            }
        }
        throw new AssertionError("Missing DataComponentInitializer for " + key);
    }

    @Test
    void smeltFocusHandlesBothBlockAndItemSmeltOutputs() {
        assertTrue(xyz.kajitsy.thaumaturgetinker.focus.SmeltFocus.isInPlaceBlockResult(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE)));
        assertFalse(xyz.kajitsy.thaumaturgetinker.focus.SmeltFocus.isInPlaceBlockResult(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT)));
        assertFalse(xyz.kajitsy.thaumaturgetinker.focus.SmeltFocus.isInPlaceBlockResult(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CHEST)));
    }

    @Test
    void tinkererFocusAuraCostScalesAndClampsCleanly() {
        var cost = Map.of(
                com.leclowndu93150.thaumaturge.api.aspect.TTAspects.IGNIS, 45,
                com.leclowndu93150.thaumaturge.api.aspect.TTAspects.PERDITIO, 12);
        assertEquals(0.57F, xyz.kajitsy.thaumaturgetinker.focus.TinkererFocus.auraCostVis(cost, 1.0F), 1.0E-5F);
        assertEquals(0.285F, xyz.kajitsy.thaumaturgetinker.focus.TinkererFocus.auraCostVis(cost, 0.5F), 1.0E-5F);
        assertEquals(0.057F, xyz.kajitsy.thaumaturgetinker.focus.TinkererFocus.auraCostVis(cost, 0.01F), 1.0E-5F);
    }

    @Test
    void focusTexturesAreSymmetricallyCentered() throws Exception {
        for (String name : List.of(
                "focusdeflect", "focusdislocation", "focusenderchest", "focusflight",
                "focusheal", "focusrecall", "focusshadowbeam", "focussmelt",
                "focustelekinesis", "focusxpdrain")) {
            var img = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/assets/thaumaturgetinker/textures/items/" + name + ".png"));
            int minX = img.getWidth(), maxX = -1, minY = img.getHeight(), maxY = -1;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    if ((img.getRGB(x, y) >>> 24) > 0) {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
            assertEquals(minX, img.getWidth() - 1 - maxX, name + " horizontal centering");
            assertEquals(minY, img.getHeight() - 1 - maxY, name + " vertical centering");
        }
    }
}
