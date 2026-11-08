package xyz.kajitsy.thaumaturgetinker.compat;

import xyz.kajitsy.thaumaturgetinker.client.gui.EnchanterScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.forge.REIPluginClient;

@REIPluginClient
public final class TTReiClientPlugin implements REIClientPlugin {
    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(EnchanterScreen.class, screen ->
            screen.extraAreas().stream()
                .map(r -> new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight()))
                .toList()
        );
    }
}
