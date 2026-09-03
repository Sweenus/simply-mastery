package net.sweenus.simplymastery.mastery.definition;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MasteryCohortTest {

    @Test
    void catalogOwnsStableOrderedProfileAndEffectMetadata() {
        Set<String> profiles = new HashSet<>();
        int cohortNodes = 0;
        for (MasteryCohort cohort : MasteryCohort.values()) {
            assertEquals("simplymastery", cohort.effectId().getNamespace());
            assertEquals("cohort/" + cohort.id(), cohort.effectId().getPath());
            assertEquals(cohort.profilePaths().size() * 27, cohort.expectedNodeCount());
            assertEquals(0, cohort.kind(cohort.profilePaths().getFirst(), "signature", 0));
            assertEquals(cohort.expectedNodeCount() - 1,
                    cohort.kind(cohort.profilePaths().getLast(), "transformation", 8));
            assertTrue(cohort.profilePaths().stream().allMatch(profiles::add));
            cohortNodes += cohort.expectedNodeCount();
        }
        assertEquals(50, profiles.size());
        assertEquals(1_350, cohortNodes);
    }
}
