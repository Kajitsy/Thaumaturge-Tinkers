package xyz.kajitsy.thaumaturgetinker.potion;

import xyz.kajitsy.thaumaturgetinker.item.InfusedSeedsItem;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Build-164 harvest order: breed seed, entropy copies, fertilize, then parent-aspect loot. */
public final class InfusedCropHarvest {
    public static List<ItemStack> collect(HolderLookup.Provider registries, InfusedSeedData data, int age, Map<ResourceKey<IAspect>, Integer> soil, RandomSource random, Consumer<ResourceKey<IAspect>> fertilize, Function<ResourceKey<IAspect>, ItemStack> loot) {
        var result = new ArrayList<ItemStack>();
        var mutated = InfusedCropProcess.mutate(registries, data, age, soil, random);
        var seed = InfusedSeedsItem.stack(mutated);
        new InfusedSeedData(mutated, data.tendencies()).write(seed);
        int count = 1;
        int entropy = data.amount(TTAspects.PERDITIO);
        while (random.nextInt(10000) < entropy * entropy) count++;
        // Vanilla stack limits are respected when spawning a rare long entropy sequence.
        while (count > 0) {
            int amount = Math.min(count, seed.getMaxStackSize());
            var split = seed.copy();
            split.setCount(amount);
            result.add(split);
            count -= amount;
        }
        if (age >= 7) {
            do {
                fertilize.accept(data.mainAspect());
            } while (random.nextInt(55) < data.amount(TTAspects.TERRA));

            do {
                var drop = loot.apply(data.mainAspect());
                if (drop != null && !drop.isEmpty()) result.add(drop.copy());
            } while (random.nextInt(75) < data.amount(TTAspects.ORDO));
        }
        return result;
    }

    private InfusedCropHarvest() {}
}
