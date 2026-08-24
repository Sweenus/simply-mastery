package net.sweenus.simplymastery.mastery.definition;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.network.UnlockResult;
import net.sweenus.simplymastery.mastery.network.UnlockRules;
import net.sweenus.simplymastery.mastery.state.MasteryState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StormsEdgeProfileTest {

    @Test
    void exactSelectorRejectsStandardAndRunicWeapons() {
        assertEquals(true, MasteryProfile.supportsItemId(Identifier.of("simplyswords", "storms_edge")));
        assertEquals(false, MasteryProfile.supportsItemId(Identifier.of("simplyswords", "iron_longsword")));
        assertEquals(false, MasteryProfile.supportsItemId(Identifier.of("simplyswords", "runic_longsword")));
        assertEquals(false, MasteryProfile.supportsItemId(Identifier.of("simplyswords", "diamond_claymore")));
    }

    @Test
    void miniatureProfileHasThreeBranchesAndTwoCapstonesEach() {
        MasteryProfile profile = MasteryProfile.STORMS_EDGE;
        assertEquals(3, profile.branches().size());
        for (MasteryProfile.Branch branch : profile.branches()) {
            assertEquals(2, profile.nodes().stream()
                    .filter(node -> node.branch().equals(branch.id()) && node.capstone())
                    .count());
        }
    }

    @Test
    void unlockRulesEnforcePrerequisitesRevisionSafeStateAndCapstoneChoice() {
        MasteryProfile profile = MasteryProfile.STORMS_EDGE;
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
