package xyz.kajitsy.thaumaturgetinker;
public final class TinkererConfig{
    public static final net.neoforged.neoforge.common.ModConfigSpec SPEC;
    public static final net.neoforged.neoforge.common.ModConfigSpec.BooleanValue ENABLE_FIRE,ENABLE_CAKE,ENABLE_FLIGHT,SHOW_PLACEMENT_MIRROR_BLOCKS;
    public static final net.neoforged.neoforge.common.ModConfigSpec.IntValue BEDROCK_DIMENSION_ID,ORE_DENSITY;
    public static final net.neoforged.neoforge.common.ModConfigSpec.ConfigValue<java.util.List<? extends String>> ORE_BLACKLIST;
    static{var builder=new net.neoforged.neoforge.common.ModConfigSpec.Builder();ENABLE_FIRE=builder.comment("Build 164: enable infused fire transformations and spread.").define("enableFire",true);ENABLE_CAKE=builder.comment("Build 164: Aer fire may transform source water into cake.").define("enableCake",true);ENABLE_FLIGHT=builder.comment("Build 164: allow awakened ichorcloth chestplate flight.").define("enableFlight",true);SHOW_PLACEMENT_MIRROR_BLOCKS=builder.comment("Display phantom preview blocks when holding the Worldshaper's Seeing Glass.").define("placementMirror.blocks.show",true);BEDROCK_DIMENSION_ID=builder.comment("Legacy Bedrock dimension id; 0 disables portal access. Modern key: thaumaturgetinker:bedrock.").defineInRange("bedrockDimensionID",-19,Integer.MIN_VALUE,Integer.MAX_VALUE);ORE_DENSITY=builder.comment("Build 164 vertical ore clusters per chunk (200 vein attempts per cluster).").defineInRange("bedrockOreDensity",1,0,Integer.MAX_VALUE);ORE_BLACKLIST=builder.comment("Original OreDictionary names excluded from Bedrock generation.").defineListAllowEmpty("bedrockOreBlacklist",java.util.List.of("oreFirestone"),()->"oreFirestone",v->v instanceof String);SPEC=builder.build();}
    public static boolean bedrockEnabled(){return KamiStartupConfig.enabled()&&(!SPEC.isLoaded()||BEDROCK_DIMENSION_ID.get()!=0);}
    public static int oreDensity(){return SPEC.isLoaded()?ORE_DENSITY.get():1;}
    public static java.util.Set<String> oreBlacklist(){return SPEC.isLoaded()?java.util.Set.copyOf(ORE_BLACKLIST.get()):java.util.Set.of("oreFirestone");}
    public static boolean flightEnabled(){return !SPEC.isLoaded()||ENABLE_FLIGHT.get();}
    public static boolean fireEnabled(){return !SPEC.isLoaded()||ENABLE_FIRE.get();}
    public static boolean cakeEnabled(){return !SPEC.isLoaded()||ENABLE_CAKE.get();}
    public static boolean showPlacementMirrorBlocks(){return !SPEC.isLoaded()||SHOW_PLACEMENT_MIRROR_BLOCKS.get();}
}
