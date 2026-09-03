package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase8AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase8UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ShadowstingMasterySkillEffectTest {
    @Test
    void signatureNodesReachOnlyShadowEchoAndTheRestOnlyShadowDance() {
        for (int node = 0; node < 27; node++) {
            boolean signature = node < 9;
            assertEquals(signature, hasShadowSetting(tune(
                    Phase8UniqueAbilities.SHADOW_ECHO, List.of(node))), "echo:" + node);
            assertEquals(!signature, hasShadowSetting(tune(
                    Phase8UniqueAbilities.SHADOW_DANCE, List.of(node))), "dance:" + node);
        }
    }

    @Test
    void nodesDoNotWriteGenericNumericSettings() {
        for (UniqueAbilityDefinition definition : List.of(Phase8UniqueAbilities.SHADOW_ECHO,
                Phase8UniqueAbilities.SHADOW_DANCE)) {
            Phase8AbilityTuning tuning = tune(definition, IntStream.range(0, 27).boxed().toList());
            for (String generic : List.of("DURATION_TICKS", "SECONDARY_DURATION_TICKS", "INTERVAL_TICKS",
                    "LOCKOUT_TICKS", "REFUND_TICKS", "DELAY_TICKS", "CHANCE", "DAMAGE_MULTIPLIER",
                    "SECONDARY_DAMAGE_MULTIPLIER", "FINAL_DAMAGE_MULTIPLIER", "OUTGOING_MULTIPLIER",
                    "PER_STACK_MULTIPLIER", "RADIUS", "SECONDARY_RADIUS", "RANGE", "TARGET_CAP",
                    "SEARCH_CAP", "COUNT", "ABSORPTION", "STATUS_DURATION_TICKS", "STATUS_AMPLIFIER",
                    "HEALTH_THRESHOLD")) {
                assertFalse(tuning.has(s(generic)), definition.id() + " leaked " + generic);
            }
        }
    }

    @Test
    void everyNodeSetsItsOwnModeBit() {
        for (int node = 0; node < 27; node++) {
            UniqueAbilityDefinition definition = node < 9
                    ? Phase8UniqueAbilities.SHADOW_ECHO : Phase8UniqueAbilities.SHADOW_DANCE;
            assertTrue(tune(definition, List.of(node)).flag(1 << node), "bit:" + node);
        }
    }

    @Test
    void cloneAndDanceChannelsStayIndependent() {
        Phase8AbilityTuning echo = tune(Phase8UniqueAbilities.SHADOW_ECHO, List.of(0, 1, 2, 3, 5, 6));
        assertEquals(8, echo.integer(s("SHADOW_CHANCE_BONUS"), 0));
        assertEquals(1.12, echo.get(s("SHADOW_CLONE_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(-1, echo.integer(s("SHADOW_CLONE_DELAY_BONUS_TICKS"), 0));
        assertEquals(60, echo.integer(s("SHADOW_MARK_DURATION_TICKS"), 0));
        assertEquals(1.08, echo.get(s("SHADOW_MARK_OUTGOING_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(.5, echo.get(s("SHADOW_TWIN_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(20, echo.integer(s("SHADOW_KILL_REFUND_TICKS"), 0));

        Phase8AbilityTuning mirror = tune(Phase8UniqueAbilities.SHADOW_ECHO, List.of(0, 1, 7));
        assertEquals(8, mirror.integer(s("SHADOW_CHANCE_BONUS"), 0));
        assertEquals(15, mirror.integer(s("SHADOW_CHANCE_PENALTY"), 0));
        assertEquals(1.12, mirror.get(s("SHADOW_CLONE_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(.45, mirror.get(s("SHADOW_MIRROR_DAMAGE_MULTIPLIER"), 0), 1.0E-6);

        Phase8AbilityTuning dance = tune(Phase8UniqueAbilities.SHADOW_DANCE, List.of(9, 10, 11, 15, 16));
        assertEquals(80, dance.integer(s("SHADOW_DANCE_DURATION_BONUS_TICKS"), 0));
        assertEquals(1, dance.integer(s("SHADOW_DANCE_INTERVAL_BONUS"), 0));
        assertEquals(3, dance.integer(s("SHADOW_DANCE_INTERVAL_FLOOR"), 0));
        assertEquals(2, dance.integer(s("SHADOW_DANCE_RADIUS_BONUS"), 0));
        assertEquals(3, dance.integer(s("SHADOW_FLOURISH_RADIUS"), 0));
        assertEquals(.6, dance.get(s("SHADOW_FLOURISH_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(.65, dance.get(s("SHADOW_MACABRE_DAMAGE_MULTIPLIER"), 0), 1.0E-6);

        Phase8AbilityTuning veil = tune(Phase8UniqueAbilities.SHADOW_DANCE, List.of(19, 20, 22, 23, 25, 26));
        assertEquals(2.5, veil.get(s("SHADOW_SMOKE_RADIUS"), 0), 1.0E-6);
        assertEquals(3, veil.get(s("SHADOW_SNARE_RADIUS"), 0), 1.0E-6);
        assertEquals(25, veil.integer(s("SHADOW_DISORIENT_DURATION_TICKS"), 0));
        assertEquals(40, veil.integer(s("SHADOW_SPEED_DURATION_TICKS"), 0));
        assertEquals(60, veil.integer(s("SHADOW_NOCTURNE_DURATION_TICKS"), 0));
        assertEquals(30, veil.integer(s("SHADOW_KILLING_RESISTANCE_TICKS"), 0));
        assertEquals(2, veil.integer(s("SHADOW_KILLING_SKIPPED_STRIKES"), 0));
    }

    private static boolean hasShadowSetting(Phase8AbilityTuning tuning) {
        return Arrays.stream(Phase8AbilityTuning.Setting.values())
                .filter(setting -> setting.name().startsWith("SHADOW_"))
                .anyMatch(tuning::has);
    }

    private static Phase8AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(Phase8UniqueAbilities.TUNING, Phase8AbilityTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(Phase8UniqueAbilities.COOLDOWN_TICKS, 200);
        MasteryProfile profile = BuiltInFamilyProfiles.profile("shadowsting");
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(Phase8UniqueAbilities.TUNING);
    }

    private static Phase8AbilityTuning.Setting s(String name) {
        return Phase8AbilityTuning.Setting.valueOf(name);
    }
}
