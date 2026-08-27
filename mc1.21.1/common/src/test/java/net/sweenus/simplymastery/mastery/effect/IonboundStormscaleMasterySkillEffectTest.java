package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase3AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase3AbilityTuning.Setting;
import net.sweenus.simplyswords.api.ability.Phase3UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IonboundStormscaleMasterySkillEffectTest {

    private static final List<Integer> ALL = IntStream.range(0, 27).boxed().toList();

    @Test
    void everyIonboundNodeRoutesToTheIonboundAbilities() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("ionbound_stormscale");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(27 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void theIonReserveNeverAltersTheBeamCadenceOrTheCorridorPull() {
        Phase3AbilityTuning beam = tune(Phase3UniqueAbilities.IONBOUND_BEAM, ALL);
        Phase3AbilityTuning crusher = tune(Phase3UniqueAbilities.IONBOUND_CRUSHER, List.of(0, 3, 5, 8));

        assertEquals(4, beam.integer(Setting.INTERVAL_TICKS, 5));
        assertEquals(145, tune(Phase3UniqueAbilities.IONBOUND_SHIELD, List.of(0))
                .integer(Setting.RESERVE_INTERVAL_TICKS, 160));
        assertFalse(crusher.has(Setting.PULL_STRENGTH));
        assertFalse(crusher.has(Setting.LOCKOUT_TICKS));
        assertFalse(crusher.has(Setting.INTERVAL_TICKS));
    }

    @Test
    void aReserveOnlyLoadoutLeavesTheBeamOnItsConfiguredCadence() {
        Phase3AbilityTuning beam = tune(Phase3UniqueAbilities.IONBOUND_BEAM, List.of(0, 8));

        assertFalse(beam.has(Setting.INTERVAL_TICKS));
        assertEquals(5, beam.integer(Setting.INTERVAL_TICKS, 5));
    }

    @Test
    void crushingFocusUsesALaneWidthInsteadOfTheCorridorWidth() {
        Phase3AbilityTuning crusher = tune(Phase3UniqueAbilities.IONBOUND_CRUSHER, List.of(9, 14));

        assertEquals(7, crusher.get(Setting.WIDTH, 6), 1.0E-6);
        assertEquals(2, crusher.get(Setting.FOCUS_LANE_WIDTH, 0), 1.0E-6);
        assertEquals(.25, crusher.get(Setting.FOCUS_DAMAGE_BONUS, 0), 1.0E-6);
    }

    @Test
    void repulsorGateExpressesItsBurstThroughADedicatedKey() {
        Phase3AbilityTuning crusher = tune(Phase3UniqueAbilities.IONBOUND_CRUSHER, List.of(17));

        assertEquals(3, crusher.get(Setting.BURST_STRENGTH, 0), 1.0E-6);
        assertEquals(.75, crusher.get(Setting.BEAM_WIDTH_MULTIPLIER, 0), 1.0E-6);
        assertFalse(crusher.has(Setting.FINAL_WIDTH));
    }

    @Test
    void slamAndBeamCapstonesComposeWithTheirPrerequisites() {
        assertEquals(2.464, tune(Phase3UniqueAbilities.IONBOUND_CRUSHER, List.of(12, 16))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.68, tune(Phase3UniqueAbilities.IONBOUND_CRUSHER, List.of(12, 17))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.77, tune(Phase3UniqueAbilities.IONBOUND_BEAM, List.of(21, 25))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.475, tune(Phase3UniqueAbilities.IONBOUND_BEAM, List.of(21, 26))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.32, tune(Phase3UniqueAbilities.IONBOUND_BEAM, List.of(21, 24))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void everyPreviouslyInertNodeNowCarriesItsOwnConsumedSettings() {
        Phase3AbilityTuning shield = tune(Phase3UniqueAbilities.IONBOUND_SHIELD, ALL);

        assertEquals(2, shield.get(Setting.SHIELD_PUSH_STRENGTH, 0), 1.0E-6);
        assertEquals(3, shield.integer(Setting.RESERVE_STACK_CAP, 0));
        assertEquals(.1, shield.get(Setting.RESERVE_STACK_BONUS, 0), 1.0E-6);
        assertEquals(.25, shield.get(Setting.SHIELD_LOW_HEALTH_THRESHOLD, 0), 1.0E-6);
        assertEquals(1200, shield.integer(Setting.SHIELD_LOCKOUT_TICKS, 0));
        assertEquals(40, shield.integer(Setting.RESERVE_REFUND_TICKS, 0));
        assertEquals(2, shield.integer(Setting.SHIELD_CUBE_COST, 1));
        assertEquals(.25, shield.get(Setting.ABILITY_DAMAGE_PENALTY, 0), 1.0E-6);
        assertEquals(.15, shield.get(Setting.ABILITY_DAMAGE_PER_CUBE, 0), 1.0E-6);
        assertEquals(.45, shield.get(Setting.ABILITY_DAMAGE_CUBE_CAP, 0), 1.0E-6);
    }

    @Test
    void trapArmourAndParalysisModesAreAllArmed() {
        Phase3AbilityTuning crusher = tune(Phase3UniqueAbilities.IONBOUND_CRUSHER, ALL);
        Phase3AbilityTuning beam = tune(Phase3UniqueAbilities.IONBOUND_BEAM, ALL);

        assertEquals(.1, crusher.get(Setting.TRAP_MOVEMENT_SPEED, 0), 1.0E-6);
        assertTrue((crusher.integer(Setting.MODE, 0) & 2048) != 0);
        assertTrue((crusher.integer(Setting.MODE, 0) & 128) != 0);
        assertEquals(6, beam.get(Setting.ARMOR_IGNORE, 0), 1.0E-6);
        assertEquals(3, beam.integer(Setting.BEAM_TARGET_CAP, 16));
        assertFalse(beam.has(Setting.TARGET_CAP));
    }

    private static Phase3AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("ionbound_stormscale");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(Phase3UniqueAbilities.COOLDOWN_TICKS, 60);
        }
        builder.set(Phase3UniqueAbilities.TUNING, Phase3AbilityTuning.EMPTY);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, definition, builder, profile.nodes().get(node));
        }
        return builder.get(Phase3UniqueAbilities.TUNING);
    }
}
