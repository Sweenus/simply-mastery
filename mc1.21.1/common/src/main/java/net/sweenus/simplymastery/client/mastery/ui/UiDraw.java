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

    public static final int SHAPE_HEX = 0;
    public static final int SHAPE_DIAMOND = 1;
    public static final int SHAPE_DISC = 2;

    private static final float HEX_CAP = 0.8660254F;
    private static final float HEX_SLOPE = 0.5773503F;

    private UiDraw() {
    }

    // --- shapes -------------------------------------------------------------

    private static float halfWidth(int kind, float radius, float dy) {
        float abs = Math.abs(dy);
        return switch (kind) {
            case SHAPE_HEX -> abs > radius * HEX_CAP ? -1.0F : radius - abs * HEX_SLOPE;
            case SHAPE_DIAMOND -> radius - abs;
            default -> {
                float square = radius * radius - dy * dy;
                yield square <= 0.0F ? -1.0F : (float) Math.sqrt(square);
            }
        };
    }

    public static void shape(DrawContext context, int kind, float cx, float cy, float radius, int argb) {
        if (radius < 0.5F || (argb >>> 24) == 0) {
            return;
        }
        int top = MathHelper.floor(cy - radius);
        int bottom = MathHelper.ceil(cy + radius);
        for (int y = top; y < bottom; y++) {
            float half = halfWidth(kind, radius, y + 0.5F - cy);
            if (half <= 0.0F) {
                continue;
            }
            context.fill(Math.round(cx - half), y, Math.round(cx + half), y + 1, argb);
        }
    }

    public static void shapeOutline(DrawContext context, int kind, float cx, float cy, float radius,
                                    float thickness, int argb) {
        if (radius < 0.5F || (argb >>> 24) == 0) {
            return;
        }
        float inner = radius - thickness;
        int top = MathHelper.floor(cy - radius);
        int bottom = MathHelper.ceil(cy + radius);
        for (int y = top; y < bottom; y++) {
            float dy = y + 0.5F - cy;
            float outerHalf = halfWidth(kind, radius, dy);
            if (outerHalf <= 0.0F) {
                continue;
            }
            float innerHalf = inner <= 0.5F ? -1.0F : halfWidth(kind, inner, dy);
            if (innerHalf <= 0.0F) {
                context.fill(Math.round(cx - outerHalf), y, Math.round(cx + outerHalf), y + 1, argb);
            } else {
                context.fill(Math.round(cx - outerHalf), y, Math.round(cx - innerHalf), y + 1, argb);
                context.fill(Math.round(cx + innerHalf), y, Math.round(cx + outerHalf), y + 1, argb);
            }
        }
    }

    public static void shapeGlow(DrawContext context, int kind, float cx, float cy, float radius,
                                 int rgb, float alpha, int layers) {
        if (alpha <= 0.002F || radius < 1.0F) {
            return;
        }
        for (int i = layers; i >= 1; i--) {
            shape(context, kind, cx, cy, radius * i / layers, MasteryTheme.argb(rgb, alpha));
        }
    }

    public static void softFloor(DrawContext context, float cx, float top, float halfWidth, float height,
                                 int rgb, float alpha) {
        int rows = Math.max(2, Math.round(height));
        for (int i = 0; i < rows; i++) {
            float t = i / (float) rows;
            float half = halfWidth * (1.0F - t * 0.55F);
            int y = Math.round(top) + i;
            int fade = MasteryTheme.argb(rgb, 0.0F);
            int lit = MasteryTheme.argb(rgb, alpha * (1.0F - t) * (1.0F - t));
            hGradient(context, Math.round(cx - half), y, Math.round(cx), y + 1, fade, lit);
            hGradient(context, Math.round(cx), y, Math.round(cx + half), y + 1, lit, fade);
        }
    }

    public static void ring(DrawContext context, float cx, float cy, float radius, float thickness, int argb) {
        shapeOutline(context, SHAPE_DISC, cx, cy, radius + thickness * 0.5F, thickness, argb);
    }

    public static void arcTicks(DrawContext context, float cx, float cy, float radius, int count,
                                float rotationDegrees, float tickLength, float thickness, int argb) {
        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(rotationDegrees + i * 360.0 / count);
            float sin = (float) Math.sin(angle);
            float cos = (float) Math.cos(angle);
            segment(context, cx + cos * radius, cy + sin * radius,
                    cx + cos * (radius + tickLength), cy + sin * (radius + tickLength), thickness, argb);
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

    public static void chamferFill(DrawContext context, int x0, int y0, int x1, int y1, int chamfer, int argb) {
        if ((argb >>> 24) == 0 || x1 <= x0 || y1 <= y0) {
            return;
        }
        context.fill(x0, y0 + chamfer, x1, y1 - chamfer, argb);
        for (int i = 0; i < chamfer; i++) {
            int inset = chamfer - i;
            context.fill(x0 + inset, y0 + i, x1 - inset, y0 + i + 1, argb);
            context.fill(x0 + inset, y1 - i - 1, x1 - inset, y1 - i, argb);
        }
    }

    public static void chamferGradient(DrawContext context, int x0, int y0, int x1, int y1, int chamfer,
                                       int topArgb, int bottomArgb) {
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        context.fillGradient(x0, y0 + chamfer, x1, y1 - chamfer, topArgb, bottomArgb);
        for (int i = 0; i < chamfer; i++) {
            int inset = chamfer - i;
            context.fill(x0 + inset, y0 + i, x1 - inset, y0 + i + 1, topArgb);
            context.fill(x0 + inset, y1 - i - 1, x1 - inset, y1 - i, bottomArgb);
        }
    }

    public static void chamferOutline(DrawContext context, int x0, int y0, int x1, int y1, int chamfer, int argb) {
        if ((argb >>> 24) == 0 || x1 <= x0 || y1 <= y0) {
            return;
        }
        context.fill(x0 + chamfer, y0, x1 - chamfer, y0 + 1, argb);
        context.fill(x0 + chamfer, y1 - 1, x1 - chamfer, y1, argb);
        context.fill(x0, y0 + chamfer, x0 + 1, y1 - chamfer, argb);
        context.fill(x1 - 1, y0 + chamfer, x1, y1 - chamfer, argb);
        for (int i = 0; i < chamfer; i++) {
            context.fill(x0 + chamfer - i - 1, y0 + i, x0 + chamfer - i, y0 + i + 1, argb);
            context.fill(x1 - chamfer + i, y0 + i, x1 - chamfer + i + 1, y0 + i + 1, argb);
            context.fill(x0 + chamfer - i - 1, y1 - i - 1, x0 + chamfer - i, y1 - i, argb);
            context.fill(x1 - chamfer + i, y1 - i - 1, x1 - chamfer + i + 1, y1 - i, argb);
        }
    }

    public static void corners(DrawContext context, int x0, int y0, int x1, int y1, int chamfer,
                               int arm, int argb) {
        if ((argb >>> 24) == 0) {
            return;
        }
        context.fill(x0 + chamfer, y0, x0 + chamfer + arm, y0 + 1, argb);
        context.fill(x0, y0 + chamfer, x0 + 1, y0 + chamfer + arm, argb);
        context.fill(x1 - chamfer - arm, y0, x1 - chamfer, y0 + 1, argb);
        context.fill(x1 - 1, y0 + chamfer, x1, y0 + chamfer + arm, argb);
        context.fill(x0 + chamfer, y1 - 1, x0 + chamfer + arm, y1, argb);
        context.fill(x0, y1 - chamfer - arm, x0 + 1, y1 - chamfer, argb);
        context.fill(x1 - chamfer - arm, y1 - 1, x1 - chamfer, y1, argb);
        context.fill(x1 - 1, y1 - chamfer - arm, x1, y1 - chamfer, argb);
    }

    public static void glassPanel(DrawContext context, int x0, int y0, int x1, int y1, int chamfer,
                                  int accentRgb, float alpha) {
        chamferGradient(context, x0, y0, x1, y1, chamfer,
                MasteryTheme.argb(MasteryTheme.PANEL_TOP, 0.86F * alpha),
                MasteryTheme.argb(MasteryTheme.PANEL_BOTTOM, 0.94F * alpha));
        chamferOutline(context, x0, y0, x1, y1, chamfer,
                MasteryTheme.argb(MasteryTheme.FRAME, 0.85F * alpha));
        context.fill(x0 + chamfer + 1, y0 + 1, x1 - chamfer - 1, y0 + 2,
                MasteryTheme.argb(MasteryTheme.FRAME_LIGHT, 0.22F * alpha));
        corners(context, x0, y0, x1, y1, chamfer, Math.clamp((x1 - x0) / 12, 6, 26),
                MasteryTheme.argb(accentRgb, 0.7F * alpha));
    }

    // --- gradients and ambience --------------------------------------------

    public static void hGradient(DrawContext context, int x0, int y0, int x1, int y1, int leftArgb, int rightArgb) {
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

    public static int blend(int argbA, int argbB, float t) {
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

    public static void bar(DrawContext context, int x0, int y0, int x1, int y1, float progress,
                           int fillRgb, float alpha) {
        chamferFill(context, x0, y0, x1, y1, 1, MasteryTheme.argb(0x060A11, 0.85F * alpha));
        chamferOutline(context, x0, y0, x1, y1, 1, MasteryTheme.argb(MasteryTheme.FRAME, 0.7F * alpha));
        int span = x1 - x0 - 4;
        int filled = Math.round(span * MasteryTheme.clamp01(progress));
        if (filled <= 0) {
            return;
        }
        hGradient(context, x0 + 2, y0 + 2, x0 + 2 + filled, y1 - 2,
                MasteryTheme.argb(MasteryTheme.dim(fillRgb, 0.45F), 0.95F * alpha),
                MasteryTheme.argb(fillRgb, 0.95F * alpha));
        context.fill(x0 + 1 + filled, y0 + 1, x0 + 3 + filled, y1 - 1,
                MasteryTheme.argb(0xFFFFFF, 0.55F * alpha));
    }

    public static void pips(DrawContext context, int x, int y, int total, int filled, int size, int gap,
                            int fillRgb, float alpha) {
        for (int i = 0; i < total; i++) {
            float cx = x + i * (size + gap) + size * 0.5F;
            float cy = y + size * 0.5F;
            if (i < filled) {
                shape(context, SHAPE_DIAMOND, cx, cy, size * 0.5F, MasteryTheme.argb(fillRgb, 0.95F * alpha));
            } else {
                shapeOutline(context, SHAPE_DIAMOND, cx, cy, size * 0.5F, 1.0F,
                        MasteryTheme.argb(fillRgb, 0.35F * alpha));
            }
        }
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
