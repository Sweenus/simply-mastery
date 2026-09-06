package net.sweenus.simplymastery.client.mastery.ui;

public final class MasteryTheme {

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

    public static int lift(int rgb, float amount) {
        return mix(rgb, 0xFFFFFF, amount);
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


    public static double relativeLuminance(int rgb) {
        return 0.2126 * linear((rgb >> 16) & 0xFF)
                + 0.7152 * linear((rgb >> 8) & 0xFF)
                + 0.0722 * linear(rgb & 0xFF);
    }

    private static double linear(int channel) {
        double c = channel / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    public static double contrastRatio(int rgbA, int rgbB) {
        double a = relativeLuminance(rgbA);
        double b = relativeLuminance(rgbB);
        return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
    }

    public static int ensureContrast(int foreground, int background, double minRatio) {
        if (contrastRatio(foreground, background) >= minRatio) {
            return foreground;
        }
        int target = relativeLuminance(background) > 0.18 ? 0x000000 : 0xFFFFFF;
        if (contrastRatio(target, background) < minRatio) {
            return target;
        }
        float low = 0.0F;
        float high = 1.0F;
        for (int i = 0; i < 16; i++) {
            float mid = (low + high) * 0.5F;
            if (contrastRatio(mix(foreground, target, mid), background) >= minRatio) {
                high = mid;
            } else {
                low = mid;
            }
        }
        return mix(foreground, target, high);
    }


    public static float[] rgbToHsv(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float span = max - min;
        float hue = 0.0F;
        if (span > 1.0E-5F) {
            if (max == r) {
                hue = ((g - b) / span) / 6.0F;
            } else if (max == g) {
                hue = (2.0F + (b - r) / span) / 6.0F;
            } else {
                hue = (4.0F + (r - g) / span) / 6.0F;
            }
            hue = (hue % 1.0F + 1.0F) % 1.0F;
        }
        return new float[] {hue, max <= 1.0E-5F ? 0.0F : span / max, max};
    }

    public static int hsvToRgb(float hue, float saturation, float value) {
        float h = (hue % 1.0F + 1.0F) % 1.0F * 6.0F;
        float s = clamp01(saturation);
        float v = clamp01(value);
        int sector = (int) Math.floor(h);
        float f = h - sector;
        float p = v * (1.0F - s);
        float q = v * (1.0F - s * f);
        float t = v * (1.0F - s * (1.0F - f));
        return switch (sector % 6) {
            case 0 -> pack(v, t, p);
            case 1 -> pack(q, v, p);
            case 2 -> pack(p, v, t);
            case 3 -> pack(p, q, v);
            case 4 -> pack(t, p, v);
            default -> pack(v, p, q);
        };
    }

    private static int pack(float r, float g, float b) {
        return (Math.clamp(Math.round(r * 255.0F), 0, 255) << 16)
                | (Math.clamp(Math.round(g * 255.0F), 0, 255) << 8)
                | Math.clamp(Math.round(b * 255.0F), 0, 255);
    }

    public static float hueLerpShortest(float from, float to, float t) {
        float delta = to - from;
        if (delta > 0.5F) {
            delta -= 1.0F;
        } else if (delta < -0.5F) {
            delta += 1.0F;
        }
        float hue = from + delta * clamp01(t);
        return (hue % 1.0F + 1.0F) % 1.0F;
    }
}
