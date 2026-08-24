package net.sweenus.simplymastery.mastery.definition;

import net.sweenus.simplymastery.mastery.network.UnlockResult;
import net.sweenus.simplymastery.mastery.network.UnlockRules;
import net.sweenus.simplymastery.mastery.state.MasteryState;
import net.sweenus.simplymastery.mastery.state.MasteryStateMigrator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MasteryMigrationAndUnlockTest {

    @Test
    void migrationRenamesNodesRefundsRemovalAndPreservesUnknownIds() {
        MasteryProfile source = ProfileTestFixtures.stormsEdge();
        MasteryProfile versionTwo = new MasteryProfile(source.schema(), source.id(), 2, source.selectors(),
                source.branches(), source.nodes(), List.of(new MasteryProfile.Migration(1, 2,
                Map.of("old_guard", "guard_root"), Map.of("removed_node", 2))));
        MasteryState old = new MasteryState(1, source.id(), 1, 0, 4,
                List.of("old_guard", "removed_node", "unknown_from_newer_version"), 7L);
        MasteryState migrated = MasteryStateMigrator.migrate(old, versionTwo);
        assertEquals(2, migrated.profileVersion());
        assertEquals(4, migrated.earnedPoints());
        assertEquals(3, migrated.availablePoints(versionTwo));
        assertTrue(migrated.owns("guard_root"));
        assertTrue(migrated.owns("removed_node"));
        assertTrue(migrated.owns("unknown_from_newer_version"));
        assertEquals(7L, migrated.mutationRevision());
    }

    @Test
    void unlockRulesRemainRevisionPrerequisiteAndChoiceSafe() {
        MasteryProfile profile = ProfileTestFixtures.stormsEdge();
        MasteryState state = MasteryState.initial(profile, 6);
        assertEquals(UnlockResult.MISSING_PREREQUISITE,
                UnlockRules.validate(profile, state, profile.node("charged_pursuit").orElseThrow(), 0L));
        assertEquals(UnlockResult.STALE_STATE,
                UnlockRules.validate(profile, state, profile.node("guard_root").orElseThrow(), 4L));
        state = state.unlock("guard_root").unlock("charged_pursuit").unlock("eye_of_storm");
        assertEquals(UnlockResult.CHOICE_CONFLICT,
                UnlockRules.validate(profile, state, profile.node("thunderhead").orElseThrow(), 3L));
    }
}
