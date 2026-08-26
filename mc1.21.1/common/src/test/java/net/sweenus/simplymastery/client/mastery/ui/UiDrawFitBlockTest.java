package net.sweenus.simplymastery.client.mastery.ui;

import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiDrawFitBlockTest {

    private static final ToIntFunction<Text> WIDTH = text -> text.getString().length() * 5;

    private static UiDraw.Fitted fit(String value, int maxWidth) {
        return UiDraw.fitBlock(WIDTH, Text.literal(value), maxWidth, 2, 0.5F);
    }

    private static List<String> strings(UiDraw.Fitted fitted) {
        return fitted.lines().stream().map(Text::getString).toList();
    }

    @Test
    void aNameThatAlreadyFitsIsLeftAloneAtFullSize() {
        UiDraw.Fitted fitted = fit("Petal Step", 60);
        assertEquals(List.of("Petal Step"), strings(fitted));
        assertEquals(1.0F, fitted.scale());
    }

    @Test
    void aTwoWordNameBreaksAtItsSpace() {
        UiDraw.Fitted fitted = fit("Blood Ritual", 54);
        assertEquals(List.of("Blood", "Ritual"), strings(fitted));
        assertEquals(1.0F, fitted.scale());
    }

    @Test
    void aThreeWordNameTakesTheBalancedSplitAndStaysOnTwoLines() {
        UiDraw.Fitted fitted = fit("Consumption and Corruption", 54);
        assertEquals(List.of("Consumption", "and Corruption"), strings(fitted));
        assertTrue(fitted.scale() < 1.0F, "the wider line still needs scaling down");
        assertTrue(fitted.scale() >= 0.5F, "but never past the floor");
    }

    @Test
    void aSingleLongWordScalesDownRatherThanBreaking() {
        UiDraw.Fitted fitted = fit("Ascendancy", 30);
        assertEquals(List.of("Ascendancy"), strings(fitted));
        assertEquals(30.0F / 50.0F, fitted.scale(), 0.0001F);
    }

    @Test
    void oneLineModeNeverBreaksAndNothingIsEverTruncated() {
        UiDraw.Fitted fitted = UiDraw.fitBlock(WIDTH, Text.literal("Gathering Charge"), 40, 1, 0.5F);
        assertEquals(List.of("Gathering Charge"), strings(fitted));
        for (String line : strings(fit("Consumption and Corruption", 20))) {
            assertFalse(line.contains("…"), "fitBlock must never ellipsise: " + line);
        }
    }
}
