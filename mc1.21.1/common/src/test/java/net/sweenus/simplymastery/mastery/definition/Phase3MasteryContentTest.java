package net.sweenus.simplymastery.mastery.definition;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.effect.SkillEffectRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Phase3MasteryContentTest {
    private static final List<String> PROFILES = List.of("stormscale", "ionbound_stormscale",
            "soulrender", "soulstalker", "whisperwind", "dreadwhisper");

    @Test
    void phase3TreesUseStableOrderAndIdentityMigration() {
        for (int profileIndex = 0; profileIndex < PROFILES.size(); profileIndex++) {
            MasteryProfile profile = BuiltInFamilyProfiles.profile(PROFILES.get(profileIndex));
            assertEquals(3, profile.version());
            assertEquals(27, profile.nodes().size());
            assertTrue(profile.migrations().stream().anyMatch(migration -> migration.fromVersion() == 2
                    && migration.toVersion() == 3 && migration.renamedNodes().isEmpty()
                    && migration.removedNodeRefunds().isEmpty()));
            for (int nodeIndex = 0; nodeIndex < profile.nodes().size(); nodeIndex++) {
                MasteryProfile.Node node = profile.nodes().get(nodeIndex);
                assertEquals(Identifier.of("simplymastery", "phase3_mastery"), node.effect().type());
                assertEquals(profileIndex * 27 + nodeIndex, node.effect().parameters().get("kind"));
                assertEquals(node.nameKey() + ".description", node.descriptionKey());
            }
        }
        assertEquals(BuiltInFamilyProfiles.profile("stormscale").progressionGroup(),
                BuiltInFamilyProfiles.profile("ionbound_stormscale").progressionGroup());
        assertEquals(BuiltInFamilyProfiles.profile("soulrender").progressionGroup(),
                BuiltInFamilyProfiles.profile("soulstalker").progressionGroup());
        assertEquals(BuiltInFamilyProfiles.profile("whisperwind").progressionGroup(),
                BuiltInFamilyProfiles.profile("dreadwhisper").progressionGroup());
    }

    @Test
    void phase3ParameterBoundsRejectUnknownKindsAndFields() {
        List<String> errors = new ArrayList<>();
        SkillEffectRegistry.validate(new MasteryProfile.Effect(
                Identifier.of("simplymastery", "phase3_mastery"), Map.of("kind", 162)), "node: ", errors);
        assertTrue(errors.stream().anyMatch(error -> error.contains("between 0 and 161")));
        errors.clear();
        SkillEffectRegistry.validate(new MasteryProfile.Effect(
                Identifier.of("simplymastery", "phase3_mastery"), Map.of("kind", 0, "extra", 1)),
                "node: ", errors);
        assertTrue(errors.stream().anyMatch(error -> error.contains("requires only kind")));
    }
}
