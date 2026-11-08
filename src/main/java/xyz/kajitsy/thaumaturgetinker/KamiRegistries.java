package xyz.kajitsy.thaumaturgetinker;
/** Separate deferred collections preserve original registration omission when KAMI is off. */
public final class KamiRegistries {
 public static final net.neoforged.neoforge.registries.DeferredRegister.Items ITEMS=net.neoforged.neoforge.registries.DeferredRegister.createItems("thaumaturgetinker");
 public static final net.neoforged.neoforge.registries.DeferredRegister.Blocks BLOCKS=net.neoforged.neoforge.registries.DeferredRegister.createBlocks("thaumaturgetinker");
 public static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> ENTITIES=net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE,"thaumaturgetinker");
 public static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS=net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.MENU,"thaumaturgetinker");
 public static void register(net.neoforged.bus.api.IEventBus bus){if(KamiStartupConfig.enabled()){ITEMS.register(bus);BLOCKS.register(bus);ENTITIES.register(bus);MENUS.register(bus);}}
}
