package xyz.kajitsy.thaumaturgetinker.focus;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public record SmeltProgress(ResourceKey<Level> dimension, BlockPos pos, BlockState input, int ticks) {}
