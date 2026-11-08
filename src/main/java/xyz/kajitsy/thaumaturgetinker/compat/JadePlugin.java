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
import xyz.kajitsy.thaumaturgetinker.client.DeviceTooltip;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;

@WailaPlugin("thaumaturgetinker")
public final class JadePlugin implements IWailaPlugin {
    public static boolean registered, clientRegistered;
    public static final String DATA = "thaumaturgetinker:device_tooltip";

    @Override
    public void register(IWailaCommonRegistration registration) {
        for (var type : List.of(AnimationTabletEntity.class, TransvectorEntity.class, RepairerEntity.class, WarpGateEntity.class, MagnetEntity.class)) {
            registration.registerBlockDataProvider(JadeServerDataProvider.INSTANCE, type);
        }
        registered = true;
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        clientRegistered = true;
        for (var type : List.of(AnimationTabletBlock.class, TransvectorBlock.class, RepairerBlock.class, WarpGateBlock.class, MagnetBlock.class)) {
            registration.registerBlockComponent(JadeClientComponentProvider.INSTANCE, type);
        }
    }

    public static void audit(ServerLevel world, BlockEntity tile) {
        if (!registered) throw new IllegalStateException("Jade did not discover addon plugin");
        var tag = new CompoundTag();
        tag.putInt("OtherProvider", 31);
        var ops = world.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var accessor = (BlockAccessor) java.lang.reflect.Proxy.newProxyInstance(
                JadePlugin.class.getClassLoader(),
                new Class<?>[]{BlockAccessor.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getBlockEntity" -> tile;
                    case "getLevel" -> world;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        JadeServerDataProvider.INSTANCE.appendServerData(tag, accessor);
        var decoded = ComponentSerialization.CODEC.listOf().parse(ops, tag.get(DATA)).getOrThrow();
        if (tag.getIntOr("OtherProvider", 0) != 31 || !decoded.equals(DeviceTooltip.lines(tile))) {
            throw new IllegalStateException("Jade localized component payload round trip");
        }
    }
}
