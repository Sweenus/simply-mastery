package net.sweenus.simplymastery.mastery.state;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MasteryPhase3StateTest {

    private static MasteryState state(int xp, int points, List<String> nodes, long revision) {
        return new MasteryState(1, Identifier.of("simplymastery", "test"), 1, xp, points, nodes, revision);
    }

    @Test
    void xpCrossesMultipleBoundariesAndStopsAtCap() {
        MasteryState updated = state(90, 0, List.of(), 3).awardXp(300, 3, 100, 25);
        assertEquals(3, updated.earnedPoints());
        assertEquals(0, updated.masteryXp());
        assertEquals(4, updated.mutationRevision());
        assertSame(updated, updated.awardXp(100, 3, 100, 25));
    }

    @Test
    void respecRetainsProgressAndIsIdempotent() {
        MasteryState original = state(44, 7, List.of("a", "b"), 9);
        MasteryState reset = original.respec();
        assertEquals(44, reset.masteryXp());
        assertEquals(7, reset.earnedPoints());
        assertTrue(reset.unlockedNodeIds().isEmpty());
        assertEquals(10, reset.mutationRevision());
        assertSame(reset, reset.respec());
    }

    @Test
    void cooldownsArePerKeyAndDeadlineBounded() {
        MasteryCooldownState state = new MasteryCooldownState(Map.of()).start("profile/a", 120L);
        assertFalse(state.ready("profile/a", 119L));
        assertTrue(state.ready("profile/a", 120L));
        assertTrue(state.ready("profile/b", 1L));
    }
}
