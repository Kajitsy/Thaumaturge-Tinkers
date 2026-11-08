package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/** Build-164 grain is an essentia ingredient, not edible food. */
public final class InfusedGrainItem extends Item {
    public final ResourceKey<IAspect> aspect;

    public InfusedGrainItem(String name, ResourceKey<IAspect> aspect) {
        super(ThaumicTinkererModern.itemProps(name));
        this.aspect = aspect;
    }

    public Map<String, Integer> aspects() {
        return Map.of(aspect.identifier().getPath(), 1, "herba", 1);
    }
}
