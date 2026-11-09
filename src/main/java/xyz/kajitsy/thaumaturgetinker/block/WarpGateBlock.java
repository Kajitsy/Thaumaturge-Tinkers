package xyz.kajitsy.thaumaturgetinker.block;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.entity.WarpGateEntity;
import xyz.kajitsy.thaumaturgetinker.item.SkyPearl;
import xyz.kajitsy.thaumaturgetinker.menu.WarpGateMenu;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class WarpGateBlock extends BaseEntityBlock {
    public static final net.neoforged.neoforge.registries.DeferredBlock<WarpGateBlock> BLOCK = KamiRegistries.BLOCKS.register("warp_gate", WarpGateBlock::new);
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.BlockItem> ITEM = KamiRegistries.ITEMS.register("warp_gate", () -> new net.minecraft.world.item.BlockItem(BLOCK.get(), ThaumicTinkererModern.itemProps("warp_gate", new net.minecraft.world.item.Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC))));
    public static final java.util.function.Supplier<BlockEntityType<WarpGateEntity>> ENTITY = KamiRegistries.ENTITIES.register("warp_gate", () -> BlockEntityType.Builder.of(WarpGateEntity::new, BLOCK.get()).build(null));
    public static final com.mojang.serialization.MapCodec<WarpGateBlock> CODEC = simpleCodec(p -> new WarpGateBlock());
    public static Consumer<WarpGateEntity> CLIENT_TICKER = g -> {};

    public WarpGateBlock() {
        super(ThaumicTinkererModern.blockProps("warp_gate", Properties.ofFullCopy(Blocks.STONE).strength(5, 2000).noOcclusion()));
    }

    @Override protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new WarpGateEntity(pos, state); }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WarpGateEntity gate && level.mayInteract(player, pos)) {
            player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, p) -> new WarpGateMenu(id, inv, gate), net.minecraft.network.chat.Component.translatable("block.thaumaturgetinker.warp_gate")), buf -> buf.writeBlockPos(pos));
        }
        return level.isClientSide() ? net.minecraft.world.InteractionResult.SUCCESS : net.minecraft.world.InteractionResult.CONSUME;
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (stack.is(SkyPearl.ITEM.get())) return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        useWithoutItem(state, level, pos, player, hit);
        return level.isClientSide() ? net.minecraft.world.ItemInteractionResult.SUCCESS : net.minecraft.world.ItemInteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof WarpGateEntity gate) {
            gate.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, ENTITY.get(), (world, pos, block, gate) -> CLIENT_TICKER.accept(gate)) : null;
    }
}
