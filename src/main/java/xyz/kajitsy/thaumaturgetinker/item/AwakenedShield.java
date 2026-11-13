package xyz.kajitsy.thaumaturgetinker.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import xyz.kajitsy.thaumaturgetinker.KamiRegistries;

@EventBusSubscriber(modid = "thaumaturgetinker")
public final class AwakenedShield extends IchorShield implements AdvancedTool {
    public static final DeferredItem<AwakenedShield> ITEM = KamiRegistries.ITEMS.register("ichor_shield_gem", AwakenedShield::new);

    private static final ProjectileDeflection MIRROR_DEFLECTION = (projectile, deflector, random) -> {
        projectile.setDeltaMovement(projectile.getDeltaMovement().reverse());
        projectile.setYRot(projectile.getYRot() + 180.0F);
        projectile.yRotO += 180.0F;
        projectile.hurtMarked = true;
    };

    public AwakenedShield() {
        super("ichor_shield_gem");
    }

    @Override
    public String toolType() {
        return "shield";
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var result = tryToggleMode(level, player, player.getItemInHand(hand));
        return result.getResult() != InteractionResult.PASS ? result : super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flags) {
        super.appendHoverText(stack, context, tooltipComponents, flags);
        tooltipComponents.add(AdvancedToolModes.description("shield", stack));
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity blocker)) {
            return;
        }
        if (!blocker.isBlocking()) {
            return;
        }
        ItemStack shield = blocker.getUseItem();
        if (!(shield.getItem() instanceof AwakenedShield) || AdvancedToolModes.get(shield) != 1) {
            return;
        }
        Projectile projectile = event.getProjectile();
        event.setCanceled(true);
        projectile.deflect(MIRROR_DEFLECTION, blocker, blocker, false);
        blocker.level().playSound(null, blocker.getX(), blocker.getY(), blocker.getZ(),
                SoundEvents.SHIELD_BLOCK, blocker.getSoundSource(), 1.0F, 0.8F + blocker.level().random.nextFloat() * 0.4F);
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!event.getBlocked()) {
            return;
        }
        LivingEntity blocker = event.getEntity();
        ItemStack shield = blocker.getUseItem();
        if (!(shield.getItem() instanceof AwakenedShield) || AdvancedToolModes.get(shield) != 2) {
            return;
        }
        DamageSource source = event.getDamageSource();
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            return;
        }
        if (source.getDirectEntity() instanceof LivingEntity attacker && !blocker.level().isClientSide()) {
            attacker.knockback(1.5, blocker.getX() - attacker.getX(), blocker.getZ() - attacker.getZ());
            attacker.hurtMarked = true;
            attacker.hurt(blocker.damageSources().thorns(blocker), 4.0F);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity blocker = event.getEntity();
        if (!blocker.isBlocking()) {
            return;
        }
        ItemStack shield = blocker.getUseItem();
        if (!(shield.getItem() instanceof AwakenedShield) || AdvancedToolModes.get(shield) != 0) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        if (event.getNewDamage() > 0.0F) {
            event.setNewDamage(event.getNewDamage() * 0.5F);
        }
    }
}
