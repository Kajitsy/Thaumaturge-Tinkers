package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.KamiStartupConfig;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.registries.DeferredItem;

@EventBusSubscriber(modid = "thaumaturgetinker")
public class KamiArmor extends Item implements IVisDiscountGear {
    public static final ResourceKey<EquipmentAsset> ASSET_ICHOR = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath("thaumaturgetinker", "ichor"));
    public static final ResourceKey<EquipmentAsset> ASSET_ICHORGEM = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath("thaumaturgetinker", "ichorgem"));

    public static final ArmorMaterial MATERIAL = new ArmorMaterial(
        0,
        Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 8, ArmorType.HELMET, 3),
        20,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("thaumaturgetinker", "ichor_cloth")),
        ASSET_ICHOR
    );

    public static final ArmorMaterial GEM_MATERIAL = new ArmorMaterial(
        0,
        Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 8, ArmorType.HELMET, 3),
        20,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("thaumaturgetinker", "ichor_cloth")),
        ASSET_ICHORGEM
    );

    public static final List<ArmorType> TYPES = List.of(ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS);
    public static final List<String> IDS = List.of("ichorcloth_helm", "ichorcloth_chest", "ichorcloth_legs", "ichorcloth_boots");
    public static final List<DeferredItem<KamiArmor>> ITEMS = new ArrayList<>();

    static {
        for (int i = 0; i < 4; i++) {
            final ArmorType type = TYPES.get(i);
            final String id = IDS.get(i);
            ITEMS.add(KamiRegistries.ITEMS.register(id, () -> new KamiArmor(id, type)));
        }
    }

    public final ArmorType armorType;
    public final ArmorMaterial armorMaterial;

    public KamiArmor(String name, ArmorType type) {
        this(name, type, MATERIAL);
    }

    public KamiArmor(String name, ArmorType type, ArmorMaterial material) {
        this(name, type, material, new Properties());
    }

    public KamiArmor(String name, ArmorType type, ArmorMaterial material, Properties properties) {
        super(ThaumicTinkererModern.itemProps(name, properties
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .humanoidArmor(material, type)
            .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)));
        this.armorType = type;
        this.armorMaterial = material;
    }

    public int getDefense() {
        return armorMaterial.defense().getOrDefault(armorType, 0);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return armorType == ArmorType.BOOTS ? 3 : 4;
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
        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
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
            float originalDamage = event.getContainer().getOriginalDamage();
            if (originalDamage > 0.0f) {
                float targetDamage = Math.max(0.0f, originalDamage * (1.0f - cappedRatio));
                event.setNewDamage(Math.min(event.getNewDamage(), targetDamage));
            }
        }
    }
}
