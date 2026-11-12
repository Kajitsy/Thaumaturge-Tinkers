package xyz.kajitsy.thaumaturgetinker.item;

import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.wands.WandRodPrimalOnUpdate;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class KamiWands {
    public static final DeferredRegister<WandCap> CAPS = DeferredRegister.create(WandCap.REGISTRY_KEY, "thaumaturgetinker");
    public static final DeferredRegister<WandRod> RODS = DeferredRegister.create(WandRod.REGISTRY_KEY, "thaumaturgetinker");

    public static final DeferredHolder<WandCap, WandCap> CAP_ICHOR = CAPS.register("ichor", () ->
        new WandCap(0.8F, List.of(), 0.0F, 10, Identifier.fromNamespaceAndPath("thaumaturgetinker", "textures/model/capichor.png"))
    );

    public static final DeferredHolder<WandRod, WandRod> ROD_ICHORCLOTH = RODS.register("ichorcloth", () ->
        new WandRod(1000, 10, Identifier.fromNamespaceAndPath("thaumaturgetinker", "textures/model/rodichorcloth.png"), new WandRodPrimalOnUpdate(), true, false, false, Identifier.fromNamespaceAndPath("thaumaturgetinker", "rod_ichorcloth"))
    );
}
