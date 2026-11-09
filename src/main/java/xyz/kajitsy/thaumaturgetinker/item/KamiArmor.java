package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.registries.DeferredItem;

@EventBusSubscriber(modid = "thaumaturgetinker")
public class KamiArmor extends ArmorItem implements IVisDiscountGear {
    public static final Holder<ArmorMaterial> MATERIAL = ThaumicTinkererModern.ARMOR_ICHOR;
    public static final Holder<ArmorMaterial> GEM_MATERIAL = ThaumicTinkererModern.ARMOR_ICHORGEM;

    public static final List<ArmorItem.Type> TYPES = List.of(ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS);
    public static final List<String> IDS = List.of("ichorcloth_helm", "ichorcloth_chest", "ichorcloth_legs", "ichorcloth_boots");
    public static final List<DeferredItem<KamiArmor>> ITEMS = new ArrayList<>();

    static {
        for (int i = 0; i < 4; i++) {
            final ArmorItem.Type type = TYPES.get(i);
            final String id = IDS.get(i);
            ITEMS.add(KamiRegistries.ITEMS.register(id, () -> new KamiArmor(id, type)));
        }
    }

    public final ArmorItem.Type armorType;
    public final Holder<ArmorMaterial> armorMaterial;

    public KamiArmor(String name, ArmorItem.Type type) {
        this(name, type, MATERIAL);
    }

    public KamiArmor(String name, ArmorItem.Type type, Holder<ArmorMaterial> material) {
        this(name, type, material, new Properties());
    }

    public KamiArmor(String name, ArmorItem.Type type, Holder<ArmorMaterial> material, Properties properties) {
        super(material, type, ThaumicTinkererModern.itemProps(name, properties
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .component(DataComponents.UNBREAKABLE, new Unbreakable(true))));
        this.armorType = type;
        this.armorMaterial = material;
    }

    @Override
    public int getDefense() {
        return super.getDefense();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return armorType == ArmorItem.Type.BOOTS ? 3 : 4;
    }

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
