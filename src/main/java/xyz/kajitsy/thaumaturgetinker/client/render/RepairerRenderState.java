package xyz.kajitsy.thaumaturgetinker.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jspecify.annotations.Nullable;

public class RepairerRenderState extends BlockEntityRenderState {
    public @Nullable ItemStackRenderState item;
    public float ticks;
}
