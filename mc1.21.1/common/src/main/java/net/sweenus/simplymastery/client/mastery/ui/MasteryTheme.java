package net.sweenus.simplymastery.client.mastery.ui;

public final class MasteryTheme {

    public static final int BACKDROP_TOP = 0x0A1220;
    public static final int BACKDROP_BOTTOM = 0x03050A;
    public static final int PANEL_TOP = 0x16202E;
    public static final int PANEL_BOTTOM = 0x080D16;
    public static final int FRAME = 0x2F4B62;
    public static final int FRAME_LIGHT = 0x76A6C4;
    public static final int ACCENT = 0x72D9E8;
    public static final int GOLD = 0xE8B65A;
    public static final int INK = 0xF2FAFF;
    public static final int INK_DIM = 0xA9BCCB;
    public static final int INK_MUTED = 0x6A7D8E;
    public static final int SUCCESS = 0x7CE8B0;
    public static final int DANGER = 0xE8798C;
    public static final int SLATE = 0x3B4A58;

    private MasteryTheme() {
    }

    public static int argb(int rgb, float alpha) {
        return (Math.clamp(Math.round(alpha * 255.0F), 0, 255) << 24) | (rgb & 0xFFFFFF);
    }

    public static int fade(int argb, float factor) {
        int alpha = Math.clamp(Math.round(((argb >>> 24) & 0xFF) * factor), 0, 255);
        return (alpha << 24) | (argb & 0xFFFFFF);
    }

    public static int rgb(int argb) {
        return argb & 0xFFFFFF;
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

    public static float easeOutQuint(float t) {
        float u = 1.0F - clamp01(t);
        return 1.0F - u * u * u * u * u;
    }

    public static float easeOutBack(float t) {
        float u = clamp01(t) - 1.0F;
        return 1.0F + 2.70158F * u * u * u + 1.70158F * u * u;
    }

    public static float easeInOutSine(float t) {
        return (float) (0.5 - 0.5 * Math.cos(Math.PI * clamp01(t)));
    }

    public static float pulse(double seconds, double period) {
        return (float) (0.5 + 0.5 * Math.sin(seconds * Math.PI * 2.0 / period));
    }
}
