package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.SkyPearlData;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.WarpGateBlock;
import xyz.kajitsy.thaumaturgetinker.client.SkyPearlClient;


import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredItem;

/** Build164 pearl: bind once to a gate, sneak-use in air to clear only its y sentinel. */
public final class SkyPearl extends Item {
    public static final DeferredItem<SkyPearl> ITEM = KamiRegistries.ITEMS.register("sky_pearl", SkyPearl::new);

    public SkyPearl() {
        super(ThaumicTinkererModern.itemProps("sky_pearl", new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }

    public static void init() {}

    @Override
    public boolean isFoil(ItemStack stack) {
        return SkyPearlData.attuned(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var stack = context.getItemInHand();
        if (!SkyPearlData.attuned(stack) && level.getBlockState(context.getClickedPos()).is(WarpGateBlock.BLOCK.get())) {
            SkyPearlData.bind(stack, context.getClickedPos(), level.dimension().identifier());
            sound(level, context.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && SkyPearlData.attuned(stack)) {
            sound(level, player);
            SkyPearlData.reset(stack);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static void sound(Level level, Player player) {
        if (player != null) {
            level.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 0.1f);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        if (!SkyPearlData.attuned(stack)) return;
        var p = SkyPearlData.position(stack);
        builder.accept(Component.literal("X: " + p.getX()));
        builder.accept(Component.literal("Y: " + p.getY()));
        builder.accept(Component.literal("Z: " + p.getZ()));
        if (FMLEnvironment.getDist().isClient()) {
            SkyPearlClient.info(stack, builder);
        }
    }
}
