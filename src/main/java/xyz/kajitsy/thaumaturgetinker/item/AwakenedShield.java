package xyz.kajitsy.thaumaturgetinker.item;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.TooltipDisplay;
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
        projectile.needsSync = true;
    };

    public AwakenedShield() {
        super("ichor_shield_gem", 0.0F, 0.0F, 180.0F);
    }

    @Override
    public String toolType() {
        return "shield";
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var result = tryToggleMode(level, player, player.getItemInHand(hand));
        return result != InteractionResult.PASS ? result : super.use(level, player, hand);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flags) {
        super.appendHoverText(stack, context, display, builder, flags);
        builder.accept(AdvancedToolModes.description("shield", stack));
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity blocker)) {
            return;
        }
        ItemStack shield = blocker.getItemBlockingWith();
        if (shield == null || !(shield.getItem() instanceof AwakenedShield) || AdvancedToolModes.get(shield) != 1) {
            return;
        }
        Projectile projectile = event.getProjectile();
        event.setCanceled(true);
        projectile.deflect(MIRROR_DEFLECTION, blocker, EntityReference.<Entity>of(blocker), false);
        BlocksAttacks blocks = shield.get(DataComponents.BLOCKS_ATTACKS);
        if (blocks != null && blocker.level() instanceof ServerLevel level) {
            blocks.onBlocked(level, blocker);
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!event.getBlocked()) {
            return;
        }
        LivingEntity blocker = event.getEntity();
        ItemStack shield = blocker.getItemBlockingWith();
        if (shield == null || !(shield.getItem() instanceof AwakenedShield) || AdvancedToolModes.get(shield) != 2) {
            return;
        }
        DamageSource source = event.getDamageSource();
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            return;
        }
        if (source.getDirectEntity() instanceof LivingEntity attacker && blocker.level() instanceof ServerLevel level) {
            attacker.knockback(1.5, blocker.getX() - attacker.getX(), blocker.getZ() - attacker.getZ());
            attacker.hurtMarked = true;
            attacker.hurtServer(level, blocker.damageSources().thorns(blocker), 4.0F);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity blocker = event.getEntity();
        ItemStack shield = blocker.getItemBlockingWith();
        if (shield == null || !(shield.getItem() instanceof AwakenedShield) || AdvancedToolModes.get(shield) != 0) {
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
