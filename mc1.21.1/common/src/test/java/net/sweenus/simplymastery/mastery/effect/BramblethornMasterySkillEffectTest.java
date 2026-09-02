package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase7AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase7UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import net.sweenus.simplyswords.world.BramblethornAbilityManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BramblethornMasterySkillEffectTest {
    @Test
    void nodesRouteOnlyToTheirRuntimeDefinition() {
        for (int node = 0; node < 27; node++) {
            assertEquals(node < 9 || node >= 18,
                    hasBrambleTuning(tune(Phase7UniqueAbilities.BRAMBLE_GRASP, List.of(node))),
                    "grasp " + node);
            assertEquals(node >= 9 && node < 18,
                    hasBrambleTuning(tune(Phase7UniqueAbilities.BRAMBLE_HUNT, List.of(node))),
                    "hunt " + node);
        }
    }

    @Test
    void everyNodeWritesDedicatedTuningWithoutGenericKeys() {
        for (int node = 0; node < 27; node++) {
            UniqueAbilityDefinition definition = node >= 9 && node < 18
                    ? Phase7UniqueAbilities.BRAMBLE_HUNT : Phase7UniqueAbilities.BRAMBLE_GRASP;
            assertTrue(hasBrambleTuning(tune(definition, List.of(node))), "node " + node);
        }
        for (UniqueAbilityDefinition definition : List.of(
                Phase7UniqueAbilities.BRAMBLE_GRASP, Phase7UniqueAbilities.BRAMBLE_HUNT)) {
            Phase7AbilityTuning tuning = tune(definition, IntStream.range(0, 27).boxed().toList());
            for (String generic : List.of("COOLDOWN_TICKS", "DURATION_TICKS", "INTERVAL_TICKS",
                    "LOCKOUT_TICKS", "REFUND_TICKS", "DAMAGE_MULTIPLIER",
                    "SECONDARY_DAMAGE_MULTIPLIER", "FINAL_DAMAGE_MULTIPLIER",
                    "INCOMING_MULTIPLIER", "OUTGOING_MULTIPLIER", "PER_STACK_MULTIPLIER",
                    "RADIUS", "RANGE", "WIDTH", "SPEED", "PULL_STRENGTH", "TARGET_CAP",
                    "COUNT", "STACK_CAP", "STATUS_DURATION_TICKS", "STATUS_AMPLIFIER",
                    "ABSORPTION", "MODE")) {
                assertFalse(tuning.has(s(generic)), definition.id() + " " + generic);
            }
        }
    }

    @Test
    void signatureAndAncientGroveComposeAgainstConfiguration() {
        Phase7AbilityTuning tuning = tune(Phase7UniqueAbilities.BRAMBLE_GRASP,
                List.of(0, 1, 2, 3, 4, 5, 6, 25));
        assertEquals(24, BramblethornAbilityManager.graspRange(20, tuning), 1.0E-6);
        assertEquals(9.5, BramblethornAbilityManager.graspRadius(8, tuning), 1.0E-6);
        assertEquals(17, BramblethornAbilityManager.graspTravelTicks(20, tuning));
        assertEquals(180, BramblethornAbilityManager.bindingDuration(100, tuning));
        assertEquals(.36, BramblethornAbilityManager.pullStrength(.3, tuning), 1.0E-6);
        assertEquals(.294, BramblethornAbilityManager.sharedDamageRatio(.35, tuning), 1.0E-6);
        assertEquals(1.2, BramblethornAbilityManager.slamDamageMultiplier(tuning), 1.0E-6);
    }

    @Test
    void transformationPrerequisitesKeepIndependentValues() {
        Phase7AbilityTuning tuning = tune(Phase7UniqueAbilities.BRAMBLE_GRASP,
                List.of(18, 19, 20, 21, 23, 24, 25));
        assertEquals(4, tuning.integer(s("BRAMBLE_BARKSKIN_ABSORPTION"), 0));
        assertEquals(2, tuning.integer(s("BRAMBLE_SAP_RISE_ABSORPTION"), 0));
        assertEquals(8, tuning.integer(s("BRAMBLE_SAP_RISE_CAP"), 0));
        assertEquals(3, tuning.integer(s("BRAMBLE_ROOTED_GUARD_TARGET_COUNT"), 0));
        assertEquals(6, tuning.integer(s("BRAMBLE_THORNWARD_TARGET_COUNT"), 0));
        assertEquals(48, tuning.integer(s("BRAMBLE_RECLAIMED_REFUND_CAP_TICKS"), 0));
        assertEquals(1, tuning.integer(s("BRAMBLE_ANCIENT_RESISTANCE_AMPLIFIER"), 0));
    }

    @Test
    void briarSacrificeDoesNotRetuneGraspCaptureRadius() {
        Phase7AbilityTuning tuning = tune(Phase7UniqueAbilities.BRAMBLE_GRASP, List.of(1, 26));
        assertEquals(9.5, BramblethornAbilityManager.graspRadius(8, tuning), 1.0E-6);
        assertEquals(2, tuning.get(s("BRAMBLE_BRIAR_RADIUS"), 0), 1.0E-6);
        assertEquals(.5, tuning.get(s("BRAMBLE_BRIAR_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(16, tuning.integer(s("BRAMBLE_BRIAR_TARGET_CAP"), 0));
    }

    @Test
    void huntBonusesAndCapstonesRemainScoped() {
        Phase7AbilityTuning tuning = tune(Phase7UniqueAbilities.BRAMBLE_HUNT,
                List.of(9, 10, 11, 12, 13, 14, 15, 16));
        assertEquals(120, BramblethornAbilityManager.huntMemoryDuration(80, tuning));
        assertEquals(4, BramblethornAbilityManager.huntCooldown(5, tuning));
        assertEquals(16, BramblethornAbilityManager.huntRange(14, tuning), 1.0E-6);
        assertEquals(1.25, BramblethornAbilityManager.huntWidth(1, tuning), 1.0E-6);
        assertEquals(1.1, BramblethornAbilityManager.huntDamageMultiplier(tuning), 1.0E-6);
        assertEquals(0, BramblethornAbilityManager.huntSlowDuration(50, tuning));
        assertEquals(4, BramblethornAbilityManager.huntTargetCap(tuning));
        assertEquals(2, tuning.integer(s("BRAMBLE_WALTZ_PROJECTILE_COUNT"), 0));
    }

    private static boolean hasBrambleTuning(Phase7AbilityTuning tuning) {
        return java.util.Arrays.stream(Phase7AbilityTuning.Setting.values())
                .filter(setting -> setting.name().startsWith("BRAMBLE_"))
                .anyMatch(tuning::has);
    }

    private static Phase7AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(Phase7UniqueAbilities.TUNING, Phase7AbilityTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) {
            builder.set(Phase7UniqueAbilities.COOLDOWN_TICKS, 240);
        }
        MasteryProfile profile = BuiltInFamilyProfiles.profile("bramblethorn");
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(Phase7UniqueAbilities.TUNING);
    }

    private static Phase7AbilityTuning.Setting s(String name) {
        return Phase7AbilityTuning.Setting.valueOf(name);
    }
}
