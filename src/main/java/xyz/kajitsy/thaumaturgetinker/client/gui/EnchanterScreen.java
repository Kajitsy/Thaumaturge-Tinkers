package xyz.kajitsy.thaumaturgetinker.client.gui;

import xyz.kajitsy.thaumaturgetinker.block.entity.EnchanterEntity;
import xyz.kajitsy.thaumaturgetinker.enchanter.EnchanterCosts;
import xyz.kajitsy.thaumaturgetinker.enchanter.EnchanterSelection;
import xyz.kajitsy.thaumaturgetinker.menu.EnchanterMenu;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;

public final class EnchanterScreen extends AbstractContainerScreen<EnchanterMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumaturgetinker", "textures/gui/enchanter.png");

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

    private void icon(GuiGraphics g, int index, int x, int y, boolean selected) {
        var entry = EnchanterCosts.ENTRIES.get(EnchanterMenu.IDS.get(index));
        g.blit(ResourceLocation.parse(entry.texture()), x, y, 0, 0, 16, 16, 16, 16);
        var tooltip = new ArrayList<Component>();
        var enchant = minecraft.level == null ? null : minecraft.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(EnchanterMenu.IDS.get(index)))).orElse(null);
        tooltip.add(enchant == null ? Component.literal(EnchanterMenu.IDS.get(index)) : enchant.value().description());
        var cost = entry.cost(Math.max(1, menu.rank(index)));
        var aspectLookup = minecraft.level == null ? null : minecraft.level.registryAccess().lookup(IAspect.REGISTRY_KEY).orElse(null);
        cost.forEach((aspectKey, amount) -> {
            var holder = aspectLookup == null ? Optional.<net.minecraft.core.Holder.Reference<IAspect>>empty() : aspectLookup.get(aspectKey);
            int color = holder.map(h -> h.value().color()).orElse(0xFFFFFF);
            String name = holder.map(h -> Component.translatable("aspect.thaumaturge." + h.value().tag()).getString()).orElse(aspectKey.location().getPath());
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
            new Rect2i(leftPos + imageWidth, topPos - 4, 56, rows * 26 + 4),
            new Rect2i(leftPos + 55, topPos - 33, 90, 59)
        );
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        hits.clear();
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        var choices = available();
        boolean twoRows = choices.size() > 8;
        var tool = menu.slots.getFirst().getItem();
        if (!tool.isEmpty()) {
            g.blit(TEXTURE, leftPos + 30, topPos + 50, 0, 166, 147, 24, 256, 256);
        }
        if (twoRows) {
            g.blit(TEXTURE, leftPos + 30, topPos + 26, 0, 166, 147, 24, 256, 256);
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
                g.blit(TEXTURE, x - 4, y - 4, 176, 0, 24, 24, 256, 256);
                icon(g, i, x, y, true);
                g.drawString(font, Component.translatable("enchantment.level." + menu.rank(i)), x + 22, y + 6, 0xFFFFFF, false);
                for (int action = 1; action <= 2; action++) {
                    int bx = x + 20 + (action - 1) * 7, by = y - 4;
                    g.blit(TEXTURE, bx, by, action == 1 ? 225 : 218, 0, 7, 7, 256, 256);
                    hits.add(new Hit(bx, by, 7, 7, 1 + i * 3 + action, List.of(Component.literal(action == 1 ? "+" : "-"))));
                }
            }
        }
        if (row > 0) {
            int sx = leftPos + 151, sy = topPos + 33;
            boolean formed = minecraft.level != null && !EnchanterEntity.findPillars(minecraft.level, menu.tile.getBlockPos()).isEmpty();
            g.blit(TEXTURE, sx, sy, 176, menu.working() ? 39 : 24, 15, 15, 256, 256);
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
                String name = holder.map(h -> Component.translatable("aspect.thaumaturge." + h.value().tag()).getString()).orElse(aspectKey.location().getPath());
                if (needed > 0) {
                    int pixels = Math.min(48, (int) (48.0 * current / needed));
                    if (pixels > 0) {
                        float r = ((color >> 16) & 0xFF) / 255.0f;
                        float gr = ((color >> 8) & 0xFF) / 255.0f;
                        float b = (color & 0xFF) / 255.0f;
                        g.setColor(r, gr, b, 1.0f);
                        g.blit(TEXTURE, x + 1, y + 4 + 48 - pixels, 210, 48 - pixels, 8, pixels, 256, 256);
                        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    }
                }
                g.blit(TEXTURE, x, y, 200, 0, 10, size, 256, 256);
                if (needed == 0) {
                    g.blit(TEXTURE, x, y + 4, 200, 52, 10, 10, 256, 256);
                }
                hits.add(new Hit(x, y, 10, size, -1, List.of(Component.literal(name), Component.literal(current + "/" + needed))));
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !menu.working()) {
            for (var hit : hits) {
                if (hit.button >= 0 && hit.contains(mouseX, mouseY)) {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, hit.button);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        for (var hit : hits) {
            if (hit.contains(mouseX, mouseY) && !hit.tooltip.isEmpty()) {
                g.renderComponentTooltip(font, hit.tooltip, mouseX, mouseY);
                break;
            }
        }
    }
}
