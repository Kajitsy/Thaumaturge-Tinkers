package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.network.AwakenedArmorStatus;

import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

@EventBusSubscriber(modid = "thaumaturgetinker")
public final class AwakenedHelm extends AwakenedArmorItem {
    public static final DeferredItem<AwakenedHelm> ITEM = KamiRegistries.ITEMS.register("ichorcloth_helm_gem", AwakenedHelm::new);

    public AwakenedHelm() {
        super("ichorcloth_helm_gem", ArmorItem.Type.HELMET, new Properties().component(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE));
    }

    public static boolean worn(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof AwakenedHelm;
    }

    public static boolean active(Player player) {
        return worn(player) && !disabled(player.getItemBySlot(EquipmentSlot.HEAD)) && AwakenedArmorStatus.enabled(player);
    }

    public boolean showNodes(ItemStack stack, LivingEntity entity) {
        return true;
    }

    public boolean showIngamePopups(ItemStack stack, LivingEntity entity) {
        return true;
    }

    public static void update(Player player) {
        boolean active = active(player);
        if (active) {
            refresh(player, MobEffects.NIGHT_VISION);
            if (player.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value())) {
                player.setAirSupply(300);
            }
            if (player.isEyeInFluidType(NeoForgeMod.LAVA_TYPE.value())) {
                player.setAirSupply(300);
            }
        } else {
            var old = player.getEffect(MobEffects.NIGHT_VISION);
            if (old != null && old.isAmbient()) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }

        if (worn(player)) {
            int food = player.getFoodData().getFoodLevel();
            if (food > 0 && food < 18 && player.getHealth() > 0 && player.getHealth() < player.getMaxHealth() && player.tickCount % 80 == 0) {
                player.heal(1);
            }
        }
    }

    public static final int NIGHT_VISION_DURATION = 300;
    public static final int NIGHT_VISION_REFRESH_THRESHOLD = 240;

    private static void refresh(Player player, Holder<MobEffect> effect) {
        var old = player.getEffect(effect);
        if (old == null || old.getDuration() <= NIGHT_VISION_REFRESH_THRESHOLD) {
            int amplifier = old != null && old.getDuration() <= NIGHT_VISION_DURATION ? old.getAmplifier() : 0;
            player.addEffect(new MobEffectInstance(effect, NIGHT_VISION_DURATION, amplifier, true, false));
        }
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            update(player);
        }
    }

    public static List<ItemStack> components(List<ItemStack> original) {
        var result = new ArrayList<>(original);
        result.set(6, PotionContents.createItemStack(Items.POTION, Potions.NIGHT_VISION));
        return result;
    }
}
