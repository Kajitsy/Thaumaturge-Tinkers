package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.block.entity.EnchanterEntity;
import xyz.kajitsy.thaumaturgetinker.enchanter.EnchanterCosts;
import xyz.kajitsy.thaumaturgetinker.enchanter.EnchanterSelection;
import xyz.kajitsy.thaumaturgetinker.menu.EnchanterMenu;


import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;

public final class EnchanterScreen extends AbstractContainerScreen<EnchanterMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("thaumaturgetinker", "textures/gui/enchanter.png");

    private record Hit(int x, int y, int width, int height, int button, List<Component> tooltip) {
        boolean contains(double mx, double my) {
            return mx >= x && mx < x + width && my >= y && my < y + height;
        }
    }

    private final List<Hit> hits = new ArrayList<>();

    public EnchanterScreen(EnchanterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private List<Integer> available() {
        var result = new ArrayList<Integer>();
        if (minecraft.level == null || menu.working()) return result;
        var selection = new EnchanterSelection();
        var tool = menu.slots.getFirst().getItem();
        var research = (minecraft.player != null && minecraft.player.hasInfiniteMaterials())
            ? EnchanterCosts.allResearch()
            : (minecraft.player != null ? EnchanterMenu.playerResearch(minecraft.player) : Set.<String>of());
        for (int i = 0; i < EnchanterMenu.IDS.size(); i++) {
            if (menu.rank(i) > 0) {
                selection.change(minecraft.level.registryAccess(), tool, research, EnchanterMenu.IDS.get(i), 1);
                selection.change(minecraft.level.registryAccess(), tool, research, EnchanterMenu.IDS.get(i), menu.rank(i));
            }
        }
        for (int i = 0; i < EnchanterMenu.IDS.size() && result.size() < 16; i++) {
            if (menu.rank(i) == 0 && selection.change(minecraft.level.registryAccess(), tool, research, EnchanterMenu.IDS.get(i), 1)) {
                result.add(i);
                selection.change(minecraft.level.registryAccess(), tool, research, EnchanterMenu.IDS.get(i), -1);
            }
        }
        return result;
    }

    private void icon(GuiGraphicsExtractor g, int index, int x, int y, boolean selected) {
        var entry = EnchanterCosts.ENTRIES.get(EnchanterMenu.IDS.get(index));
        g.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse(entry.texture()), x, y, 0, 0, 16, 16, 16, 16);
        var tooltip = new ArrayList<Component>();
        var enchant = minecraft.level == null ? null : minecraft.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(EnchanterMenu.IDS.get(index)))).orElse(null);
        tooltip.add(enchant == null ? Component.literal(EnchanterMenu.IDS.get(index)) : enchant.value().description());
        var cost = entry.cost(Math.max(1, menu.rank(index)));
        var aspectLookup = minecraft.level == null ? null : minecraft.level.registryAccess().lookup(IAspect.REGISTRY_KEY).orElse(null);
        cost.forEach((aspectKey, amount) -> {
            var holder = aspectLookup == null ? Optional.<net.minecraft.core.Holder.Reference<IAspect>>empty() : aspectLookup.get(aspectKey);
            int color = holder.map(h -> h.value().color()).orElse(0xFFFFFF);
            String name = holder.map(h -> Component.translatable("aspect.thaumaturge." + h.value().tag()).getString()).orElse(aspectKey.identifier().getPath());
            tooltip.add(Component.literal(name + " x " + amount).withColor(color));
        });
        if (selected) tooltip.add(Component.translatable("ttmisc.clickToRemove"));
        hits.add(new Hit(x, y, 16, 16, 1 + index * 3, tooltip));
    }

    public List<Rect2i> extraAreas() {
        int rows = 0;
        for (int i = 0; i < EnchanterMenu.IDS.size(); i++) {
            if (menu.rank(i) > 0) rows++;
        }
        if (rows == 0) return List.of();
        return List.of(
            new Rect2i(leftPos + imageWidth, topPos - 4, 52, rows * 26 + 4),
            new Rect2i(leftPos + 55, topPos - 33, 90, 59)
        );
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partial) {
        hits.clear();
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        var choices = available();
        boolean twoRows = choices.size() > 8;
        var tool = menu.slots.getFirst().getItem();
        if (!tool.isEmpty()) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 30, topPos + 50, 0, 166, 147, 24, 256, 256);
        }
        if (twoRows) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 30, topPos + 26, 0, 166, 147, 24, 256, 256);
        }
        for (int n = 0; n < choices.size(); n++) {
            icon(g, choices.get(n), leftPos + 34 + (n % 8) * 16, topPos + (twoRows && n < 8 ? 30 : 54), false);
        }
        var total = new LinkedHashMap<ResourceKey<IAspect>, Integer>();
        int row = 0;
        for (int i = 0; i < EnchanterMenu.IDS.size(); i++) {
            if (menu.rank(i) > 0) {
                var cost = EnchanterCosts.ENTRIES.get(EnchanterMenu.IDS.get(i)).cost(menu.rank(i));
                cost.forEach((aspect, amount) -> total.merge(aspect, amount, Integer::sum));
                int x = leftPos + imageWidth + 4, y = topPos + 4 + row++ * 26;
                g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x - 4, y - 4, 176, 0, 24, 24, 256, 256);
                icon(g, i, x, y, true);
                g.text(font, Component.translatable("enchantment.level." + menu.rank(i)), x + 22, y + 6, 0xFFFFFF, false);
                for (int action = 1; action <= 2; action++) {
                    int bx = x + 20 + (action - 1) * 7, by = y - 4;
                    g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, bx, by, action == 1 ? 225 : 218, 0, 7, 7, 256, 256);
                    hits.add(new Hit(bx, by, 7, 7, 1 + i * 3 + action, List.of(Component.literal(action == 1 ? "+" : "-"))));
                }
            }
        }
        if (row > 0) {
            int sx = leftPos + 151, sy = topPos + 33;
            boolean formed = minecraft.level != null && !EnchanterEntity.findPillars(minecraft.level, menu.tile.getBlockPos()).isEmpty();
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, sx, sy, 176, menu.working() ? 39 : 24, 15, 15, 256, 256);
            hits.add(new Hit(sx, sy, 15, 15, formed ? 0 : -1, List.of(Component.translatable("ttmisc.startEnchant"))));
            var primals = TTAspects.PRIMALS;
            var aspectLookup = minecraft.level == null ? null : minecraft.level.registryAccess().lookup(IAspect.REGISTRY_KEY).orElse(null);
            for (int i = 0; i < 6; i++) {
                var aspectKey = primals.get(i);
                int needed = total.getOrDefault(aspectKey, 0);
                int current = menu.progress(i);
                int size = needed == 0 ? 11 : 59;
                int x = leftPos + 55 + i * 15;
                int y = topPos + (twoRows ? 26 : 50) - size;
                var holder = aspectLookup == null ? Optional.<net.minecraft.core.Holder.Reference<IAspect>>empty() : aspectLookup.get(aspectKey);
                int color = holder.map(h -> h.value().color()).orElse(0xFFFFFF);
                String name = holder.map(h -> Component.translatable("aspect.thaumaturge." + h.value().tag()).getString()).orElse(aspectKey.identifier().getPath());
                if (needed > 0) {
                    int pixels = Math.min(48, (int) (48.0 * current / needed));
                    if (pixels > 0) {
                        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 1, y + 4 + 48 - pixels, 210, 48 - pixels, 8, pixels, 256, 256, 0xFF000000 | color);
                    }
                }
                g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 200, 0, 10, size, 256, 256);
                if (needed == 0) {
                    g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 4, 200, 52, 10, 10, 256, 256);
                }
                hits.add(new Hit(x, y, 10, size, -1, List.of(Component.literal(name), Component.literal(current + "/" + needed))));
            }
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && !menu.working()) {
            for (var hit : hits) {
                if (hit.button >= 0 && hit.contains(event.x(), event.y())) {
                    if (minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, hit.button);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor g, int x, int y) {
        super.extractTooltip(g, x, y);
        for (var hit : hits) {
            if (hit.contains(x, y) && !hit.tooltip.isEmpty()) {
                g.setComponentTooltipForNextFrame(font, hit.tooltip, x, y);
                break;
            }
        }
    }
}
