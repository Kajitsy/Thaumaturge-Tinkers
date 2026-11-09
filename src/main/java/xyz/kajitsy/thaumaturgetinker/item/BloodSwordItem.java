package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = "thaumaturgetinker")
public final class BloodSwordItem extends Item {
    public static final ToolMaterial MATERIAL = new ToolMaterial(
        BlockTags.INCORRECT_FOR_WOODEN_TOOL,
        950,
        0,
        0,
        22,
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/iron"))
    );
    public static final Set<UUID> SELF_DAMAGE = new HashSet<>();

    public BloodSwordItem() {
        super(ThaumicTinkererModern.itemProps("blood_sword", new Properties()
            .sword(MATERIAL, 10.0F, -2.4F)
            .attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 10, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "blood_sword_speed"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                .build()
            )
        ));
    }

    public static boolean active(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBooleanOr("Activated", false);
    }

    public static void toggle(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("Activated", !tag.getBooleanOr("Activated", false)));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            toggle(stack);
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable(active(stack) ? "ttmisc.bloodSword.activateEssentiaHarvest" : "ttmisc.bloodSword.deactivateEssentiaHarvest"));
            }
            return InteractionResult.SUCCESS;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    private static void pay(LivingEntity entity, float amount) {
        if (!SELF_DAMAGE.add(entity.getUUID())) return;
        try {
            if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                entity.hurtServer(serverLevel, entity.damageSources().fellOutOfWorld(), amount);
            }
        } finally {
            SELF_DAMAGE.remove(entity.getUUID());
        }
    }

    @SubscribeEvent
    public static void incoming(LivingIncomingDamageEvent event) {
        var victim = event.getEntity();
        if (victim.level().isClientSide() || SELF_DAMAGE.contains(victim.getUUID())) return;
        if (victim instanceof Player && victim.isUsingItem() && victim.getUseItem().getItem() instanceof BloodSwordItem) {
            event.setCanceled(true);
            pay(victim, 3);
        }
        if (event.getSource().getDirectEntity() instanceof LivingEntity attacker && attacker.getMainHandItem().getItem() instanceof BloodSwordItem && !SELF_DAMAGE.contains(attacker.getUUID())) {
            pay(attacker, 2);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void drops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide() || !event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof Player player)) return;
        var held = player.getMainHandItem();
        if (!(held.getItem() instanceof BloodSwordItem) || !active(held)) return;
        var aspects = MobAspects.MOBS.get(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString());
        if (aspects == null) return;
        event.getDrops().clear();
        for (var aspect : aspects) {
            var drop = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), MobAspects.stack(aspect, 0));
            drop.setPickUpDelay(10);
            event.getDrops().add(drop);
        }
    }
}
