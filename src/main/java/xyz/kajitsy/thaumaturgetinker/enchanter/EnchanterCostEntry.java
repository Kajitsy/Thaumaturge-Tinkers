package xyz.kajitsy.thaumaturgetinker.enchanter;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;
import java.util.LinkedHashMap;
import java.util.Map;

public record EnchanterCostEntry(String texture, boolean vanilla, String research, Map<ResourceKey<IAspect>, Integer> base) {
    public Map<ResourceKey<IAspect>, Integer> cost(int level) {
        if (level < 1 || level > 255) throw new IllegalArgumentException("Invalid enchantment level");
        double multiplier = level * (1.0 + level * 0.2);
        var result = new LinkedHashMap<ResourceKey<IAspect>, Integer>();
        base.forEach((aspect, amount) -> result.put(aspect, (int)(amount * multiplier)));
        return result;
    }
}
