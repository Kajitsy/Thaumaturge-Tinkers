package xyz.kajitsy.thaumaturgetinker.potion;

import net.minecraft.server.level.ServerLevel;

public record PotionHit(ServerLevel level, long time) {}
