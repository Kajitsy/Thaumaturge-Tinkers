package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.mixin.CatAvoidGoalAccessor;
import xyz.kajitsy.thaumaturgetinker.mixin.CatCreeperAccessor;
import xyz.kajitsy.thaumaturgetinker.mixin.CatMobAccessor;


import java.util.ArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.registries.DeferredItem;

import net.minecraft.world.entity.EquipmentSlot;
import org.jspecify.annotations.Nullable;

public final class CatAmulet extends Item {
    public static final DeferredItem<CatAmulet> ITEM = KamiRegistries.ITEMS.register("cat_amulet", CatAmulet::new);

    public CatAmulet() {
        super(ThaumicTinkererModern.itemProps("cat_amulet", new Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }

    public static void init() {}

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof LivingEntity living) onWornTick(stack, living);
    }

    public void onWornTick(ItemStack stack, LivingEntity wearer) {
        if (wearer.level().isClientSide()) return;
        for (var mob : wearer.level().getEntitiesOfClass(Mob.class, new AABB(wearer.getX() - 10, wearer.getY() - 4, wearer.getZ() - 10, wearer.getX() + 10, wearer.getY() + 4, wearer.getZ() + 10))) {
            var access = (CatMobAccessor) mob;
            var goals = new ArrayList<>(access.thaumaturgetinker$goals().getAvailableGoals());
            goals.addAll(access.thaumaturgetinker$targets().getAvailableGoals());
            for (var wrapped : goals) {
                var goal = wrapped.getGoal();
                if (goal instanceof CatAvoidGoalAccessor avoid && (avoid.thaumaturgetinker$avoidClass() == Ocelot.class || avoid.thaumaturgetinker$avoidClass() == Cat.class)) {
                    avoid.thaumaturgetinker$avoidClass(Player.class);
                }
                if (goal instanceof CatTargetGoal target && target.thaumaturgetinker$targetType() == Player.class) {
                    target.thaumaturgetinker$targetType(EndCrystal.class);
                }
            }
            if (mob instanceof Creeper creeper) {
                creeper.setSwellDir(-1);
                ((CatCreeperAccessor) creeper).thaumaturgetinker$fuse(0);
                creeper.setTarget(null);
            }
        }
    }
}
