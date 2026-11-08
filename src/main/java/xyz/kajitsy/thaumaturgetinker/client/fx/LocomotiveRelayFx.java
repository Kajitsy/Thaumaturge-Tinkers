package xyz.kajitsy.thaumaturgetinker.client.fx;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Original relay sparkle call, with bounded symmetric iteration fixing negative-direction loop errors. */
public final class LocomotiveRelayFx {
    public static void tick(Level level, BlockPos pos, BlockPos end) {
        if (end == null || end.getY() != pos.getY() || (end.getX() != pos.getX() && end.getZ() != pos.getZ())) return;
        int dx = Integer.signum(end.getX() - pos.getX());
        int dz = Integer.signum(end.getZ() - pos.getZ());
        int distance = Math.max(Math.abs(end.getX() - pos.getX()), Math.abs(end.getZ() - pos.getZ()));
        if (distance > 32) return;
        double phase = .05 * (level.getGameTime() % 20);
        int color = dx > 0 || dz > 0 ? 2 : 14;
        for (int i = 0; i < distance; i++) {
            if (level.isClientSide()) {
                level.addParticle(new SparkleParticleOptions(color, 1.0f, 0, 0.05f, 0f, 10, true),
                        pos.getX() + .5 + dx * (i + phase),
                        pos.getY() + .5,
                        pos.getZ() + .5 + dz * (i + phase),
                        0, 0, 0);
            }
        }
    }
}
