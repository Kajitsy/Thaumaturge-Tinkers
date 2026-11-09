package xyz.kajitsy.thaumaturgetinker.potion;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
        var list = tag.getCompound("mainAspect").getList("Aspects", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag value = list.getCompound(i);
            var aspect = parseAspect(value.getString("key"));
            if (aspect != null && value.getInt("amount") > 0) {
                main = aspect;
                break;
            }
        }
        var tendencies = new LinkedHashMap<ResourceKey<IAspect>, Integer>();
        int remaining = MAX_TENDENCIES;
        var tendencyList = tag.getCompound("aspectTendencies").getList("Aspects", Tag.TAG_COMPOUND);
        for (int i = 0; i < tendencyList.size(); i++) {
            CompoundTag value = tendencyList.getCompound(i);
            var aspect = parseAspect(value.getString("key"));
            if (aspect == null || !TTAspects.PRIMALS.contains(aspect)) continue;
            if (value.getInt("amount") < 0) continue;
            int amount = Math.min(remaining, value.getInt("amount"));
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
            entry.putString("key", mainAspect.location().getPath());
            entry.putInt("amount", 1);
            mainList.add(entry);
        }
        mainTag.put("Aspects", mainList);
        tag.put("mainAspect", mainTag);

        var tendencyTag = new CompoundTag();
        var tendencyList = new ListTag();
        for (var e : tendencies.entrySet()) {
            var entry = new CompoundTag();
            entry.putString("key", e.getKey().location().getPath());
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
