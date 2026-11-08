package xyz.kajitsy.thaumaturgetinker.focus;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EnderChestFocus extends TinkererFocus {
    public EnderChestFocus() {
        super("focus_ender_chest");
    }

    @Override
    public Map<ResourceKey<IAspect>, Integer> getVisCost(ItemStack focus) {
        return Map.of(TTAspects.ORDO, 100, TTAspects.PERDITIO, 100);
    }

    @Override
    public String getSortingHelperBase() {
        return "ENDERCHEST";
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x132223;
    }

    @Override
    public int[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new int[]{2};
    }

    @Override
    public InteractionResult onCast(ItemStack wand, Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (consumeVis(wand, player)) {
                player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> ChestMenu.threeRows(id, inventory, p.getEnderChestInventory()),
                    Component.translatable("container.enderchest")
                ));
                level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }
}
