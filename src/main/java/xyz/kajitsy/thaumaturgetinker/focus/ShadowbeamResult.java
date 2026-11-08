package xyz.kajitsy.thaumaturgetinker.focus;

import net.minecraft.world.phys.Vec3;

public record ShadowbeamResult(Vec3 position, Vec3 velocity, int blockHits, int entityHits) {}
