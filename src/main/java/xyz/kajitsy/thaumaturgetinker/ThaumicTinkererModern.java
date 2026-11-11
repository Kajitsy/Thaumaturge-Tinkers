package xyz.kajitsy.thaumaturgetinker;

import xyz.kajitsy.thaumaturgetinker.block.AnimationTabletBlock;
import xyz.kajitsy.thaumaturgetinker.block.DislocatorBlock;
import xyz.kajitsy.thaumaturgetinker.block.EnchanterBlock;
import xyz.kajitsy.thaumaturgetinker.block.ForcefieldBlock;
import xyz.kajitsy.thaumaturgetinker.block.FunnelBlock;
import xyz.kajitsy.thaumaturgetinker.block.GasBlock;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFarmlandBlock;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireAttachment;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireBlock;
import xyz.kajitsy.thaumaturgetinker.block.InfusedFireKind;
import xyz.kajitsy.thaumaturgetinker.block.InfusedGrainBlock;
import xyz.kajitsy.thaumaturgetinker.block.LocomotiveBlock;
import xyz.kajitsy.thaumaturgetinker.block.MagnetBlock;
import xyz.kajitsy.thaumaturgetinker.block.NitorLightBlock;
import xyz.kajitsy.thaumaturgetinker.block.OrderFireResolver;
import xyz.kajitsy.thaumaturgetinker.block.PlatformBlock;
import xyz.kajitsy.thaumaturgetinker.block.RepairerBlock;
import xyz.kajitsy.thaumaturgetinker.block.SummonBlock;
import xyz.kajitsy.thaumaturgetinker.block.TransvectorBlock;
import xyz.kajitsy.thaumaturgetinker.block.entity.AnimationTabletEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.DislocatorEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.EnchanterEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.ForcefieldEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.FunnelEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.InfusedFarmlandEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.InfusedGrainEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.LocomotiveEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.LocomotiveRelayEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.MagnetEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.PlatformEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.RepairerEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.SummonEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.TransvectorEntity;
import xyz.kajitsy.thaumaturgetinker.compat.TTCuriosCompat;
import xyz.kajitsy.thaumaturgetinker.focus.DeflectFocus;
import xyz.kajitsy.thaumaturgetinker.focus.DislocationFocus;
import xyz.kajitsy.thaumaturgetinker.focus.EnderChestFocus;
import xyz.kajitsy.thaumaturgetinker.focus.FlightFocus;
import xyz.kajitsy.thaumaturgetinker.focus.HealFocus;
import xyz.kajitsy.thaumaturgetinker.focus.SmeltFocus;
import xyz.kajitsy.thaumaturgetinker.focus.TelekinesisFocus;
import xyz.kajitsy.thaumaturgetinker.item.BloodSwordItem;
import xyz.kajitsy.thaumaturgetinker.item.BrightNitorItem;
import xyz.kajitsy.thaumaturgetinker.item.CleansingTalismanItem;
import xyz.kajitsy.thaumaturgetinker.item.ConnectorItem;
import xyz.kajitsy.thaumaturgetinker.item.GasItem;
import xyz.kajitsy.thaumaturgetinker.item.GasRemoverItem;
import xyz.kajitsy.thaumaturgetinker.item.InfusedGrainItem;
import xyz.kajitsy.thaumaturgetinker.item.InfusedInkwellItem;
import xyz.kajitsy.thaumaturgetinker.item.InfusedSeedsItem;
import xyz.kajitsy.thaumaturgetinker.item.LegacyCropEggItem;
import xyz.kajitsy.thaumaturgetinker.item.MobAspects;
import xyz.kajitsy.thaumaturgetinker.item.RevealingHelmItem;
import xyz.kajitsy.thaumaturgetinker.item.SoulMouldItem;
import xyz.kajitsy.thaumaturgetinker.item.XPTalismanItem;
import xyz.kajitsy.thaumaturgetinker.menu.AnimationTabletMenu;
import xyz.kajitsy.thaumaturgetinker.menu.EnchanterMenu;
import xyz.kajitsy.thaumaturgetinker.menu.MagnetMenu;
import xyz.kajitsy.thaumaturgetinker.potion.InfusedCropLoot;
import xyz.kajitsy.thaumaturgetinker.potion.InfusedPotions;
import xyz.kajitsy.thaumaturgetinker.recipe.SpellClothRecipe;
import xyz.kajitsy.thaumaturgetinker.world.BedrockDimensionCondition;


