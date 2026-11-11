package xyz.kajitsy.thaumaturgetinker.block;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;
import xyz.kajitsy.thaumaturgetinker.ThaumicTinkererModern;

@EventBusSubscriber(modid = ThaumicTinkererModern.ID)
public final class InfusedFireAttachment {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ThaumicTinkererModern.ID);

    public static final Supplier<AttachmentType<Integer>> TYPE = ATTACHMENTS.register(
        "infused_fire",
        () -> AttachmentType.builder(() -> -1)
            .serialize(Codec.INT, kind -> kind >= 0)
            .sync(ByteBufCodecs.VAR_INT)
            .build()
    );

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }

    public static @Nullable InfusedFireKind get(Entity entity) {
        if (!entity.hasData(TYPE)) return null;
        int ordinal = entity.getData(TYPE);
        InfusedFireKind[] kinds = InfusedFireKind.values();
        return ordinal >= 0 && ordinal < kinds.length ? kinds[ordinal] : null;
    }

    public static void set(Entity entity, InfusedFireKind kind) {
        int ordinal = kind.ordinal();
        if (!entity.hasData(TYPE) || entity.getData(TYPE) != ordinal) {
            entity.setData(TYPE, ordinal);
        }
    }

    public static void clear(Entity entity) {
        if (entity.hasData(TYPE) && entity.getData(TYPE) != -1) {
            entity.setData(TYPE, -1);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (get(entity) != null && (entity.getRemainingFireTicks() <= 0 || entity.isInLava())) {
            clear(entity);
        }
    }
}
