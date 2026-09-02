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

final class Phase8RemainingMasterySkillEffectTest {
    @Test
    void plagueNodesRouteToTheirOwnedDefinitions() {
        List<UniqueAbilityDefinition> definitions = List.of(Phase8UniqueAbilities.PLAGUE_PESTILENCE,
                Phase8UniqueAbilities.PLAGUE_DEATH_KNELL, Phase8UniqueAbilities.PLAGUE_OUTBREAK);
        for (int node = 0; node < 27; node++) {
            for (int branch = 0; branch < 3; branch++) {
                assertEquals(node / 9 == branch, hasPrefix(tune("toxic_longsword",
                        definitions.get(branch), List.of(node)), "PLAGUE_"), node + ":" + branch);
            }
        }
    }

    @Test
    void soulkeeperNodesRouteToTheirOwnedDefinitions() {
        List<UniqueAbilityDefinition> definitions = List.of(Phase8UniqueAbilities.SOULKEEPER_LANTERNS,
                Phase8UniqueAbilities.SOULKEEPER_VELOCITY, Phase8UniqueAbilities.SOULKEEPER_CONCLAVE);
        for (int node = 0; node < 27; node++) {
            for (int branch = 0; branch < 3; branch++) {
                assertEquals(node / 9 == branch, hasPrefix(tune("soulkeeper",
                        definitions.get(branch), List.of(node)), "SOUL"), node + ":" + branch);
            }
        }
    }

    @Test
    void remainingPhase8WeaponsDoNotWriteGenericNumericSettings() {
        for (String profile : List.of("toxic_longsword", "soulkeeper")) {
            List<UniqueAbilityDefinition> definitions = profile.equals("toxic_longsword")
                    ? List.of(Phase8UniqueAbilities.PLAGUE_PESTILENCE,
                    Phase8UniqueAbilities.PLAGUE_DEATH_KNELL, Phase8UniqueAbilities.PLAGUE_OUTBREAK)
                    : List.of(Phase8UniqueAbilities.SOULKEEPER_LANTERNS,
                    Phase8UniqueAbilities.SOULKEEPER_VELOCITY, Phase8UniqueAbilities.SOULKEEPER_CONCLAVE);
            for (int branch = 0; branch < 3; branch++) {
                Phase8AbilityTuning tuning = tune(profile, definitions.get(branch),
                        IntStream.range(branch * 9, branch * 9 + 9).boxed().toList());
                for (String generic : List.of("COOLDOWN_TICKS", "DURATION_TICKS", "INTERVAL_TICKS",
                        "LOCKOUT_TICKS", "DELAY_TICKS", "CHANCE", "FLAT_DAMAGE", "DAMAGE_MULTIPLIER",
                        "SECONDARY_DAMAGE_MULTIPLIER", "FINAL_DAMAGE_MULTIPLIER", "OUTGOING_MULTIPLIER",
                        "PER_STACK_MULTIPLIER", "RADIUS", "SECONDARY_RADIUS", "RANGE", "SPEED",
                        "PULL_STRENGTH", "TARGET_CAP", "COUNT", "STACK_CAP", "FEVER",
                        "STATUS_DURATION_TICKS", "ABSORPTION", "HEALTH_THRESHOLD")) {
                    assertFalse(tuning.has(s(generic)), profile + " leaked " + generic);
                }
            }
        }
    }

    @Test
    void capstoneValuesComposeWithoutColliding() {
        Phase8AbilityTuning funeral = tune("toxic_longsword",
                Phase8UniqueAbilities.PLAGUE_DEATH_KNELL, List.of(9, 16));
        assertEquals(1.1, funeral.get(s("PLAGUE_TOLL_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(.65, funeral.get(s("PLAGUE_PAIRED_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(.8, funeral.get(s("PLAGUE_PAIRED_RADIUS_MULTIPLIER"), 0), 1.0E-6);

        Phase8AbilityTuning grand = tune("soulkeeper",
                Phase8UniqueAbilities.SOULKEEPER_CONCLAVE, List.of(18, 25));
        assertEquals(60, grand.integer(s("SOUL_EXTRA_DURATION_BONUS_TICKS"), 0));
        assertEquals(180, grand.integer(s("SOUL_GRAND_DURATION_TICKS"), 0));
        assertEquals(6, grand.integer(s("SOUL_GRAND_LANTERN_COUNT"), 0));
    }

    private static boolean hasPrefix(Phase8AbilityTuning tuning, String prefix) {
        return Arrays.stream(Phase8AbilityTuning.Setting.values())
                .filter(setting -> setting.name().startsWith(prefix))
                .anyMatch(tuning::has);
    }

    private static Phase8AbilityTuning tune(String profileId, UniqueAbilityDefinition definition,
                                            List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(Phase8UniqueAbilities.TUNING, Phase8AbilityTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(Phase8UniqueAbilities.COOLDOWN_TICKS, 800);
        MasteryProfile profile = BuiltInFamilyProfiles.profile(profileId);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(Phase8UniqueAbilities.TUNING);
    }

    private static Phase8AbilityTuning.Setting s(String name) {
        return Phase8AbilityTuning.Setting.valueOf(name);
    }
}
