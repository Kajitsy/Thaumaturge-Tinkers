package xyz.kajitsy.thaumaturgetinker;

import xyz.kajitsy.thaumaturgetinker.item.InfusedInkwellItem;
import xyz.kajitsy.thaumaturgetinker.item.InfusedSeedsItem;
import xyz.kajitsy.thaumaturgetinker.item.KamiWands;
import xyz.kajitsy.thaumaturgetinker.item.LegacyCropEggItem;
import xyz.kajitsy.thaumaturgetinker.item.MobAspectItem;
import xyz.kajitsy.thaumaturgetinker.item.SoulMouldItem;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class TinkererCreativeTab {
    private static final List<String> ORDER = List.of(
        // Kami Awakened Tools
        "ichor_pick_gem", "ichor_shovel_gem", "ichor_axe_gem", "ichor_sword_gem",
        // Kami Regular Tools
        "ichor_pick", "ichor_shovel", "ichor_axe", "ichor_sword",
        // Other Tools & Weapons
        "blood_sword", "placement_mirror", "connector", "protoclay",
        // Kami Awakened Armor
        "ichorcloth_helm_gem", "ichorcloth_chest_gem", "ichorcloth_legs_gem", "ichorcloth_boots_gem",
        // Kami Regular Armor
        "ichorcloth_helm", "ichorcloth_chest", "ichorcloth_legs", "ichorcloth_boots",
        // Other Armor
        "revealing_helm",
        // Baubles & Accessories
        "cat_amulet", "cleansing_talisman", "xp_talisman", "block_talisman", "ichor_pouch",
        // Foci
        "focus_smelt", "focus_flight", "focus_heal", "focus_telekinesis", "focus_recall",
        "focus_dislocation", "focus_ender_chest", "focus_xp_drain", "focus_deflect", "focus_shadowbeam",
        // Kami Materials & Components
        "ichor", "ichorium", "ichor_nugget", "ichor_cloth", "ichor_cap", "ichorcloth_rod", "sky_pearl",
        // Items & Resources
        "bright_nitor", "gas_remover", "gaseous_light_bottle", "gaseous_shadow_bottle", "spell_cloth",
        "infused_seeds", "infused_grain",
        // Blocks & Devices
        "enchanter", "repairer", "funnel", "transvector_interface", "dislocator",
        "magnet", "mob_magnet", "platform", "animation_tablet", "summon_tablet",
        "locomotive", "locomotive_relay", "warp_gate", "forcefield",
        // Decorative Quartz Blocks
        "dark_quartz", "dark_quartz_block", "chiseled_dark_quartz", "dark_quartz_pillar",
        "dark_quartz_stairs", "dark_quartz_slab"
    );

    private static ItemStack wand(boolean kami) {
        var cap = kami ? KamiWands.CAP_ICHOR.get() : TTWandParts.CAP_THAUMIUM.get();
        var rod = kami ? KamiWands.ROD_ICHORCLOTH.get() : TTWandParts.ROD_SILVERWOOD.get();
        var result = ItemWand.create(TTItems.WAND.get(), cap, rod, false);
        WandVisHelper.fill(result);
        return result;
    }

    public static ItemStack icon() {
        return wand(KamiStartupConfig.enabled());
    }

    private static int orderOf(ItemStack stack) {
        var path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        int idx = ORDER.indexOf(path);
        return idx != -1 ? idx : ORDER.size();
    }

    public static List<ItemStack> contents() {
        var result = new ArrayList<ItemStack>();
        result.add(wand(false));
        if (KamiStartupConfig.enabled()) result.add(wand(true));
        var queue = new ArrayList<ItemStack>();
        ThaumicTinkererModern.itemEntries().forEach(entry -> {
            var item = entry.get();
            if (item instanceof InfusedSeedsItem) {
                for (var aspect : TTAspects.PRIMALS) queue.add(InfusedSeedsItem.stack(aspect));
            } else if (item instanceof SoulMouldItem) {
                /* Excluded from creative tab per user request */
            } else if (item instanceof InfusedInkwellItem) {
                /* ItemInfusedInkwell.shouldDisplayInTab false */
            } else if (!(item instanceof LegacyCropEggItem) && !(item instanceof MobAspectItem aspect && aspect.form == 1)) {
                queue.add(new ItemStack(item));
            }
        });
        queue.sort(Comparator.comparingInt(TinkererCreativeTab::orderOf));
        result.addAll(queue);
        return result;
    }
}
