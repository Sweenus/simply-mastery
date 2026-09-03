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

final class BloodwakeMasterySkillEffectTest {
    private static final List<UniqueAbilityDefinition> DEFINITIONS = List.of(
            Phase8UniqueAbilities.BLOOD_BURST,
            Phase8UniqueAbilities.BLOOD_RITES,
            Phase8UniqueAbilities.BLOOD_GROUND);

    @Test
    void allNodesRouteOnlyToTheirOwnedDefinition() {
        for (int node = 0; node < 27; node++) {
            for (int branch = 0; branch < 3; branch++) {
                assertEquals(node / 9 == branch, hasBloodSetting(tune(
                        DEFINITIONS.get(branch), List.of(node))), node + ":" + branch);
            }
        }
    }

    @Test
    void nodesDoNotWriteGenericNumericSettings() {
        for (int branch = 0; branch < 3; branch++) {
            Phase8AbilityTuning tuning = tune(DEFINITIONS.get(branch),
                    IntStream.range(branch * 9, branch * 9 + 9).boxed().toList());
            for (String generic : List.of("COOLDOWN_TICKS", "DURATION_TICKS", "INTERVAL_TICKS",
                    "WINDUP_TICKS", "DELAY_TICKS", "DAMAGE_MULTIPLIER", "SECONDARY_DAMAGE_MULTIPLIER",
                    "OUTGOING_MULTIPLIER", "RADIUS", "RANGE", "ANGLE", "TARGET_CAP", "COUNT", "FEVER",
                    "ABSORPTION", "STATUS_AMPLIFIER", "HEALTH_THRESHOLD")) {
                assertFalse(tuning.has(s(generic)), DEFINITIONS.get(branch).id() + " leaked " + generic);
            }
        }
    }

    @Test
    void everyNodeSetsItsOwnModeBit() {
        for (int node = 0; node < 27; node++) {
            assertTrue(tune(DEFINITIONS.get(node / 9), List.of(node)).flag(1 << node), "bit:" + node);
        }
    }

    @Test
    void burstRiteAndGroundChannelsStayIndependent() {
        Phase8AbilityTuning burst = tune(Phase8UniqueAbilities.BLOOD_BURST, List.of(0, 1, 2, 3, 4, 5, 6));
        assertEquals(3, burst.integer(s("BLOOD_VEIN_INTERVAL"), 0));
        assertEquals(.5, burst.get(s("BLOOD_BURST_RADIUS_BONUS"), 0), 1.0E-6);
        assertEquals(1.12, burst.get(s("BLOOD_BURST_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(40, burst.integer(s("BLOOD_BLEED_DURATION_BONUS_TICKS"), 0));
        assertEquals(2, burst.integer(s("BLOOD_FEAST_INTERVAL"), 0));
        assertEquals(1.25, burst.get(s("BLOOD_HEMORRHAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(4, burst.integer(s("BLOOD_CHAIN_CAP"), 0));

        Phase8AbilityTuning spray = tune(Phase8UniqueAbilities.BLOOD_BURST, List.of(1, 2, 7));
        assertEquals(7, spray.get(s("BLOOD_CONE_RANGE"), 0), 1.0E-6);
        assertEquals(12, spray.integer(s("BLOOD_CONE_TARGET_CAP"), 0));
        assertEquals(1.12, spray.get(s("BLOOD_BURST_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(1.35, spray.get(s("BLOOD_SPRAY_DAMAGE_MULTIPLIER"), 0), 1.0E-6);

        Phase8AbilityTuning rites = tune(Phase8UniqueAbilities.BLOOD_RITES, List.of(9, 10, 11, 12, 13, 14, 15));
        assertEquals(-2, rites.integer(s("BLOOD_COOLDOWN_BONUS_TICKS"), 0));
        assertEquals(3, rites.integer(s("BLOOD_WAVE_STEP_BONUS"), 0));
        assertEquals(2, rites.integer(s("BLOOD_SCREAM_TARGET_BONUS"), 0));
        assertEquals(-6, rites.integer(s("BLOOD_BLADE_HOVER_BONUS_TICKS"), 0));
        assertEquals(1.1, rites.get(s("BLOOD_BLADE_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(2, rites.integer(s("BLOOD_FLY_COUNT_BONUS"), 0));
        assertEquals(-3, rites.integer(s("BLOOD_DELUGE_INTERVAL_BONUS"), 0));
        assertEquals(300, rites.integer(s("BLOOD_MEMORY_WINDOW_TICKS"), 0));

        Phase8AbilityTuning ground = tune(Phase8UniqueAbilities.BLOOD_GROUND, List.of(18, 19, 20, 21, 23, 24));
        assertEquals(100, ground.integer(s("BLOOD_STAIN_DURATION_BONUS_TICKS"), 0));
        assertEquals(1, ground.integer(s("BLOOD_STAIN_SLOW_BONUS"), 0));
        assertEquals(-8, ground.integer(s("BLOOD_STAIN_HEAL_INTERVAL_BONUS"), 0));
        assertEquals(.5, ground.get(s("BLOOD_STAIN_HEAL_BONUS"), 0), 1.0E-6);
        assertEquals(1.08, ground.get(s("BLOOD_FOOTING_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(800, ground.integer(s("BLOOD_CONFLUENCE_CAP_TICKS"), 0));

        Phase8AbilityTuning sea = tune(Phase8UniqueAbilities.BLOOD_GROUND, List.of(18, 25));
        assertEquals(300, sea.integer(s("BLOOD_STAIN_DURATION_BONUS_TICKS"), 0));
        assertEquals(1.6, sea.get(s("BLOOD_SEA_RADIUS_MULTIPLIER"), 0), 1.0E-6);
    }

    @Test
    void riteCooldownComposesAgainstTheConfiguredValue() {
        assertEquals(8, cooldown(List.of(9)));
        assertEquals(38, cooldown(List.of(9, 16)));
        assertEquals(10, cooldown(List.of(17)));
    }

    private static int cooldown(List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(Phase8UniqueAbilities.BLOOD_RITES)
                .set(Phase8UniqueAbilities.TUNING, Phase8AbilityTuning.EMPTY)
                .set(Phase8UniqueAbilities.COOLDOWN_TICKS, 10);
        MasteryProfile profile = BuiltInFamilyProfiles.profile("bloodwake");
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, Phase8UniqueAbilities.BLOOD_RITES, builder, profile.nodes().get(node));
        }
        return builder.get(Phase8UniqueAbilities.COOLDOWN_TICKS);
    }

    private static boolean hasBloodSetting(Phase8AbilityTuning tuning) {
        return Arrays.stream(Phase8AbilityTuning.Setting.values())
                .filter(setting -> setting.name().startsWith("BLOOD_"))
                .anyMatch(tuning::has);
    }

    private static Phase8AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(Phase8UniqueAbilities.TUNING, Phase8AbilityTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(Phase8UniqueAbilities.COOLDOWN_TICKS, 10);
        MasteryProfile profile = BuiltInFamilyProfiles.profile("bloodwake");
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(Phase8UniqueAbilities.TUNING);
    }

    private static Phase8AbilityTuning.Setting s(String name) {
        return Phase8AbilityTuning.Setting.valueOf(name);
    }
}
