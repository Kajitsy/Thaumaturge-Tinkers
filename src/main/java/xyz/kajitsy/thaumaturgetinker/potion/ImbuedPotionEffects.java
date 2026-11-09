package xyz.kajitsy.thaumaturgetinker.potion;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ThaumicTinkererModern.ID)
public final class ImbuedPotionEffects {
    private record Hit(ServerLevel level, long time) {}

    private static final Map<LivingEntity, Hit> AIR_HITS = new WeakHashMap<>(), FIRE_HITS = new WeakHashMap<>();

    @SubscribeEvent
    public static void incoming(LivingIncomingDamageEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        var target = event.getEntity();
        if (event.getSource().getEntity() instanceof Player attacker) {
            if (attacker.hasEffect(InfusedPotions.EARTH)) earth(attacker, target);
            if (attacker.hasEffect(InfusedPotions.AIR)) AIR_HITS.put(target, new Hit(level, level.getGameTime()));
            if (attacker.hasEffect(InfusedPotions.FIRE)) FIRE_HITS.put(target, new Hit(level, level.getGameTime()));
        } else if (target instanceof Player victim && event.getSource().getEntity() instanceof LivingEntity mobAttacker) {
            if (victim.hasEffect(InfusedPotions.EARTH)) earth(victim, mobAttacker);
        }
    }

    @SubscribeEvent
    public static void brewingRecipes(net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent event) {
        registerBrew(event, ThaumicTinkererModern.INFUSED_GRAIN_AIR, InfusedPotions.POTION_AIR);
        registerBrew(event, ThaumicTinkererModern.INFUSED_GRAIN_FIRE, InfusedPotions.POTION_FIRE);
        registerBrew(event, ThaumicTinkererModern.INFUSED_GRAIN_EARTH, InfusedPotions.POTION_EARTH);
        registerBrew(event, ThaumicTinkererModern.INFUSED_GRAIN_WATER, InfusedPotions.POTION_WATER);
    }

    private static void registerBrew(net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent event, java.util.function.Supplier<? extends net.minecraft.world.level.ItemLike> ingredient, java.util.function.Supplier<? extends net.minecraft.world.level.ItemLike> result) {
        event.getBuilder().addRecipe(new net.neoforged.neoforge.common.brewing.IBrewingRecipe() {
            @Override
            public boolean isInput(net.minecraft.world.item.ItemStack input) {
                if (!input.is(net.minecraft.world.item.Items.POTION)) return false;
                var contents = input.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
                return contents != null && contents.is(net.minecraft.world.item.alchemy.Potions.AWKWARD);
            }

            @Override
            public boolean isIngredient(net.minecraft.world.item.ItemStack ing) {
                return ing.is(ingredient.get().asItem());
            }

            @Override
            public net.minecraft.world.item.ItemStack getOutput(net.minecraft.world.item.ItemStack input, net.minecraft.world.item.ItemStack ing) {
                return isInput(input) && isIngredient(ing) ? new net.minecraft.world.item.ItemStack(result.get()) : net.minecraft.world.item.ItemStack.EMPTY;
            }
        });
    }

    @SubscribeEvent
    public static void levelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) tickHits(level, level.getGameTime());
    }

    public static boolean tracked(LivingEntity entity, boolean fire) {
        return (fire ? FIRE_HITS : AIR_HITS).containsKey(entity);
    }

    public static void tickHits(ServerLevel level, long now) {
        applyHits(AIR_HITS, level, now, false);
        applyHits(FIRE_HITS, level, now, true);
    }

    private static void applyHits(Map<LivingEntity, Hit> hits, ServerLevel level, long now, boolean fire) {
        var iterator = hits.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var target = entry.getKey();
            var hit = entry.getValue();
            if (target.isRemoved() || !target.isAlive() || target.level() != hit.level()) {
                iterator.remove();
                continue;
            }
            if (hit.level() != level) continue;
            if (now % 5 == 0) {
                if (fire) {
                    target.igniteForSeconds(6);
                    for (var point : fireSphere(level.getRandom(), target.getX(), target.getY(), target.getZ())) {
                        level.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 1, 0, 0, 0, 0);
                    }
                    var nearby = level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3), e -> e != target && !(e instanceof Player));
                    for (var e : nearby) {
                        e.igniteForSeconds(6);
                    }
                } else {
                    var random = level.getRandom();
                    target.setDeltaMovement(random.nextFloat() - .5, random.nextFloat(), random.nextFloat() - .5);
                    target.hurtMarked = true;
                    level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.5, target.getZ(), 4, 0.2, 0.2, 0.2, 0.05);
                }
            }
            // Build 164 checks strict expiration after the final possible application.
            if (now > hit.time() + (fire ? 6000 : 20)) iterator.remove();
        }
    }

    public static List<Vec3> fireSphere(RandomSource random, double x, double y, double z) {
        var points = new ArrayList<Vec3>(30);
        for (int i = 0; i < 30; i++) {
            double theta = random.nextFloat() * 2f * Math.PI, phi = random.nextFloat() * 2f * Math.PI;
            points.add(new Vec3(x + 2.5 * Math.sin(theta) * Math.cos(phi), y + 1 + 2.5 * Math.sin(theta) * Math.sin(phi), z + 2.5 * Math.cos(theta)));
        }
        return points;
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        water(event.getEntity());
    }

    public static int earth(Player player, LivingEntity target) {
        if (!(player.level() instanceof ServerLevel level) || target.level() != level || !player.hasEffect(InfusedPotions.EARTH)) return 0;
        boolean xAxis = Math.abs(target.getZ() - player.getZ()) < Math.abs(target.getX() - player.getX());
        int x = (int) ((target.getX() + player.getX()) / 2), y = (int) (player.getY() + 2), z = (int) ((target.getZ() + player.getZ()) / 2);
        int count = 0;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                var pos = xAxis ? new BlockPos(x, y + i, z + j) : new BlockPos(x + j, y + i, z);
                if (!level.isInWorldBounds(pos) || !level.isLoaded(pos) || (!level.isEmptyBlock(pos) && !level.getBlockState(pos).is(ThaumicTinkererModern.FORCEFIELD.get()))) continue;
                if (level.setBlockAndUpdate(pos, ThaumicTinkererModern.FORCEFIELD.get().defaultBlockState())) {
                    count++;
                    level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.1);
                }
            }
        }
        return count;
    }

    public static int water(Player player) {
        if (!(player.level() instanceof ServerLevel level) || !player.hasEffect(InfusedPotions.WATER)) return 0;
        int changed = 0;
        for (int x = (int) (player.getX() - 2); x < player.getX() + 2; x++) {
            for (int y = (int) (player.getY() - 2); y < player.getY() + 2; y++) {
                for (int z = (int) (player.getZ() - 2); z < player.getZ() + 2; z++) {
                    var pos = new BlockPos(x, y, z);
                    if (!level.isInWorldBounds(pos) || !level.isLoaded(pos) || !level.getBlockState(pos).is(Blocks.LAVA)) continue;
                    if (level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState())) {
                        changed++;
                        level.sendParticles(ParticleTypes.SPLASH, x + .5, y + .5, z + .5, 5, 0.2, 0.2, 0.2, 0.1);
                    }
                }
            }
        }
        return changed;
    }
}
