package xyz.kajitsy.thaumaturgetinker.compat;

import xyz.kajitsy.thaumaturgetinker.block.AnimationTabletBlock;
import xyz.kajitsy.thaumaturgetinker.block.MagnetBlock;
import xyz.kajitsy.thaumaturgetinker.block.RepairerBlock;
import xyz.kajitsy.thaumaturgetinker.block.TransvectorBlock;
import xyz.kajitsy.thaumaturgetinker.block.WarpGateBlock;
import xyz.kajitsy.thaumaturgetinker.block.entity.AnimationTabletEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.MagnetEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.RepairerEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.TransvectorEntity;
import xyz.kajitsy.thaumaturgetinker.block.entity.WarpGateEntity;
import xyz.kajitsy.thaumaturgetinker.item.SoulMouldItem;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin("thaumaturgetinker")
public final class JadePlugin implements IWailaPlugin {
    public static final String DATA = "thaumaturgetinker:device_tooltip";
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "devices");

    @Override
    public void register(IWailaCommonRegistration registration) {
        for (var type : List.of(AnimationTabletEntity.class, TransvectorEntity.class, RepairerEntity.class, WarpGateEntity.class, MagnetEntity.class)) {
            registration.registerBlockDataProvider(ServerProvider.INSTANCE, type);
        }
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        for (var type : List.of(AnimationTabletBlock.class, TransvectorBlock.class, RepairerBlock.class, WarpGateBlock.class, MagnetBlock.class)) {
            registration.registerBlockComponent(ClientProvider.INSTANCE, type);
        }
    }

    public static Component text(String key, Object... args) {
        return Component.translatable("ttwaila." + key, args);
    }

    /** Original Waila information, generated from authoritative device state. */
    public static List<Component> lines(BlockEntity tile) {
        var lines = new ArrayList<Component>();
        if (tile instanceof AnimationTabletEntity tablet) {
            var stack = tablet.inventory.getItem(0);
            lines.add(text("currentTool", stack.isEmpty() ? text("nothing") : stack.getHoverName()));
            if (!stack.isEmpty()) {
                lines.add(text(tablet.leftClick ? "leftClick" : "rightClick"));
                lines.add(text(tablet.redstone ? "redstone" : "autonomous"));
            }
        } else if (tile instanceof TransvectorEntity device) {
            var level = device.getLevel();
            var pos = device.target();
            BlockState target = null;
            if (level != null && pos != null && device.inRange(pos) && level.isLoaded(pos)) {
                var s = level.getBlockState(pos);
                if (!s.isAir()) target = s;
            }
            lines.add(text("connected", target == null ? text("nothing") : target.getBlock().getName()));
            if (target != null) lines.add(Component.literal("x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ()));
        } else if (tile instanceof RepairerEntity repairer) {
            var stack = repairer.inventory.getItem(0);
            if (!stack.isEmpty()) lines.add(text(stack.getDamageValue() > 0 ? "repairing" : "finishedRepairing", stack.getHoverName()));
        } else if (tile instanceof WarpGateEntity gate) {
            // Original provider inverted these labels; the actual gate rejects incoming when locked.
            lines.add(text(gate.locked ? "disallowIncoming" : "allowIncoming"));
        } else if (tile instanceof MagnetEntity magnet) {
            boolean pulling = magnet.getBlockState().getValue(MagnetBlock.PULLING);
            lines.add(text(pulling ? "pullingMode" : "pushingMode"));
            if (magnet.mobs()) {
                String key = (pulling ? "pulling" : "pushing") + (magnet.adult ? "Adult" : "Child");
                var filter = magnet.inventory.getStackInSlot(0);
                if (filter.isEmpty()) {
                    lines.add(text(key));
                } else {
                    var id = ResourceLocation.tryParse(SoulMouldItem.pattern(filter));
                    var type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
                    lines.add(text(key + "Type", type == null ? Component.translatable("ttmisc.soulMould.nonAssigned") : type.getDescription()));
                }
            }
        }
        return List.copyOf(lines);
    }

    public enum ServerProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override public ResourceLocation getUid() { return UID; }

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
            var ops = accessor.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            tag.put(DATA, ComponentSerialization.CODEC.listOf().encodeStart(ops, lines(accessor.getBlockEntity())).getOrThrow());
        }
    }

    public enum ClientProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override public ResourceLocation getUid() { return UID; }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var data = accessor.getServerData().get(DATA);
            if (data == null) return;
            var ops = accessor.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            ComponentSerialization.CODEC.listOf().parse(ops, data).result().ifPresent(l -> l.forEach(tooltip::add));
        }
    }
}
