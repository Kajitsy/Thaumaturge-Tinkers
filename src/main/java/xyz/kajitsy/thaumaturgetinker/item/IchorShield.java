package xyz.kajitsy.thaumaturgetinker.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class IchorShield extends ShieldItem {
    public IchorShield() {
        this("ichor_shield");
    }

    public IchorShield(String name) {
        this(name, 0.1F, 0.5F, 120.0F);
    }

    protected IchorShield(String name, float blockDelaySeconds, float disableCooldownScale, float blockingAngle) {
        super(KamiTools.properties(name)
                .stacksTo(1)
                .component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
                .equippableUnswappable(EquipmentSlot.OFFHAND)
                .delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> new BlocksAttacks(
                        blockDelaySeconds,
                        disableCooldownScale,
                        List.of(new BlocksAttacks.DamageReduction(blockingAngle, Optional.empty(), 0.0F, 1.0F)),
                        new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                        Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                        Optional.of(SoundEvents.SHIELD_BLOCK),
                        Optional.of(SoundEvents.SHIELD_BREAK)
                ))
                .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
