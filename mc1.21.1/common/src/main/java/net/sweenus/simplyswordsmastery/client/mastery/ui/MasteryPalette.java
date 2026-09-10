package net.sweenus.simplyswordsmastery.client.mastery.ui;

public final class MasteryPalette {

    private static final float MAX_HUE_SHIFT = 0.06F;
    private static final float MAX_CONTRACTION = 0.25F;

    public static final MasteryPalette DEFAULT = new MasteryPalette(new Spec());

    public final boolean LIGHT;
    public final boolean THEMED;

    public final int GROUND;
    public final int GROUND_DEEP;
    public final int FRAME_OUTER;
    public final int FRAME_INNER;
    public final int PANEL;
    public final int CARD;
    public final int WELL;

    public final int RULE;
    public final int LOCKED_FRAME;
    public final int PIP_EMPTY;
    public final int LOCKED_DASH;
    public final int BRACKET;

    public final int DISPLAY;
    public final int INK;
    public final int BODY;
    public final int INK_DIM;
    public final int INK_SOFT;
    public final int INK_MUTED;
    public final int INK_FAINT;

    public final int LOCKED_FILL;
    public final int LOCKED_GLYPH;
    public final int ON_ACCENT;
    public final int HOVER_FILL;

    public final int ACCENT;
    public final int ACCENT_HOT;

    public final int BEVEL_LIGHT;
    public final int BEVEL_DARK;
    public final float BEVEL_LIGHT_ALPHA;
    public final float BEVEL_DARK_ALPHA;

    public final int TEXTURE_INK;
    public final int HIGHLIGHT;
    public final int SHADOW;
    public final int SCRIM;

    private MasteryPalette(Spec spec) {
        LIGHT = spec.light;
        THEMED = spec.themed;
        GROUND = spec.ground;
        GROUND_DEEP = spec.groundDeep;
        FRAME_OUTER = spec.frameOuter;
        FRAME_INNER = spec.frameInner;
        PANEL = spec.panel;
        CARD = spec.card;
        WELL = spec.well;
        RULE = spec.rule;
        LOCKED_FRAME = spec.lockedFrame;
        PIP_EMPTY = spec.pipEmpty;
        LOCKED_DASH = spec.lockedDash;
        BRACKET = spec.bracket;
        DISPLAY = spec.display;
        INK = spec.ink;
        BODY = spec.body;
        INK_DIM = spec.inkDim;
        INK_SOFT = spec.inkSoft;
        INK_MUTED = spec.inkMuted;
        INK_FAINT = spec.inkFaint;
        LOCKED_FILL = spec.lockedFill;
        LOCKED_GLYPH = spec.lockedGlyph;
        ON_ACCENT = spec.onAccent;
        HOVER_FILL = spec.hoverFill;
        ACCENT = spec.accent;
        ACCENT_HOT = spec.accentHot;
        BEVEL_LIGHT = spec.bevelLight;
        BEVEL_DARK = spec.bevelDark;
        BEVEL_LIGHT_ALPHA = spec.bevelLightAlpha;
        BEVEL_DARK_ALPHA = spec.bevelDarkAlpha;
        TEXTURE_INK = spec.textureInk;
        HIGHLIGHT = spec.highlight;
        SHADOW = spec.shadow;
        SCRIM = spec.scrim;
    }

    public static MasteryPalette from(int bgTop, int bgBottom, int border) {
        int top = bgTop & 0xFFFFFF;
        int bottom = bgBottom & 0xFFFFFF;
        int frame = border & 0xFFFFFF;

        int chrome = MasteryTheme.mix(top, bottom, 0.5F);
        float hue = MasteryTheme.rgbToHsv(chrome)[0];

        Spec spec = new Spec();
        spec.themed = true;
        spec.light = MasteryTheme.relativeLuminance(chrome) > 0.40;

        if (spec.light) {
            lightChrome(spec, hue);
        } else {
            darkChrome(spec, hue);
        }

        spec.scrim = MasteryTheme.dim(bottom, 0.80F);
        spec.frameOuter = MasteryTheme.dim(frame, 0.35F);
        spec.bevelLightAlpha = spec.light ? 0.10F : 0.26F;
        spec.bevelDarkAlpha = spec.light ? 0.22F : 0.40F;
        spec.textureInk = spec.light ? 0x000000 : 0xFFFFFF;

        float[] signature = MasteryTheme.rgbToHsv(frame);
        int accent = MasteryTheme.hsvToRgb(
                signature[0],
                Math.clamp(signature[1], spec.light ? 0.45F : 0.35F, spec.light ? 0.75F : 0.62F),
                Math.clamp(signature[2], spec.light ? 0.42F : 0.78F, spec.light ? 0.62F : 0.95F));
        spec.accent = MasteryTheme.ensureContrast(accent, spec.ground, 3.0);
        spec.onAccent = MasteryTheme.ensureContrast(
                MasteryTheme.relativeLuminance(spec.accent) > 0.45 ? 0x0D0B0A : 0xFAF6EE,
                spec.accent, 4.5);
        spec.accentHot = hot(spec.accent, spec.onAccent, spec.light);

        floors(spec);
        return new MasteryPalette(spec);
    }

