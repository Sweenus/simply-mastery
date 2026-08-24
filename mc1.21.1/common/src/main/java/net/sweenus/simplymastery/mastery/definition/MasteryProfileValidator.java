package net.sweenus.simplymastery.mastery.definition;

import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.effect.SkillEffectRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MasteryProfileValidator {

    private MasteryProfileValidator() {
    }

    public static void validate(MasteryProfile profile, int pointBudget) {
        List<String> errors = new ArrayList<>();
        if (profile.schema() != MasteryProfile.CURRENT_SCHEMA) {
            errors.add("profile " + profile.id() + ": unsupported schema " + profile.schema());
        }
        if (profile.version() < 1) {
            errors.add("profile " + profile.id() + ": profile_version must be positive");
        }
        if (profile.selectors().isEmpty() || profile.selectors().size() > 128) {
            errors.add("profile " + profile.id() + ": selector count must be between 1 and 128");
        }
        if (profile.branches().size() != 3) {
            errors.add("profile " + profile.id() + ": expected exactly 3 branches, found " + profile.branches().size());
        }
        if (profile.nodes().isEmpty() || profile.nodes().size() > MasteryProfile.MAX_NODES) {
            errors.add("profile " + profile.id() + ": node count must be between 1 and " + MasteryProfile.MAX_NODES);
        }
        if (profile.migrations().size() > 64) {
            errors.add("profile " + profile.id() + ": may not declare more than 64 migrations");
        }

        Set<String> branchIds = uniqueIds(profile.branches().stream().map(MasteryProfile.Branch::id).toList(),
                "branch", profile.id(), errors);
        Map<String, MasteryProfile.Node> nodes = new LinkedHashMap<>();
        for (MasteryProfile.Node node : profile.nodes()) {
            if (nodes.putIfAbsent(node.id(), node) != null) {
                errors.add("profile " + profile.id() + ", node " + node.id() + ": duplicate node id");
            }
            validateNode(profile, node, branchIds, errors);
        }
        validateSelectors(profile, errors);
        validatePrerequisites(profile, nodes, errors);
        validateBranches(profile, nodes, pointBudget, errors);
        validateCapstoneSpacing(profile, errors);
        validateMigrations(profile, errors);
        if (!errors.isEmpty()) {
            throw new ProfileValidationException(errors);
        }
    }

    public static void validateRegistry(List<MasteryProfile> profiles, int pointBudget) {
        List<String> errors = new ArrayList<>();
        if (profiles.size() > 256) errors.add("profile registry may not contain more than 256 profiles");
        Set<Identifier> ids = new HashSet<>();
        for (MasteryProfile profile : profiles) {
            try {
                validate(profile, pointBudget);
            } catch (ProfileValidationException exception) {
                errors.addAll(exception.errors());
            }
            if (!ids.add(profile.id())) {
                errors.add("duplicate profile id " + profile.id());
            }
        }
        validateSelectorAmbiguity(profiles, errors);
        int canonicalBytes = MasteryProfile.CODEC.listOf().encodeStart(JsonOps.INSTANCE, profiles)
                .getOrThrow().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        if (canonicalBytes > MasteryProfile.MAX_CANONICAL_JSON_BYTES) {
            errors.add("canonical profile registry exceeds " + MasteryProfile.MAX_CANONICAL_JSON_BYTES + " bytes");
        }
        if (!errors.isEmpty()) {
            throw new ProfileValidationException(errors);
        }
    }

    private static Set<String> uniqueIds(List<String> ids, String kind, Identifier profileId, List<String> errors) {
        Set<String> result = new HashSet<>();
        for (String id : ids) {
            if (id.isBlank() || id.length() > 64) {
                errors.add("profile " + profileId + ": invalid " + kind + " id '" + id + "'");
            } else if (!result.add(id)) {
                errors.add("profile " + profileId + ": duplicate " + kind + " id " + id);
            }
        }
        return result;
    }

    private static void validateNode(MasteryProfile profile, MasteryProfile.Node node, Set<String> branches,
                                     List<String> errors) {
        String where = "profile " + profile.id() + ", node " + node.id() + ": ";
        if (node.id().isBlank() || node.id().length() > 64) errors.add(where + "invalid node id");
        if (!branches.contains(node.branch())) errors.add(where + "unknown branch " + node.branch());
        if (!Double.isFinite(node.x()) || !Double.isFinite(node.y()) || node.x() < 0.0 || node.x() > 1.0
                || node.y() < 0.0 || node.y() > 1.0) errors.add(where + "coordinates must be within [0,1]");
        if (node.cost() < 1 || node.cost() > 21) errors.add(where + "cost must be between 1 and 21");
        if (node.requires().size() > 16) errors.add(where + "may not have more than 16 prerequisites");
        if (node.nameKey().length() > 160 || node.descriptionKey().length() > 160) {
            errors.add(where + "translation keys may not exceed 160 characters");
        }
        if (node.effect().parameters().size() > 16) errors.add(where + "effect may not have more than 16 parameters");
        if (profile.version() >= 2 && node.effect().type().equals(Identifier.of("simplymastery", "none"))) {
            errors.add(where + "released content may not use the inert effect");
        }
        if (profile.version() >= 2 && node.icon().isEmpty()) {
            errors.add(where + "released content requires an icon override identifier");
        }
        if (node.capstone() != !node.choiceGroup().isEmpty()) {
            errors.add(where + "capstones require a choice_group and ordinary nodes must not declare one");
        }
        validateEffect(node, where, errors);
    }

    private static void validateEffect(MasteryProfile.Node node, String where, List<String> errors) {
        SkillEffectRegistry.validate(node.effect(), where, errors);
    }

    private static void validateSelectors(MasteryProfile profile, List<String> errors) {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < profile.selectors().size(); i++) {
            MasteryProfile.Selector selector = profile.selectors().get(i);
            if (selector.item().isPresent() == selector.formFamily().isPresent()) {
                errors.add("profile " + profile.id() + ", selector " + i
                        + ": declare exactly one of item or form_family");
            }
            if (selector.priority() < -10000 || selector.priority() > 10000) {
                errors.add("profile " + profile.id() + ", selector " + i + ": priority is outside [-10000,10000]");
            }
            String key = selector.item().map(id -> "item:" + id)
                    .orElseGet(() -> selector.formFamily().map(id -> "family:" + id).orElse("invalid"));
            if (!seen.add(key + "@" + selector.priority())) {
                errors.add("profile " + profile.id() + ", selector " + i + ": duplicate selector " + key);
            }
        }
    }

    private static void validatePrerequisites(MasteryProfile profile, Map<String, MasteryProfile.Node> nodes,
                                              List<String> errors) {
        for (MasteryProfile.Node node : profile.nodes()) {
            for (String required : node.requires()) {
                MasteryProfile.Node parent = nodes.get(required);
                if (parent == null) {
                    errors.add("profile " + profile.id() + ", node " + node.id()
                            + ": dangling prerequisite " + required);
                } else if (!parent.branch().equals(node.branch())) {
                    errors.add("profile " + profile.id() + ", node " + node.id()
                            + ": cross-branch prerequisite " + required);
                }
            }
        }
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (MasteryProfile.Node node : profile.nodes()) {
            detectCycle(profile.id(), node, nodes, visiting, visited, errors);
        }
    }

    private static void detectCycle(Identifier profileId, MasteryProfile.Node node,
                                    Map<String, MasteryProfile.Node> nodes, Set<String> visiting,
                                    Set<String> visited, List<String> errors) {
        if (visited.contains(node.id())) return;
        if (!visiting.add(node.id())) {
            errors.add("profile " + profileId + ", node " + node.id() + ": prerequisite cycle");
            return;
        }
        for (String requirement : node.requires()) {
            MasteryProfile.Node parent = nodes.get(requirement);
            if (parent != null) detectCycle(profileId, parent, nodes, visiting, visited, errors);
        }
        visiting.remove(node.id());
        visited.add(node.id());
    }

    private static void validateBranches(MasteryProfile profile, Map<String, MasteryProfile.Node> nodes,
                                         int pointBudget, List<String> errors) {
        for (MasteryProfile.Branch branch : profile.branches()) {
            List<MasteryProfile.Node> branchNodes = profile.nodes().stream()
                    .filter(node -> node.branch().equals(branch.id())).toList();
            List<MasteryProfile.Node> roots = branchNodes.stream().filter(node -> node.requires().isEmpty()).toList();
            List<MasteryProfile.Node> capstones = branchNodes.stream().filter(MasteryProfile.Node::capstone).toList();
            long regularNodes = branchNodes.stream().filter(node -> !node.capstone()).count();
            if (profile.version() >= 2 && (regularNodes < 7 || regularNodes > 9)) {
                errors.add("profile " + profile.id() + ", branch " + branch.id()
                        + ": expected 7-9 regular nodes, found " + regularNodes);
            }
            if (roots.size() != 1) {
                errors.add("profile " + profile.id() + ", branch " + branch.id()
                        + ": expected exactly one root, found " + roots.size());
            }
            if (capstones.size() != 2) {
                errors.add("profile " + profile.id() + ", branch " + branch.id()
                        + ": expected exactly two capstones, found " + capstones.size());
                continue;
            }
            if (capstones.get(0).choiceGroup().isEmpty()
                    || !capstones.get(0).choiceGroup().equals(capstones.get(1).choiceGroup())) {
                errors.add("profile " + profile.id() + ", branch " + branch.id()
                        + ": both capstones must share one non-empty choice_group");
            }
            double dx = capstones.get(0).x() - capstones.get(1).x();
            double dy = capstones.get(0).y() - capstones.get(1).y();
            if (dx * dx + dy * dy < 0.0025) {
                errors.add("profile " + profile.id() + ", branch " + branch.id()
                        + ": capstone hit boxes overlap at baseline layout");
            }
            if (roots.size() == 1) {
                for (MasteryProfile.Node capstone : capstones) {
                    if (!dependsOn(capstone, roots.get(0).id(), nodes, new HashSet<>())) {
                        errors.add("profile " + profile.id() + ", node " + capstone.id()
                                + ": capstone is not reachable from branch root " + roots.get(0).id());
                    }
                }
                int cheapest = capstones.stream().mapToInt(node -> routeCost(node, nodes, new HashSet<>())).min()
                        .orElse(Integer.MAX_VALUE);
                if (cheapest > pointBudget) {
                    errors.add("profile " + profile.id() + ", branch " + branch.id()
                            + ": cheapest capstone route costs " + cheapest + " points, budget is " + pointBudget);
                }
                if (profile.version() >= 2 && (cheapest < 6 || cheapest > 8)) {
                    errors.add("profile " + profile.id() + ", branch " + branch.id()
                            + ": cheapest capstone route must cost 6-8 points, found " + cheapest);
                }
            }
        }
    }

    private static void validateCapstoneSpacing(MasteryProfile profile, List<String> errors) {
        List<MasteryProfile.Node> capstones = profile.nodes().stream().filter(MasteryProfile.Node::capstone).toList();
        for (int i = 0; i < capstones.size(); i++) {
            for (int j = i + 1; j < capstones.size(); j++) {
                MasteryProfile.Node first = capstones.get(i);
                MasteryProfile.Node second = capstones.get(j);
                double dx = first.x() - second.x();
                double dy = first.y() - second.y();
                if (dx * dx + dy * dy < 0.0025) {
                    errors.add("profile " + profile.id() + ": capstones " + first.id() + " and "
                            + second.id() + " overlap at baseline layout");
                }
            }
        }
    }

    private static boolean dependsOn(MasteryProfile.Node node, String root,
                                     Map<String, MasteryProfile.Node> nodes, Set<String> seen) {
        if (node.id().equals(root)) return true;
        if (!seen.add(node.id())) return false;
        return node.requires().stream().map(nodes::get).filter(java.util.Objects::nonNull)
                .anyMatch(parent -> dependsOn(parent, root, nodes, seen));
    }

    private static int routeCost(MasteryProfile.Node node, Map<String, MasteryProfile.Node> nodes, Set<String> seen) {
        if (!seen.add(node.id())) return 0;
        int cost = node.cost();
        for (String required : node.requires()) {
            MasteryProfile.Node parent = nodes.get(required);
            if (parent != null) cost += routeCost(parent, nodes, seen);
        }
        return cost;
    }

    private static void validateMigrations(MasteryProfile profile, List<String> errors) {
        Set<Integer> starts = new HashSet<>();
        for (MasteryProfile.Migration migration : profile.migrations()) {
            if (migration.fromVersion() < 1 || migration.toVersion() <= migration.fromVersion()
                    || migration.toVersion() > profile.version()) {
                errors.add("profile " + profile.id() + ": invalid migration " + migration.fromVersion()
                        + " -> " + migration.toVersion());
            }
            if (!starts.add(migration.fromVersion())) {
                errors.add("profile " + profile.id() + ": duplicate migration from version "
                        + migration.fromVersion());
            }
            for (int refund : migration.removedNodeRefunds().values()) {
                if (refund < 0 || refund > 21) {
                    errors.add("profile " + profile.id() + ": migration refund must be between 0 and 21");
                }
            }
        }
    }

    private static void validateSelectorAmbiguity(List<MasteryProfile> profiles, List<String> errors) {
        Map<String, List<Selection>> grouped = new HashMap<>();
        for (MasteryProfile profile : profiles) {
            for (MasteryProfile.Selector selector : profile.selectors()) {
                String key = selector.item().map(id -> "item:" + id)
                        .orElseGet(() -> selector.formFamily().map(id -> "family:" + id).orElse("invalid"));
                grouped.computeIfAbsent(key, ignored -> new ArrayList<>())
                        .add(new Selection(profile.id(), selector.priority()));
            }
        }
        grouped.forEach((key, candidates) -> {
            int best = candidates.stream().mapToInt(Selection::priority).max().orElse(Integer.MIN_VALUE);
            long winners = candidates.stream().filter(candidate -> candidate.priority() == best)
                    .map(Selection::profileId).distinct().count();
            if (winners > 1) errors.add("ambiguous selector " + key + " at priority " + best);
        });
    }

    private record Selection(Identifier profileId, int priority) {
    }
}
