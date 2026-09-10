package net.sweenus.simplyswordsmastery.mastery.state;

import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public final class MasteryStateMigrator {

    private static final Logger LOGGER = LoggerFactory.getLogger("Simply Swords: Mastery/Migrations");

    private MasteryStateMigrator() {
    }

    public static MasteryState migrate(MasteryState state, MasteryProfile profile) {
        int version = state.profileVersion();
        Set<String> unlocked = new LinkedHashSet<>(state.unlockedNodeIds());
        while (version < profile.version()) {
            int current = version;
            MasteryProfile.Migration migration = profile.migrations().stream()
                    .filter(candidate -> candidate.fromVersion() == current).findFirst().orElse(null);
            if (migration == null) {
                LOGGER.warn("Resetting allocations for mastery profile {}: no migration from version {} to {}",
                        profile.id(), version, profile.version());
                unlocked.clear();
                version = profile.version();
                break;
            }
            List<String> replacement = new ArrayList<>();
            for (String node : unlocked) {
                if (!migration.removedNodeRefunds().containsKey(node)) {
                    replacement.add(migration.renamedNodes().getOrDefault(node, node));
                }
            }
            unlocked = new LinkedHashSet<>(replacement);
            version = migration.toVersion();
        }
        Set<String> accepted = new LinkedHashSet<>();
        Set<String> choices = new HashSet<>();
        int remaining = state.earnedPoints();
        boolean progressed;
        do {
            progressed = false;
            for (String id : unlocked) {
                if (accepted.contains(id)) continue;
                MasteryProfile.Node node = profile.node(id).orElse(null);
                if (node == null || !accepted.containsAll(node.requires()) || node.cost() > remaining
                        || !node.choiceGroup().isEmpty() && choices.contains(node.choiceGroup())) continue;
                accepted.add(id);
                if (!node.choiceGroup().isEmpty()) choices.add(node.choiceGroup());
                remaining -= node.cost();
                progressed = true;
            }
        } while (progressed);
        List<String> reconciled = unlocked.stream().filter(accepted::contains).toList();
        if (version == state.profileVersion() && reconciled.equals(state.unlockedNodeIds())) return state;
        return new MasteryState(state.schemaVersion(), state.profileId(), version, state.masteryXp(),
                state.earnedPoints(), reconciled, state.mutationRevision() + 1L);
    }
}
