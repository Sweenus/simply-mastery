package net.sweenus.simplymastery.mastery.definition;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MasteryNodeAuditReportTest {
    @Test
    void reportAccountsForEveryNodeThroughThePhase3Review() {
        Map<Identifier, MasteryProfile> profiles = new LinkedHashMap<>();
        BuiltInFamilyProfiles.addMissing(profiles);

        String report = MasteryNodeAuditReport.render(profiles.values().stream().toList());

        assertEquals(1_404, report.lines().filter(line -> line.startsWith("| `simplymastery:")).count());
        assertTrue(report.contains("Profiles: 52"));
        assertTrue(report.contains("Nodes: 1404"));
        assertTrue(report.contains("Reviewed: 378"));
        assertTrue(report.contains("Pending: 1026"));
        assertTrue(report.contains("| verified | 378 |"));
        assertTrue(report.contains("| unreachable | 0 |"));
        assertTrue(report.contains("| redundant | 0 |"));
        assertTrue(report.contains("| conflicting | 0 |"));
        assertTrue(report.contains("| unsafe | 0 |"));
        assertTrue(report.contains("| redesign required | 0 |"));
    }
}
