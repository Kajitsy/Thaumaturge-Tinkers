package xyz.kajitsy.thaumaturgetinker.focus;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class TelekinesisFocus extends TinkererFocus {
    public TelekinesisFocus() {
        super("focus_telekinesis");
    }

    @Override
    public Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus) {
        return Map.of(TTAspects.AER, 5, TTAspects.PERDITIO, 5);
    }

    @Override
    public String getSortingHelperBase() {
        return "TELEKINESIS";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x9C00BE;
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Override
    public int[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new int[0];
    }

    @Override
    public void onUsingFocusTick(ItemStack wand, Player player, int count) {
        if (player.level().isClientSide()) return;
        var target = player.position().add(0, player.getBbHeight() / 2 + 0.5, 0);
        if (!player.isShiftKeyDown()) target = target.add(player.getLookAngle().scale(5));
        var items = player.level().getEntitiesOfClass(ItemEntity.class, new AABB(target.subtract(6, 6, 6), target.add(6, 6, 6)), ItemEntity::isAlive);
        if (items.isEmpty() || !consumeVis(wand, player)) return;
        for (var item : items) {
            var delta = target.subtract(item.position().add(0, item.getBbHeight() / 2, 0));
            if (delta.length() > 1) delta = delta.normalize();
            item.setDeltaMovement(delta.scale(0.3333));
            item.hurtMarked = true;
        }
    }
}
