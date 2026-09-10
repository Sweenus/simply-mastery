package net.sweenus.simplyswordsmastery.client.mastery.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.sweenus.simplyswordsmastery.config.MasteryConfig;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfile;

public final class MasteryChrome {

    public static final int FRAME_INSET = 2;
    public static final int GRID_SPACING = 14;
    public static final float MICRO_SCALE = 0.5F;
    public static final int BUTTON_HEIGHT = 18;
    public static final float FEEDBACK_SECONDS = 3.2F;

    private MasteryChrome() {
    }

    public record Frame(
            int screenWidth, int screenHeight, boolean compact, boolean dockDetail, int dockHeight,
            int headerLeft, int headerTop, int headerRight, int headerBottom,
            int canvasLeft, int canvasTop, int canvasRight, int canvasBottom, int bodyBottom,
            int showcaseLeft, int showcaseTop, int showcaseRight, int showcaseBottom,
            int statusLeft, int statusTop, int statusRight, int statusBottom,
            int backButtonX, int backButtonWidth,
            int rewardsButtonX, int rewardsButtonWidth,
            int respecButtonX, int respecButtonWidth,
            int closeButtonX, int closeButtonWidth,
            int headerContentLeft, int headerContentRight, int buttonY) {

        public int headerHeight() {
            return headerBottom - headerTop;
        }

        public boolean roomy() {
            return headerHeight() >= 40;
        }
    }

    public static Frame frame(int screenWidth, int screenHeight) {
        int marginX = Math.max(10, Math.round(screenWidth * 0.025F));
        int marginY = Math.max(8, Math.round(screenHeight * 0.025F));
        int headerHeight = Math.clamp(Math.round(screenHeight * 0.095F), 32, 48);
        int headerLeft = marginX;
        int headerTop = marginY;
        int headerRight = screenWidth - marginX;
        int headerBottom = headerTop + headerHeight;

        int backButtonWidth = Math.min(56, Math.max(38, screenWidth / 12));
        int closeButtonWidth = Math.min(64, Math.max(46, screenWidth / 12));
        int respecButtonWidth = Math.min(78, Math.max(58, screenWidth / 10));
        int rewardsButtonWidth = Math.min(78, Math.max(54, screenWidth / 10));
        int backButtonX = headerLeft + 7;
        int closeButtonX = headerRight - 7 - closeButtonWidth;
        int respecButtonX = closeButtonX - 6 - respecButtonWidth;
        int rewardsButtonX = respecButtonX - 6 - rewardsButtonWidth;
        int headerContentLeft = backButtonX + backButtonWidth + 13;
        int headerContentRight = rewardsButtonX - 12;
        int buttonY = headerTop + (headerHeight - BUTTON_HEIGHT) / 2;

        int bodyTop = headerBottom + Math.max(5, Math.round(screenHeight * 0.02F));
        int bodyBottom = screenHeight - marginY;
        int desiredShowcaseLeft = Math.round(screenWidth * 0.715F);
        boolean compact = screenWidth - marginX - desiredShowcaseLeft < 104;

        int statusBottom = bodyBottom;
        int statusTop = bodyBottom - 18;
        int dockHeight = compact ? Math.clamp(Math.round(screenHeight * 0.28F), 58, 128) : 0;

        int canvasTop = bodyTop;
        int canvasLeft = marginX;
        int canvasRight;
        int canvasBottom;
        int showcaseLeft;
        int showcaseTop;
        int showcaseRight;
        int showcaseBottom;
        int statusLeft;
        int statusRight;
        if (compact) {
            canvasRight = screenWidth - marginX;
            showcaseLeft = 0;
            showcaseRight = 0;
            showcaseTop = 0;
            showcaseBottom = 0;
            statusLeft = canvasLeft;
            statusRight = canvasRight;
            canvasBottom = Math.max(canvasTop + 60, statusTop - 6 - dockHeight - 6);
        } else {
            canvasRight = Math.round(screenWidth * 0.695F);
            showcaseLeft = desiredShowcaseLeft;
            showcaseRight = screenWidth - marginX;
            showcaseTop = bodyTop;
            showcaseBottom = statusTop - 6;
            statusLeft = canvasLeft;
            statusRight = showcaseRight;
            canvasBottom = statusTop - 6;
        }

        return new Frame(screenWidth, screenHeight, compact, compact, dockHeight,
                headerLeft, headerTop, headerRight, headerBottom,
                canvasLeft, canvasTop, canvasRight, canvasBottom, statusTop - 6,
                showcaseLeft, showcaseTop, showcaseRight, showcaseBottom,
                statusLeft, statusTop, statusRight, statusBottom,
                backButtonX, backButtonWidth,
                rewardsButtonX, rewardsButtonWidth,
                respecButtonX, respecButtonWidth,
                closeButtonX, closeButtonWidth,
                headerContentLeft, headerContentRight, buttonY);
    }

