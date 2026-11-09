package xyz.kajitsy.thaumaturgetinker.potion;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Shared seed/plant state. Legacy nested AspectList keys survive modern stack components. */
public record InfusedSeedData(ResourceKey<IAspect> mainAspect, Map<ResourceKey<IAspect>, Integer> tendencies) {
    public static final int MAX_TENDENCIES = 50;

public InfusedSeedData {
        var clean = new LinkedHashMap<ResourceKey<IAspect>, Integer>();
        int remaining = MAX_TENDENCIES;
        if (tendencies != null) {
            for (var e : tendencies.entrySet()) {
                var aspect = e.getKey();
                if (aspect == null || !TTAspects.PRIMALS.contains(aspect) || e.getValue() == null || e.getValue() < 0) continue;
                int amount = Math.min(remaining, e.getValue());
                clean.put(aspect, amount);
                remaining -= amount;
            }
        }
        tendencies = Map.copyOf(clean);
    }

    public static InfusedSeedData read(ItemStack stack) {
        return read(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }

    private static ResourceKey<IAspect> parseAspect(String str) {
        if (str == null || str.isEmpty()) return null;
        if (!str.contains(":")) str = "thaumaturge:" + str;
        return ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.parse(str));
    }

    public static InfusedSeedData read(CompoundTag tag) {
        ResourceKey<IAspect> main = null;
        var list = tag.getCompoundOrEmpty("mainAspect").getListOrEmpty("Aspects");
        for (var entry : list) {
            if (entry instanceof CompoundTag value) {
                var aspect = parseAspect(value.getStringOr("key", ""));
                if (aspect != null && value.getIntOr("amount", 0) > 0) {
                    main = aspect;
                    break;
                }
            }
        }
        var tendencies = new LinkedHashMap<ResourceKey<IAspect>, Integer>();
        int remaining = MAX_TENDENCIES;
        for (var entry : tag.getCompoundOrEmpty("aspectTendencies").getListOrEmpty("Aspects")) {
            if (entry instanceof CompoundTag value) {
                var aspect = parseAspect(value.getStringOr("key", ""));
                if (aspect == null || !TTAspects.PRIMALS.contains(aspect)) continue;
                if (value.getIntOr("amount", 0) < 0) continue;
                int amount = Math.min(remaining, value.getIntOr("amount", 0));
                tendencies.put(aspect, amount);
                remaining -= amount;
            }
        }
        return new InfusedSeedData(main, tendencies);
    }

    public void write(net.minecraft.world.level.storage.ValueOutput output) {
        if (mainAspect != null) {
            output.putString("mainAspect", mainAspect.identifier().toString());
        }
        var list = output.childrenList("tendencies");
        for (var e : tendencies.entrySet()) {
            var item = list.addChild();
            item.putString("key", e.getKey().identifier().toString());
            item.putInt("amount", e.getValue());
        }
    }

    public static InfusedSeedData read(net.minecraft.world.level.storage.ValueInput input) {
        var main = input.getString("mainAspect").map(InfusedSeedData::parseAspect).orElse(null);
        var tendencies = new LinkedHashMap<ResourceKey<IAspect>, Integer>();
        int remaining = MAX_TENDENCIES;
        for (var item : input.childrenListOrEmpty("tendencies")) {
            var aspect = parseAspect(item.getStringOr("key", ""));
            if (aspect == null || !TTAspects.PRIMALS.contains(aspect)) continue;
            int amount = Math.min(remaining, item.getIntOr("amount", 0));
            if (amount <= 0) continue;
            tendencies.put(aspect, amount);
            remaining -= amount;
        }
        return new InfusedSeedData(main, tendencies);
    }

    public void write(ItemStack stack) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        write(tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public void write(CompoundTag tag) {
        var mainTag = new CompoundTag();
        var mainList = new ListTag();
        if (mainAspect != null) {
            var entry = new CompoundTag();
            entry.putString("key", mainAspect.identifier().getPath());
            entry.putInt("amount", 1);
            mainList.add(entry);
        }
        mainTag.put("Aspects", mainList);
        tag.put("mainAspect", mainTag);

        var tendencyTag = new CompoundTag();
        var tendencyList = new ListTag();
        for (var e : tendencies.entrySet()) {
            var entry = new CompoundTag();
            entry.putString("key", e.getKey().identifier().getPath());
            entry.putInt("amount", e.getValue());
            tendencyList.add(entry);
        }
        tendencyTag.put("Aspects", tendencyList);
        tag.put("aspectTendencies", tendencyTag);
    }

    public int amount(ResourceKey<IAspect> aspect) {
        return tendencies.getOrDefault(aspect, 0);
    }

    public int growthBound() {
        return Math.max(1, (2510 - amount(TTAspects.AQUA) * amount(TTAspects.AQUA)) * 6);
    }

    public int transferBound() {
        return Math.max(1, (2550 - amount(TTAspects.AER) * amount(TTAspects.AER)) * 10);
    }
}
