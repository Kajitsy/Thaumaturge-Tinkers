package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredItem;

public final class Protoclay extends Item {
    public static final DeferredItem<Protoclay> ITEM = KamiRegistries.ITEMS.register("protoclay", Protoclay::new);

    public Protoclay() {
        super(ThaumicTinkererModern.itemProps("protoclay", new Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }

    public static void init() {}

    public static String target(BlockState state) {
        for (String type : new String[]{"pick", "shovel", "axe"}) {
            if (AdvancedToolMining.material(state, type)) return type;
        }
        return "";
    }

    public static void update(Player player) {
        var current = player.getMainHandItem();
        if (!(current.getItem() instanceof AdvancedTool tool) || tool.toolType().equals("sword") || !player.swinging) return;
        var eye = player.getEyePosition();
        var hit = player.level().clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(player.blockInteractionRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
        if (hit.getType() != HitResult.Type.BLOCK) return;
        String desired = target(player.level().getBlockState(hit.getBlockPos()));
        if (desired.isEmpty() || desired.equals(tool.toolType())) return;
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            var stack = inventory.getItem(slot);
            if (stack == current || !(stack.getItem() instanceof AdvancedTool candidate) || !candidate.toolType().equals(desired)) continue;
            inventory.setItem(inventory.getSelectedSlot(), stack);
            inventory.setItem(slot, current);
            break;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof Player player) {
            update(player);
        }
    }
}
