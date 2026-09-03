package net.sweenus.simplymastery.mastery.definition;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MasteryBalanceReportTest {

    @Test
    void completeCatalogHasExpectedCohortAccountingAndNoWarnings() {
        var profiles = new LinkedHashMap<net.minecraft.util.Identifier, MasteryProfile>();
        BuiltInFamilyProfiles.addMissing(profiles);
        assertEquals(52, profiles.size());
        assertTrue(MasteryBalanceReport.warnings(profiles.values().stream().toList()).isEmpty());
        String report = MasteryBalanceReport.render(profiles.values().stream().toList());
        assertTrue(report.contains("Profiles: 52"));
        assertTrue(report.contains("Nodes: 1404"));
        assertTrue(report.contains("Reference nodes: 54"));
        assertTrue(report.contains("Cohort nodes: 1350"));
        assertTrue(report.contains("Warnings: 0"));
    }
}
