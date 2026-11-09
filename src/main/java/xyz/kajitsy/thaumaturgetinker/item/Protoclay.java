package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = ThaumicTinkererModern.ID)
public final class Protoclay extends Item {
    public static final DeferredItem<Protoclay> ITEM = KamiRegistries.ITEMS.register("protoclay", Protoclay::new);

    public Protoclay() {
        super(ThaumicTinkererModern.itemProps("protoclay", new Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }

    public static String target(BlockState state) {
        if (state.is(Blocks.BEDROCK) || state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return "pick";
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) return "shovel";
        if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES) || state.is(BlockTags.SWORD_EFFICIENT)) return "axe";
        return "";
    }

    public static boolean hasProtoclay(Player player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(ITEM.get())) return true;
        }
        return false;
    }

    public static void switchTool(Player player, BlockPos pos) {
        var current = player.getMainHandItem();
        if (!(current.getItem() instanceof AdvancedTool tool) || tool.toolType().equals("sword")) return;
        String desired = target(player.level().getBlockState(pos));
        if (desired.isEmpty() || desired.equals(tool.toolType())) return;
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            var stack = inventory.getItem(slot);
            if (stack == current || !(stack.getItem() instanceof AdvancedTool candidate) || !candidate.toolType().equals(desired)) continue;
            inventory.setItem(inventory.getSelectedSlot(), stack);
            inventory.setItem(slot, current);
            inventory.setChanged();
            break;
        }
    }

    public static void update(Player player) {
        if (!player.swinging) return;
        var hit = AdvancedTool.raycast(player);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        switchTool(player, hit.getBlockPos());
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        var player = event.getEntity();
        if (hasProtoclay(player)) {
            switchTool(player, event.getPos());
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof Player player) {
            update(player);
        }
    }
}
