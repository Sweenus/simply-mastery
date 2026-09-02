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

final class Phase7RemainingMasterySkillEffectTest {
    @Test
    void hiveheartNodesRouteToProcOrSwarm() {
        for (int node = 0; node < 27; node++) {
            assertEquals(node < 9, hasPrefix(tune("hiveheart",
                    Phase7UniqueAbilities.HIVEHEART_PROC, List.of(node)), "HIVE_"), "proc " + node);
            assertEquals(node >= 9, hasPrefix(tune("hiveheart",
                    Phase7UniqueAbilities.HIVEHEART_SWARM, List.of(node)), "HIVE_"), "swarm " + node);
        }
    }

    @Test
    void chompolotlNodesRouteToProcOrBlueSummonDefinitions() {
        for (int node = 0; node < 27; node++) {
            assertEquals(node < 9, hasPrefix(tune("chompolotl",
                    Phase7UniqueAbilities.CHOMPOLOTL_PROC, List.of(node)), "CHOMP_"), "proc " + node);
            assertEquals(node >= 9, hasPrefix(tune("chompolotl",
                    Phase7UniqueAbilities.CHOMPOLOTL_RALLY, List.of(node)), "CHOMP_"), "rally " + node);
            assertEquals(node >= 9, hasPrefix(tune("chompolotl",
                    Phase7UniqueAbilities.CHOMPOLOTL_GUARDIAN, List.of(node)), "CHOMP_"), "guardian " + node);
        }
    }

    @Test
    void allRemainingNodesUseDedicatedSettings() {
        for (String profile : List.of("hiveheart", "chompolotl")) {
            UniqueAbilityDefinition passive = profile.equals("hiveheart")
                    ? Phase7UniqueAbilities.HIVEHEART_PROC : Phase7UniqueAbilities.CHOMPOLOTL_PROC;
            UniqueAbilityDefinition active = profile.equals("hiveheart")
                    ? Phase7UniqueAbilities.HIVEHEART_SWARM : Phase7UniqueAbilities.CHOMPOLOTL_RALLY;
            Phase7AbilityTuning passiveTuning = tune(profile, passive,
                    IntStream.range(0, 9).boxed().toList());
            Phase7AbilityTuning activeTuning = tune(profile, active,
                    IntStream.range(9, 27).boxed().toList());
            for (Phase7AbilityTuning tuning : List.of(passiveTuning, activeTuning)) {
                for (String generic : List.of("COOLDOWN_TICKS", "DURATION_TICKS", "INTERVAL_TICKS",
                        "LOCKOUT_TICKS", "REFUND_TICKS", "CHANCE", "DAMAGE_MULTIPLIER",
                        "SECONDARY_DAMAGE_MULTIPLIER", "FINAL_DAMAGE_MULTIPLIER",
                        "INCOMING_MULTIPLIER", "OUTGOING_MULTIPLIER", "PER_STACK_MULTIPLIER",
                        "RADIUS", "RANGE", "WIDTH", "SPEED", "TARGET_CAP", "SEARCH_CAP",
                        "COUNT", "STACK_CAP", "STATUS_DURATION_TICKS", "STATUS_AMPLIFIER",
                        "ABSORPTION", "HEALTH_THRESHOLD")) {
                    assertFalse(tuning.has(s(generic)), profile + " leaked " + generic);
                }
            }
        }
    }

    @Test
    void capstoneValuesRemainIndependent() {
        Phase7AbilityTuning hive = tune("hiveheart", Phase7UniqueAbilities.HIVEHEART_SWARM,
                List.of(16, 17, 25, 26));
        assertEquals(16, hive.integer(s("HIVE_CLOUD_COUNT"), 0));
        assertEquals(4, hive.integer(s("HIVE_HUNT_COUNT"), 0));
        assertEquals(4, hive.get(s("HIVE_PHALANX_RANGE"), 0), 1.0E-6);
        assertEquals(12, hive.get(s("HIVE_VENGEFUL_RANGE"), 0), 1.0E-6);

        Phase7AbilityTuning chomp = tune("chompolotl", Phase7UniqueAbilities.CHOMPOLOTL_RALLY,
                List.of(16, 17, 25, 26));
        assertEquals(3, chomp.integer(s("CHOMP_BRIGADE_COUNT"), 0));
        assertEquals(3, chomp.integer(s("CHOMP_HUNTER_COUNT"), 0));
        assertEquals(600, chomp.integer(s("CHOMP_ETERNAL_LIFESPAN_TICKS"), 0));
        assertEquals(2, chomp.get(s("CHOMP_RAVAGER_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
    }

    private static boolean hasPrefix(Phase7AbilityTuning tuning, String prefix) {
        return java.util.Arrays.stream(Phase7AbilityTuning.Setting.values())
                .filter(setting -> setting.name().startsWith(prefix))
                .anyMatch(tuning::has);
    }

    private static Phase7AbilityTuning tune(String profileId, UniqueAbilityDefinition definition,
                                            List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(Phase7UniqueAbilities.TUNING, Phase7AbilityTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(Phase7UniqueAbilities.COOLDOWN_TICKS, 600);
        MasteryProfile profile = BuiltInFamilyProfiles.profile(profileId);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(Phase7UniqueAbilities.TUNING);
    }

    private static Phase7AbilityTuning.Setting s(String name) {
        return Phase7AbilityTuning.Setting.valueOf(name);
    }
}
