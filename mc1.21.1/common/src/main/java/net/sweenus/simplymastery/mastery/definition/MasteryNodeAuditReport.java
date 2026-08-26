package net.sweenus.simplymastery.mastery.definition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;

public final class MasteryNodeAuditReport {
    private MasteryNodeAuditReport() {
    }

    public static void write(Path output, List<MasteryProfile> profiles) throws IOException {
        Files.createDirectories(output.getParent());
        Files.writeString(output, render(profiles), StandardCharsets.UTF_8);
    }

    static String render(List<MasteryProfile> profiles) {
        EnumMap<Verdict, Integer> counts = new EnumMap<>(Verdict.class);
        for (Verdict verdict : Verdict.values()) counts.put(verdict, 0);
        int nodes = profiles.stream().mapToInt(profile -> profile.nodes().size()).sum();
        for (MasteryProfile profile : profiles) {
            for (MasteryProfile.Node node : profile.nodes()) {
                Verdict verdict = MasteryNodeAuditCatalog.find(profile.id(), node.id())
                        .map(Evidence::verdict).orElse(Verdict.PENDING);
                counts.compute(verdict, (ignored, count) -> count + 1);
            }
        }
        StringBuilder result = new StringBuilder("# Simply Mastery node behavior audit\n\n")
                .append("Profiles: ").append(profiles.size()).append("  \n")
                .append("Nodes: ").append(nodes).append("  \n")
                .append("Reviewed: ").append(nodes - counts.get(Verdict.PENDING)).append("  \n")
                .append("Pending: ").append(counts.get(Verdict.PENDING)).append("\n\n")
                .append("## Verdict totals\n\n")
                .append("| Verdict | Nodes |\n|---|---:|\n");
        for (Verdict verdict : Verdict.values()) {
            result.append("| ").append(label(verdict)).append(" | ").append(counts.get(verdict)).append(" |\n");
        }
        result.append("\n## Node evidence\n\n")
                .append("| Profile | Node | Effect | Base mechanic | Trigger | Definitions | Tuning | Consumer | Cleanup | Test evidence | Verdict | Finding / repair |\n")
                .append("|---|---|---|---|---|---|---|---|---|---|---|---|\n");
        profiles.stream().sorted(java.util.Comparator.comparing(profile -> profile.id().toString()))
                .forEach(profile -> appendProfile(result, profile));
        return result.toString();
    }

    private static void appendProfile(StringBuilder result, MasteryProfile profile) {
        for (MasteryProfile.Node node : profile.nodes()) {
            Evidence evidence = MasteryNodeAuditCatalog.find(profile.id(), node.id()).orElse(Evidence.PENDING);
            result.append("| `").append(profile.id()).append("` | `").append(node.id()).append("` | `")
                    .append(node.effect().type()).append("` | ").append(cell(evidence.baseMechanic()))
                    .append(" | ").append(cell(evidence.trigger())).append(" | ")
                    .append(cell(evidence.definitions())).append(" | ").append(cell(evidence.tuning()))
                    .append(" | ").append(cell(evidence.consumer())).append(" | ")
                    .append(cell(evidence.cleanup())).append(" | ").append(cell(evidence.testEvidence()))
                    .append(" | ").append(label(evidence.verdict())).append(" | ")
                    .append(cell(evidence.finding())).append(" |\n");
        }
    }

    private static String cell(String value) {
        if (value == null || value.isBlank()) return "—";
        return value.replace("|", "\\|").replace('\n', ' ');
    }

    private static String label(Verdict verdict) {
        return verdict.name().toLowerCase().replace('_', ' ');
    }

    public enum Verdict {
        PENDING,
        VERIFIED,
        INERT,
        UNREACHABLE,
        REDUNDANT,
        MISDESCRIBED,
        CONFLICTING,
        UNSAFE,
        REDESIGN_REQUIRED
    }

    public record Evidence(String baseMechanic, String trigger, String definitions, String tuning,
                           String consumer, String cleanup, String testEvidence, Verdict verdict,
                           String finding) {
        private static final Evidence PENDING = new Evidence("", "", "", "", "", "", "",
                Verdict.PENDING, "Awaiting per-weapon audit");
    }
}
