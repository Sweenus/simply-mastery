package net.sweenus.simplymastery.client.mastery.ui;

public final class MasteryTheme {

    // Ground and chrome ------------------------------------------------------
    public static final int GROUND = 0x14100F;
    public static final int GROUND_DEEP = 0x070605;
    public static final int FRAME_OUTER = 0x241C19;
    public static final int FRAME_INNER = 0x0D0A09;
    public static final int PANEL = 0x0F0C0B;
    public static final int CARD = 0x191412;
    public static final int WELL = 0x0A0807;

    // Rules and edges --------------------------------------------------------
    public static final int RULE = 0x2F2521;
    public static final int LOCKED_FRAME = 0x332924;
    public static final int PIP_EMPTY = 0x3A2E28;
    public static final int LOCKED_DASH = 0x463731;
    public static final int BRACKET = 0x4A3C34;

    // Ink --------------------------------------------------------------------
    public static final int DISPLAY = 0xF7F0E2;
    public static final int INK = 0xECE2D0;
    public static final int BODY = 0xC9BCAA;
    public static final int INK_DIM = 0x9C8B78;
    public static final int INK_SOFT = 0x8A7C6E;
    public static final int INK_MUTED = 0x7A6A5C;
    public static final int INK_FAINT = 0x57483F;

    // Nodes ------------------------------------------------------------------
    public static final int LOCKED_FILL = 0x1D1715;
    public static final int LOCKED_GLYPH = 0x57483F;
    public static final int ON_ACCENT = 0x140F0E;
    public static final int HOVER_FILL = 0x1A1513;

    // Accent (per-lane overrides come from MasteryProfile.Branch.color) -------
    public static final int ACCENT = 0xEC3013;
    public static final int ACCENT_HOT = 0xFF563C;

    // Bevel ------------------------------------------------------------------
    public static final int BEVEL_LIGHT = 0xFFFFFF;
    public static final int BEVEL_DARK = 0x000000;
    public static final float BEVEL_LIGHT_ALPHA = 0.26F;
    public static final float BEVEL_DARK_ALPHA = 0.40F;

    private MasteryTheme() {
    }

    public static int argb(int rgb, float alpha) {
        return (Math.clamp(Math.round(alpha * 255.0F), 0, 255) << 24) | (rgb & 0xFFFFFF);
    }

    public static int mix(int rgbA, int rgbB, float t) {
        float f = Math.clamp(t, 0.0F, 1.0F);
        int r = Math.round(((rgbA >> 16) & 0xFF) + (((rgbB >> 16) & 0xFF) - ((rgbA >> 16) & 0xFF)) * f);
        int g = Math.round(((rgbA >> 8) & 0xFF) + (((rgbB >> 8) & 0xFF) - ((rgbA >> 8) & 0xFF)) * f);
        int b = Math.round((rgbA & 0xFF) + ((rgbB & 0xFF) - (rgbA & 0xFF)) * f);
        return (r << 16) | (g << 8) | b;
    }

    public static int dim(int rgb, float amount) {
        return mix(rgb, 0x000000, amount);
    }

    public static int desaturate(int rgb, float amount) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int grey = Math.round(r * 0.299F + g * 0.587F + b * 0.114F);
        return mix(rgb, (grey << 16) | (grey << 8) | grey, amount);
    }

    public static float clamp01(float t) {
        return Math.clamp(t, 0.0F, 1.0F);
    }

    public static float lerp(float from, float to, float t) {
        return from + (to - from) * clamp01(t);
    }

    public static float easeOutCubic(float t) {
        float u = 1.0F - clamp01(t);
        return 1.0F - u * u * u;
    }

    public static float easeOutBack(float t) {
        float u = clamp01(t) - 1.0F;
        return 1.0F + 2.70158F * u * u * u + 1.70158F * u * u;
    }

    public static float pulse(double seconds, double period) {
        return (float) (0.5 + 0.5 * Math.sin(seconds * Math.PI * 2.0 / period));
    }

    /** Quantised {@link #pulse}, matching the design's {@code steps(n, end)} keyframes. */
    public static float stepPulse(double seconds, double period, int steps) {
        if (steps <= 1) {
            return pulse(seconds, period);
        }
        double phase = ((seconds % period) + period) % period / period;
        return (float) (Math.floor(phase * steps) / (steps - 1.0));
    }

    /** The design's {@code mt-flicker}: a two-state 0.9 / 0.55 opacity swap. */
    public static float flicker(double seconds, double period) {
        double phase = ((seconds % period) + period) % period / period;
        return phase < 0.5 ? 0.9F : 0.55F;
    }
}
