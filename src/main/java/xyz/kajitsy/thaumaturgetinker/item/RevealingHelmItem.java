package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class RevealingHelmItem extends Item implements IVisDiscountGear {
    public static final ResourceKey<EquipmentAsset> ASSET_REVEALING = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath("thaumaturgetinker", "revealing"));
    public static final ArmorMaterial MATERIAL = new ArmorMaterial(
        25,
        Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 2),
        25,
        SoundEvents.ARMOR_EQUIP_IRON,
        1.0F,
        0.0F,
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/thaumium")),
        ASSET_REVEALING
    );

    public RevealingHelmItem() {
        super(ThaumicTinkererModern.itemProps("revealing_helm", new Properties().humanoidArmor(MATERIAL, ArmorType.HELMET).durability(500).rarity(Rarity.UNCOMMON).component(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE)));
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return 5;
    }
}
