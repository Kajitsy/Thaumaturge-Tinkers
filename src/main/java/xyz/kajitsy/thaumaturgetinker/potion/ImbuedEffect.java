package xyz.kajitsy.thaumaturgetinker.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class ImbuedEffect extends MobEffect {
    public ImbuedEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    public ImbuedEffect() {
        this(0);
    }
}
