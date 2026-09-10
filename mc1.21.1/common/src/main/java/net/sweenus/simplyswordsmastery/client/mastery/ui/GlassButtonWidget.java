package net.sweenus.simplyswordsmastery.client.mastery.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.sweenus.simplyswordsmastery.config.MasteryConfig;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class GlassButtonWidget extends ButtonWidget {

    private final Supplier<MasteryPalette> palette;
    private final ToIntFunction<MasteryPalette> accent;
    private final boolean primary;
    private final Anim highlight = new Anim(0.0F, 28.0F);
    private long lastFrameMs = Util.getMeasuringTimeMs();

    public GlassButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress,
                             Supplier<MasteryPalette> palette, ToIntFunction<MasteryPalette> accent) {
        this(x, y, width, height, message, onPress, palette, accent, false);
    }

    public GlassButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress,
                             Supplier<MasteryPalette> palette, ToIntFunction<MasteryPalette> accent,
                             boolean primary) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.palette = palette;
        this.accent = accent;
        this.primary = primary;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MasteryPalette theme = palette.get();
        int accentRgb = accent.applyAsInt(theme);

        long now = Util.getMeasuringTimeMs();
        float seconds = Math.min(0.1F, (now - lastFrameMs) / 1000.0F);
        lastFrameMs = now;
        highlight.target(active && (isHovered() || isFocused()) ? 1.0F : 0.0F);
        highlight.advance(seconds, MasteryConfig.CLIENT.reducedMotion);

        int x0 = getX();
        int y0 = getY();
        int x1 = x0 + getWidth();
        int y1 = y0 + getHeight();
        float lit = highlight.value();
        float alpha = active ? 1.0F : 0.55F;
        int tint = active ? accentRgb : MasteryTheme.desaturate(accentRgb, 0.85F);

        int ink;
        if (primary) {
            int fill = MasteryTheme.mix(tint, theme.ACCENT_HOT, lit);
            context.fill(x0, y0, x1, y1, MasteryTheme.argb(fill, 0.98F * alpha));
            UiDraw.bevel(context, theme, x0, y0, x1, y1, 1, alpha);
            ink = MasteryTheme.ensureContrast(theme.ON_ACCENT, fill, 4.5);
        } else {
            context.fill(x0, y0, x1, y1,
                    MasteryTheme.argb(MasteryTheme.mix(theme.PANEL, theme.HOVER_FILL, lit),
                            0.95F * alpha));
            UiDraw.boxOutline(context, x0, y0, x1, y1, 1,
                    MasteryTheme.argb(MasteryTheme.mix(theme.RULE, tint, lit),
                            (0.85F + 0.15F * lit) * alpha));
            ink = active
                    ? MasteryTheme.mix(theme.INK_SOFT, theme.INK, lit)
                    : theme.INK_MUTED;
        }

        // The hover sweep survives from the old chrome, squared off into a flat accent bar.
        int sweep = Math.round((x1 - x0 - 2) * MasteryTheme.easeOutCubic(lit));
        if (sweep > 0 && !primary) {
            context.fill(x0 + 1, y1 - 2, x0 + 1 + sweep, y1 - 1,
                    MasteryTheme.argb(tint, 0.9F * alpha));
        }

        MinecraftClient client = MinecraftClient.getInstance();
        UiDraw.centeredText(context, client.textRenderer, getMessage(), (x0 + x1) * 0.5F,
                y0 + (getHeight() - 8) * 0.5F, MasteryTheme.argb(ink, alpha), 1.0F, false);
    }
}
