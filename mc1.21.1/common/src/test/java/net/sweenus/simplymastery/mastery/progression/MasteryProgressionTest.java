package net.sweenus.simplymastery.mastery.progression;

import net.sweenus.simplymastery.config.MasteryServerConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MasteryProgressionTest {

    @Test
    void healthCategoryCapAndAntiFarmAreAppliedInOrder() {
        MasteryServerConfig config = new MasteryServerConfig();
        config.killBaseXp = 10;
        config.maxHealthXpPerTwenty = 2;
        config.hostilePercent = 125;
        config.bossPercent = 250;
        config.maximumKillXp = 40;
        config.antiFarmFullValueKills = 2;
        config.antiFarmRepeatedPercent = 25;
        assertEquals(15, MasteryProgression.calculateXp(20.0F, true, false, 0, config));
        assertEquals(30, MasteryProgression.calculateXp(20.0F, false, true, 0, config));
        assertEquals(3, MasteryProgression.calculateXp(20.0F, true, false, 2, config));
        assertEquals(40, MasteryProgression.calculateXp(1000.0F, false, true, 0, config));
    }

    @Test
    void eligibilityHonorsFriendlyPassiveAndPvpPolicy() {
        MasteryServerConfig config = new MasteryServerConfig();
        assertFalse(MasteryProgression.eligibleClassification(true, false, false, false, true, config));
        assertFalse(MasteryProgression.eligibleClassification(false, true, false, false, true, config));
        assertFalse(MasteryProgression.eligibleClassification(false, false, false, false, false, config));
        assertTrue(MasteryProgression.eligibleClassification(false, false, false, false, true, config));
        assertFalse(MasteryProgression.eligibleClassification(false, false, true, false, true, config));
        config.allowPvpXp = true;
        assertTrue(MasteryProgression.eligibleClassification(false, false, true, false, true, config));
        assertFalse(MasteryProgression.eligibleClassification(false, false, true, true, true, config));
    }

    @Test
    void antiFarmWindowRetainsBoundaryAndExpiresOlderKills() {
        ArrayDeque<Long> kills = new ArrayDeque<>();
        assertEquals(0, MasteryProgression.repeatedKills(kills, 100L, 20));
        assertEquals(1, MasteryProgression.repeatedKills(kills, 120L, 20));
        assertEquals(0, MasteryProgression.repeatedKills(kills, 141L, 20));
    }
}
