package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;

import java.util.List;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Static event handlers for {@link KamiArmor}.
 * <p>
 * Kept in a dedicated class because NeoForge does not allow an annotated subscriber's supertype to declare
 * {@code @SubscribeEvent} methods, and the awakened armor pieces extend {@link KamiArmor}.
 */
@EventBusSubscriber(modid = "thaumaturgetinker")
public final class KamiArmorEvents {
    private KamiArmorEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!KamiStartupConfig.enabled()) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        float totalRatio = 0.0f;
        boolean hasKami = false;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof KamiArmor kami) {
                hasKami = true;
                totalRatio += kami.getDefense() * 0.0425f;
            } else if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                double[] defense = new double[1];
                stack.forEachModifier(slot, (attr, mod) -> {
                    if (attr.equals(Attributes.ARMOR)) {
                        defense[0] += mod.amount();
                    }
                });
                totalRatio += (float) (defense[0] / 25.0);
            }
        }

        if (hasKami && totalRatio > 0.0f) {
            float cappedRatio = Math.min(1.0f, totalRatio);
            float originalDamage = event.getOriginalDamage();
            if (originalDamage > 0.0f) {
                float targetDamage = Math.max(0.0f, originalDamage * (1.0f - cappedRatio));
                event.setNewDamage(Math.min(event.getNewDamage(), targetDamage));
            }
        }
    }
}
