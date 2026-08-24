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
        String opening = "storms_edge_signature_opening";
        MasteryProfile versionTwo = new MasteryProfile(source.schema(), source.id(), 2, source.selectors(),
                source.branches(), source.nodes(), List.of(new MasteryProfile.Migration(1, 2,
                Map.of("old_guard", opening), Map.of("removed_node", 2))));
        MasteryState old = new MasteryState(1, source.id(), 1, 0, 4,
                List.of("old_guard", "removed_node", "unknown_from_newer_version"), 7L);
        MasteryState migrated = MasteryStateMigrator.migrate(old, versionTwo);
        assertEquals(2, migrated.profileVersion());
        assertEquals(4, migrated.earnedPoints());
        assertEquals(3, migrated.availablePoints(versionTwo));
        assertTrue(migrated.owns(opening));
        assertTrue(migrated.owns("removed_node"));
        assertTrue(migrated.owns("unknown_from_newer_version"));
        assertEquals(7L, migrated.mutationRevision());
    }

    @Test
    void unlockRulesRemainRevisionPrerequisiteAndChoiceSafe() {
        MasteryProfile profile = ProfileTestFixtures.stormsEdge();
        MasteryState state = MasteryState.initial(profile, 9);
        assertEquals(UnlockResult.MISSING_PREREQUISITE,
                UnlockRules.validate(profile, state, profile.node("storms_edge_signature_cadence").orElseThrow(), 0L));
        assertEquals(UnlockResult.STALE_STATE,
                UnlockRules.validate(profile, state, profile.node("storms_edge_signature_opening").orElseThrow(), 4L));
        state = state.unlock("storms_edge_signature_opening").unlock("storms_edge_signature_cadence")
                .unlock("storms_edge_signature_pressure").unlock("storms_edge_signature_reversal")
                .unlock("storms_edge_signature_reserve").unlock("storms_edge_signature_focus");
        assertEquals(UnlockResult.CHOICE_CONFLICT,
                UnlockRules.validate(profile, state, profile.node("storms_edge_signature_release").orElseThrow(), 6L));
    }
}
