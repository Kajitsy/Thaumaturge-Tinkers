package xyz.kajitsy.thaumaturgetinker.item;

import xyz.kajitsy.thaumaturgetinker.KamiRegistries;
import xyz.kajitsy.thaumaturgetinker.TinkererConfig;
import xyz.kajitsy.thaumaturgetinker.focus.DeflectFocus;
import xyz.kajitsy.thaumaturgetinker.network.AwakenedArmorStatus;


import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

@EventBusSubscriber(modid = "thaumaturgetinker")
public final class AwakenedChest extends AwakenedArmorItem {
    public static final DeferredItem<AwakenedChest> ITEM = KamiRegistries.ITEMS.register("ichorcloth_chest_gem", AwakenedChest::new);
    private static final Set<Player> FLIGHT = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public AwakenedChest() {
        super("ichorcloth_chest_gem", ArmorItem.Type.CHESTPLATE);
    }

    public static void update(Player player) {
        update(player, TinkererConfig.flightEnabled());
    }

    @SuppressWarnings("deprecation")
    public static void update(Player player, boolean enableFlight) {
        var stack = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean active = stack.getItem() instanceof AwakenedChest && !disabled(stack) && AwakenedArmorStatus.enabled(player);
        if (active && !player.level().isClientSide()) {
            DeflectFocus.protect(player);
        }
        boolean wants = active && enableFlight;
        var abilities = player.getAbilities();
        boolean changed = false;
        if (FLIGHT.contains(player)) {
            if (wants) {
                changed = !abilities.mayfly;
                abilities.mayfly = true;
            } else {
                if (!abilities.instabuild) {
                    changed = abilities.mayfly || abilities.flying || abilities.invulnerable;
                    abilities.mayfly = false;
                    abilities.flying = false;
                    abilities.invulnerable = false;
                }
                FLIGHT.remove(player);
            }
        } else if (wants) {
            FLIGHT.add(player);
            changed = !abilities.mayfly;
            abilities.mayfly = true;
        }
        if (changed && player instanceof ServerPlayer) {
            player.onUpdateAbilities();
        }
    }

    @SubscribeEvent
    public static void tick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            update(player);
        }
    }
}
