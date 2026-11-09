package xyz.kajitsy.thaumaturgetinker.enchanter;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Machine-independent server process; the block supplies the verified structure state. */
public final class EnchanterProcess {
    public final EnchanterSelection selection = new EnchanterSelection();
    private final Map<ResourceKey<IAspect>, Integer> current = new LinkedHashMap<>();
    private boolean working;
    public int lastDrainIndex;

    public boolean working() {
        return working;
    }

    public int collected(ResourceKey<IAspect> aspect) {
        return current.getOrDefault(aspect, 0);
    }

    public boolean start() {
        if (working || selection.entries().isEmpty()) return false;
        working = true;
        return true;
    }

    public boolean change(ServerLevel level, ItemStack tool, Set<String> research, String id, int rank) {
        return !working && selection.change(level.registryAccess(), tool, research, id, rank);
    }

    /** Returns true whenever persistent state or inventory was changed. */
    public boolean tick(ServerLevel level, ItemStack tool, ItemStack wand, boolean structureValid) {
        if (tool.isEmpty()) {
            boolean changed = working || !selection.entries().isEmpty();
            working = false;
            selection.clear();
            return changed;
        }
        if (!working) return false;
        if (!structureValid) {
            working = false;
            current.clear();
            return true;
        }
        var total = selection.total();
        var missing = new ArrayList<ResourceKey<IAspect>>();
        for (var aspect : TTAspects.PRIMALS) {
            if (collected(aspect) < total.getOrDefault(aspect, 0)) {
                missing.add(aspect);
            }
        }
        if (missing.isEmpty()) {
            var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            for (var id : selection.entries().keySet()) {
                if (registry.get(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(id))).isEmpty()) {
                    working = false;
                    return true;
                }
            }
            selection.entries().forEach((id, rank) -> tool.enchant(registry.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(id))), rank));
            working = false;
            current.clear();
            selection.clear();
            return true;
        }
        if (!(wand.getItem() instanceof ItemWand itemWand) || itemWand.usesStaffRod(wand)) {
            return false;
        }
        missing.removeIf(aspect -> WandVisHelper.storedIn(wand, aspect) < 100);
        if (missing.isEmpty()) return false;
        lastDrainIndex = level.getRandom().nextInt(missing.size());
        var aspect = missing.get(lastDrainIndex);
        if (!WandVisHelper.drainOne(wand, null, aspect, 100, false)) return false;
        current.merge(aspect, 1, Integer::sum);
        return true;
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putBoolean("Working", working);
        var curTag = new CompoundTag();
        current.forEach((k, v) -> curTag.putInt(k.location().toString(), v));
        tag.put("Current", curTag);
        var selTag = new CompoundTag();
        selection.entries().forEach(selTag::putInt);
        tag.put("Selection", selTag);
        return tag;
    }

    public void load(ServerLevel level, ItemStack tool, CompoundTag tag) {
        working = tag.getBoolean("Working");
        current.clear();
        var curTag = tag.getCompound("Current");
        for (var key : curTag.keySet()) {
            var rk = ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.parse(key));
            current.put(rk, curTag.getInt(key));
        }
        selection.clear();
        var selTag = tag.getCompound("Selection");
        for (var key : selTag.keySet()) {
            selection.change(level.registryAccess(), tool, EnchanterCosts.allResearch(), key, selTag.getInt(key));
        }
    }
}
