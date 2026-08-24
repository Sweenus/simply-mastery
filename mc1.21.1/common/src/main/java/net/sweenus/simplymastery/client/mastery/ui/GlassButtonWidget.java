package net.sweenus.simplymastery.client.mastery.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.sweenus.simplymastery.config.MasteryConfig;

public class GlassButtonWidget extends ButtonWidget {

    private final int accentRgb;
    private final Anim highlight = new Anim(0.0F, 16.0F);
    private long lastFrameMs = Util.getMeasuringTimeMs();

    public GlassButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, int accentRgb) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.accentRgb = accentRgb;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
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
        int accent = active ? accentRgb : MasteryTheme.desaturate(accentRgb, 0.85F);

        UiDraw.chamferGradient(context, x0, y0, x1, y1, 3,
                MasteryTheme.argb(MasteryTheme.mix(MasteryTheme.PANEL_TOP, accent, 0.10F * lit), (0.78F + 0.12F * lit) * alpha),
                MasteryTheme.argb(MasteryTheme.PANEL_BOTTOM, 0.92F * alpha));
        UiDraw.chamferOutline(context, x0, y0, x1, y1, 3,
                MasteryTheme.argb(MasteryTheme.mix(MasteryTheme.FRAME, accent, lit), (0.75F + 0.25F * lit) * alpha));

        int sweep = Math.round((x1 - x0 - 8) * MasteryTheme.easeOutCubic(lit));
        if (sweep > 0) {
            UiDraw.hGradient(context, x0 + 4, y1 - 3, x0 + 4 + sweep, y1 - 2,
                    MasteryTheme.argb(accent, 0.0F), MasteryTheme.argb(accent, 0.9F * alpha));
        }
        if (lit > 0.01F) {
            UiDraw.chamferFill(context, x0 + 1, y0 + 1, x1 - 1, y0 + 2, 1,
                    MasteryTheme.argb(0xFFFFFF, 0.16F * lit * alpha));
        }

        int ink = active
                ? MasteryTheme.mix(MasteryTheme.INK_DIM, MasteryTheme.INK, lit)
                : MasteryTheme.INK_MUTED;
        MinecraftClient client = MinecraftClient.getInstance();
        Text fitted = UiDraw.fit(client.textRenderer, getMessage(), Math.max(1, getWidth() - 8));
        UiDraw.centeredText(context, client.textRenderer, fitted, (x0 + x1) * 0.5F,
                y0 + (getHeight() - 8) * 0.5F, MasteryTheme.argb(ink, alpha), 1.0F, false);
    }
}
