package net.sweenus.simplyswordsmastery.client.mastery.ui;

import net.minecraft.item.ItemStack;
import net.sweenus.simplyswordsmastery.client.mastery.theme.TooltipThemeSupport;
import net.sweenus.simplyswordsmastery.config.MasteryConfig;

public final class MasteryPaletteSource {

    private MasteryPalette palette = MasteryPalette.DEFAULT;
    private Object identity;

    public MasteryPalette palette() {
        return palette;
    }

    public boolean refresh(ItemStack stack) {
        if (!MasteryConfig.CLIENT.themedFromTooltips) {
            return reset();
        }
        TooltipThemeSupport.Resolved resolved = TooltipThemeSupport.resolve(stack).orElse(null);
        if (resolved == null) {
            return reset();
        }
        if (resolved.identity().equals(identity)) {
            return false;
        }
        identity = resolved.identity();
        palette = resolved.palette();
        return true;
    }

    private boolean reset() {
        if (identity == null && palette == MasteryPalette.DEFAULT) {
            return false;
        }
        palette = MasteryPalette.DEFAULT;
        identity = null;
        return true;
    }
}
