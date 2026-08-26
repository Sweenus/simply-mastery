package net.sweenus.simplymastery.client.mastery.ui;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.state.MasteryState;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class MasteryUiPolicy {

    private MasteryUiPolicy() {
    }

    public static MasteryNodeState nodeState(MasteryProfile profile, MasteryState state, MasteryProfile.Node node) {
        if (state.owns(node.id())) {
            return MasteryNodeState.OWNED;
        }
        if (!node.choiceGroup().isEmpty() && profile.nodes().stream()
                .filter(other -> node.choiceGroup().equals(other.choiceGroup()))
                .anyMatch(other -> state.owns(other.id()))) {
            return MasteryNodeState.CONFLICT;
        }
        return state.unlockedNodeIds().containsAll(node.requires()) && state.availablePoints(profile) >= node.cost()
                ? MasteryNodeState.REACHABLE
                : MasteryNodeState.LOCKED;
    }

    public static int achievableNodes(MasteryProfile profile, String branchId) {
        int plain = 0;
        Set<String> groups = new HashSet<>();
        for (MasteryProfile.Node node : profile.nodes()) {
            if (branchId != null && !branchId.equals(node.branch())) {
                continue;
            }
            if (node.choiceGroup().isEmpty()) {
                plain++;
            } else {
                groups.add(node.choiceGroup());
            }
        }
        return plain + groups.size();
    }

    public static boolean requiresConfirmation(MasteryProfile.Node node, boolean ordinaryConfirmation) {
        return node.capstone() || ordinaryConfirmation;
    }

    public static Optional<Identifier> iconTexture(MasteryProfile.Node node) {
        return node.icon().map(icon -> {
            String path = icon.getPath();
            if (!path.startsWith("textures/")) {
                path = "textures/" + path;
            }
            if (!path.endsWith(".png")) {
                path += ".png";
            }
            return Identifier.of(icon.getNamespace(), path);
        });
    }
}
