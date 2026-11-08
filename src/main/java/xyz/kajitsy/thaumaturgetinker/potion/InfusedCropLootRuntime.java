package xyz.kajitsy.thaumaturgetinker.potion;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public final class InfusedCropLootRuntime {
    private static final Map<ServerLevel, InfusedCropLoot> CACHE = new WeakHashMap<>();

    public static InfusedCropLoot get(ServerLevel level) {
        return CACHE.computeIfAbsent(level, l -> InfusedCropLoot.complete(l.registryAccess()));
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(InfusedCropLootRuntime::reload);
    }

    private static void reload(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath("thaumaturgetinker", "crop_loot"), (ResourceManagerReloadListener) resources -> CACHE.clear());
    }
}