    public static int accent(MasteryPalette palette, MasteryProfile profile) {
        if (palette.THEMED) {
            return palette.ACCENT;
        }
        if (profile == null || profile.branches().isEmpty()) {
            return palette.ACCENT;
        }
        int[] base = new int[profile.branches().size()];
        for (int i = 0; i < base.length; i++) {
            base[i] = profile.branches().get(i).color() & 0xFFFFFF;
        }
        float blend = MasteryConfig.CLIENT.themedFromTooltips
                ? MasteryConfig.CLIENT.branchAccentBlend
                : 0.0F;
        return palette.harmoniseBranches(base, blend)[0];
    }

    public static void background(DrawContext context, MasteryPalette palette, int width, int height,
                                  int accent, double seconds, float motion, float appear) {
        float dim = MasteryConfig.CLIENT.backgroundDim;
        context.fill(0, 0, width, height,
                MasteryTheme.argb(palette.GROUND_DEEP, Math.min(0.97F, dim + 0.2F) * appear));
        context.fill(FRAME_INSET, FRAME_INSET, width - FRAME_INSET, height - FRAME_INSET,
                MasteryTheme.argb(palette.GROUND, dim * appear));
        UiDraw.hatch45(context, FRAME_INSET, FRAME_INSET, width - FRAME_INSET, height - FRAME_INSET, 4,
                MasteryTheme.argb(palette.TEXTURE_INK, 0.014F * appear));
        UiDraw.motes(context, width, height, seconds, 48, accent, 0.9F * motion * appear);
        UiDraw.vignette(context, width, height, palette.GROUND_DEEP, 0.5F * appear);

        UiDraw.boxOutline(context, FRAME_INSET - 1, FRAME_INSET - 1, width - FRAME_INSET + 1,
                height - FRAME_INSET + 1, 1, MasteryTheme.argb(palette.FRAME_OUTER, 0.95F * appear));
        UiDraw.boxOutline(context, 0, 0, width, height, 1,
                MasteryTheme.argb(palette.FRAME_INNER, 0.95F * appear));
        UiDraw.cornerBrackets(context, FRAME_INSET - 1, FRAME_INSET - 1, width - FRAME_INSET + 1,
                height - FRAME_INSET + 1, 6, 2, MasteryTheme.argb(accent, 0.9F * appear));
    }

    public static void headerPanel(DrawContext context, MasteryPalette palette, Frame frame, int accent,
                                   float alpha) {
        UiDraw.panel(context, palette, frame.headerLeft(), frame.headerTop(), frame.headerRight(),
                frame.headerBottom(), palette.PANEL, accent, alpha);
        context.fill(frame.headerLeft(), frame.headerBottom() - 1, frame.headerRight(), frame.headerBottom(),
                MasteryTheme.argb(palette.RULE, alpha));
    }

    public static void bodyPanel(DrawContext context, MasteryPalette palette, int x0, int y0, int x1, int y1,
                                 int accent, float alpha, boolean grid) {
        UiDraw.panel(context, palette, x0, y0, x1, y1, palette.GROUND, accent, alpha);
        if (grid) {
            UiDraw.gridLines(context, x0 + 1, y0 + 1, x1 - 1, y1 - 1, GRID_SPACING,
                    MasteryTheme.argb(palette.TEXTURE_INK, 0.022F * alpha));
        }
    }

    public static void showcasePanel(DrawContext context, MasteryPalette palette, Frame frame, int accent,
                                     float alpha) {
        if (frame.compact()) {
            return;
        }
        UiDraw.panel(context, palette, frame.showcaseLeft(), frame.showcaseTop(), frame.showcaseRight(),
                frame.showcaseBottom(), palette.PANEL, accent, alpha);
    }

