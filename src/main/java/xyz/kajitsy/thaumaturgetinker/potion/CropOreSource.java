package xyz.kajitsy.thaumaturgetinker.potion;

import net.minecraft.world.item.ItemStack;
import java.util.List;

public record CropOreSource(String name, List<ItemStack> items) {}
