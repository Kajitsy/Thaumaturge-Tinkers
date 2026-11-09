package xyz.kajitsy.thaumaturgetinker.enchanter;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/** Server-side selection validation; absent optional enchantments cannot be selected. */
public final class EnchanterSelection {
    private final LinkedHashMap<String, Integer> selected = new LinkedHashMap<>();

    public Map<String, Integer> entries() {
        return Collections.unmodifiableMap(selected);
    }

    public boolean change(RegistryAccess access, ItemStack tool, Set<String> research, String id, int requested) {
        if (requested == -1) return selected.remove(id) != null;
        var data = EnchanterCosts.ENTRIES.get(id);
        var location = ResourceLocation.tryParse(id);
        if (id.equals("thaumaturgetinker:auto_smelt") && BuiltInRegistries.ITEM.getKey(tool.getItem()).toString().equals("thaumaturge:elemental_axe")) {
            return false;
        }
        if (data == null || location == null || tool.isEmpty()
            || !data.research().isEmpty() && !research.contains(data.research())) return false;
        var registry = access.lookupOrThrow(Registries.ENCHANTMENT);
        var holder = registry.get(ResourceKey.create(Registries.ENCHANTMENT, location)).orElse(null);
        if (holder == null || !holder.value().canEnchant(tool)) return false;
        var existing = tool.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        if (existing.getLevel(holder) >= holder.value().getMaxLevel()) return false;
        for (var e : existing.keySet()) {
            if (!Enchantment.areCompatible(holder, e)) return false;
        }
        int minLevel = existing.getLevel(holder) + 1;
        int maxLevel = holder.value().getMaxLevel();
        if (minLevel > maxLevel) return false;
        int level = selected.containsKey(id) ? Math.max(minLevel, Math.min(maxLevel, requested)) : minLevel;
        for (var other : selected.keySet()) {
            if (other.equals(id)) continue;
            var otherHolder = registry.get(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(other))).orElse(null);
            if (otherHolder == null || !Enchantment.areCompatible(holder, otherHolder)) return false;
        }
        selected.put(id, level);
        return true;
    }

    public Map<ResourceKey<IAspect>, Integer> total() {
        var result = new LinkedHashMap<ResourceKey<IAspect>, Integer>();
        selected.forEach((id, level) -> {
            var cost = EnchanterCosts.ENTRIES.get(id).cost(level);
            cost.forEach((aspect, amount) -> result.merge(aspect, amount, Integer::sum));
        });
        return result;
    }

    public void clear() {
        selected.clear();
    }
}
