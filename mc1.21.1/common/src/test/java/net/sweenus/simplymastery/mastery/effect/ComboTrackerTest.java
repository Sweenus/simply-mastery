package net.sweenus.simplymastery.mastery.effect;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ComboTrackerTest {

    @Test
    void handsAreIndependentAndLifecycleClearsAreScoped() {
        ComboTracker tracker = new ComboTracker();
        UUID player = UUID.randomUUID();
        assertFalse(tracker.advance(player, SkillEffectAccess.HandKey.MAIN_HAND, "combo", 10, 20, 2));
        assertFalse(tracker.advance(player, SkillEffectAccess.HandKey.OFF_HAND, "combo", 10, 20, 2));
        assertEquals(2, tracker.size());
        tracker.clear(player, SkillEffectAccess.HandKey.MAIN_HAND);
        assertEquals(1, tracker.size());
        assertTrue(tracker.advance(player, SkillEffectAccess.HandKey.OFF_HAND, "combo", 20, 20, 2));
        assertEquals(0, tracker.size());
        tracker.advance(player, SkillEffectAccess.HandKey.MAIN_HAND, "combo", 50, 20, 2);
        tracker.clear(player);
        assertEquals(0, tracker.size());
    }

    @Test
    void expiredWindowRestartsSequence() {
        ComboTracker tracker = new ComboTracker();
        UUID player = UUID.randomUUID();
        tracker.advance(player, SkillEffectAccess.HandKey.MAIN_HAND, "combo", 10, 5, 2);
        assertFalse(tracker.advance(player, SkillEffectAccess.HandKey.MAIN_HAND, "combo", 16, 5, 2));
    }
}
