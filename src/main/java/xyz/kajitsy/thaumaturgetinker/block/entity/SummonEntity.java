package xyz.kajitsy.thaumaturgetinker.block.entity;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.item.MobAspect;
import xyz.kajitsy.thaumaturgetinker.item.MobAspectItem;
import xyz.kajitsy.thaumaturgetinker.item.MobAspects;


import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SummonEntity extends BlockEntity {
    long lastCheck = Long.MIN_VALUE;
    public static final Map<String, String> RECIPES = recipes();

    private static Map<String, String> recipes() {
        var recipes = new LinkedHashMap<String, String>();
        for (var id : MobAspects.SUMMON_ORDER) {
            recipes.putIfAbsent(signature(MobAspects.MOBS.get(id)), id);
        }
        return Map.copyOf(recipes);
    }

    public static String signature(List<MobAspect> aspects) {
        return aspects.stream().map(MobAspect::tag).sorted().collect(Collectors.joining(","));
    }

    public SummonEntity(BlockPos pos, BlockState state) {
        super(ThaumicTinkererModern.SUMMON_ENTITY.get(), pos, state);
    }

    public static boolean matchesLast(ItemStack stack, Level level, BlockPos pos) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return !tag.contains("LastTablet") || (tag.getLongOr("LastTablet", 0L) == pos.asLong() && tag.getStringOr("LastDimension", "").equals(level.dimension().identifier().toString()));
    }

    public static void mark(ItemStack stack, Level level, BlockPos pos) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putLong("LastTablet", pos.asLong());
            tag.putString("LastDimension", level.dimension().identifier().toString());
        });
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SummonEntity tile) {
        if (!(level instanceof ServerLevel server)) return;
        long time = level.getGameTime();
        if (time % 300 != 0 || time == tile.lastCheck) return;
        tile.lastCheck = time;
        tile.setChanged();
        if (level.hasNeighborSignal(pos)) return;

        for (int radius = 1; radius < 6; radius++) {
            var pedestals = new ArrayList<BlockEntityPedestal>();
            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    var at = pos.offset(x, 0, z);
                    if (level.isLoaded(at) && level.getBlockEntity(at) instanceof BlockEntityPedestal pedestal && pedestal.getItem().getItem() instanceof MobAspectItem) {
                        pedestals.add(pedestal);
                    }
                }
            }
            for (int i = 0; i < pedestals.size(); i++) {
                for (int j = i + 1; j < pedestals.size(); j++) {
                    for (int k = j + 1; k < pedestals.size(); k++) {
                        var selected = List.of(pedestals.get(i), pedestals.get(j), pedestals.get(k));
                        var aspects = selected.stream().map(p -> ((MobAspectItem) p.getItem().getItem()).aspect).toList();
                        String id = RECIPES.get(signature(aspects));
                        if (id == null) continue;
                        boolean infused = selected.stream().allMatch(p -> ((MobAspectItem) p.getItem().getItem()).form == 2);
                        if (infused && time % 1200 != 0) return;
                        boolean same = selected.stream().allMatch(p -> matchesLast(p.getItem(), level, pos));
                        if (!infused || same) {
                            if (id.equals("minecraft:skeleton") && level.dimension() == Level.NETHER) id = "minecraft:wither_skeleton";
                            var typeOpt = BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(id));
                            if (typeOpt.isEmpty()) return;
                            var entity = typeOpt.get().value().create(level, EntitySpawnReason.MOB_SUMMONED);
                            if (!(entity instanceof Mob mob)) return;
                            mob.snapTo(pos.getX() + .5, pos.getY() + 1.0, pos.getZ() + .5, 0f, 0f);
                            net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(mob, server, server.getCurrentDifficultyAt(pos), EntitySpawnReason.MOB_SUMMONED, null);
                            mob.setPersistenceRequired();
                            if (!server.addFreshEntity(mob)) return;
                            // Original consumes whole non-infused pedestal stacks, rather than one item each.
                            if (!infused) selected.forEach(p -> p.setItem(ItemStack.EMPTY));
                            for (int n = 0; n < 3; n++) {
                                var at = selected.get(n).getBlockPos();
                                server.sendParticles(ParticleTypes.PORTAL, at.getX() + .5, at.getY() + 1.2, at.getZ() + .5, 10, 0.2, 0.2, 0.2, 0.1);
                            }
                            server.sendParticles(ParticleTypes.WITCH, pos.getX() + .5, pos.getY() + 1.0, pos.getZ() + .5, 20, 0.3, 0.3, 0.3, 0.1);
                        }
                        if (infused) selected.forEach(p -> {
                            mark(p.getItem(), level, pos);
                            p.setItem(p.getItem());
                        });
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("LastCheck", lastCheck);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        lastCheck = input.getLongOr("LastCheck", Long.MIN_VALUE);
    }
}
