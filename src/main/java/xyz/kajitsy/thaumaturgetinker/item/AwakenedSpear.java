package xyz.kajitsy.thaumaturgetinker.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.registries.DeferredItem;
import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.network.SoulHearts;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class AwakenedSpear extends IchorSpear implements AdvancedTool {
    public static final DeferredItem<AwakenedSpear> ITEM = KamiRegistries.ITEMS.register("ichor_spear_gem", AwakenedSpear::new);

    private static final Set<UUID> ATTACKING_GROUP = ConcurrentHashMap.newKeySet();

    public AwakenedSpear() {
        super("ichor_spear_gem");
    }

    @Override
    public String toolType() {
        return "spear";
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
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
        builder.accept(AdvancedToolModes.description("spear", stack));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (!player.level().isClientSide() && target instanceof LivingEntity living && living.hurtTime == 0 && living.isAlive() && AdvancedToolModes.get(stack) == 2) {
            living.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1, 1));
        }
        return super.onLeftClickEntity(stack, player, target);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (!(attacker instanceof Player player) || player.level().isClientSide() || !ATTACKING_GROUP.add(player.getUUID())) {
            return;
        }
        try {
            int mode = AdvancedToolModes.get(stack);
            if (mode == 1) {
                var box = new AABB(
                        target.getX() - 3, target.getY() - 3, target.getZ() - 3,
                        target.getX() + 3, target.getY() + 3, target.getZ() + 3);
                var targets = player.level().getEntitiesOfClass(target.getClass(), box);
                for (var nearby : targets) {
                    if (nearby != target && nearby != player && nearby.isAlive()) {
                        player.attack(nearby);
                    }
                }
            } else if (mode == 2) {
                SoulHearts.add(player);
                player.heal(2.0F);
            }
        } finally {
            ATTACKING_GROUP.remove(player.getUUID());
        }
    }
}
