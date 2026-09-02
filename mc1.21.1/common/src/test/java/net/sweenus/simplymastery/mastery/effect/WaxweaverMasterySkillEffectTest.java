package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase7AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase7UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WaxweaverMasterySkillEffectTest {
    @Test
    void nodesRouteOnlyToTheirRuntimeDefinitions() {
        for (int node = 0; node < 27; node++) {
            boolean prison = node < 9 || node == 20 || node == 23;
            boolean tempo = node >= 9 && node < 18;
            boolean revival = node >= 18;
            assertEquals(prison, hasWaxTuning(tune(Phase7UniqueAbilities.WAXWEAVER_PRISON, List.of(node))),
                    "prison " + node);
            assertEquals(tempo, hasWaxTuning(tune(Phase7UniqueAbilities.WAXWEAVER_TEMPO, List.of(node))),
                    "tempo " + node);
            assertEquals(revival, hasWaxTuning(tune(Phase7UniqueAbilities.WAXWEAVER_REVIVAL, List.of(node))),
                    "revival " + node);
        }
    }

    @Test
    void everyNodeWritesDedicatedWaxTuningWithoutGenericValues() {
        for (int node = 0; node < 27; node++) {
            UniqueAbilityDefinition definition = node < 9 || node == 20 || node == 23
                    ? Phase7UniqueAbilities.WAXWEAVER_PRISON
                    : node < 18 ? Phase7UniqueAbilities.WAXWEAVER_TEMPO
                    : Phase7UniqueAbilities.WAXWEAVER_REVIVAL;
            assertTrue(hasWaxTuning(tune(definition, List.of(node))), "node " + node);
        }
        for (UniqueAbilityDefinition definition : List.of(
                Phase7UniqueAbilities.WAXWEAVER_PRISON,
                Phase7UniqueAbilities.WAXWEAVER_TEMPO,
                Phase7UniqueAbilities.WAXWEAVER_REVIVAL)) {
            Phase7AbilityTuning tuning = tune(definition, IntStream.range(0, 27).boxed().toList());
            for (String generic : List.of("COOLDOWN_TICKS", "DURATION_TICKS", "INTERVAL_TICKS",
                    "LOCKOUT_TICKS", "REFUND_TICKS", "CHANCE", "DAMAGE_MULTIPLIER",
                    "SECONDARY_DAMAGE_MULTIPLIER", "FINAL_DAMAGE_MULTIPLIER",
                    "INCOMING_MULTIPLIER", "OUTGOING_MULTIPLIER", "PER_STACK_MULTIPLIER",
                    "RADIUS", "RANGE", "WIDTH", "SPEED", "PULL_STRENGTH", "TARGET_CAP",
                    "SEARCH_CAP", "COUNT", "STACK_CAP", "FIRE_TICKS", "STATUS_DURATION_TICKS",
                    "STATUS_AMPLIFIER", "ABSORPTION", "HEALTH_THRESHOLD")) {
                assertFalse(tuning.has(s(generic)), definition.id() + " leaked " + generic);
            }
        }
    }

    @Test
    void cadenceRefundAndAreaSettingsRemainIndependent() {
        Phase7AbilityTuning prison = tune(Phase7UniqueAbilities.WAXWEAVER_PRISON,
                List.of(2, 3, 5, 6, 8, 20, 23));
        assertEquals(-2, prison.integer(s("WAX_TAUNT_INTERVAL_BONUS_TICKS"), 0));
        assertEquals(10, prison.integer(s("WAX_BRITTLE_DURATION_REDUCTION_TICKS"), 0));
        assertEquals(.75, prison.get(s("WAX_EXPLOSION_RADIUS_BONUS"), 0), 1.0E-6);
        assertEquals(2, prison.get(s("WAX_TAUNT_RADIUS_BONUS"), 0), 1.0E-6);

        Phase7AbilityTuning tempo = tune(Phase7UniqueAbilities.WAXWEAVER_TEMPO,
                List.of(10, 13));
        assertEquals(4, tempo.integer(s("WAX_TEMPO_STACK_CAP"), 0));
        assertEquals(24, tempo.integer(s("WAX_REFUND_CAP_TICKS"), 0));

        Phase7AbilityTuning revival = tune(Phase7UniqueAbilities.WAXWEAVER_REVIVAL,
                List.of(22, 26));
        assertEquals(4, revival.get(s("WAX_MOLTEN_RADIUS"), 0), 1.0E-6);
        assertEquals(8, revival.integer(s("WAX_MOLTEN_TARGET_CAP"), 0));
        assertEquals(6, revival.get(s("WAX_EMERGENCE_RADIUS"), 0), 1.0E-6);
        assertEquals(16, revival.integer(s("WAX_EMERGENCE_TARGET_CAP"), 0));
    }

    private static boolean hasWaxTuning(Phase7AbilityTuning tuning) {
        return java.util.Arrays.stream(Phase7AbilityTuning.Setting.values())
                .filter(setting -> setting.name().startsWith("WAX_"))
                .anyMatch(tuning::has);
    }

    private static Phase7AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(Phase7UniqueAbilities.TUNING, Phase7AbilityTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) {
            builder.set(Phase7UniqueAbilities.COOLDOWN_TICKS, 260);
        }
        MasteryProfile profile = BuiltInFamilyProfiles.profile("waxweaver");
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(Phase7UniqueAbilities.TUNING);
    }

    private static Phase7AbilityTuning.Setting s(String name) {
        return Phase7AbilityTuning.Setting.valueOf(name);
    }
}
