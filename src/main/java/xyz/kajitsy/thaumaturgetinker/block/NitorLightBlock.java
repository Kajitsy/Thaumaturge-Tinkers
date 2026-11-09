package xyz.kajitsy.thaumaturgetinker.block;

import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;
import xyz.kajitsy.thaumaturgetinker.item.AwakenedLegs;


import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class NitorLightBlock extends Block {
    public static final BooleanProperty AWAKENED = BooleanProperty.create("awakened");

    public NitorLightBlock() {
        super(ThaumicTinkererModern.blockProps("nitor_light", Properties.of().replaceable().noCollission().noOcclusion().noLootTable().lightLevel(s -> s.getValue(AWAKENED) ? 15 : 12)));
        registerDefaultState(stateDefinition.any().setValue(AWAKENED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AWAKENED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (!level.isClientSide()) level.scheduleTick(pos, this, delay(level));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < .03f) {
            level.addParticle(new SparkleParticleOptions(4, 1.0f, 0, 0.05f, 0f, 10, true), pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 0, 0, 0);
        }
    }

    private static int delay(Level level) {
        return level.dimension() == Level.NETHER ? 60 : 20;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean owner = false;
        double range = state.getValue(AWAKENED) ? 6 : 1;
        for (var player : level.getEntitiesOfClass(Player.class, new AABB(pos.getX() - range, pos.getY() - range, pos.getZ() - range, pos.getX() + range, pos.getY() + range, pos.getZ() + range))) {
            if (player.getInventory().contains(stack -> stack.is(ThaumicTinkererModern.BRIGHT_NITOR.get())) || player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof AwakenedLegs) {
                owner = true;
                break;
            }
        }
        if (!owner) level.removeBlock(pos, false);
        else level.scheduleTick(pos, this, delay(level));
    }
}
