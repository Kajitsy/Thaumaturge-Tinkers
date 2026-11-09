package xyz.kajitsy.thaumaturgetinker.potion;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.aspect.AspectCombinations;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;

/** Build-164 growth and tendency exchange; world mutation is performed by the crop entity. */
public final class InfusedCropProcess {
    public record Transfer(int neighbor, InfusedSeedData recipient, InfusedSeedData donor, ResourceKey<IAspect> aspect) {}

    public static int grow(InfusedSeedData data, int age, int light, RandomSource random) {
        return light >= 9 && age < 7 && random.nextInt(data.growthBound()) == 0 ? age + 1 : age;
    }

    public static Transfer transfer(InfusedSeedData data, List<InfusedSeedData> neighbors, RandomSource random) {
        if (random.nextInt(data.transferBound()) != 0 || data.mainAspect() == null || TTAspects.PRIMALS.contains(data.mainAspect())) {
            return null;
        }
        for (int i = 0; i < neighbors.size(); i++) {
            var neighbor = neighbors.get(i);
            if (neighbor == null || neighbor.mainAspect() == null) continue;
            var own = new LinkedHashMap<>(data.tendencies());
            var other = new LinkedHashMap<>(neighbor.tendencies());
            var aspect = neighbor.mainAspect();
            if (TTAspects.PRIMALS.contains(aspect)) {
                if (own.getOrDefault(aspect, 0) >= 5) continue;
                own.put(aspect, own.getOrDefault(aspect, 0) + 1);
                saturate(own, 50, random);
                return new Transfer(i, new InfusedSeedData(data.mainAspect(), own), neighbor, aspect);
            }
            var choices = new ArrayList<>(other.keySet());
            if (choices.isEmpty()) return null;
            aspect = choices.get(random.nextInt(choices.size()));
            if (other.getOrDefault(aspect, 0) < own.getOrDefault(aspect, 0)) return null;
            own.put(aspect, own.getOrDefault(aspect, 0) + 1);
            int remainingOther = other.getOrDefault(aspect, 0) - 1;
            if (remainingOther > 0) other.put(aspect, remainingOther);
            else other.remove(aspect);
            saturate(own, 50, random);
            return new Transfer(i, new InfusedSeedData(data.mainAspect(), own), new InfusedSeedData(neighbor.mainAspect(), other), aspect);
        }
        return null;
    }

    public static void saturate(Map<ResourceKey<IAspect>, Integer> map, int maximum, RandomSource random) {
        int sum = 0;
        for (int val : map.values()) sum += val;
        while (sum > maximum) {
            var candidates = new ArrayList<ResourceKey<IAspect>>();
            for (var entry : map.entrySet()) {
                if (entry.getValue() > 0) candidates.add(entry.getKey());
            }
            if (candidates.isEmpty()) break;
            var picked = candidates.get(random.nextInt(candidates.size()));
            int next = map.get(picked) - 1;
            if (next <= 0) map.remove(picked);
            else map.put(picked, next);
            sum--;
        }
    }

    public static ResourceKey<IAspect> mutate(HolderLookup.Provider registries, InfusedSeedData data, int age, Map<ResourceKey<IAspect>, Integer> soil, RandomSource random) {
        if (age < 7 || data.mainAspect() == null || soil == null) return data.mainAspect();
        var mainHolder = Aspects.resolve(registries, data.mainAspect());
        if (mainHolder == null) return data.mainAspect();
        for (var entry : soil.entrySet()) {
            int amount = Math.min(20, Math.max(0, entry.getValue()));
            if (random.nextInt(10) >= (data.amount(TTAspects.IGNIS) + 1) * amount * amount) continue;
            var soilHolder = Aspects.resolve(registries, entry.getKey());
            if (soilHolder != null) {
                var result = AspectCombinations.result(registries, soilHolder, mainHolder);
                if (result != null && result.getKey() != null) return result.getKey();
            }
        }
        return data.mainAspect();
    }

    private InfusedCropProcess() {}
}
