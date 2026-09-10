package net.sweenus.simplyswordsmastery.client.mastery.theme;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.sweenus.simplyswordsmastery.client.mastery.ui.MasteryPalette;
import net.sweenus.simplytooltips.api.ThemeDefinition;
import net.sweenus.simplytooltips.api.TooltipTheme;
import net.sweenus.simplytooltips.client.render.ItemThemeRegistry;
import net.sweenus.simplytooltips.client.render.ThemeRegistry;

final class TooltipThemeBridge {

    private static ThemeDefinition cachedDefinition;
    private static MasteryPalette cachedPalette;

    private TooltipThemeBridge() {
    }

    static TooltipThemeSupport.Resolved resolve(ItemStack stack) {
        ThemeDefinition definition = ThemeRegistry.get(themeKey(stack));
        if (!definition.equals(cachedDefinition)) {
            cachedDefinition = definition;
            cachedPalette = derive(definition.colors());
        }
        return new TooltipThemeSupport.Resolved(definition, cachedPalette);
    }

    private static String themeKey(ItemStack stack) {
        String key = ItemThemeRegistry.resolveForStack(stack);
        if (key != null) {
            return key;
        }
        Rarity rarity = stack.getRarity();
        return switch (rarity != null ? rarity : Rarity.COMMON) {
            case UNCOMMON -> "rarity_uncommon";
            case RARE -> "rarity_rare";
            case EPIC -> "rarity_epic";
            default -> "rarity_common";
        };
    }

    private static MasteryPalette derive(TooltipTheme colors) {
        return MasteryPalette.from(colors.bgTop(), colors.bgBottom(), colors.border());
    }
}