import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Build-164 content, adapted to NeoForge 26.1.2 and Thaumaturge. Original authors: kajitsy. */
@Mod(ThaumicTinkererModern.ID)
public final class ThaumicTinkererModern {
    public static final String ID = "thaumaturgetinker";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);

    public static net.minecraft.resources.ResourceKey<Block> blockKey(String name) {
        return net.minecraft.resources.ResourceKey.create(Registries.BLOCK, net.minecraft.resources.Identifier.fromNamespaceAndPath(ID, name));
    }

    public static net.minecraft.resources.ResourceKey<Item> itemKey(String name) {
        return net.minecraft.resources.ResourceKey.create(Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath(ID, name));
    }

    public static BlockBehaviour.Properties blockProps(String name, BlockBehaviour.Properties properties) {
        return properties.setId(blockKey(name));
    }

    public static Item.Properties itemProps(String name, Item.Properties properties) {
        return properties.setId(itemKey(name));
    }

    public static Item.Properties itemProps(String name) {
        return itemProps(name, new Item.Properties());
    }

    public static final DeferredItem<LegacyCropEggItem> LEGACY_CROP_EGG = ITEMS.register("legacy_crop_egg", LegacyCropEggItem::new);
    public static final DeferredItem<InfusedGrainItem> INFUSED_GRAIN_AIR = ITEMS.register("infused_grain_air", () -> new InfusedGrainItem("infused_grain_air", TTAspects.AER));
    public static final DeferredItem<InfusedGrainItem> INFUSED_GRAIN_FIRE = ITEMS.register("infused_grain_fire", () -> new InfusedGrainItem("infused_grain_fire", TTAspects.IGNIS));
    public static final DeferredItem<InfusedGrainItem> INFUSED_GRAIN_EARTH = ITEMS.register("infused_grain_earth", () -> new InfusedGrainItem("infused_grain_earth", TTAspects.TERRA));
    public static final DeferredItem<InfusedGrainItem> INFUSED_GRAIN_WATER = ITEMS.register("infused_grain_water", () -> new InfusedGrainItem("infused_grain_water", TTAspects.AQUA));
    public static final DeferredItem<InfusedSeedsItem> INFUSED_SEEDS = ITEMS.register("infused_seeds", InfusedSeedsItem::new);

    public static final DeferredBlock<ForcefieldBlock> FORCEFIELD = BLOCKS.register("forcefield", ForcefieldBlock::new);
    public static final DeferredBlock<InfusedGrainBlock> INFUSED_GRAIN = BLOCKS.register("infused_grain", InfusedGrainBlock::new);
    public static final DeferredBlock<InfusedFarmlandBlock> INFUSED_FARMLAND = BLOCKS.register("infused_farmland", InfusedFarmlandBlock::new);
    public static final DeferredBlock<PlatformBlock> PLATFORM = BLOCKS.register("platform", PlatformBlock::new);
    public static final DeferredBlock<AnimationTabletBlock> ANIMATION_TABLET = BLOCKS.register("animation_tablet", AnimationTabletBlock::new);
    public static final DeferredBlock<LocomotiveBlock> LOCOMOTIVE = BLOCKS.register("locomotive", () -> new LocomotiveBlock(false));
    public static final DeferredBlock<LocomotiveBlock> LOCOMOTIVE_RELAY = BLOCKS.register("locomotive_relay", () -> new LocomotiveBlock(true));
    public static final DeferredBlock<SummonBlock> SUMMON = BLOCKS.register("summon_tablet", SummonBlock::new);
    public static final DeferredItem<RevealingHelmItem> REVEALING_HELM = ITEMS.register("revealing_helm", RevealingHelmItem::new);
    public static final DeferredItem<BloodSwordItem> BLOOD_SWORD = ITEMS.register("blood_sword", BloodSwordItem::new);
    public static final DeferredBlock<DislocatorBlock> DISLOCATOR = BLOCKS.register("dislocator", DislocatorBlock::new);
    public static final DeferredBlock<MagnetBlock> MAGNET = BLOCKS.register("magnet", () -> new MagnetBlock(false));
    public static final DeferredBlock<MagnetBlock> MOB_MAGNET = BLOCKS.register("mob_magnet", () -> new MagnetBlock(true));
    public static final DeferredItem<SoulMouldItem> SOUL_MOULD = ITEMS.register("soul_mould", SoulMouldItem::new);

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ID);
    public static final Supplier<MenuType<EnchanterMenu>> ENCHANTER_MENU = MENUS.register("enchanter",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new EnchanterMenu(id, inv, (EnchanterEntity) inv.player.level().getBlockEntity(buf.readBlockPos()))));
    public static final Supplier<MenuType<AnimationTabletMenu>> ANIMATION_TABLET_MENU = MENUS.register("animation_tablet",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new AnimationTabletMenu(id, inv, (AnimationTabletEntity) inv.player.level().getBlockEntity(buf.readBlockPos()))));
    public static final Supplier<MenuType<MagnetMenu>> MAGNET_MENU = MENUS.register("mob_magnet",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new MagnetMenu(id, inv, (MagnetEntity) inv.player.level().getBlockEntity(buf.readBlockPos()))));

    public static final DeferredItem<SmeltFocus> FOCUS_SMELT = ITEMS.register("focus_smelt", SmeltFocus::new);
    public static final DeferredItem<FlightFocus> FOCUS_FLIGHT = ITEMS.register("focus_flight", FlightFocus::new);
    public static final DeferredItem<DislocationFocus> FOCUS_DISLOCATION = ITEMS.register("focus_dislocation", DislocationFocus::new);
    public static final DeferredItem<EnderChestFocus> FOCUS_ENDER_CHEST = ITEMS.register("focus_ender_chest", EnderChestFocus::new);
    public static final DeferredItem<HealFocus> FOCUS_HEAL = ITEMS.register("focus_heal", HealFocus::new);
    public static final DeferredItem<DeflectFocus> FOCUS_DEFLECT = ITEMS.register("focus_deflect", DeflectFocus::new);
    public static final DeferredItem<TelekinesisFocus> FOCUS_TELEKINESIS = ITEMS.register("focus_telekinesis", TelekinesisFocus::new);

    public static final DeferredBlock<EnchanterBlock> ENCHANTER = BLOCKS.register("enchanter", EnchanterBlock::new);
    public static final DeferredBlock<RepairerBlock> REPAIRER = BLOCKS.register("repairer", RepairerBlock::new);
    public static final DeferredBlock<FunnelBlock> FUNNEL = BLOCKS.register("funnel", FunnelBlock::new);
    public static final DeferredBlock<TransvectorBlock> INTERFACE = BLOCKS.register("transvector_interface", TransvectorBlock::new);
    public static final DeferredItem<ConnectorItem> CONNECTOR = ITEMS.register("connector", ConnectorItem::new);

    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final Supplier<BlockEntityType<ForcefieldEntity>> FORCEFIELD_ENTITY = ENTITIES.register("forcefield",
            () -> new BlockEntityType<>(ForcefieldEntity::new, FORCEFIELD.get()));
    public static final Supplier<BlockEntityType<InfusedGrainEntity>> INFUSED_GRAIN_ENTITY = ENTITIES.register("infused_grain",
            () -> new BlockEntityType<>(InfusedGrainEntity::new, INFUSED_GRAIN.get()));
    public static final Supplier<BlockEntityType<InfusedFarmlandEntity>> INFUSED_FARMLAND_ENTITY = ENTITIES.register("infused_farmland",
            () -> new BlockEntityType<>(InfusedFarmlandEntity::new, INFUSED_FARMLAND.get()));
    public static final Supplier<BlockEntityType<PlatformEntity>> PLATFORM_ENTITY = ENTITIES.register("platform",
            () -> new BlockEntityType<>(PlatformEntity::new, PLATFORM.get()));
    public static final Supplier<BlockEntityType<AnimationTabletEntity>> ANIMATION_TABLET_ENTITY = ENTITIES.register("animation_tablet",
            () -> new BlockEntityType<>(AnimationTabletEntity::new, ANIMATION_TABLET.get()));
    public static final Supplier<BlockEntityType<LocomotiveEntity>> LOCOMOTIVE_ENTITY = ENTITIES.register("locomotive",
            () -> new BlockEntityType<>(LocomotiveEntity::new, LOCOMOTIVE.get()));
    public static final Supplier<BlockEntityType<LocomotiveRelayEntity>> LOCOMOTIVE_RELAY_ENTITY = ENTITIES.register("locomotive_relay",
            () -> new BlockEntityType<>(LocomotiveRelayEntity::new, LOCOMOTIVE_RELAY.get()));
    public static final Supplier<BlockEntityType<SummonEntity>> SUMMON_ENTITY = ENTITIES.register("summon_tablet",
            () -> new BlockEntityType<>(SummonEntity::new, SUMMON.get()));
    public static final Supplier<BlockEntityType<DislocatorEntity>> DISLOCATOR_ENTITY = ENTITIES.register("dislocator",
            () -> new BlockEntityType<>(DislocatorEntity::new, DISLOCATOR.get()));
    public static final Supplier<BlockEntityType<MagnetEntity>> MAGNET_ENTITY = ENTITIES.register("magnet",
            () -> new BlockEntityType<>(MagnetEntity::new, MAGNET.get(), MOB_MAGNET.get()));
    public static final Supplier<BlockEntityType<EnchanterEntity>> ENCHANTER_ENTITY = ENTITIES.register("enchanter",
            () -> new BlockEntityType<>(EnchanterEntity::new, ENCHANTER.get()));
    public static final Supplier<BlockEntityType<RepairerEntity>> REPAIRER_ENTITY = ENTITIES.register("repairer",
            () -> new BlockEntityType<>(RepairerEntity::new, REPAIRER.get()));
    public static final Supplier<BlockEntityType<FunnelEntity>> FUNNEL_ENTITY = ENTITIES.register("funnel",
            () -> new BlockEntityType<>(FunnelEntity::new, FUNNEL.get()));
    public static final Supplier<BlockEntityType<TransvectorEntity>> TRANSVECTOR_ENTITY = ENTITIES.register("transvector_interface",
            () -> new BlockEntityType<>(TransvectorEntity::new, INTERFACE.get()));

    public static final DeferredItem<Item> DARK_QUARTZ = ITEMS.registerSimpleItem("dark_quartz");
    public static final DeferredBlock<GasBlock> LIGHT_GAS = BLOCKS.register("gaseous_light", () -> new GasBlock(false));
    public static final DeferredBlock<GasBlock> SHADOW_GAS = BLOCKS.register("gaseous_shadow", () -> new GasBlock(true));
    public static final DeferredItem<GasItem> LIGHT_BOTTLE = ITEMS.register("gaseous_light_bottle", () -> new GasItem("gaseous_light_bottle", LIGHT_GAS));
    public static final DeferredItem<GasItem> SHADOW_BOTTLE = ITEMS.register("gaseous_shadow_bottle", () -> new GasItem("gaseous_shadow_bottle", SHADOW_GAS));
    public static final DeferredBlock<NitorLightBlock> NITOR_LIGHT = BLOCKS.register("nitor_light", NitorLightBlock::new);
    public static final DeferredItem<BrightNitorItem> BRIGHT_NITOR = ITEMS.register("bright_nitor", BrightNitorItem::new);
    public static final DeferredItem<GasRemoverItem> GAS_REMOVER = ITEMS.register("gas_remover", GasRemoverItem::new);
    public static final DeferredItem<XPTalismanItem> XP_TALISMAN = ITEMS.register("xp_talisman", XPTalismanItem::new);
    public static final DeferredItem<CleansingTalismanItem> CLEANSING_TALISMAN = ITEMS.register("cleansing_talisman", CleansingTalismanItem::new);
    public static final DeferredItem<InfusedInkwellItem> INFUSED_INKWELL = ITEMS.register("infused_inkwell", InfusedInkwellItem::new);
    public static final DeferredItem<Item> SPELL_CLOTH = ITEMS.register("spell_cloth", () -> new Item(itemProps("spell_cloth", new Item.Properties().durability(35).rarity(Rarity.UNCOMMON))));

    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ID);
    public static final Supplier<net.minecraft.world.item.crafting.RecipeSerializer<SpellClothRecipe>> SPELL_CLOTH_SERIALIZER = SERIALIZERS.register("spell_cloth",
            () -> SpellClothRecipe.SERIALIZER);

    private static BlockBehaviour.Properties quartzProperties(String name) {
        return blockProps(name, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK).strength(.8f, 10f).requiresCorrectToolForDrops());
    }

    public static final DeferredBlock<Block> QUARTZ = BLOCKS.register("dark_quartz_block", () -> new Block(quartzProperties("dark_quartz_block")));
    public static final DeferredBlock<Block> CHISELED = BLOCKS.register("chiseled_dark_quartz", () -> new Block(quartzProperties("chiseled_dark_quartz")));
    public static final DeferredBlock<RotatedPillarBlock> PILLAR = BLOCKS.register("dark_quartz_pillar", () -> new RotatedPillarBlock(quartzProperties("dark_quartz_pillar")));
    public static final DeferredBlock<StairBlock> STAIRS = BLOCKS.register("dark_quartz_stairs", () -> new StairBlock(QUARTZ.get().defaultBlockState(), quartzProperties("dark_quartz_stairs")));
    public static final DeferredBlock<SlabBlock> SLAB = BLOCKS.register("dark_quartz_slab", () -> new SlabBlock(quartzProperties("dark_quartz_slab")));

    public static final java.util.Map<InfusedFireKind, DeferredBlock<InfusedFireBlock>> FIRES = new java.util.EnumMap<>(InfusedFireKind.class);

    static {
        for (var kind : InfusedFireKind.values()) {
            var fire = BLOCKS.register("fire_" + kind.name().toLowerCase(java.util.Locale.ROOT), () -> new InfusedFireBlock(kind));
            FIRES.put(kind, fire);
            ITEMS.registerSimpleBlockItem(fire);
        }
        ITEMS.registerSimpleBlockItem(PLATFORM);
        ITEMS.registerSimpleBlockItem(LOCOMOTIVE);
        ITEMS.registerSimpleBlockItem(LOCOMOTIVE_RELAY);
        ITEMS.registerSimpleBlockItem(ANIMATION_TABLET);
        MobAspects.register();
        ITEMS.registerSimpleBlockItem(SUMMON);
        ITEMS.registerSimpleBlockItem(DISLOCATOR);
        ITEMS.registerSimpleBlockItem(MAGNET);
        ITEMS.registerSimpleBlockItem(MOB_MAGNET);
        ITEMS.registerSimpleBlockItem(REPAIRER);
        ITEMS.registerSimpleBlockItem(ENCHANTER);
        ITEMS.registerSimpleBlockItem(FUNNEL);
        ITEMS.registerSimpleBlockItem(INTERFACE);
        ITEMS.registerSimpleBlockItem(QUARTZ);
        ITEMS.registerSimpleBlockItem(CHISELED);
        ITEMS.registerSimpleBlockItem(PILLAR);
        ITEMS.registerSimpleBlockItem(STAIRS);
        ITEMS.registerSimpleBlockItem(SLAB);

        TABS.register("thaumic_tinkerer", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.thaumaturgetinker"))
                .icon(TinkererCreativeTab::icon)
                .displayItems((p, out) -> TinkererCreativeTab.contents().forEach(out::accept))
                .build());
    }

    public ThaumicTinkererModern(IEventBus bus, ModContainer container) {
        KamiStartupConfig.register(container);
        container.registerConfig(ModConfig.Type.COMMON, TinkererConfig.SPEC);
        TTCuriosCompat.init(bus);
        BedrockDimensionCondition.init(bus);
        InfusedPotions.register(bus);
        InfusedFireAttachment.register(bus);
        KamiRegistries.register(bus);
        MENUS.register(bus);
        ENTITIES.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TABS.register(bus);
        SERIALIZERS.register(bus);
        OrderFireResolver.register();
        InfusedCropLoot.register();

        bus.addListener((net.neoforged.neoforge.data.event.GatherDataEvent.Client e) -> {
            var builder = new net.minecraft.core.RegistrySetBuilder()
                .add(com.leclowndu93150.thaumaturge.api.aspect.IAspect.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectBootstrap::bootstrap)
                .add(com.leclowndu93150.thaumaturge.api.research.IResearchCategory.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.worldgen.research.CategoryBootstrap::bootstrap)
                .add(com.leclowndu93150.thaumaturge.api.research.scan.ScanEntry.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.worldgen.scan.ScanEntryBootstrap::bootstrap)
                .add(com.leclowndu93150.thaumaturge.content.pech.PechTradeTable.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.worldgen.pech.PechTradeBootstrap::bootstrap)
                .add(com.leclowndu93150.thaumaturge.api.recipe.Blueprint.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.worldgen.blueprint.BlueprintBootstrap::bootstrap)
                .add(net.minecraft.core.registries.Registries.DAMAGE_TYPE, com.leclowndu93150.thaumaturge.data.damagetype.TTDamageTypeBootstrap::bootstrap)
                .add(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE, com.leclowndu93150.thaumaturge.data.worldgen.feature.TTConfiguredFeatures::bootstrap)
                .add(net.minecraft.core.registries.Registries.PLACED_FEATURE, com.leclowndu93150.thaumaturge.data.worldgen.feature.TTPlacedFeatures::bootstrap)
                .add(net.minecraft.core.registries.Registries.BIOME, com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes::bootstrap)
                .add(net.minecraft.core.registries.Registries.DIMENSION_TYPE, com.leclowndu93150.thaumaturge.data.worldgen.dimension.OuterLandsBootstrap::bootstrapTypes)
                .add(net.minecraft.core.registries.Registries.LEVEL_STEM, com.leclowndu93150.thaumaturge.data.worldgen.dimension.OuterLandsBootstrap::bootstrapStems)
                .add(net.minecraft.core.registries.Registries.STRUCTURE, com.leclowndu93150.thaumaturge.data.worldgen.feature.TTStructureBootstrap::bootstrapStructures)
                .add(net.minecraft.core.registries.Registries.STRUCTURE_SET, com.leclowndu93150.thaumaturge.data.worldgen.feature.TTStructureBootstrap::bootstrapSets)
                .add(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.BIOME_MODIFIERS, com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomeModifiers::bootstrap)
                .add(com.leclowndu93150.thaumaturge.api.spell.part.SpellPart.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.spell.SpellPartBootstrap::bootstrap)
                .add(com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.spell.AffinityBootstrap::bootstrap)
                .add(net.minecraft.core.registries.Registries.PROCESSOR_LIST, com.leclowndu93150.thaumaturge.data.labyrinth.LabyrinthProcessorBootstrap::bootstrap)
                .add(com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.labyrinth.LabyrinthRoomBootstrap::bootstrap)
                .add(com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.labyrinth.LabyrinthEncounterBootstrap::bootstrap)
                .add(com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.labyrinth.LabyrinthDefinitionBootstrap::bootstrap)
                .add(com.leclowndu93150.thaumaturge.content.eldritch.site.ObeliskSite.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.labyrinth.ObeliskSiteBootstrap::bootstrap)
                .add(com.leclowndu93150.thaumaturge.content.infusion.instability.InstabilityOutcome.REGISTRY_KEY, com.leclowndu93150.thaumaturge.data.infusion.InstabilityOutcomeBootstrap::bootstrap)
                .add(net.minecraft.core.registries.Registries.TRIM_MATERIAL, com.leclowndu93150.thaumaturge.data.trim.TTTrimBootstrap::materials)
                .add(net.minecraft.core.registries.Registries.TRIM_PATTERN, com.leclowndu93150.thaumaturge.data.trim.TTTrimBootstrap::patterns)
                .add(net.minecraft.core.registries.Registries.VILLAGER_TRADE, com.leclowndu93150.thaumaturge.data.trade.TTVillagerTradeBootstrap::bootstrap);
            e.createDatapackRegistryObjects(builder, java.util.Set.of("thaumaturge", "thaumaturgetinker"));
            e.createBlockAndItemTags(com.leclowndu93150.thaumaturge.data.tag.TTBlockTagsProvider::new, com.leclowndu93150.thaumaturge.data.tag.TTItemTagsProvider::new);
            e.createProvider(com.leclowndu93150.thaumaturge.data.tag.TTDamageTypeTagsProvider::new);
            e.createProvider(com.leclowndu93150.thaumaturge.data.tag.TTStructureTagsProvider::new);
            e.createProvider(com.leclowndu93150.thaumaturge.data.tag.TTBiomeTagsProvider::new);
            e.createProvider(com.leclowndu93150.thaumaturge.data.tag.TTMobEffectTagsProvider::new);
            e.createProvider(com.leclowndu93150.thaumaturge.data.tag.TTFluidTagsProvider::new);
            e.createProvider(com.leclowndu93150.thaumaturge.data.tag.TTEntityTypeTagsProvider::new);
            e.createProvider(com.leclowndu93150.thaumaturge.data.labyrinth.TTLabyrinthRoomTagsProvider::new);
            e.createProvider(com.leclowndu93150.thaumaturge.data.tag.TTVillagerTradeTagsProvider::new);
        });

        bus.addListener((RegisterCapabilitiesEvent e) -> {
            e.registerBlockEntity(Capabilities.Item.BLOCK, ANIMATION_TABLET_ENTITY.get(), (tile, side) -> VanillaContainerWrapper.of(tile.inventory));
            e.registerBlockEntity(Capabilities.Item.BLOCK, REPAIRER_ENTITY.get(), (tile, side) -> VanillaContainerWrapper.of(tile.inventory));
            e.registerBlockEntity(EssentiaCapabilities.TRANSPORT, REPAIRER_ENTITY.get(), (tile, side) -> tile);
            e.registerBlockEntity(Capabilities.Item.BLOCK, FUNNEL_ENTITY.get(), (tile, side) -> side == Direction.DOWN ? null : VanillaContainerWrapper.of(tile.inventory));
            e.registerBlockEntity(Capabilities.Item.BLOCK, TRANSVECTOR_ENTITY.get(), (tile, side) -> tile.forward(Capabilities.Item.BLOCK, side));
            e.registerBlockEntity(Capabilities.Fluid.BLOCK, TRANSVECTOR_ENTITY.get(), (tile, side) -> tile.forward(Capabilities.Fluid.BLOCK, side));
            e.registerBlockEntity(Capabilities.Energy.BLOCK, TRANSVECTOR_ENTITY.get(), (tile, side) -> tile.forward(Capabilities.Energy.BLOCK, side));
            e.registerBlockEntity(AspectCapabilities.CONTAINER, TRANSVECTOR_ENTITY.get(), (tile, side) -> tile.forward(AspectCapabilities.CONTAINER, side));
            e.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TRANSVECTOR_ENTITY.get(), (tile, side) -> tile.forward(EssentiaCapabilities.TRANSPORT, side));
            e.registerBlockEntity(EssentiaCapabilities.STORAGE, TRANSVECTOR_ENTITY.get(), (tile, side) -> tile.forward(EssentiaCapabilities.STORAGE, side));
            e.registerBlockEntity(EssentiaCapabilities.ASPECT_QUERY, TRANSVECTOR_ENTITY.get(), (tile, side) -> tile.forward(EssentiaCapabilities.ASPECT_QUERY, side));
        });
    }

    public static Stream<DeferredHolder<Item, ? extends Item>> itemEntries() {
        return Stream.concat(
                ITEMS.getEntries().stream(),
                KamiStartupConfig.enabled() ? KamiRegistries.ITEMS.getEntries().stream() : Stream.empty()
        );
    }

    public static Stream<DeferredHolder<Block, ? extends Block>> blockEntries() {
        return Stream.concat(
                BLOCKS.getEntries().stream(),
                KamiStartupConfig.enabled() ? KamiRegistries.BLOCKS.getEntries().stream() : Stream.empty()
        );
    }
}
