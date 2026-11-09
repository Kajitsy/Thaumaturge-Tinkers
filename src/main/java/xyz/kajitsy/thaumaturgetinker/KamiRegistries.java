package xyz.kajitsy.thaumaturgetinker;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.kajitsy.thaumaturgetinker.block.WarpGateBlock;
import xyz.kajitsy.thaumaturgetinker.focus.RecallFocus;
import xyz.kajitsy.thaumaturgetinker.focus.ShadowbeamFocus;
import xyz.kajitsy.thaumaturgetinker.focus.XPDrainFocus;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedAxe;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedBoots;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedChest;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedHelm;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedLegs;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedPick;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedShovel;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedSword;
import xyz.kajitsy.thaumaturgetinker.item.BlockTalisman;
import xyz.kajitsy.thaumaturgetinker.item.CatAmulet;
import xyz.kajitsy.thaumaturgetinker.item.IchorPouch;
import xyz.kajitsy.thaumaturgetinker.item.KamiArmor;
import xyz.kajitsy.thaumaturgetinker.item.KamiTools;
import xyz.kajitsy.thaumaturgetinker.item.KamiWands;
import xyz.kajitsy.thaumaturgetinker.item.PlacementMirror;
import xyz.kajitsy.thaumaturgetinker.item.Protoclay;
import xyz.kajitsy.thaumaturgetinker.item.SkyPearl;
import xyz.kajitsy.thaumaturgetinker.menu.IchorPouchMenu;
import xyz.kajitsy.thaumaturgetinker.menu.WarpGateMenu;
import xyz.kajitsy.thaumaturgetinker.world.BedrockPortal;

/** Separate deferred collections preserve original registration omission when KAMI is off. */
public final class KamiRegistries {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("thaumaturgetinker");
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("thaumaturgetinker");
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "thaumaturgetinker");
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "thaumaturgetinker");

    public static void register(IEventBus bus) {
        if (!KamiStartupConfig.enabled()) return;
        KamiWands.CAPS.register(bus);
        KamiWands.RODS.register(bus);
        List.of(
                WarpGateBlock.BLOCK, WarpGateMenu.TYPE, SkyPearl.ITEM, ShadowbeamFocus.ITEM,
                RecallFocus.ITEM, XPDrainFocus.ITEM, PlacementMirror.ITEM, BlockTalisman.ITEM,
                CatAmulet.ITEM, IchorPouch.ITEM, IchorPouchMenu.TYPE, BedrockPortal.BLOCK,
                KamiResources.ITEMS, KamiTools.PICK, AwakenedPick.ITEM, AwakenedShovel.ITEM,
                AwakenedAxe.ITEM, AwakenedSword.ITEM, Protoclay.ITEM, KamiArmor.ITEMS,
                AwakenedHelm.ITEM, AwakenedChest.ITEM, AwakenedLegs.ITEM, AwakenedBoots.ITEM
        );
        ITEMS.register(bus);
        BLOCKS.register(bus);
        ENTITIES.register(bus);
        MENUS.register(bus);
    }
}
