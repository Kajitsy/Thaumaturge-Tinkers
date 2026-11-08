package xyz.kajitsy.thaumaturgetinker.client.fx;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Build164 uses the local client particle renderer. */
public final class WarpGateFx {
    public static int attempted, emitted;

    public static int threshold(ParticleStatus setting) {
        return switch (setting) {
            case ALL -> 4;
            case DECREASED -> 2;
            case MINIMAL -> 0;
        };
    }

    public static void emit(ResourceKey<Level> dimension, List<Vec3> points) {
        var mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.level == null || mc.level.dimension() != dimension) return;
            for (var point : points) {
                attempted++;
                if (mc.level.getRandom().nextInt(6) < threshold(mc.options.particles().get())) {
                    mc.level.addParticle(new SparkleParticleOptions(14, 1.5f, 0, 0.05f, 0f, 18, true), point.x, point.y, point.z, 0, 0, 0);
                    emitted++;
                }
            }
        });
    }
}
