package xyz.kajitsy.thaumaturgetinker.focus;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Unspawned, synchronous legacy throwable. Neither gravity nor drag changes its cached movement. */
public final class ShadowbeamTrace {
    public static final int STEPS = 300;

    public record Start(Vec3 position, Vec3 velocity) {}
    public record Result(Vec3 position, Vec3 velocity, int blockHits, int entityHits) {}

    public static Start start(double x, double y, double z, float eyeHeight, float yaw, float pitch) {
        float yr = yaw / 180f * (float) Math.PI, pr = pitch / 180f * (float) Math.PI;
        double vx = -Mth.sin(yr) * Mth.cos(pr) * .4f, vy = -Mth.sin(pr) * .4f, vz = Mth.cos(yr) * Mth.cos(pr) * .4f;
        float length = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
        double spread = (double) .0075f;
        var velocity = new Vec3((vx / length + spread) * 1.5f / 10d, (vy / length + spread) * 1.5f / 10d, (vz / length + spread) * 1.5f / 10d);
        return new Start(new Vec3(x - (double) (Mth.cos(yr) * .16f), y + (double) eyeHeight - (double) .1f, z - (double) (Mth.sin(yr) * .16f)), velocity);
    }

    public static Vec3 reflect(Vec3 velocity, Direction side) {
        var n = new Vec3(side.getStepX(), side.getStepY(), side.getStepZ());
        return n.scale(-2 * velocity.dot(n)).add(velocity);
    }

    /** Legacy AABB intercept tests all six planes, including exits from inside a target.
     * Modern AABB.clip tests entry planes only and has a different parallel tolerance. */
    public static Optional<Vec3> intercept(AABB box, Vec3 from, Vec3 to) {
        double[] low = {box.minX, box.minY, box.minZ}, high = {box.maxX, box.maxY, box.maxZ};
        double[] start = {from.x, from.y, from.z}, delta = {to.x - from.x, to.y - from.y, to.z - from.z};
        Vec3 nearest = null;
        double distance = Double.POSITIVE_INFINITY;
        for (int axis = 0; axis < 3; axis++) {
            if (delta[axis] * delta[axis] < (double) 1.0E-7f) continue;
            for (double plane : new double[]{low[axis], high[axis]}) {
                double fraction = (plane - start[axis]) / delta[axis];
                if (fraction < 0 || fraction > 1) continue;
                var point = new Vec3(start[0] + delta[0] * fraction, start[1] + delta[1] * fraction, start[2] + delta[2] * fraction);
                double[] coords = {point.x, point.y, point.z};
                boolean inside = true;
                for (int other = 0; other < 3; other++) {
                    if (other != axis && (coords[other] < low[other] || coords[other] > high[other])) inside = false;
                }
                double candidate = from.distanceToSqr(point);
                if (inside && candidate < distance) {
                    nearest = point;
                    distance = candidate;
                }
            }
        }
        return Optional.ofNullable(nearest);
    }

    public static boolean sparkle(int iteration) {
        return iteration > 2;
    }

    public static Result run(ServerLevel level, Player shooter, Start start) {
        return run(level, shooter, start, null);
    }

    public static Result run(ServerLevel level, Player shooter, Start start, Consumer<Vec3> sparkle) {
        Vec3 position = start.position(), velocity = start.velocity();
        int blockHits = 0, entityHits = 0;
        for (int step = 0; step < STEPS; step++) {
            var end = position.add(velocity);
            var block = level.clip(new ClipContext(position, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, shooter));
            boolean blocked = block.getType() == HitResult.Type.BLOCK;
            var entityEnd = blocked ? block.getLocation() : end;
            var bounds = new AABB(position.x - .125, position.y, position.z - .125, position.x + .125, position.y + .25, position.z + .125).expandTowards(velocity).inflate(1);
            Entity target = null;
            double nearest = 0;
            for (var candidate : level.getEntities((Entity) null, bounds, e -> e.isPickable())) {
                if (candidate == shooter && step + 1 < 5) continue;
                var hit = intercept(candidate.getBoundingBox().inflate((double) .3f), position, entityEnd);
                if (hit.isEmpty()) continue;
                double distance = position.distanceTo(hit.get());
                if (distance < nearest || nearest == 0) {
                    nearest = distance;
                    target = candidate;
                }
            }
            if (target != null) {
                entityHits++;
                if (target != shooter && (!(target instanceof Player) || level.isPvpAllowed())) {
                    target.hurt(level.damageSources().playerAttack(shooter), 8);
                }
            } else if (blocked && !level.getBlockState(block.getBlockPos()).is(Blocks.NETHER_PORTAL)) {
                blockHits++;
                velocity = reflect(velocity, block.getDirection());
            }
            // Original impact changes motion before parent moves, from the old position.
            position = position.add(velocity);
            if (sparkle != null && sparkle(step)) {
                sparkle.accept(new Vec3((float) position.x, (float) position.y, (float) position.z));
            }
        }
        return new Result(position, velocity, blockHits, entityHits);
    }
}
