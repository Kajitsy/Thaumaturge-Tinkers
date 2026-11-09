package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;


import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredItem;

public final class BlockTalisman extends Item {
    public static final DeferredItem<BlockTalisman> ITEM = KamiRegistries.ITEMS.register("block_talisman", BlockTalisman::new);

    public BlockTalisman() {
        super(ThaumicTinkererModern.itemProps("block_talisman", new Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }

    public static CompoundTag data(ItemStack s) {
        return s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static Block block(ItemStack s) {
        var id = Identifier.tryParse(data(s).getStringOr("blockName", ""));
        return id == null ? Blocks.AIR : BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.AIR);
    }

    public static int count(ItemStack s) {
        return Math.max(0, data(s).getIntOr("blockCount", 0));
    }

    public static boolean active(ItemStack s) {
        return data(s).getBooleanOr("active", false);
    }

    public static void count(ItemStack s, int value) {
        CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.putInt("blockCount", Math.max(0, value)));
    }

    public static int remove(ItemStack s, int amount) {
        int take = Math.min(count(s), Math.max(0, amount));
        count(s, count(s) - take);
        return take;
    }

    public static boolean bind(ItemStack s, Block b) {
        if (block(s) != Blocks.AIR && count(s) > 0) return false;
        CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.putString("blockName", BuiltInRegistries.BLOCK.getKey(b).toString()));
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        var s = p.getItemInHand(hand);
        if (p.isShiftKeyDown() && block(s) != Blocks.AIR) {
            CustomData.update(DataComponents.CUSTOM_DATA, s, t -> t.putBoolean("active", !active(s)));
            if (!level.isClientSide()) {
                level.playSound(null, p.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.3f, 0.1f);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var s = context.getItemInHand();
        var p = context.getPlayer();
        var level = context.getLevel();
        if (p == null) return InteractionResult.PASS;
        if (!level.mayInteract(p, context.getClickedPos()) || !p.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), s)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (bind(s, level.getBlockState(context.getClickedPos()).getBlock())) {
            return InteractionResult.CONSUME;
        }
        var item = block(s).asItem();
        if (item == Items.AIR || count(s) == 0) return InteractionResult.PASS;
        var rawHandler = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK, context.getClickedPos(), context.getClickedFace());
        if (rawHandler != null) {
            try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                int inserted = rawHandler.insert(net.neoforged.neoforge.transfer.item.ItemResource.of(item), count(s), tx);
                if (inserted > 0) {
                    tx.commit();
                    remove(s, inserted);
                }
            }
            return InteractionResult.CONSUME;
        }
        var placed = new ItemStack(item);
        var result = placed.useOn(new UseOnContext(level, p, context.getHand(), placed, new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside())));
        if (result.consumesAction()) {
            remove(s, 1);
        }
        return result;
    }

    @Override
    public void inventoryTick(ItemStack s, ServerLevel level, net.minecraft.world.entity.Entity entity, EquipmentSlot slot) {
        if (entity instanceof Player player) {
            handleTick(s, player);
        }
    }

    public void onWornTick(ItemStack s, LivingEntity wearer) {
        if (!wearer.level().isClientSide() && wearer instanceof Player player) {
            handleTick(s, player);
        }
    }

    public static void handleTick(ItemStack s, Player p) {
        if (!active(s)) return;
        var level = p.level();
        var boundBlock = block(s);
        var boundItem = boundBlock.asItem();

        var area = p.getBoundingBox().inflate(6.0);
        var droppedItems = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area, e -> e.isAlive() && !e.hasPickUpDelay());
        for (var itemEntity : droppedItems) {
            var itemStack = itemEntity.getItem();
            var target = p.position().add(0, p.getBbHeight() / 2.0, 0);
            var delta = target.subtract(itemEntity.position());
            if (delta.lengthSqr() > 0.01) {
                itemEntity.setDeltaMovement(delta.normalize().scale(0.35));
                itemEntity.hurtMarked = true;
            }

            if (boundItem != Items.AIR && itemStack.is(boundItem)) {
                if (itemEntity.distanceToSqr(p) < 2.25 || p.getBoundingBox().inflate(0.5).intersects(itemEntity.getBoundingBox())) {
                    long current = count(s);
                    int toAdd = itemStack.getCount();
                    if (current + toAdd <= Integer.MAX_VALUE) {
                        count(s, (int) (current + toAdd));
                        p.take(itemEntity, toAdd);
                        itemEntity.discard();
                    }
                }
            }
        }

        if (boundItem == Items.AIR) return;
        var inv = p.getInventory();
        int highest = -1;
        boolean free = false;
        for (int i = 0; i < 36; i++) {
            var stack = inv.getItem(i);
            if (stack.isEmpty()) {
                free = true;
                continue;
            }
            if (stack.is(boundItem) && (highest == -1 || highest > 8 && stack.getCount() > inv.getItem(highest).getCount())) {
                highest = i;
            }
        }
        if (highest == -1) {
            var held = p.getMainHandItem();
            if (free && (held.isEmpty() || held.is(boundItem))) {
                int amount = Math.min(64, count(s));
                if (amount > 0) {
                    var stack = new ItemStack(boundItem, amount);
                    inv.add(stack);
                    remove(s, amount - stack.getCount());
                }
            }
        } else {
            long stored = count(s);
            for (int i = 0; i < 36; i++) {
                var stack = inv.getItem(i);
                if (i == highest || !stack.is(boundItem)) continue;
                if (stored + stack.getCount() > Integer.MAX_VALUE) continue;
                stored += stack.getCount();
                inv.setItem(i, ItemStack.EMPTY);
            }
            count(s, (int) stored);
            var stack = inv.getItem(highest);
            stack.grow(remove(s, stack.getMaxStackSize() - stack.getCount()));
        }
        inv.setChanged();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack s, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> builder, TooltipFlag flag) {
        if (block(s) != Blocks.AIR) {
            builder.accept(Component.translatable(block(s).getDescriptionId()).append(" (x" + count(s) + ")"));
        }
        builder.accept(Component.translatable(active(s) ? "ttmisc.active" : "ttmisc.inactive"));
    }
}
