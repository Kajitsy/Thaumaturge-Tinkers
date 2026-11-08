package xyz.kajitsy.thaumaturgetinker.mixin;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ServerGamePacketListenerImpl.class)
public interface FlightConnectionAccessor {
    @Accessor("aboveGroundTickCount") void thaumaturgetinker$resetFloatingTicks(int count);
}