    private static void darkChrome(Spec spec, float hue) {
        Spec base = new Spec();
        spec.ground = tint(base.ground, hue);
        spec.groundDeep = tint(base.groundDeep, hue);
        spec.frameInner = tint(base.frameInner, hue);
        spec.panel = tint(base.panel, hue);
        spec.card = tint(base.card, hue);
        spec.well = tint(base.well, hue);
        spec.rule = tint(base.rule, hue);
        spec.lockedFrame = tint(base.lockedFrame, hue);
        spec.pipEmpty = tint(base.pipEmpty, hue);
        spec.lockedDash = tint(base.lockedDash, hue);
        spec.bracket = tint(base.bracket, hue);
        spec.display = tint(base.display, hue);
        spec.ink = tint(base.ink, hue);
        spec.body = tint(base.body, hue);
        spec.inkDim = tint(base.inkDim, hue);
        spec.inkSoft = tint(base.inkSoft, hue);
        spec.inkMuted = tint(base.inkMuted, hue);
        spec.inkFaint = tint(base.inkFaint, hue);
        spec.lockedFill = tint(base.lockedFill, hue);
        spec.lockedGlyph = spec.inkFaint;
        spec.hoverFill = tint(base.hoverFill, hue);
    }

    private static void lightChrome(Spec spec, float hue) {
        spec.ground = MasteryTheme.hsvToRgb(hue, 0.10F, 0.93F);
        spec.groundDeep = MasteryTheme.hsvToRgb(hue, 0.14F, 0.80F);
        spec.frameInner = MasteryTheme.hsvToRgb(hue, 0.16F, 0.76F);
        spec.panel = MasteryTheme.hsvToRgb(hue, 0.08F, 0.97F);
        spec.card = MasteryTheme.hsvToRgb(hue, 0.05F, 1.00F);
        spec.well = MasteryTheme.hsvToRgb(hue, 0.14F, 0.87F);
        spec.rule = MasteryTheme.hsvToRgb(hue, 0.18F, 0.72F);
        spec.lockedFrame = MasteryTheme.hsvToRgb(hue, 0.19F, 0.69F);
        spec.pipEmpty = MasteryTheme.hsvToRgb(hue, 0.20F, 0.66F);
        spec.lockedDash = MasteryTheme.hsvToRgb(hue, 0.21F, 0.62F);
        spec.bracket = MasteryTheme.hsvToRgb(hue, 0.22F, 0.58F);
        spec.display = MasteryTheme.hsvToRgb(hue, 0.55F, 0.16F);
        spec.ink = MasteryTheme.hsvToRgb(hue, 0.50F, 0.20F);
        spec.body = MasteryTheme.hsvToRgb(hue, 0.42F, 0.30F);
        spec.inkDim = MasteryTheme.hsvToRgb(hue, 0.34F, 0.44F);
        spec.inkSoft = MasteryTheme.hsvToRgb(hue, 0.30F, 0.50F);
        spec.inkMuted = MasteryTheme.hsvToRgb(hue, 0.28F, 0.56F);
        spec.inkFaint = MasteryTheme.hsvToRgb(hue, 0.24F, 0.66F);
        spec.lockedFill = MasteryTheme.hsvToRgb(hue, 0.09F, 0.90F);
        spec.lockedGlyph = spec.inkFaint;
        spec.hoverFill = MasteryTheme.hsvToRgb(hue, 0.12F, 0.90F);
    }

    private static void floors(Spec spec) {
        spec.display = MasteryTheme.ensureContrast(spec.display, hardest(spec.display, spec), 4.5);
        spec.ink = MasteryTheme.ensureContrast(spec.ink, hardest(spec.ink, spec), 4.5);
        spec.body = MasteryTheme.ensureContrast(spec.body, hardest(spec.body, spec), 4.5);
        spec.inkDim = MasteryTheme.ensureContrast(spec.inkDim, hardest(spec.inkDim, spec), 3.0);
        spec.inkSoft = MasteryTheme.ensureContrast(spec.inkSoft, hardest(spec.inkSoft, spec), 3.0);
        spec.inkMuted = MasteryTheme.ensureContrast(spec.inkMuted, hardest(spec.inkMuted, spec), 2.0);
        spec.inkFaint = MasteryTheme.ensureContrast(spec.inkFaint, hardest(spec.inkFaint, spec), 2.0);
        spec.lockedGlyph = spec.inkFaint;
        spec.rule = MasteryTheme.ensureContrast(spec.rule, spec.panel, 1.5);
        spec.lockedFrame = MasteryTheme.ensureContrast(spec.lockedFrame, spec.panel, 1.5);
        spec.pipEmpty = MasteryTheme.ensureContrast(spec.pipEmpty, spec.panel, 1.35);
        spec.lockedDash = MasteryTheme.ensureContrast(spec.lockedDash, spec.panel, 1.5);
        spec.bracket = MasteryTheme.ensureContrast(spec.bracket, spec.panel, 1.5);
    }

