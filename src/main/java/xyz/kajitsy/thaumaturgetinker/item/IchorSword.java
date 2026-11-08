package xyz.kajitsy.thaumaturgetinker.item;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/** 1.7 swords reduce positive blockable damage immediately, before armor, from every direction. */
public class IchorSword extends Item {
    public IchorSword() {
        this("ichor_sword");
    }

    public IchorSword(String name) {
        super(KamiTools.properties(name).sword(KamiTools.TIER, 4.0F, -2.4F));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }


    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    public static float reduce(Player player, DamageSource source, float damage) {
        return damage > 0 && !source.is(DamageTypeTags.BYPASSES_ARMOR) && player.isUsingItem() && player.getUseItem().getItem() instanceof IchorSword ? (1 + damage) * 0.5f : damage;
    }
}
