package xyz.kajitsy.thaumaturgetinker.block;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.block.entity.InfusedFarmlandEntity;


import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Original permanent, full-height farmland; hydrated appearance does not require nearby water. */
public final class InfusedFarmlandBlock extends FarmBlock implements EntityBlock {
    public static final MapCodec<FarmBlock> CODEC = simpleCodec(p -> new InfusedFarmlandBlock());

    public InfusedFarmlandBlock() {
        super(ThaumicTinkererModern.blockProps("infused_farmland", Properties.ofFullCopy(Blocks.FARMLAND).strength(.6f)));
    }

    @Override
    public MapCodec<FarmBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float distance) {}

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction direction, BlockState plant) {
        return BuiltInRegistries.BLOCK.getKey(plant.getBlock()).equals(ResourceLocation.fromNamespaceAndPath(ThaumicTinkererModern.ID, "infused_grain"))
                ? TriState.TRUE
                : TriState.FALSE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfusedFarmlandEntity(pos, state);
    }
}
