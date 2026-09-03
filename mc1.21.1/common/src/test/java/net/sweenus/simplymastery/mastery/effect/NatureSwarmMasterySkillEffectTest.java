package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.NatureSwarmMasteryTuning;
import net.sweenus.simplyswords.api.ability.NatureSwarmMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NatureSwarmMasterySkillEffectTest {
    @Test
    void hiveheartNodesRouteToProcOrSwarm() {
        for (int node = 0; node < 27; node++) {
            assertEquals(node < 9, hasPrefix(tune("hiveheart",
                    NatureSwarmMasteryAbilities.HIVEHEART_PROC, List.of(node)), "HIVE_"), "proc " + node);
            assertEquals(node >= 9, hasPrefix(tune("hiveheart",
                    NatureSwarmMasteryAbilities.HIVEHEART_SWARM, List.of(node)), "HIVE_"), "swarm " + node);
        }
    }

    @Test
    void chompolotlNodesRouteToProcOrBlueSummonDefinitions() {
        for (int node = 0; node < 27; node++) {
            assertEquals(node < 9, hasPrefix(tune("chompolotl",
                    NatureSwarmMasteryAbilities.CHOMPOLOTL_PROC, List.of(node)), "CHOMP_"), "proc " + node);
            assertEquals(node >= 9, hasPrefix(tune("chompolotl",
                    NatureSwarmMasteryAbilities.CHOMPOLOTL_RALLY, List.of(node)), "CHOMP_"), "rally " + node);
            assertEquals(node >= 9, hasPrefix(tune("chompolotl",
                    NatureSwarmMasteryAbilities.CHOMPOLOTL_GUARDIAN, List.of(node)), "CHOMP_"), "guardian " + node);
        }
    }

    @Test
    void allRemainingNodesUseDedicatedSettings() {
        for (String profile : List.of("hiveheart", "chompolotl")) {
            UniqueAbilityDefinition passive = profile.equals("hiveheart")
                    ? NatureSwarmMasteryAbilities.HIVEHEART_PROC : NatureSwarmMasteryAbilities.CHOMPOLOTL_PROC;
            UniqueAbilityDefinition active = profile.equals("hiveheart")
                    ? NatureSwarmMasteryAbilities.HIVEHEART_SWARM : NatureSwarmMasteryAbilities.CHOMPOLOTL_RALLY;
            NatureSwarmMasteryTuning passiveTuning = tune(profile, passive,
                    IntStream.range(0, 9).boxed().toList());
            NatureSwarmMasteryTuning activeTuning = tune(profile, active,
                    IntStream.range(9, 27).boxed().toList());
            for (NatureSwarmMasteryTuning tuning : List.of(passiveTuning, activeTuning)) {
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
        NatureSwarmMasteryTuning hive = tune("hiveheart", NatureSwarmMasteryAbilities.HIVEHEART_SWARM,
                List.of(16, 17, 25, 26));
        assertEquals(16, hive.integer(s("HIVE_CLOUD_COUNT"), 0));
        assertEquals(4, hive.integer(s("HIVE_HUNT_COUNT"), 0));
        assertEquals(4, hive.get(s("HIVE_PHALANX_RANGE"), 0), 1.0E-6);
        assertEquals(12, hive.get(s("HIVE_VENGEFUL_RANGE"), 0), 1.0E-6);

        NatureSwarmMasteryTuning chomp = tune("chompolotl", NatureSwarmMasteryAbilities.CHOMPOLOTL_RALLY,
                List.of(16, 17, 25, 26));
        assertEquals(3, chomp.integer(s("CHOMP_BRIGADE_COUNT"), 0));
        assertEquals(3, chomp.integer(s("CHOMP_HUNTER_COUNT"), 0));
        assertEquals(600, chomp.integer(s("CHOMP_ETERNAL_LIFESPAN_TICKS"), 0));
        assertEquals(2, chomp.get(s("CHOMP_RAVAGER_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
    }

    private static boolean hasPrefix(NatureSwarmMasteryTuning tuning, String prefix) {
        return java.util.Arrays.stream(NatureSwarmMasteryTuning.Setting.values())
                .filter(setting -> setting.name().startsWith(prefix))
                .anyMatch(tuning::has);
    }

    private static NatureSwarmMasteryTuning tune(String profileId, UniqueAbilityDefinition definition,
                                            List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(NatureSwarmMasteryAbilities.TUNING, NatureSwarmMasteryTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(NatureSwarmMasteryAbilities.COOLDOWN_TICKS, 600);
        MasteryProfile profile = BuiltInFamilyProfiles.profile(profileId);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(NatureSwarmMasteryAbilities.TUNING);
    }

    private static NatureSwarmMasteryTuning.Setting s(String name) {
        return NatureSwarmMasteryTuning.Setting.valueOf(name);
    }
}
