package net.sweenus.simplymastery.mastery.state;

import net.sweenus.simplymastery.mastery.definition.MasteryProfile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MasteryStateMigrator {

    private MasteryStateMigrator() {
    }

    public static MasteryState migrate(MasteryState state, MasteryProfile profile) {
        if (state.profileVersion() >= profile.version()) return state;
        int version = state.profileVersion();
        Set<String> unlocked = new LinkedHashSet<>(state.unlockedNodeIds());
        while (version < profile.version()) {
            int current = version;
            MasteryProfile.Migration migration = profile.migrations().stream()
                    .filter(candidate -> candidate.fromVersion() == current).findFirst().orElse(null);
            if (migration == null) break;
            List<String> replacement = new ArrayList<>();
            for (String node : unlocked) {
                replacement.add(migration.renamedNodes().getOrDefault(node, node));
            }
            unlocked = new LinkedHashSet<>(replacement);
            version = migration.toVersion();
        }
        if (version < profile.version()) version = profile.version();
        return new MasteryState(state.schemaVersion(), state.profileId(), version, state.masteryXp(),
                state.earnedPoints(),
                List.copyOf(unlocked), state.mutationRevision());
    }
}
