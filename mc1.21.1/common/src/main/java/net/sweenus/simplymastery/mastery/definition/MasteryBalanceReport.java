package net.sweenus.simplymastery.mastery.definition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MasteryBalanceReport {

    private MasteryBalanceReport() {
    }

    public static void write(Path output, List<MasteryProfile> profiles) throws IOException {
        Files.createDirectories(output.getParent());
        Files.writeString(output, render(profiles), StandardCharsets.UTF_8);
    }

    static String render(List<MasteryProfile> profiles) {
        StringBuilder markdown = new StringBuilder("# Simply Mastery balance ledger\n\n")
                .append("Profiles: ").append(profiles.size()).append("  \n")
                .append("Nodes: ").append(profiles.stream().mapToInt(profile -> profile.nodes().size()).sum())
                .append("\n\n")
                .append("Route cost includes the node and all of its prerequisites. Cooldowns are shown in seconds.\n\n")
                .append("| Profile | Branch | Node | Kind | Route | Effect | Cooldown | Trigger frequency | Combat role |\n")
                .append("|---|---|---|---|---:|---|---:|---|---|\n");
        profiles.stream().sorted(java.util.Comparator.comparing(profile -> profile.id().toString()))
                .forEach(profile -> appendProfile(markdown, profile));
        return markdown.toString();
    }

    private static void appendProfile(StringBuilder markdown, MasteryProfile profile) {
        Map<String, MasteryProfile.Node> nodes = new HashMap<>();
        profile.nodes().forEach(node -> nodes.put(node.id(), node));
        for (MasteryProfile.Node node : profile.nodes()) {
            Balance balance = balance(node.effect());
            int cooldown = node.effect().parameters().getOrDefault("cooldown_ticks", 0);
            markdown.append("| `").append(profile.id()).append("` | `").append(node.branch())
                    .append("` | `").append(node.id()).append("` | ")
                    .append(node.capstone() ? "capstone" : "regular").append(" | ")
                    .append(routeCost(node, nodes, new HashSet<>())).append(" | `")
                    .append(node.effect().type()).append("` | ")
                    .append(cooldown == 0 ? "—" : formatSeconds(cooldown)).append(" | ")
                    .append(balance.frequency()).append(" | ").append(balance.role()).append(" |\n");
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

    private static Balance balance(MasteryProfile.Effect effect) {
        String path = effect.type().getPath();
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
            default -> new Balance("event-driven", "utility");
        };
    }

    private static String combo(MasteryProfile.Effect effect) {
        return "every " + effect.parameters().getOrDefault("hits", 3) + " hits within "
                + formatSeconds(effect.parameters().getOrDefault("window_ticks", 60)) + "s, after cooldown";
    }

    private record Balance(String frequency, String role) {
    }
}
