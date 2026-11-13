package xyz.kajitsy.thaumaturgetinker.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

import java.util.function.Consumer;

public final class AwakenedSpear extends IchorSpear implements AdvancedTool {
    public static final DeferredItem<AwakenedSpear> ITEM = KamiRegistries.ITEMS.register("ichor_spear_gem", AwakenedSpear::new);

    private boolean attackingGroup;

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
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attackingGroup || !(attacker instanceof Player player) || !target.isAlive()) {
            return;
        }
        int mode = AdvancedToolModes.get(stack);
        if (mode == 1) {
            var box = new AABB(
                    target.getX() - 3, target.getY() - 3, target.getZ() - 3,
                    target.getX() + 3, target.getY() + 3, target.getZ() + 3);
            var targets = player.level().getEntitiesOfClass(target.getClass(), box);
            attackingGroup = true;
            try {
                for (var nearby : targets) {
                    if (nearby != target && nearby.isAlive()) {
                        player.attack(nearby);
                    }
                }
            } finally {
                attackingGroup = false;
            }
        } else if (mode == 2) {
            target.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1, 1));
            if (!player.level().isClientSide()) {
                SoulHearts.add(player);
                player.heal(2.0F);
            }
        }
    }
}
