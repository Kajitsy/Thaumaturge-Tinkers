package xyz.kajitsy.thaumaturgetinker.potion;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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

    public void write(CompoundTag tag) {
        ListTag list = new ListTag();
        for (var entry : aspects.entrySet()) {
            CompoundTag item = new CompoundTag();
            item.putString("key", entry.getKey().location().toString());
            item.putInt("amount", entry.getValue());
            list.add(item);
        }
        tag.put("aspects", list);
    }

    public void read(CompoundTag tag) {
        aspects.clear();
        ListTag list = tag.getList("aspects", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag item = list.getCompound(i);
            String keyStr = item.getString("key");
            if (keyStr.isEmpty()) continue;
            if (!keyStr.contains(":")) keyStr = "thaumaturge:" + keyStr;
            var aspect = ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.parse(keyStr));
            aspects.put(aspect, item.getInt("amount"));
        }
    }
}
