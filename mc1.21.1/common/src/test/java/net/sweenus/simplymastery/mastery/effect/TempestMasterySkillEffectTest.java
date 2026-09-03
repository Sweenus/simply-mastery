package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.StormFrostWaterMasteryTuning;
import net.sweenus.simplyswords.api.ability.StormFrostWaterMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TempestMasterySkillEffectTest {
    @Test
    void nodesRouteToTheDefinitionThatConsumesThem() {
        assertTrue(tune(StormFrostWaterMasteryAbilities.TEMPEST_VORTEX, List.of(0))
                .has(s("TEMPEST_START_RADIUS_BONUS")));
        assertFalse(tune(StormFrostWaterMasteryAbilities.TEMPEST_MARK, List.of(0))
                .has(s("TEMPEST_START_RADIUS_BONUS")));

        assertTrue(tune(StormFrostWaterMasteryAbilities.TEMPEST_MARK, List.of(9))
                .has(s("TEMPEST_MARK_DURATION_BONUS_TICKS")));
        assertFalse(tune(StormFrostWaterMasteryAbilities.TEMPEST_VORTEX, List.of(9))
                .has(s("TEMPEST_MARK_DURATION_BONUS_TICKS")));

        assertTrue(tune(StormFrostWaterMasteryAbilities.TEMPEST_MARK, List.of(17))
                .has(s("TEMPEST_PRISMATIC_STACK_CAP")));
        assertTrue(tune(StormFrostWaterMasteryAbilities.TEMPEST_VORTEX, List.of(17))
                .has(s("TEMPEST_PRISMATIC_DAMAGE_MULTIPLIER")));

        assertTrue(tune(StormFrostWaterMasteryAbilities.TEMPEST_VORTEX, List.of(18))
                .has(s("TEMPEST_RECALL_RETAIN_COUNT")));
        assertFalse(tune(StormFrostWaterMasteryAbilities.TEMPEST_MARK, List.of(18))
                .has(s("TEMPEST_RECALL_RETAIN_COUNT")));
    }

    @Test
    void everyNodeWritesAScopedKeyAndItsModeBit() {
        for (int node = 0; node < 27; node++) {
            UniqueAbilityDefinition definition = node >= 9 && node < 17
                    ? StormFrostWaterMasteryAbilities.TEMPEST_MARK : StormFrostWaterMasteryAbilities.TEMPEST_VORTEX;
            StormFrostWaterMasteryTuning tuning = tune(definition, List.of(node));
            assertTrue(java.util.Arrays.stream(StormFrostWaterMasteryTuning.Setting.values())
                    .filter(setting -> setting.name().startsWith("TEMPEST_"))
                    .anyMatch(tuning::has), "node " + node);
            assertEquals(1 << node, tuning.integer(s("MODE"), 0), "node " + node);
        }
    }

    @Test
    void noTempestNodeWritesSharedGenericKeys() {
        StormFrostWaterMasteryTuning mark = tune(StormFrostWaterMasteryAbilities.TEMPEST_MARK,
                IntStream.range(0, 27).boxed().toList());
        StormFrostWaterMasteryTuning vortex = tune(StormFrostWaterMasteryAbilities.TEMPEST_VORTEX,
                IntStream.range(0, 27).boxed().toList());
        for (String generic : List.of("DURATION_TICKS", "COUNT", "RADIUS", "TARGET_CAP", "STACK_CAP",
                "LOCKOUT_TICKS", "ABSORPTION", "STATUS_DURATION_TICKS", "DAMAGE_MULTIPLIER",
                "SECONDARY_DAMAGE_MULTIPLIER", "FINAL_DAMAGE_MULTIPLIER", "OUTGOING_MULTIPLIER",
                "PER_STACK_MULTIPLIER", "RANGE", "PULL_STRENGTH", "INTERVAL_TICKS", "SEARCH_CAP")) {
            assertFalse(mark.has(s(generic)), "mark " + generic);
            assertFalse(vortex.has(s(generic)), "vortex " + generic);
        }
    }

    @Test
    void signatureAndTransformationMultipliersCompose() {
        StormFrostWaterMasteryTuning tuning = tune(StormFrostWaterMasteryAbilities.TEMPEST_VORTEX,
                List.of(1, 2, 5, 8, 19, 25));

        assertEquals(1.1, tuning.get(s("TEMPEST_RADIUS_PER_STACK_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(40, tuning.integer(s("TEMPEST_DURATION_BONUS_TICKS"), 0));
        assertEquals(1.35, tuning.get(s("TEMPEST_MAX_SIZE_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(1.1 * 1.35, tuning.get(s("TEMPEST_VORTEX_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(.12, tuning.get(s("TEMPEST_CONSUMED_DAMAGE_PER_STACK"), 0), 1.0E-6);
        assertEquals(1.8, tuning.get(s("TEMPEST_SINGULARITY_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
    }

    @Test
    void elementalExodusAddsWavesWithoutSuppressingTheVortex() {
        StormFrostWaterMasteryTuning tuning = tune(StormFrostWaterMasteryAbilities.TEMPEST_VORTEX, List.of(26));
        assertEquals(2, tuning.integer(s("TEMPEST_WAVE_COUNT"), 0));
        assertEquals(.75, tuning.get(s("TEMPEST_WAVE_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertFalse(tuning.has(s("DURATION_TICKS")));
    }

    private static StormFrostWaterMasteryTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("tempest");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(StormFrostWaterMasteryAbilities.TUNING, StormFrostWaterMasteryTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(StormFrostWaterMasteryAbilities.COOLDOWN_TICKS, 200);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(StormFrostWaterMasteryAbilities.TUNING);
    }

    private static StormFrostWaterMasteryTuning.Setting s(String name) {
        return StormFrostWaterMasteryTuning.Setting.valueOf(name);
    }
}
