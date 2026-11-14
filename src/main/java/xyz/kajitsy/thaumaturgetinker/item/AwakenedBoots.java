package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.network.AwakenedArmorStatus;


import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

@EventBusSubscriber(modid = "thaumaturgetinker")
public final class AwakenedBoots extends AwakenedArmorItem {
    public static final DeferredItem<AwakenedBoots> ITEM = KamiRegistries.ITEMS.register("ichorcloth_boots_gem", AwakenedBoots::new);
    private static final Map<Player, Double> STEPS = new WeakHashMap<>();

    public AwakenedBoots() {
        super("ichorcloth_boots_gem", ArmorItem.Type.BOOTS);
    }

    public static boolean worn(Player p) {
        return p.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof AwakenedBoots;
    }

    public static boolean active(Player p) {
        return worn(p) && !disabled(p.getItemBySlot(EquipmentSlot.FEET)) && AwakenedArmorStatus.enabled(p);
    }

    public static float airSpeed(Player p) {
        return p.isSprinting() ? 0.05f : 0.04f;
    }

    public static void update(Player p) {
        boolean active = active(p);
        if (p.level().isClientSide()) {
            var step = p.getAttribute(Attributes.STEP_HEIGHT);
            if (step != null) {
                if (active) {
                    STEPS.putIfAbsent(p, step.getBaseValue());
                    step.setBaseValue(p.isShiftKeyDown() ? STEPS.getOrDefault(p, 0.6) : 1.0);
                } else if (STEPS.containsKey(p)) {
                    step.setBaseValue(STEPS.remove(p));
                }
            }
        }
        if (!active) return;
        p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 2, 1, true, false));
        if ((p.onGround() || p.getAbilities().flying) && p.zza > 0) {
            p.moveRelative(p.getAbilities().flying ? 0.075f : 0.15f, new Vec3(0, 0, 1));
        }
        p.fallDistance = 0;
        var pos = new BlockPos((int) p.getX(), (int) p.getY() - 1, (int) p.getZ());
        if (p.level().getBlockState(pos).is(Blocks.DIRT)) {
            p.level().setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
        }
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post e) {
        if (e.getEntity() instanceof Player p) {
            update(p);
        }
    }

    @SubscribeEvent
    public static void jump(LivingEvent.LivingJumpEvent e) {
        if (e.getEntity() instanceof Player p && active(p)) {
            p.setDeltaMovement(p.getDeltaMovement().add(0, 0.3, 0));
        }
    }

    @SubscribeEvent
    public static void fall(LivingFallEvent e) {
        if (e.getEntity() instanceof Player p && worn(p)) {
            e.setDistance(0);
        }
    }

    public static boolean canReflectDamage(DamageSource source) {
        return !source.is(DamageTypes.THORNS) && !source.is(DamageTypeTags.IS_EXPLOSION);
    }

    @SubscribeEvent
    public static void onDamagePost(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide() || !active(player)) {
            return;
        }
        var source = event.getSource();
        if (!canReflectDamage(source)) {
            return;
        }
        if (source.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker
                && attacker != player
                && attacker.isAlive()
                && !attacker.isDeadOrDying()
                && attacker.getPose() != Pose.DYING) {
            float reflected = Math.max(1.0f, event.getOriginalDamage());
            attacker.hurt(player.damageSources().thorns(player), reflected);
        }
    }
}
