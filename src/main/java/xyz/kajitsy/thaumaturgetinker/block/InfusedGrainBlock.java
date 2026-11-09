package xyz.kajitsy.thaumaturgetinker.block;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.entity.InfusedFarmlandEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.InfusedGrainEntity;
import xyz.kajitsy.thaumaturgetinker.potion.InfusedCropHarvest;
import xyz.kajitsy.thaumaturgetinker.potion.InfusedCropLoot;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;

/** Crop growth runs every loaded server tick through its entity, never vanilla random growth. */
public final class InfusedGrainBlock extends CropBlock implements EntityBlock {
    public static final IntegerProperty ASPECT = IntegerProperty.create("aspect", 0, 6);
    public static final MapCodec<InfusedGrainBlock> CODEC = simpleCodec(p -> new InfusedGrainBlock());

    public InfusedGrainBlock() {
        super(ThaumicTinkererModern.blockProps("infused_grain", Properties.ofFullCopy(Blocks.WHEAT)));
        registerDefaultState(defaultBlockState().setValue(ASPECT, 6));
    }

    @Override
    public MapCodec<InfusedGrainBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ASPECT);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getBlock() instanceof InfusedFarmlandBlock;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfusedGrainEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide() && type == ThaumicTinkererModern.INFUSED_GRAIN_ENTITY.get()
                ? (l, p, s, t) -> InfusedGrainEntity.tick((ServerLevel) l, p, s, (InfusedGrainEntity) t)
                : null;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ThaumicTinkererModern.INFUSED_SEEDS.get();
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        var stack = new ItemStack(ThaumicTinkererModern.INFUSED_SEEDS.get());
        if (level.getBlockEntity(pos) instanceof InfusedGrainEntity crop) crop.data().write(stack);
        return stack;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            if (server.getBlockEntity(pos) instanceof InfusedGrainEntity crop) {
                var soil = server.getBlockEntity(pos.below()) instanceof InfusedFarmlandEntity farmland ? farmland : null;
                var loot = InfusedCropLoot.get(server);
                var drops = InfusedCropHarvest.collect(server.registryAccess(), crop.data(), state.getValue(AGE), soil == null ? null : soil.aspects(), server.getRandom(), a -> {
                    if (soil != null) soil.fertilize(a);
                }, a -> loot.roll(a, server.getRandom()));
                for (var stack : drops) {
                    var entity = new ItemEntity(server, pos.getX() + .15 + server.getRandom().nextFloat() * .7, pos.getY() + .15 + server.getRandom().nextFloat() * .7, pos.getZ() + .15 + server.getRandom().nextFloat() * .7, stack);
                    entity.setPickUpDelay(10);
                    server.addFreshEntity(entity);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static int texture(ResourceKey<IAspect> aspect) {
        if (aspect == null) return 6;
        for (int i = 0; i < TTAspects.PRIMALS.size(); i++) {
            if (aspect.equals(TTAspects.PRIMALS.get(i))) return i;
        }
        return 6;
    }
}
