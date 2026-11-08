package xyz.kajitsy.thaumaturgetinker.potion;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;

/** Build-164 farmland reservoir; separate from the seed's primal-only tendencies. */
public final class InfusedSoilData {
    public static final int MAX_ASPECTS = 20;
    private final Map<ResourceKey<IAspect>, Integer> aspects = new LinkedHashMap<>();

    public Map<ResourceKey<IAspect>, Integer> aspects() {
        return Map.copyOf(aspects);
    }

    public void fertilize(ResourceKey<IAspect> aspect, RandomSource random) {
        if (aspect == null) return;
        aspects.put(aspect, aspects.getOrDefault(aspect, 0) + 1);
        InfusedCropProcess.saturate(aspects, MAX_ASPECTS, random);
    }

    public void write(net.minecraft.world.level.storage.ValueOutput output) {
        var list = output.childrenList("aspects");
        for (var entry : aspects.entrySet()) {
            var item = list.addChild();
            item.putString("key", entry.getKey().identifier().getPath());
            item.putInt("amount", entry.getValue());
        }
    }

    public void read(net.minecraft.world.level.storage.ValueInput input) {
        aspects.clear();
        var list = input.childrenListOrEmpty("aspects");
        for (var item : list) {
            String keyStr = item.getStringOr("key", "");
            if (keyStr.isEmpty()) continue;
            if (!keyStr.contains(":")) keyStr = "thaumaturge:" + keyStr;
            var aspect = ResourceKey.create(IAspect.REGISTRY_KEY, Identifier.parse(keyStr));
            aspects.put(aspect, item.getIntOr("amount", 1));
        }
    }
}
