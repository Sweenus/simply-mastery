package net.sweenus.simplymastery.mastery.definition;

import net.sweenus.simplymastery.client.mastery.ui.MasteryLayout;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MasteryLayoutTest {

    private static final int[][] FRAMEBUFFERS = {
            {854, 480}, {1280, 720}, {1920, 1080}, {3440, 1440}
    };

    @Test
    void uiMatrixKeepsPanelsControlsAndNodesReachable() {
        MasteryProfile profile = ProfileTestFixtures.stormsEdge();
        for (int[] framebuffer : FRAMEBUFFERS) {
            for (int scale = 1; scale <= 4; scale++) {
                int width = (int) Math.ceil(framebuffer[0] / (double) scale);
                int height = (int) Math.ceil(framebuffer[1] / (double) scale);
                if (width < 320 || height < 240) {
                    continue;
                }
                verifyLayout(new MasteryLayout(profile, width, height));
            }
        }
    }

    @Test
    void directionalNavigationReachesEveryNodeAtCompactAndWideSizes() {
        MasteryProfile profile = ProfileTestFixtures.stormsEdge();
        assertKeyboardReachable(new MasteryLayout(profile, 320, 240));
        assertKeyboardReachable(new MasteryLayout(profile, 1920, 1080));
    }

    @Test
    void screenshotScaleKeepsTheWeaponShowcaseOnTheRight() {
        MasteryLayout layout = new MasteryLayout(ProfileTestFixtures.stormsEdge(), 403, 262);
        assertFalse(layout.compact);
        assertTrue(layout.showcaseLeft > layout.canvasRight);
        assertTrue(layout.showcaseRight - layout.showcaseLeft >= 104);
        assertFalse(layout.dockDetail);
        verifyLayout(layout);
    }

    private static void verifyLayout(MasteryLayout layout) {
        assertTrue(layout.headerLeft >= 0 && layout.headerRight <= layout.screenWidth);
        assertTrue(layout.headerTop >= 0 && layout.headerBottom <= layout.screenHeight);
        assertTrue(layout.canvasLeft >= 0 && layout.canvasRight <= layout.screenWidth);
        assertTrue(layout.canvasTop >= layout.headerBottom && layout.canvasBottom < layout.statusTop);
        assertTrue(layout.backButtonX >= layout.headerLeft);
        assertTrue(layout.backButtonX + layout.backButtonWidth < layout.respecButtonX);
        assertTrue(layout.respecButtonX + layout.respecButtonWidth < layout.closeButtonX);
        assertTrue(layout.closeButtonX + layout.closeButtonWidth <= layout.headerRight);
        assertTrue(layout.headerContentLeft <= layout.headerContentRight);
        if (!layout.compact) {
            assertTrue(layout.canvasRight < layout.showcaseLeft);
            assertTrue(layout.showcaseRight <= layout.screenWidth);
        }
        for (int i = 0; i < layout.nodeCount(); i++) {
            assertTrue(layout.x(i) - layout.radius(i) >= layout.canvasLeft);
            assertTrue(layout.x(i) + layout.radius(i) <= layout.canvasRight);
            assertTrue(layout.y(i) - layout.radius(i) >= layout.canvasTop);
            assertTrue(layout.y(i) + layout.radius(i) <= layout.canvasBottom);
            assertEquals(i, layout.nodeAt(layout.x(i), layout.y(i)));
        }
    }

    private static void assertKeyboardReachable(MasteryLayout layout) {
        Set<Integer> reached = new HashSet<>();
        ArrayDeque<Integer> open = new ArrayDeque<>();
        reached.add(0);
        open.add(0);
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        while (!open.isEmpty()) {
            int current = open.removeFirst();
            for (int[] direction : directions) {
                int next = layout.neighbour(current, direction[0], direction[1]);
                if (next >= 0 && reached.add(next)) {
                    open.add(next);
                }
            }
        }
        assertEquals(layout.nodeCount(), reached.size());
    }
}
