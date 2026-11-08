package xyz.kajitsy.thaumaturgetinker.client.fx;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Original sparkles: local integrated world only, never broadcast on dedicated servers. */
public final class ShadowbeamFx {
    public static int emitted;

    public static void emit(ResourceKey<Level> dimension, List<Vec3> points) {
        var mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.level == null || mc.level.dimension() != dimension) return;
            for (var p : points) {
                mc.level.addParticle(new SparkleParticleOptions(0x111111, 0.8f, 0, 0.05f, 0.05f, 5, false), p.x, p.y, p.z, 0, 0, 0);
                emitted++;
            }
        });
    }
}
