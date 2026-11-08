package xyz.kajitsy.thaumaturgetinker.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class AnimationTabletRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean leftClick;
    public float swingProgress;
    public float ticks;
    public @Nullable ItemStackRenderState item;
    public boolean isBlockItem;
}
