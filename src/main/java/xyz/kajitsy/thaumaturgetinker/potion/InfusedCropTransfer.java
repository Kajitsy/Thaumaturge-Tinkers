package xyz.kajitsy.thaumaturgetinker.potion;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;

public record InfusedCropTransfer(int neighbor, InfusedSeedData recipient, InfusedSeedData donor, ResourceKey<IAspect> aspect) {}
