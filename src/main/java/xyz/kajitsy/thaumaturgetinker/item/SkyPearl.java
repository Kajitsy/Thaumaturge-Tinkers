package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.SkyPearlData;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.WarpGateBlock;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;

/** Build164 pearl: bind once to a gate, sneak-use in air to clear only its y sentinel. */
public final class SkyPearl extends Item {
    public static final DeferredItem<SkyPearl> ITEM = KamiRegistries.ITEMS.register("sky_pearl", SkyPearl::new);
    public static BiConsumer<ItemStack, Consumer<Component>> CLIENT_TOOLTIP = (s, b) -> {};

    public SkyPearl() {
        super(ThaumicTinkererModern.itemProps("sky_pearl", new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return SkyPearlData.attuned(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var stack = context.getItemInHand();
        if (!SkyPearlData.attuned(stack) && level.getBlockState(context.getClickedPos()).is(WarpGateBlock.BLOCK.get())) {
            SkyPearlData.bind(stack, context.getClickedPos(), level.dimension().location());
            sound(level, context.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && SkyPearlData.attuned(stack)) {
            sound(level, player);
            SkyPearlData.reset(stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    private static void sound(Level level, Player player) {
        if (player != null) {
            level.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 0.1f);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipComponents, flag);
        if (!SkyPearlData.attuned(stack)) return;
        var p = SkyPearlData.position(stack);
        tooltipComponents.add(Component.literal("X: " + p.getX()));
        tooltipComponents.add(Component.literal("Y: " + p.getY()));
        tooltipComponents.add(Component.literal("Z: " + p.getZ()));
        CLIENT_TOOLTIP.accept(stack, tooltipComponents::add);
    }
}
