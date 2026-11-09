package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.network.SoulHearts;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.registries.DeferredItem;

public final class AwakenedSword extends IchorSword implements AdvancedTool {
    public static final DeferredItem<AwakenedSword> ITEM = KamiRegistries.ITEMS.register("ichor_sword_gem", AwakenedSword::new);

    public AwakenedSword() {
        super("ichor_sword_gem");
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public String toolType() {
        return "sword";
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        var result = tryToggleMode(level, p, p.getItemInHand(hand));
        return result.getResult() != InteractionResult.PASS ? result : super.use(level, p, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flags) {
        super.appendHoverText(stack, context, tooltipComponents, flags);
        tooltipComponents.add(AdvancedToolModes.description("sword", stack));
    }

    private boolean attackingGroup;

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (!attackingGroup && target instanceof LivingEntity living && living.hurtTime == 0 && living.isAlive()) {
            int mode = AdvancedToolModes.get(stack);
            if (mode == 1) {
                var box = new AABB(target.getX() - 3, target.getY() - 3, target.getZ() - 3, target.getX() + 3, target.getY() + 3, target.getZ() + 3);
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
                living.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1, 1));
                if (!player.level().isClientSide()) {
                    SoulHearts.add(player);
                    player.heal(2.0F);
                }
            }
        }
        return super.onLeftClickEntity(stack, player, target);
    }
}