    private static int tint(int shipped, float hue) {
        float[] c = MasteryTheme.rgbToHsv(shipped);
        return MasteryTheme.hsvToRgb(hue, c[1], c[2]);
    }

    private static int hot(int accent, int onAccent, boolean light) {
        int target = light ? MasteryTheme.dim(accent, 0.25F) : MasteryTheme.lift(accent, 0.30F);
        if (MasteryTheme.contrastRatio(onAccent, target) >= 3.0) {
            return target;
        }
        float low = 0.0F;
        float high = 1.0F;
        for (int i = 0; i < 12; i++) {
            float mid = (low + high) * 0.5F;
            if (MasteryTheme.contrastRatio(onAccent, MasteryTheme.mix(target, accent, mid)) >= 3.0) {
                high = mid;
            } else {
                low = mid;
            }
        }
        return MasteryTheme.mix(target, accent, high);
    }

    private static int hardest(int colour, Spec spec) {
        int worst = spec.panel;
        double ratio = MasteryTheme.contrastRatio(colour, spec.panel);
        for (int surface : new int[] {spec.ground, spec.card}) {
            double candidate = MasteryTheme.contrastRatio(colour, surface);
            if (candidate < ratio) {
                ratio = candidate;
                worst = surface;
            }
        }
        return worst;
    }

    public int emphasise(int rgb, float t) {
        return MasteryTheme.mix(rgb, LIGHT ? 0x000000 : 0xFFFFFF, t);
    }

    public int[] harmoniseBranches(int[] branchRgb, float t) {
        int[] out = new int[branchRgb.length];
        for (int i = 0; i < branchRgb.length; i++) {
            out[i] = branchRgb[i] & 0xFFFFFF;
        }
        if (out.length == 0 || t <= 0.001F) {
            return out;
        }

        float accentHue = MasteryTheme.rgbToHsv(ACCENT)[0];
        float[] arc = new float[out.length];
        float peak = 0.0F;
        for (int i = 0; i < out.length; i++) {
            float delta = accentHue - MasteryTheme.rgbToHsv(out[i])[0];
            if (delta > 0.5F) {
                delta -= 1.0F;
            } else if (delta < -0.5F) {
                delta += 1.0F;
            }
            arc[i] = delta;
            peak = Math.max(peak, Math.abs(delta));
        }
        if (peak < 1.0E-5F) {
            return out;
        }

        float scale = Math.min(MAX_CONTRACTION * t, MAX_HUE_SHIFT * t / peak);
        for (int i = 0; i < out.length; i++) {
            float[] c = MasteryTheme.rgbToHsv(out[i]);
            out[i] = MasteryTheme.ensureContrast(
                    MasteryTheme.hsvToRgb(c[0] + arc[i] * scale, c[1], c[2]), GROUND, 3.0);
        }
        return out;
    }

    private static final class Spec {
        boolean light = false;
        boolean themed = false;
        int ground = 0x14100F;
        int groundDeep = 0x070605;
        int frameOuter = 0x241C19;
        int frameInner = 0x0D0A09;
        int panel = 0x0F0C0B;
        int card = 0x191412;
        int well = 0x0A0807;
        int rule = 0x2F2521;
        int lockedFrame = 0x332924;
        int pipEmpty = 0x3A2E28;
        int lockedDash = 0x463731;
        int bracket = 0x4A3C34;
        int display = 0xF7F0E2;
        int ink = 0xECE2D0;
        int body = 0xC9BCAA;
        int inkDim = 0x9C8B78;
        int inkSoft = 0x8A7C6E;
        int inkMuted = 0x7A6A5C;
        int inkFaint = 0x57483F;
        int lockedFill = 0x1D1715;
        int lockedGlyph = 0x57483F;
        int onAccent = 0x140F0E;
        int hoverFill = 0x1A1513;
        int accent = 0xEC3013;
        int accentHot = 0xFF563C;
        int bevelLight = 0xFFFFFF;
        int bevelDark = 0x000000;
        float bevelLightAlpha = 0.26F;
        float bevelDarkAlpha = 0.40F;
        int textureInk = 0xFFFFFF;
        int highlight = 0xFFFFFF;
        int shadow = 0x000000;
        int scrim = 0x070605;
    }
}
