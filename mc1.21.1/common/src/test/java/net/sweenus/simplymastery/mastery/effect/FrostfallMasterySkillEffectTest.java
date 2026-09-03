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

final class FrostfallMasterySkillEffectTest {
    @Test
    void throwAndFieldNodesRouteToTheirConsumers() {
        assertTrue(tune(StormFrostWaterMasteryAbilities.FROSTFALL_THROW, List.of(0)).tuning
                .has(s("FROSTFALL_DIRECT_DAMAGE_MULTIPLIER")));
        assertFalse(tune(StormFrostWaterMasteryAbilities.FROSTFALL_FIELD, List.of(0)).tuning
                .has(s("FROSTFALL_DIRECT_DAMAGE_MULTIPLIER")));
        assertTrue(tune(StormFrostWaterMasteryAbilities.FROSTFALL_THROW, List.of(17)).tuning
                .has(s("FROSTFALL_RECALL_DAMAGE_MULTIPLIER")));
        assertTrue(tune(StormFrostWaterMasteryAbilities.FROSTFALL_FIELD, List.of(18)).tuning
                .has(s("FROSTFALL_PULSE_RADIUS_BONUS")));
        assertFalse(tune(StormFrostWaterMasteryAbilities.FROSTFALL_THROW, List.of(18)).tuning
                .has(s("FROSTFALL_PULSE_RADIUS_BONUS")));
    }

    @Test
    void everyNodeWritesDedicatedTuningAndItsModeBit() {
        for (int node = 0; node < 27; node++) {
            UniqueAbilityDefinition definition = node < 18
                    ? StormFrostWaterMasteryAbilities.FROSTFALL_THROW : StormFrostWaterMasteryAbilities.FROSTFALL_FIELD;
            StormFrostWaterMasteryTuning tuning = tune(definition, List.of(node)).tuning;
            assertTrue(java.util.Arrays.stream(StormFrostWaterMasteryTuning.Setting.values())
                    .filter(setting -> setting.name().startsWith("FROSTFALL_"))
                    .anyMatch(tuning::has), "node " + node);
            assertEquals(1 << node, tuning.integer(s("MODE"), 0), "node " + node);
        }
    }

    @Test
    void noFrostfallNodeWritesSharedGenericKeys() {
        StormFrostWaterMasteryTuning throwTuning = tune(StormFrostWaterMasteryAbilities.FROSTFALL_THROW,
                IntStream.range(0, 27).boxed().toList()).tuning;
        StormFrostWaterMasteryTuning fieldTuning = tune(StormFrostWaterMasteryAbilities.FROSTFALL_FIELD,
                IntStream.range(0, 27).boxed().toList()).tuning;
        for (String generic : List.of("COOLDOWN_TICKS", "DURATION_TICKS", "LOCKOUT_TICKS", "REFUND_TICKS",
                "DAMAGE_MULTIPLIER", "SECONDARY_DAMAGE_MULTIPLIER", "FINAL_DAMAGE_MULTIPLIER",
                "OUTGOING_MULTIPLIER", "RADIUS", "RANGE", "SPEED", "PULL_STRENGTH", "TARGET_CAP",
                "COUNT", "PULSE_COUNT", "FREEZE_TICKS", "STATUS_DURATION_TICKS", "STATUS_AMPLIFIER")) {
            assertFalse(throwTuning.has(s(generic)), "throw " + generic);
            assertFalse(fieldTuning.has(s(generic)), "field " + generic);
        }
    }

    @Test
    void skirmisherCooldownIsRelativeToTheLiveDefinitionCooldown() {
        Tuned tuned = tune(StormFrostWaterMasteryAbilities.FROSTFALL_THROW, List.of(8));
        assertEquals(.5, tuned.tuning.get(s("FROSTFALL_COOLDOWN_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(30, tuned.cooldown);
    }

    @Test
    void returnSpeedModifiersCompose() {
        StormFrostWaterMasteryTuning tuning = tune(StormFrostWaterMasteryAbilities.FROSTFALL_THROW, List.of(8, 9)).tuning;
        assertEquals(1.25 * 1.15, tuning.get(s("FROSTFALL_RETURN_SPEED_MULTIPLIER"), 1), 1.0E-6);
    }

    private static Tuned tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("frostfall");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(StormFrostWaterMasteryAbilities.TUNING, StormFrostWaterMasteryTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(StormFrostWaterMasteryAbilities.COOLDOWN_TICKS, 60);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return new Tuned(builder.get(StormFrostWaterMasteryAbilities.TUNING),
                definition.cooldownKey().isPresent() ? builder.get(StormFrostWaterMasteryAbilities.COOLDOWN_TICKS) : 0);
    }

    private static StormFrostWaterMasteryTuning.Setting s(String name) {
        return StormFrostWaterMasteryTuning.Setting.valueOf(name);
    }

    private record Tuned(StormFrostWaterMasteryTuning tuning, int cooldown) {
    }
}
