package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.mixin.ExperienceOrbAccessor;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class XPTalismanItem extends Item {
    public XPTalismanItem() {
        super(ThaumicTinkererModern.itemProps("xp_talisman", new Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    }

    public static int xp(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("xp"), 0, 1500);
    }

    public static boolean enabled(ItemStack stack) {
        return CleansingTalismanItem.enabled(stack);
    }

    public static void xp(ItemStack stack, int value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putInt("xp", Math.clamp(value, 0, 1500)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            CleansingTalismanItem.toggle(stack);
            if (!level.isClientSide()) {
                level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, .3f, .1f);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (!level.isClientSide()) {
            if (!bottle(stack, player)) {
                drainToPlayer(stack, player);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof LivingEntity living) onWornTick(stack, living);
    }

    public static boolean drainToPlayer(ItemStack stack, Player player) {
        int stored = xp(stack);
        if (stored <= 0) return false;
        int toGive = Math.min(stored, 30);
        xp(stack, stored - toGive);
        player.giveExperiencePoints(toGive);
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, .1f, .1f + player.getRandom().nextFloat() * .5f);
        fx(player, player.getX(), player.getY(), player.getZ());
        return true;
    }

    public static boolean bottle(ItemStack stack, Player player) {
        if (xp(stack) < 10) return false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var empty = player.getInventory().getItem(slot);
            if (!empty.is(Items.GLASS_BOTTLE)) continue;
            empty.shrink(1);
            xp(stack, xp(stack) - 10);
            var bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
            player.getInventory().placeItemBackInInventory(bottle);
            player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, .1f, .1f + player.getRandom().nextFloat() * .5f);
            fx(player, player.getX(), player.getY(), player.getZ());
            return true;
        }
        return false;
    }

    public void onWornTick(ItemStack stack, LivingEntity entity) {
        if (entity.level().isClientSide() || !enabled(stack)) return;
        if (xp(stack) >= 1500) {
            CleansingTalismanItem.setEnabled(stack, false);
            return;
        }
        var pos = entity.position();
        var box = new AABB(pos.x - 3, pos.y - 3, pos.z - 3, pos.x + 3, pos.y + 3, pos.z + 3);
        for (var orb : entity.level().getEntitiesOfClass(ExperienceOrb.class, box, o -> o.isAlive())) {
            var access = (ExperienceOrbAccessor) orb;
            int value = orb.getValue();
            if (value <= 0) continue;
            int count = Math.min(access.thaumaturgetinker$count(), (1500 - xp(stack)) / value);
            if (count <= 0) continue;
            xp(stack, xp(stack) + count * value);
            int left = access.thaumaturgetinker$count() - count;
            orb.level().playSound(null, orb.getX(), orb.getY(), orb.getZ(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, value / 10f, 1f);
            fx(entity, orb.getX(), orb.getY(), orb.getZ());
            if (left == 0) orb.discard();
            else access.thaumaturgetinker$count(left);
            if (xp(stack) >= 1500) break;
        }
    }

    private static void fx(LivingEntity entity, double x, double y, double z) {
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel level) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT, x, y, z, 4, 0.1, 0.1, 0.1, 0.05);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.literal("XP: " + xp(stack) + " / 1500"));
        tooltipComponents.add(Component.translatable(xp(stack) >= 1500 ? "ttmisc.full" : enabled(stack) ? "ttmisc.absorbing" : "ttmisc.notAbsorbing"));
    }
}
