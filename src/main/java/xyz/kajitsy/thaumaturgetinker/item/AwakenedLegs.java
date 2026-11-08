package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.network.AwakenedArmorStatus;


import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

@EventBusSubscriber(modid = "thaumaturgetinker")
public final class AwakenedLegs extends AwakenedArmorItem {
    public static final DeferredItem<AwakenedLegs> ITEM = KamiRegistries.ITEMS.register("ichorcloth_legs_gem", AwakenedLegs::new);

    public AwakenedLegs() {
        super("ichorcloth_legs_gem", ArmorType.LEGGINGS);
    }

    public static void init() {}

    public static boolean worn(Player player) {
        return player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof AwakenedLegs;
    }

    public static boolean active(Player player) {
        return worn(player) && !disabled(player.getItemBySlot(EquipmentSlot.LEGS)) && AwakenedArmorStatus.enabled(player);
    }

    public static List<BlockPos> lights(Player player) {
        int x = Mth.floor(player.getX()), y = (int) player.getY() + 1, z = Mth.floor(player.getZ());
        float yaw = Mth.wrapDegrees(player.getYRot() + 90) * (float) Math.PI / 180;
        double dx = Math.cos(yaw), dz = Math.sin(yaw), length = Math.sqrt(dx * dx + dz * dz);
        dx /= length;
        dz /= length;
        double vx = dx, vz = dz;
        var list = new ArrayList<BlockPos>();
        list.add(new BlockPos(x, y, z));
        for (int i = 0; i < 5; i++) {
            vx += dx;
            vz += dz;
            list.add(new BlockPos(x + (int) vx, y, z + (int) vz));
        }
        return list;
    }

    public static void update(Player player) {
        if (!active(player)) return;
        var old = player.getEffect(MobEffects.FIRE_RESISTANCE);
        int amplifier = old != null && old.getDuration() <= 202 ? old.getAmplifier() : 10;
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 202, amplifier, true, true));
        for (var pos : lights(player)) {
            BrightNitorItem.place(player.level(), pos, true);
        }
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            update(player);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (healFire(event.getEntity(), event.getSource(), event.getAmount())) {
            event.setCanceled(true);
        }
    }

    public static boolean healFire(LivingEntity entity, DamageSource source, float amount) {
        if (!entity.level().isClientSide() && entity instanceof Player player && active(player) && source.is(DamageTypeTags.IS_FIRE)) {
            player.heal(amount);
            return true;
        }
        return false;
    }

    public static List<ItemStack> components(List<ItemStack> original) {
        var result = new ArrayList<>(original);
        result.set(6, PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE));
        return result;
    }
}
