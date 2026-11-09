package xyz.kajitsy.thaumaturgetinker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Original x/y/z binding and y=-1 reset sentinel; dimension IDs use modern registry names. */
public final class SkyPearlData {
    private static net.minecraft.nbt.CompoundTag tag(ItemStack stack) {
        var data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new net.minecraft.nbt.CompoundTag() : data.copyTag();
    }

    public static boolean attuned(ItemStack stack) {
        var t = tag(stack);
        return t.contains("y") && t.getInt("y") != -1;
    }

    public static BlockPos position(ItemStack stack) {
        if (!attuned(stack)) return BlockPos.ZERO;
        var t = tag(stack);
        return new BlockPos(t.getInt("x"), t.getInt("y"), t.getInt("z"));
    }

    public static ResourceLocation dimension(ItemStack stack) {
        if (!attuned(stack)) return ResourceLocation.withDefaultNamespace("overworld");
        var id = ResourceLocation.tryParse(tag(stack).getString("dim"));
        return id == null ? ResourceLocation.withDefaultNamespace("overworld") : id;
    }

    public static ServerLevel targetLevel(ServerPlayer player, ItemStack pearl, boolean fallbackToCurrent) {
        var targetDim = dimension(pearl);
        var server = player.level().getServer();
        var level = server != null ? server.getLevel(ResourceKey.create(Registries.DIMENSION, targetDim)) : null;
        if (level == null && (fallbackToCurrent || targetDim.equals(player.level().dimension().location()))) {
            return player.serverLevel();
        }
        return level;
    }

    public static void bind(ItemStack stack, BlockPos pos, ResourceLocation dimension) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> {
            t.putInt("x", pos.getX());
            t.putInt("y", pos.getY());
            t.putInt("z", pos.getZ());
            t.putString("dim", dimension.toString());
        });
    }

    public static void reset(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putInt("y", -1));
    }

    public static String distance(ItemStack stack, net.minecraft.world.phys.Vec3 viewer, boolean simple) {
        var p = position(stack);
        double dx = p.getX() - viewer.x, dy = simple ? 0 : p.getY() - viewer.y, dz = p.getZ() - viewer.z;
        return new java.math.BigDecimal(Math.sqrt(Math.pow(dx, 2.0) + Math.pow(dy, 2.0) + Math.pow(dz, 2.0))).setScale(2, java.math.RoundingMode.UP).toString();
    }
}
