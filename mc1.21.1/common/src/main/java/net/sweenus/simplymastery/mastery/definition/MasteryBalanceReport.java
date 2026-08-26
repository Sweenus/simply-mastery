package net.sweenus.simplymastery.mastery.definition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MasteryBalanceReport {

    private static final Pattern PHASE_EFFECT = Pattern.compile("phase(\\d+)_mastery");
    private static final Map<Integer, Integer> PHASE_COUNTS = Map.of(
            2, 162, 3, 162, 4, 81, 5, 162, 6, 189, 7, 108, 8, 162, 9, 189, 10, 135);

    private MasteryBalanceReport() {
    }

    public static void write(Path output, List<MasteryProfile> profiles) throws IOException {
        Files.createDirectories(output.getParent());
        Files.writeString(output, render(profiles), StandardCharsets.UTF_8);
    }

    static String render(List<MasteryProfile> profiles) {
        List<String> warnings = warnings(profiles);
        long phasedNodes = profiles.stream().flatMap(profile -> profile.nodes().stream())
                .filter(node -> phase(node.effect()) != 0).count();
        StringBuilder markdown = new StringBuilder("# Simply Mastery balance ledger\n\n")
                .append("Profiles: ").append(profiles.size()).append("  \n")
                .append("Nodes: ").append(profiles.stream().mapToInt(profile -> profile.nodes().size()).sum())
                .append("  \nReference nodes: ").append(profiles.stream().mapToInt(profile -> profile.nodes().size()).sum() - phasedNodes)
                .append("  \nPhased nodes: ").append(phasedNodes)
                .append("  \nWarnings: ").append(warnings.size()).append("\n\n")
                .append("## Audit warnings\n\n");
        if (warnings.isEmpty()) {
            markdown.append("None.\n\n");
        } else {
            warnings.forEach(warning -> markdown.append("- ").append(warning).append("\n"));
            markdown.append("\n");
        }
        markdown.append("## Node ledger\n\n")
                .append("Route cost includes the node and all of its prerequisites. Cooldowns are shown in seconds.\n\n")
                .append("| Profile | Branch theme | Node | Tier | Route | Effect | Kind | Cooldown | Trigger frequency | Combat role |\n")
                .append("|---|---|---|---|---:|---|---:|---:|---|---|\n");
        profiles.stream().sorted(java.util.Comparator.comparing(profile -> profile.id().toString()))
                .forEach(profile -> appendProfile(markdown, profile));
        return markdown.toString();
    }

    private static void appendProfile(StringBuilder markdown, MasteryProfile profile) {
        Map<String, MasteryProfile.Node> nodes = new HashMap<>();
        profile.nodes().forEach(node -> nodes.put(node.id(), node));
        for (MasteryProfile.Node node : profile.nodes()) {
            Balance balance = balance(profile, node);
            int cooldown = node.effect().parameters().getOrDefault("cooldown_ticks", 0);
            markdown.append("| `").append(profile.id()).append("` | ").append(branchTheme(profile, node.branch()))
                    .append(" | `").append(node.id()).append("` | ")
                    .append(node.capstone() ? "capstone" : "regular").append(" | ")
                    .append(routeCost(node, nodes, new HashSet<>())).append(" | `")
                    .append(node.effect().type()).append("` | ")
                    .append(node.effect().parameters().getOrDefault("kind", -1) < 0 ? "—"
                            : node.effect().parameters().get("kind")).append(" | ")
                    .append(cooldown == 0 ? "—" : formatSeconds(cooldown)).append(" | ")
                    .append(balance.frequency()).append(" | ").append(balance.role()).append(" |\n");
        }
    }

    static List<String> warnings(List<MasteryProfile> profiles) {
        List<String> warnings = new ArrayList<>();
        int nodeCount = profiles.stream().mapToInt(profile -> profile.nodes().size()).sum();
        if (profiles.size() != 52) warnings.add("Expected 52 profiles, found " + profiles.size() + ".");
        if (nodeCount != 1_404) warnings.add("Expected 1,404 nodes, found " + nodeCount + ".");

        Map<Integer, TreeSet<Integer>> kinds = new LinkedHashMap<>();
        PHASE_COUNTS.keySet().stream().sorted().forEach(phase -> kinds.put(phase, new TreeSet<>()));
        int referenceNodes = 0;
        for (MasteryProfile profile : profiles) {
            auditProfile(profile, warnings);
            for (MasteryProfile.Node node : profile.nodes()) {
                int phase = phase(node.effect());
                if (phase == 0) {
                    referenceNodes++;
                    if (balance(profile, node).equals(Balance.GENERIC)) {
                        warnings.add(profile.id() + "/" + node.id() + " has no balance classification.");
                    }
                    continue;
                }
                Integer kind = node.effect().parameters().get("kind");
                if (node.effect().parameters().size() != 1 || kind == null) {
                    warnings.add(profile.id() + "/" + node.id() + " must contain only a kind parameter.");
                    continue;
                }
                Set<Integer> phaseKinds = kinds.get(phase);
                if (phaseKinds == null) {
                    warnings.add(profile.id() + "/" + node.id() + " uses unsupported phase " + phase + ".");
                } else if (!phaseKinds.add(kind)) {
                    warnings.add("Phase " + phase + " kind " + kind + " is duplicated.");
                }
            }
        }
        if (referenceNodes != 54) warnings.add("Expected 54 reference nodes, found " + referenceNodes + ".");
        for (Map.Entry<Integer, Integer> expected : PHASE_COUNTS.entrySet()) {
            TreeSet<Integer> actual = kinds.get(expected.getKey());
            if (actual.size() != expected.getValue() || actual.isEmpty() || actual.iterator().next() != 0
                    || actual.last() != expected.getValue() - 1) {
                warnings.add("Phase " + expected.getKey() + " must contain kinds 0-"
                        + (expected.getValue() - 1) + "; found " + actual.size() + " distinct kinds.");
            }
        }
        return List.copyOf(warnings);
    }

    private static void auditProfile(MasteryProfile profile, List<String> warnings) {
        if (profile.branches().size() != 3 || profile.nodes().size() != 27) {
            warnings.add(profile.id() + " must contain 3 branches and 27 nodes.");
        }
        Map<String, MasteryProfile.Node> nodes = new HashMap<>();
        profile.nodes().forEach(node -> nodes.put(node.id(), node));
        for (MasteryProfile.Branch branch : profile.branches()) {
            List<MasteryProfile.Node> branchNodes = profile.nodes().stream()
                    .filter(node -> node.branch().equals(branch.id())).toList();
            List<MasteryProfile.Node> capstones = branchNodes.stream().filter(MasteryProfile.Node::capstone).toList();
            if (branchNodes.size() != 9 || capstones.size() != 2) {
                warnings.add(profile.id() + "/" + branch.id() + " must contain 7 regular nodes and 2 capstones.");
                continue;
            }
            String group = capstones.getFirst().choiceGroup();
            if (group.isEmpty() || !group.equals(capstones.getLast().choiceGroup())) {
                warnings.add(profile.id() + "/" + branch.id() + " capstones must share one exclusive group.");
            }
            for (MasteryProfile.Node capstone : capstones) {
                if (routeCost(capstone, nodes, new HashSet<>()) != 7) {
                    warnings.add(profile.id() + "/" + capstone.id() + " must have route cost 7.");
                }
            }
        }
    }

    private static int routeCost(MasteryProfile.Node node, Map<String, MasteryProfile.Node> nodes, Set<String> seen) {
        if (!seen.add(node.id())) return 0;
        int total = node.cost();
        for (String requirement : node.requires()) {
            MasteryProfile.Node parent = nodes.get(requirement);
            if (parent != null) total += routeCost(parent, nodes, seen);
        }
        return total;
    }

    private static String formatSeconds(int ticks) {
        return ticks % 20 == 0 ? Integer.toString(ticks / 20) : "%.1f".formatted(ticks / 20.0);
    }

    private static Balance balance(MasteryProfile profile, MasteryProfile.Node node) {
        MasteryProfile.Effect effect = node.effect();
        String path = effect.type().getPath();
        if (phase(effect) != 0) {
            String frequency = switch (node.branch()) {
                case "signature" -> "signature ability or passive trigger";
                case "combat" -> "combat event or active follow-up";
                case "transformation" -> "defensive or transformation event";
                default -> "authored ability event";
            };
            return new Balance(frequency, branchTheme(profile, node.branch()));
        }
        return switch (path) {
            case "stormstep" -> new Balance("first successful hit after cooldown", "mobility / engage");
            case "echo_strike" -> new Balance("first successful hit after cooldown", "single-target burst");
            case "combo_strike" -> new Balance(combo(effect), "single-target combo burst");
            case "battle_flow" -> new Balance(combo(effect), "combat tempo / self-buff");
            case "hindering_strike" -> new Balance("first successful hit after cooldown", "control / setup");
            case "finishing_strike" -> new Balance("hit below "
                    + effect.parameters().getOrDefault("health_percent", 30) + "% health after cooldown",
                    "execution");
            case "soul_mend" -> new Balance("eligible kill after cooldown", "sustain");
            case "kill_flow" -> new Balance("eligible kill after cooldown", "kill-chain tempo");
            case "counterstrike" -> new Balance("damage received from a living attacker after cooldown",
                    "retaliation");
            case "guarded_recovery" -> new Balance("damage received after cooldown", "defense / recovery");
            case "leeching_strike" -> new Balance("first successful hit after cooldown", "sustain");
            case "cleaving_echo" -> new Balance(combo(effect), "area pressure");
            case "stormbreak_conduit", "slipstream", "crosswind", "storm_chaser", "flashguard" ->
                    new Balance("each Stormbreak cast", "active mobility / control");
            case "capacitor" -> new Balance("each unique corridor hit", "active burst setup");
            case "afterimage" -> new Balance("once per target per Stormbreak", "path control");
            case "eye_of_storm" -> new Balance("each Stormbreak cast", "focused active burst");
            case "thunderhead" -> new Balance("each Stormbreak cast", "active area control");
            case "static_reserve", "building_voltage" -> new Balance("each successful melee hit", "cooldown cycling");
            case "charged_pursuit", "feedback_loop", "perpetual_motion" ->
                    new Balance("each successful refresh", "combat tempo");
            case "live_wire", "arc_lash" -> new Balance("eligible melee hit after internal cooldown", "lightning pressure");
            case "quickening_current" -> new Balance("eligible kill during pursuit", "kill-chain mobility");
            case "unbroken_pace" -> new Balance("damage received while sprinting after cooldown", "defense / mobility");
            case "flashover" -> new Balance("each successful refresh", "chain burst");
            case "ionize", "pressure_drop", "updraft" -> new Balance("eligible Stormbreak hit", "storm control");
            case "fulmination" -> new Balance("Ionized kill", "area burst");
            case "stormshield" -> new Balance("thunderclap with at least one target", "defense");
            case "reverberation" -> new Balance("each thunderclap", "delayed area burst");
            case "judgment_bolt" -> new Balance("each thunderclap with a survivor", "focused burst");
            case "supercell" -> new Balance("four pulses per Stormbreak", "persistent area control");
            case "sulfurous_edge", "scorching_brand", "blast_furnace", "backdraft", "crucible_strike" ->
                    new Balance("each Brimstone eruption", "melee area burst");
            case "kindling_blows", "flashpoint" -> new Balance("each failed eruption roll", "proc consistency");
            case "cinder_scatter" -> new Balance("up to three targets per eruption", "ranged pressure");
            case "chain_reaction" -> new Balance("up to eight eruption kills", "kill-chain area burst");
            case "lengthened_chain", "furnace_bellows", "stoked_furnace", "shackling_heat" ->
                    new Balance("each Brimstone Rite", "active area control");
            case "overpressure" -> new Balance("each successful rite pulse", "active damage ramp");
            case "snapback" -> new Balance("each successful target jump", "retarget burst");
            case "molten_wake" -> new Balance("once per second per residual field", "persistent area control");
            case "executioners_drop" -> new Balance("each Brimstone Rite", "accelerated plunge burst");
            case "perpetual_furnace" -> new Balance("each Brimstone Rite", "sustained area damage");
            case "cinder_mantle", "tempered_flesh", "walking_furnace" ->
                    new Balance("each Brimstone Rite", "active defense");
            case "heat_sink", "ashen_step" -> new Balance("damage received during Brimstone Rite", "reactive defense");
            case "furnace_reprisal" -> new Balance("damage received after internal cooldown", "retaliation");
            case "forged_resolve" -> new Balance("once per rite below health threshold", "emergency defense");
            case "bulwark_pulse" -> new Balance("rite pulse hitting at least three targets", "area defense");
            case "last_reprisal" -> new Balance("once per rite on low-health crossing", "emergency reprisal");
            default -> Balance.GENERIC;
        };
    }

    private static int phase(MasteryProfile.Effect effect) {
        Matcher matcher = PHASE_EFFECT.matcher(effect.type().getPath());
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static String branchTheme(MasteryProfile profile, String branchId) {
        return BuiltInFamilyProfiles.familiesForCoverage().stream()
                .filter(family -> family.profilePath().equals(profile.id().getPath()))
                .flatMap(family -> family.branches().stream())
                .filter(branch -> branch.id().equals(branchId))
                .map(branch -> branch.title() + " / " + branch.style().name().toLowerCase().replace('_', ' '))
                .findFirst().orElse(branchId);
    }

    private static String combo(MasteryProfile.Effect effect) {
        return "every " + effect.parameters().getOrDefault("hits", 3) + " hits within "
                + formatSeconds(effect.parameters().getOrDefault("window_ticks", 60)) + "s, after cooldown";
    }

    private record Balance(String frequency, String role) {
        private static final Balance GENERIC = new Balance("unclassified", "unclassified");
    }
}
