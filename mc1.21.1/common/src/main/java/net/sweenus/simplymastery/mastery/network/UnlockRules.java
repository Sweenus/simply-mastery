package net.sweenus.simplymastery.mastery.network;

import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.state.MasteryState;

public final class UnlockRules {

    private UnlockRules() {
    }

    public static UnlockResult validate(MasteryProfile profile, MasteryState state,
                                        MasteryProfile.Node node, long expectedRevision) {
        if (state.mutationRevision() != expectedRevision) {
            return UnlockResult.STALE_STATE;
        }
        if (state.owns(node.id())) {
            return UnlockResult.ALREADY_OWNED;
        }
        if (!state.unlockedNodeIds().containsAll(node.requires())) {
            return UnlockResult.MISSING_PREREQUISITE;
        }
        if (state.availablePoints(profile) < node.cost()) {
            return UnlockResult.INSUFFICIENT_POINTS;
        }
        if (!node.choiceGroup().isEmpty()) {
            boolean conflict = profile.nodes().stream()
                    .filter(other -> node.choiceGroup().equals(other.choiceGroup()))
                    .anyMatch(other -> state.owns(other.id()));
            if (conflict) {
                return UnlockResult.CHOICE_CONFLICT;
            }
        }
        return UnlockResult.SUCCESS;
    }
}
