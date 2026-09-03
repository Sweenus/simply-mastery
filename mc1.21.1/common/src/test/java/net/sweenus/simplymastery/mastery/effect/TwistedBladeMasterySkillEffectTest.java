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

final class TwistedBladeMasterySkillEffectTest {
    @Test
    void allNodesRouteOnlyToTheirOwnedDefinition() {
        List<UniqueAbilityDefinition> definitions = List.of(Phase8UniqueAbilities.TWISTED_FEROCITY,
                Phase8UniqueAbilities.TWISTED_CRESCENDO, Phase8UniqueAbilities.TWISTED_FINALE);
        for (int node = 0; node < 27; node++) {
            for (int branch = 0; branch < 3; branch++) {
                assertEquals(node / 9 == branch, hasTwistedSetting(tune(
                        definitions.get(branch), List.of(node))), node + ":" + branch);
            }
        }
    }

    @Test
    void nodesDoNotWriteGenericNumericSettings() {
        List<UniqueAbilityDefinition> definitions = List.of(Phase8UniqueAbilities.TWISTED_FEROCITY,
                Phase8UniqueAbilities.TWISTED_CRESCENDO, Phase8UniqueAbilities.TWISTED_FINALE);
        for (int branch = 0; branch < 3; branch++) {
            Phase8AbilityTuning tuning = tune(definitions.get(branch),
                    IntStream.range(branch * 9, branch * 9 + 9).boxed().toList());
            for (String generic : List.of("DURATION_TICKS", "DURATION_CAP_TICKS", "INTERVAL_TICKS",
                    "LOCKOUT_TICKS", "DELAY_TICKS", "CHANCE", "DAMAGE_MULTIPLIER",
                    "SECONDARY_DAMAGE_MULTIPLIER", "FINAL_DAMAGE_MULTIPLIER", "OUTGOING_MULTIPLIER",
                    "PER_STACK_MULTIPLIER", "RADIUS", "RANGE", "PULL_STRENGTH", "KNOCKBACK",
                    "TARGET_CAP", "COUNT", "STACK_CAP", "STATUS_DURATION_TICKS", "HEALTH_THRESHOLD")) {
                assertFalse(tuning.has(s(generic)), definitions.get(branch).id() + " leaked " + generic);
            }
        }
    }

    @Test
    void pathsAndCapstonesKeepIndependentValues() {
        Phase8AbilityTuning ferocity = tune(Phase8UniqueAbilities.TWISTED_FEROCITY,
                List.of(0, 1, 2, 4, 6, 8));
        assertEquals(8, ferocity.integer(s("TWISTED_CHANCE_BONUS"), 0));
        assertEquals(30, ferocity.integer(s("TWISTED_DURATION_BONUS_TICKS"), 0));
        assertEquals(.01, ferocity.get(s("TWISTED_ATTACK_SPEED_PER_STACK_BONUS"), 0), 1.0E-6);
        assertEquals(8, ferocity.integer(s("TWISTED_CADENCE_THRESHOLD"), 0));
        assertEquals(3, ferocity.integer(s("TWISTED_MAX_STACK_BONUS"), 0));
        assertEquals(20, ferocity.integer(s("TWISTED_FEVER_MAX_STACKS"), 0));

        Phase8AbilityTuning crescendo = tune(Phase8UniqueAbilities.TWISTED_CRESCENDO,
                List.of(9, 10, 13, 15, 17));
        assertEquals(1.1, crescendo.get(s("TWISTED_CRESCENDO_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(.4, crescendo.get(s("TWISTED_CRESCENDO_RADIUS_BONUS"), 0), 1.0E-6);
        assertEquals(1.15, crescendo.get(s("TWISTED_SYNC_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(.6, crescendo.get(s("TWISTED_DOUBLE_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(.65, crescendo.get(s("TWISTED_ORCHESTRA_DAMAGE_MULTIPLIER"), 0), 1.0E-6);

        Phase8AbilityTuning finale = tune(Phase8UniqueAbilities.TWISTED_FINALE,
                List.of(18, 19, 20, 24, 26));
        assertEquals(30, finale.integer(s("TWISTED_FINALE_WINDOW_BONUS_TICKS"), 0));
        assertEquals(1.15, finale.get(s("TWISTED_FINALE_MIN_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(1.2, finale.get(s("TWISTED_FINALE_MAX_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(1.2, finale.get(s("TWISTED_PERFECT_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(.55, finale.get(s("TWISTED_SUSTAINED_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
    }

    private static boolean hasTwistedSetting(Phase8AbilityTuning tuning) {
        return Arrays.stream(Phase8AbilityTuning.Setting.values())
                .filter(setting -> setting.name().startsWith("TWISTED_"))
                .anyMatch(tuning::has);
    }

    private static Phase8AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(Phase8UniqueAbilities.TUNING, Phase8AbilityTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(Phase8UniqueAbilities.COOLDOWN_TICKS, 1);
        MasteryProfile profile = BuiltInFamilyProfiles.profile("twisted_blade");
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(Phase8UniqueAbilities.TUNING);
    }

    private static Phase8AbilityTuning.Setting s(String name) {
        return Phase8AbilityTuning.Setting.valueOf(name);
    }
}