    public static void statusRule(DrawContext context, MasteryPalette palette, Frame frame, float alpha) {
        context.fill(frame.statusLeft(), frame.statusTop(), frame.statusRight(), frame.statusTop() + 1,
                MasteryTheme.argb(palette.RULE, 0.95F * alpha));
    }

    public static void microLabel(DrawContext context, TextRenderer renderer, Text label, float x, float y,
                                  int rgb, float alpha) {
        UiDraw.text(context, renderer, label, x, y, MasteryTheme.argb(rgb, alpha), MICRO_SCALE, false);
    }

    public static void vRule(DrawContext context, MasteryPalette palette, int x, int top, int bottom,
                             float alpha) {
        context.fill(x, top, x + 1, bottom, MasteryTheme.argb(palette.RULE, 0.95F * alpha));
    }

    public static int keyHint(DrawContext context, TextRenderer renderer, MasteryPalette palette, int x, int y,
                              String key, String action, float alpha) {
        int capRight = UiDraw.keyCap(context, renderer, Text.translatable("screen.simplyswordsmastery.key." + key),
                x, y, palette.INK_DIM, palette.BRACKET, 0.85F * alpha);
        Text label = Text.translatable("screen.simplyswordsmastery.key." + action);
        UiDraw.text(context, renderer, label, capRight + 4, y + 4,
                MasteryTheme.argb(palette.INK_MUTED, 0.85F * alpha), MICRO_SCALE, false);
        return capRight + 11 + Math.round(renderer.getWidth(label) * MICRO_SCALE);
    }

    public static void card(DrawContext context, MasteryPalette palette, int x0, int y0, int x1, int y1,
                            int outlineRgb, float alpha) {
        context.fill(x0 + 2, y0 + 3, x1 + 2, y1 + 3, MasteryTheme.argb(palette.SHADOW, 0.45F * alpha));
        context.fill(x0, y0, x1, y1, MasteryTheme.argb(palette.CARD, 0.97F * alpha));
        UiDraw.boxOutline(context, x0, y0, x1, y1, 1, MasteryTheme.argb(outlineRgb, 0.9F * alpha));
    }

    public static boolean feedback(DrawContext context, TextRenderer renderer, MasteryPalette palette,
                                   Frame frame, Text message, int rgb, float timer, float motion) {
        if (timer <= 0.0F) {
            return false;
        }
        float fade = Math.min(1.0F, timer / 0.5F)
                * Math.min(1.0F, (FEEDBACK_SECONDS - timer) / 0.12F);
        float lift = (1.0F - Math.min(1.0F, (FEEDBACK_SECONDS - timer) / 0.25F)) * 5.0F * motion;
        float messageY = frame.statusTop() + 5 - lift;
        context.fill(frame.statusLeft() + 2, Math.round(messageY) + 1, frame.statusLeft() + 5,
                Math.round(messageY) + 4, MasteryTheme.argb(rgb, 0.95F * fade));
        UiDraw.text(context, renderer, UiDraw.fit(renderer, message,
                        frame.statusRight() - frame.statusLeft() - 10), frame.statusLeft() + 8, messageY,
                MasteryTheme.argb(rgb, fade), 1.0F, true);
        return true;
    }

    public static int statCell(DrawContext context, TextRenderer renderer, MasteryPalette palette, Frame frame,
                               int right, int floor, int ruleTop, int ruleBottom, Text label, Text value,
                               int valueRgb, float alpha, boolean rule) {
        int cellWidth = Math.max(Math.round(renderer.getWidth(label) * MICRO_SCALE),
                renderer.getWidth(value)) + 12;
        int left = right - cellWidth;
        if (left < floor) {
            return right;
        }
        int cellTop = frame.headerTop() + (frame.headerHeight() - 14) / 2;
        microLabel(context, renderer, label, left + 6, cellTop, palette.INK_MUTED, alpha);
        UiDraw.text(context, renderer, value, left + 6, cellTop + 6,
                MasteryTheme.argb(valueRgb, alpha), 1.0F, true);
        if (rule) {
            vRule(context, palette, left, ruleTop, ruleBottom, alpha);
        }
        return left;
    }
}
