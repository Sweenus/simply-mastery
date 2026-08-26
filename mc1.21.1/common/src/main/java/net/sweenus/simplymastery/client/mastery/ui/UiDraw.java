package net.sweenus.simplymastery.client.mastery.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class UiDraw {

    private UiDraw() {
    }

    // --- squares ------------------------------------------------------------

    /** A filled square centred on (cx, cy) with the given half-extent. */
    public static void box(DrawContext context, float cx, float cy, float half, int argb) {
        if (half < 0.5F || (argb >>> 24) == 0) {
            return;
        }
        context.fill(Math.round(cx - half), Math.round(cy - half),
                Math.round(cx + half), Math.round(cy + half), argb);
    }

    /** A square outline centred on (cx, cy), the flat-design replacement for {@code ring}. */
    public static void boxRing(DrawContext context, float cx, float cy, float half, float thickness, int argb) {
        if (half < 0.5F) {
            return;
        }
        boxOutline(context, Math.round(cx - half), Math.round(cy - half),
                Math.round(cx + half), Math.round(cy + half), Math.max(1, Math.round(thickness)), argb);
    }

    /**
     * Concentric squares standing in for a radial glow falloff. Bands are weighted
     * quadratically so the outer edge fades to nothing instead of ending on a hard step, and
     * two bands never land on the same rounded extent, which would double that step.
     */
    public static void boxGlow(DrawContext context, float cx, float cy, float half, int rgb,
                               float alpha, int layers) {
        if (alpha <= 0.002F || half < 1.0F || layers <= 0) {
            return;
        }
        int outer = Math.round(half);
        int bands = Math.clamp(layers, 1, outer);
        int previous = -1;
        for (int i = bands; i >= 1; i--) {
            int extent = Math.round(outer * i / (float) bands);
            if (extent == previous) {
                continue;
            }
            previous = extent;
            float t = 1.0F - (i - 1) / (float) bands;
            box(context, cx, cy, extent, MasteryTheme.argb(rgb, alpha * t * t));
        }
    }

    // --- squared chrome -----------------------------------------------------

    /** The design's inset bevel: a light top/left pair over a dark bottom/right pair. */
    public static void bevel(DrawContext context, int x0, int y0, int x1, int y1, int thickness,
                            float alpha) {
        if (x1 <= x0 || y1 <= y0 || thickness <= 0 || alpha <= 0.004F) {
            return;
        }
        int light = MasteryTheme.argb(MasteryTheme.BEVEL_LIGHT, MasteryTheme.BEVEL_LIGHT_ALPHA * alpha);
        int dark = MasteryTheme.argb(MasteryTheme.BEVEL_DARK, MasteryTheme.BEVEL_DARK_ALPHA * alpha);
        int t = Math.min(thickness, Math.min(x1 - x0, y1 - y0) / 2);
        if (t <= 0) {
            return;
        }
        context.fill(x0, y0, x1, y0 + t, light);
        context.fill(x0, y0 + t, x0 + t, y1 - t, light);
        context.fill(x0, y1 - t, x1, y1, dark);
        context.fill(x1 - t, y0 + t, x1, y1 - t, dark);
    }

    /** Four detached L-brackets, as used for the artboard's selection ring and panel corners. */
    public static void cornerBrackets(DrawContext context, int x0, int y0, int x1, int y1, int arm,
                                      int thickness, int argb) {
        if ((argb >>> 24) == 0 || x1 <= x0 || y1 <= y0) {
            return;
        }
        int a = Math.min(arm, Math.min(x1 - x0, y1 - y0) / 2);
        int t = Math.max(1, Math.min(thickness, a));
        context.fill(x0, y0, x0 + a, y0 + t, argb);
        context.fill(x0, y0, x0 + t, y0 + a, argb);
        context.fill(x1 - a, y0, x1, y0 + t, argb);
        context.fill(x1 - t, y0, x1, y0 + a, argb);
        context.fill(x0, y1 - t, x0 + a, y1, argb);
        context.fill(x0, y1 - a, x0 + t, y1, argb);
        context.fill(x1 - a, y1 - t, x1, y1, argb);
        context.fill(x1 - t, y1 - a, x1, y1, argb);
    }

    /**
     * Corner brackets offset outward from a node, replacing the orbiting arc ticks.
     * {@code travel} takes the place of the old rotation parameter.
     */
    public static void marchingBrackets(DrawContext context, float cx, float cy, float half, float travel,
                                        int arm, int thickness, int argb) {
        float reach = half + travel;
        cornerBrackets(context, Math.round(cx - reach), Math.round(cy - reach),
                Math.round(cx + reach), Math.round(cy + reach), arm, thickness, argb);
    }

    /** 45-degree hatch, the artboard's ground texture. */
    public static void hatch45(DrawContext context, int x0, int y0, int x1, int y1, int spacing, int argb) {
        if ((argb >>> 24) == 0 || x1 <= x0 || y1 <= y0 || spacing <= 0) {
            return;
        }
        int height = y1 - y0;
        for (int start = x0 - height; start < x1; start += spacing) {
            for (int i = 0; i < height; i++) {
                int x = start + i;
                if (x < x0 || x >= x1) {
                    continue;
                }
                context.fill(x, y0 + i, x + 1, y0 + i + 1, argb);
            }
        }
    }

    /** The artboard's 48px modular grid, scaled to interface units. */
    public static void gridLines(DrawContext context, int x0, int y0, int x1, int y1, int spacing, int argb) {
        if ((argb >>> 24) == 0 || spacing <= 1) {
            return;
        }
        for (int x = x0 + spacing; x < x1; x += spacing) {
            context.fill(x, y0, x + 1, y1, argb);
        }
        for (int y = y0 + spacing; y < y1; y += spacing) {
            context.fill(x0, y, x1, y + 1, argb);
        }
    }

    /** An orthogonal dashed run, for the artboard's locked connectors. */
    public static void dashLine(DrawContext context, float x0, float y0, float x1, float y1,
                                float dash, float gap, float phase, float thickness, int argb) {
        float period = dash + gap;
        if (period <= 0.1F || (argb >>> 24) == 0) {
            return;
        }
        float dx = x1 - x0;
        float dy = y1 - y0;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.5F) {
            return;
        }
        float ux = dx / length;
        float uy = dy / length;
        float cursor = -(((phase % period) + period) % period);
        while (cursor < length) {
            float from = Math.max(0.0F, cursor);
            float to = Math.min(length, cursor + dash);
            if (to > from) {
                segment(context, x0 + ux * from, y0 + uy * from, x0 + ux * to, y0 + uy * to, thickness, argb);
            }
            cursor += period;
        }
    }

    // --- lines and paths ----------------------------------------------------

    public static void segment(DrawContext context, float x0, float y0, float x1, float y1,
                               float thickness, int argb) {
        if ((argb >>> 24) == 0) {
            return;
        }
        float half = Math.max(0.5F, thickness * 0.5F);
        float dx = x1 - x0;
        float dy = y1 - y0;
        if (Math.abs(dy) < 0.6F) {
            context.fill(Math.round(Math.min(x0, x1)), Math.round(y0 - half),
                    Math.round(Math.max(x0, x1)), Math.round(y0 + half), argb);
            return;
        }
        if (Math.abs(dx) < 0.6F) {
            context.fill(Math.round(x0 - half), Math.round(Math.min(y0, y1)),
                    Math.round(x0 + half), Math.round(Math.max(y0, y1)), argb);
            return;
        }
        int steps = MathHelper.ceil(Math.max(Math.abs(dx), Math.abs(dy)));
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            float px = x0 + dx * t;
            float py = y0 + dy * t;
            context.fill(Math.round(px - half), Math.round(py - half),
                    Math.round(px + half), Math.round(py + half), argb);
        }
    }

    public static float pathLength(float[] path, int points) {
        float total = 0.0F;
        for (int i = 1; i < points; i++) {
            float dx = path[i * 2] - path[i * 2 - 2];
            float dy = path[i * 2 + 1] - path[i * 2 - 1];
            total += (float) Math.sqrt(dx * dx + dy * dy);
        }
        return total;
    }

    public static void pathRange(DrawContext context, float[] path, int points, float from, float to,
                                 float thickness, int argb) {
        if (to <= from || points < 2) {
            return;
        }
        float travelled = 0.0F;
        for (int i = 1; i < points; i++) {
            float ax = path[i * 2 - 2];
            float ay = path[i * 2 - 1];
            float dx = path[i * 2] - ax;
            float dy = path[i * 2 + 1] - ay;
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length <= 0.001F) {
                continue;
            }
            float start = travelled;
            travelled += length;
            float low = Math.max(from, start);
            float high = Math.min(to, travelled);
            if (high <= low) {
                continue;
            }
            float t0 = (low - start) / length;
            float t1 = (high - start) / length;
            segment(context, ax + dx * t0, ay + dy * t0, ax + dx * t1, ay + dy * t1, thickness, argb);
        }
    }

    public static void pathDashed(DrawContext context, float[] path, int points, float length, float limit,
                                  float dash, float gap, float phase, float thickness, int argb) {
        float period = dash + gap;
        if (period <= 0.1F) {
            return;
        }
        float end = Math.min(length, limit);
        float cursor = -(((phase % period) + period) % period);
        while (cursor < end) {
            pathRange(context, path, points, Math.max(0.0F, cursor), Math.min(end, cursor + dash),
                    thickness, argb);
            cursor += period;
        }
    }

    // --- panels -------------------------------------------------------------

    /** A square outline of arbitrary thickness. */
    public static void boxOutline(DrawContext context, int x0, int y0, int x1, int y1, int thickness, int argb) {
        if ((argb >>> 24) == 0 || x1 <= x0 || y1 <= y0) {
            return;
        }
        int t = Math.max(1, Math.min(thickness, Math.min(x1 - x0, y1 - y0) / 2));
        context.fill(x0, y0, x1, y0 + t, argb);
        context.fill(x0, y1 - t, x1, y1, argb);
        context.fill(x0, y0 + t, x0 + t, y1 - t, argb);
        context.fill(x1 - t, y0 + t, x1, y1 - t, argb);
    }

    /** A flat panel: solid fill, a 1px rule and accent corner brackets. No gradient, no radius. */
    public static void panel(DrawContext context, int x0, int y0, int x1, int y1, int fillRgb,
                             int accentRgb, float alpha) {
        if (x1 <= x0 || y1 <= y0 || alpha <= 0.004F) {
            return;
        }
        context.fill(x0, y0, x1, y1, MasteryTheme.argb(fillRgb, 0.95F * alpha));
        boxOutline(context, x0, y0, x1, y1, 1, MasteryTheme.argb(MasteryTheme.RULE, 0.95F * alpha));
        cornerBrackets(context, x0, y0, x1, y1, Math.clamp((x1 - x0) / 12, 4, 10), 1,
                MasteryTheme.argb(accentRgb, 0.75F * alpha));
    }

    // --- gradients and ambience --------------------------------------------

    private static void hGradient(DrawContext context, int x0, int y0, int x1, int y1, int leftArgb, int rightArgb) {
        int span = x1 - x0;
        if (span <= 0 || y1 <= y0) {
            return;
        }
        int step = Math.max(1, span / 96);
        for (int x = x0; x < x1; x += step) {
            int next = Math.min(x1, x + step);
            float t = (x - x0) / (float) span;
            context.fill(x, y0, next, y1, blend(leftArgb, rightArgb, t));
        }
    }

    private static int blend(int argbA, int argbB, float t) {
        float f = MasteryTheme.clamp01(t);
        int alpha = Math.round(((argbA >>> 24) & 0xFF) + ((((argbB >>> 24) & 0xFF)) - ((argbA >>> 24) & 0xFF)) * f);
        return (Math.clamp(alpha, 0, 255) << 24) | MasteryTheme.mix(argbA & 0xFFFFFF, argbB & 0xFFFFFF, f);
    }

    public static void vignette(DrawContext context, int width, int height, int rgb, float strength) {
        int band = Math.max(24, height / 3);
        int side = Math.max(24, width / 4);
        int solid = MasteryTheme.argb(rgb, strength);
        int clear = MasteryTheme.argb(rgb, 0.0F);
        context.fillGradient(0, 0, width, band, solid, clear);
        context.fillGradient(0, height - band, width, height, clear, solid);
        hGradient(context, 0, 0, side, height, solid, clear);
        hGradient(context, width - side, 0, width, height, clear, solid);
    }

    public static void motes(DrawContext context, int width, int height, double seconds, int count,
                             int rgb, float intensity) {
        if (intensity <= 0.01F) {
            return;
        }
        for (int i = 0; i < count; i++) {
            int hash = i * 0x9E3779B9;
            hash ^= hash >>> 15;
            hash *= 0x85EBCA6B;
            hash ^= hash >>> 13;
            float baseX = ((hash >>> 8) & 1023) / 1023.0F;
            float baseY = ((hash >>> 18) & 511) / 511.0F;
            float depth = 0.35F + ((hash >>> 4) & 7) / 10.0F;
            int size = 1 + ((hash >>> 2) & 1);
            float y = baseY - (float) (seconds * 0.018 * depth);
            y -= MathHelper.floor(y);
            float x = baseX + (float) Math.sin(seconds * 0.16 * depth + i) * 0.008F;
            float alpha = (0.05F + depth * 0.12F) * intensity;
            int px = Math.round(x * width);
            int py = Math.round(y * height);
            context.fill(px, py, px + size, py + size, MasteryTheme.argb(rgb, alpha));
        }
    }

    // --- widgets ------------------------------------------------------------

    /**
     * The artboard's segmented progress strip. {@code progress} is a float so animated meters
     * still read as the last cell lights.
     */
    public static void segmentStrip(DrawContext context, int x, int y, int width, int height, int total,
                                    float progress, int gap, int fillRgb, float alpha) {
        if (total <= 0 || width <= 0 || height <= 0 || alpha <= 0.004F) {
            return;
        }
        int cells = Math.min(total, Math.max(1, (width + gap) / Math.max(2, 1 + gap)));
        float span = (width + gap) / (float) cells;
        float lit = MasteryTheme.clamp01(progress) * cells;
        for (int i = 0; i < cells; i++) {
            int x0 = x + Math.round(i * span);
            int x1 = Math.max(x0 + 1, x + Math.round((i + 1) * span) - gap);
            float fill = MasteryTheme.clamp01(lit - i);
            if (fill <= 0.001F) {
                context.fill(x0, y, x1, y + height, MasteryTheme.argb(MasteryTheme.PIP_EMPTY, 0.95F * alpha));
                continue;
            }
            context.fill(x0, y, x1, y + height, MasteryTheme.argb(MasteryTheme.PIP_EMPTY, 0.95F * alpha));
            int end = Math.max(x0 + 1, Math.round(x0 + (x1 - x0) * fill));
            context.fill(x0, y, end, y + height, MasteryTheme.argb(fillRgb, 0.95F * alpha));
            if (height >= 3) {
                context.fill(x0, y, end, y + 1, MasteryTheme.argb(0xFFFFFF, 0.22F * alpha));
                context.fill(x0, y + height - 1, end, y + height, MasteryTheme.argb(0x000000, 0.30F * alpha));
            }
        }
    }

    public static void pips(DrawContext context, int x, int y, int total, int filled, int size, int gap,
                            int fillRgb, float alpha) {
        for (int i = 0; i < total; i++) {
            int x0 = x + i * (size + gap);
            if (i < filled) {
                context.fill(x0, y, x0 + size, y + size, MasteryTheme.argb(fillRgb, 0.95F * alpha));
                context.fill(x0, y, x0 + size, y + 1, MasteryTheme.argb(0xFFFFFF, 0.22F * alpha));
            } else {
                boxOutline(context, x0, y, x0 + size, y + size, 1,
                        MasteryTheme.argb(fillRgb, 0.35F * alpha));
            }
        }
    }

    /** A footer keybind chip: label boxed in a 1px rule, as in the artboard's legend. */
    public static int keyCap(DrawContext context, TextRenderer renderer, Text label, int x, int y,
                             int inkRgb, int ruleRgb, float alpha) {
        int textWidth = renderer.getWidth(label);
        int x1 = x + textWidth + 6;
        int y1 = y + 11;
        boxOutline(context, x, y, x1, y1, 1, MasteryTheme.argb(ruleRgb, 0.9F * alpha));
        text(context, renderer, label, x + 3, y + 2, MasteryTheme.argb(inkRgb, alpha), 1.0F, false);
        return x1;
    }

    // --- text ---------------------------------------------------------------

    public static void text(DrawContext context, TextRenderer renderer, Text value, float x, float y,
                            int argb, float scale, boolean shadow) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0.0F);
        matrices.scale(scale, scale, 1.0F);
        context.drawText(renderer, value, 0, 0, argb, shadow);
        matrices.pop();
    }

    public static void text(DrawContext context, TextRenderer renderer, OrderedText value, float x, float y,
                            int argb, float scale, boolean shadow) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0.0F);
        matrices.scale(scale, scale, 1.0F);
        context.drawText(renderer, value, 0, 0, argb, shadow);
        matrices.pop();
    }

    public static void centeredText(DrawContext context, TextRenderer renderer, Text value, float cx, float y,
                                    int argb, float scale, boolean shadow) {
        text(context, renderer, value, cx - renderer.getWidth(value) * scale * 0.5F, y, argb, scale, shadow);
    }

    public static void rightText(DrawContext context, TextRenderer renderer, Text value, float right, float y,
                                 int argb, float scale, boolean shadow) {
        text(context, renderer, value, right - renderer.getWidth(value) * scale, y, argb, scale, shadow);
    }

    public static Text fit(TextRenderer renderer, Text value, int maxWidth) {
        if (maxWidth <= 0) {
            return Text.empty();
        }
        if (renderer.getWidth(value) <= maxWidth) {
            return value;
        }
        String suffix = "…";
        int suffixWidth = renderer.getWidth(suffix);
        if (maxWidth < suffixWidth) {
            return Text.empty();
        }
        int available = maxWidth - suffixWidth;
        return Text.literal(renderer.trimToWidth(value, available).getString() + suffix);
    }

    public static void liveItem(DrawContext context, ItemStack stack, float x, float y, float scale, float depth) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        try {
            matrices.translate(x, y, depth);
            matrices.scale(scale, scale, 1.0F);
            context.drawItem(stack, 0, 0);
        } finally {
            matrices.pop();
        }
    }

    public static void textureIcon(DrawContext context, Identifier texture, float cx, float cy, int size) {
        int x = Math.round(cx - size * 0.5F);
        int y = Math.round(cy - size * 0.5F);
        context.drawTexture(texture, x, y, 0.0F, 0.0F, size, size, 16, 16);
    }
}
