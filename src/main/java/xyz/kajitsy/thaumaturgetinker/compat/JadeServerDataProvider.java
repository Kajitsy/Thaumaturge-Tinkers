package xyz.kajitsy.thaumaturgetinker.compat;

import xyz.kajitsy.thaumaturgetinker.client.DeviceTooltip;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum JadeServerDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return Identifier.fromNamespaceAndPath("thaumaturgetinker", "devices");
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        var lines = DeviceTooltip.lines(accessor.getBlockEntity());
        var ops = accessor.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        tag.put(JadePlugin.DATA, ComponentSerialization.CODEC.listOf().encodeStart(ops, lines).getOrThrow());
    }
}
