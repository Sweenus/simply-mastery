package net.sweenus.simplymastery.client.mastery.theme;

import net.minecraft.item.ItemStack;
import net.sweenus.simplymastery.client.mastery.ui.MasteryPalette;

import java.util.Optional;

public final class TooltipThemeSupport {

    private static final boolean AVAILABLE = detect();

    private TooltipThemeSupport() {
    }

    private static boolean detect() {
        try {
            Class.forName("net.sweenus.simplytooltips.client.render.ThemeRegistry", false,
                    TooltipThemeSupport.class.getClassLoader());
            Class.forName("net.sweenus.simplytooltips.client.render.ItemThemeRegistry", false,
                    TooltipThemeSupport.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean available() {
        return AVAILABLE;
    }

    public static Optional<Resolved> resolve(ItemStack stack) {
        if (!AVAILABLE || stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(TooltipThemeBridge.resolve(stack));
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    public record Resolved(Object identity, MasteryPalette palette) {
    }
}
