package net.sweenus.simplymastery.client.mastery.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class MasteryShowcase {

    private MasteryShowcase() {
    }

    public record Hover(int left, int top, int right, int bottom) {
    }

    public static Hover draw(DrawContext context, TextRenderer renderer, MasteryPalette palette, int accent,
                             int panelLeft, int panelTop, int panelRight, int panelBottom,
                             ItemStack stack, Text profileLabel, Text countText, Text meterLabel,
                             int meterTotal, float progress, double time, float intensity, float appear) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0F, (1.0F - appear) * 18.0F, 0.0F);

        Text weaponName = stack.getName();
        boolean showProfile = profileLabel != null
                && !profileLabel.getString().equals(weaponName.getString());
        int centerX = (panelLeft + panelRight) / 2;
        int footerHeight = showProfile ? 60 : 49;
        int wellLeft = panelLeft + 4;
        int wellRight = panelRight - 4;
        int wellTop = panelTop + 4;
        int wellBottom = panelBottom - footerHeight - 2;
        int centerY = (wellTop + wellBottom) / 2;
        float aura = Math.min((wellRight - wellLeft) * 0.46F, (wellBottom - wellTop) * 0.45F);

        context.fill(wellLeft, wellTop, wellRight, wellBottom,
                MasteryTheme.argb(palette.WELL, 0.95F * appear));
        UiDraw.hatch45(context, wellLeft, wellTop, wellRight, wellBottom, 6,
                MasteryTheme.argb(palette.TEXTURE_INK, 0.02F * appear));
        UiDraw.cornerBrackets(context, wellLeft + 3, wellTop + 3, wellRight - 3, wellBottom - 3, 5, 1,
                MasteryTheme.argb(palette.BRACKET, 0.9F * appear));

        float breathe = 1.0F + 0.03F * (float) Math.sin(time * 0.55) * intensity;
        UiDraw.boxGlow(context, centerX, centerY, aura * 1.2F * breathe,
                accent, 0.045F * appear * (0.6F + 0.5F * progress), 14);
        UiDraw.marchingBrackets(context, centerX, centerY, aura * 0.94F,
                (float) (Math.sin(time * 0.5) * 2.0) * intensity, 6, 1,
                MasteryTheme.argb(accent, (0.12F + 0.22F * progress) * appear));
        UiDraw.marchingBrackets(context, centerX, centerY, aura * 1.04F,
                (float) (-Math.sin(time * 0.5) * 2.0) * intensity, 4, 1,
                MasteryTheme.argb(accent, (0.08F + 0.18F * progress) * appear));

        float driftX = (float) Math.sin(time * 0.37 + 1.2) * 3.0F * intensity;
        float driftY = ((float) Math.sin(time * 0.90) * 6.0F + (float) Math.sin(time * 0.27) * 2.0F) * intensity;
        float scale = Math.clamp(aura * 1.3F / 16.0F, 1.5F, 12.0F)
                * (1.0F + (float) Math.sin(time * 0.55) * 0.012F * intensity);
        float itemX = centerX - 8.0F * scale + driftX;
        float itemY = centerY - 8.0F * scale + driftY;
        UiDraw.liveItem(context, stack, itemX, itemY, scale, 140.0F);
        int hoverX = Math.round(itemX);
        int hoverY = Math.round(itemY + (1.0F - appear) * 18.0F);
        int hoverSize = Math.round(16.0F * scale);

        int textLeft = panelLeft + 8;
        int textRight = panelRight - 8;
        int nameY = panelBottom - footerHeight + 6;
        context.fill(panelLeft + 1, nameY - 5, panelRight - 1, nameY - 4,
                MasteryTheme.argb(palette.RULE, appear));
        if (showProfile) {
            MasteryChrome.microLabel(context, renderer,
                    UiDraw.fit(renderer, profileLabel, (textRight - textLeft) * 2),
                    textLeft, nameY, palette.INK_MUTED, appear);
            nameY += 6;
        }
        UiDraw.text(context, renderer, UiDraw.fit(renderer, weaponName, textRight - textLeft),
                textLeft, nameY, MasteryTheme.argb(accent, appear), 1.0F, true);

        int barTop = nameY + 16;
        MasteryChrome.microLabel(context, renderer, meterLabel, textLeft, barTop - 7,
                palette.INK_MUTED, appear);
        UiDraw.rightText(context, renderer, countText, textRight, barTop - 9,
                MasteryTheme.argb(palette.INK, appear), 1.0F, false);
        UiDraw.segmentStrip(context, palette, textLeft, barTop, textRight - textLeft, 4,
                Math.max(1, meterTotal), progress, 1, accent, appear);
        matrices.pop();
        return new Hover(hoverX - 1, hoverY - 1, hoverX + hoverSize + 1, hoverY + hoverSize + 1);
    }
}
