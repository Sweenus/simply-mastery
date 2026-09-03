package net.sweenus.simplymastery.mastery.definition;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.effect.SkillEffectRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class StormFrostWaterMasteryContentTest {
    private static final List<String> PROFILES = List.of(
            "stormbringer", "mjolnir", "thunderbrand", "tempest", "frostfall", "icewhisper", "livyatan");

    @Test
    void stormFrostWaterTreesUseStableOrderAndIdentityMigration() {
        for (int profileIndex = 0; profileIndex < PROFILES.size(); profileIndex++) {
            MasteryProfile profile = BuiltInFamilyProfiles.profile(PROFILES.get(profileIndex));
            assertEquals(4, profile.version());
            assertEquals(27, profile.nodes().size());
            assertTrue(profile.migrations().stream().anyMatch(migration -> migration.fromVersion() == 3
                    && migration.toVersion() == 4 && migration.renamedNodes().isEmpty()
                    && migration.removedNodeRefunds().isEmpty()));
            for (int nodeIndex = 0; nodeIndex < profile.nodes().size(); nodeIndex++) {
                MasteryProfile.Node node = profile.nodes().get(nodeIndex);
                assertEquals(Identifier.of("simplymastery", "cohort/storm_frost_water"), node.effect().type());
                assertEquals(profileIndex * 27 + nodeIndex, node.effect().parameters().get("kind"));
                assertEquals(node.nameKey() + ".description", node.descriptionKey());
            }
        }
    }

    @Test
    void stormFrostWaterParameterBoundsRejectUnknownKindsAndFields() {
        List<String> errors = new ArrayList<>();
        SkillEffectRegistry.validate(new MasteryProfile.Effect(
                Identifier.of("simplymastery", "cohort/storm_frost_water"), Map.of("kind", 189)), "node: ", errors);
        assertTrue(errors.stream().anyMatch(error -> error.contains("between 0 and 188")));
        errors.clear();
        SkillEffectRegistry.validate(new MasteryProfile.Effect(
                Identifier.of("simplymastery", "cohort/storm_frost_water"), Map.of("kind", 0, "extra", 1)),
                "node: ", errors);
        assertTrue(errors.stream().anyMatch(error -> error.contains("requires only kind")));
    }
}
