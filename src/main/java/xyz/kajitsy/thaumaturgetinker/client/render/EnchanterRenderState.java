package xyz.kajitsy.thaumaturgetinker.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jspecify.annotations.Nullable;

public class EnchanterRenderState extends BlockEntityRenderState {
    public @Nullable ItemStackRenderState tool;
    public @Nullable ItemStackRenderState wand;
    public float ticks;
}
