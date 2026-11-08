package xyz.kajitsy.thaumaturgetinker.compat;

import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum JadeClientComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return Identifier.fromNamespaceAndPath("thaumaturgetinker", "devices");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var data = accessor.getServerData().get(JadePlugin.DATA);
        if (data == null) return;
        var ops = accessor.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        ComponentSerialization.CODEC.listOf().parse(ops, data).result().ifPresent(lines -> lines.forEach(tooltip::add));
    }
}
