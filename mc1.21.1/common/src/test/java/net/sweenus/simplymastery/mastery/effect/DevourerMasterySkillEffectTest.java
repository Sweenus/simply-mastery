package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.AbyssalSpectralMasteryTuning;
import net.sweenus.simplyswords.api.ability.AbyssalSpectralMasteryTuning.Setting;
import net.sweenus.simplyswords.api.ability.AbyssalSpectralMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

final class DevourerMasterySkillEffectTest {

    @Test
    void everyDevourerNodeRoutesToTheAbyssalSpectralAbilityEffect() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("the_devourer");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class,
                    SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(27 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void owningEveryNodeLeavesEachSharedSettingAtItsIntendedValue() {
        AbyssalSpectralMasteryTuning tuning = tune(range(0, 27));

        assertEquals(.03, tuning.get(Setting.BONUS_PER_TRIGGER, 0), 1.0E-6);
        assertEquals(.25, tuning.get(Setting.ROUTED_DAMAGE_BONUS, 0), 1.0E-6);
        assertEquals(.75, tuning.get(Setting.SECONDARY_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.7, tuning.get(Setting.REPRISAL_SECONDARY_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.42, tuning.get(Setting.PULL_STRENGTH, 0), 1.0E-6);
        assertEquals(2.0, tuning.get(Setting.REPRISAL_PUSH_STRENGTH, 0), 1.0E-6);
        assertEquals(40.0, tuning.get(Setting.STATUS_DURATION_TICKS, 0));
        assertEquals(60.0, tuning.get(Setting.ABSORPTION_DURATION_TICKS, 0));
        assertEquals(10.0, tuning.get(Setting.FOLLOW_RANGE, 0));
        assertEquals(30.0, tuning.get(Setting.RANGE, 0));
    }

    @Test
    void pulseIntervalsAreSeparateSettings() {
        AbyssalSpectralMasteryTuning both = tune(List.of(5, 8));

        assertEquals(16.0, both.get(Setting.ACCELERATED_INTERVAL_TICKS, 0));
        assertEquals(30.0, both.get(Setting.PULSE_INTERVAL_TICKS, 0));
        assertEquals(100.0, both.get(Setting.ACCELERATE_THRESHOLD_TICKS, 0));
    }

    @Test
    void wanderingHungerConfiguresItsOwnFollowRange() {
        AbyssalSpectralMasteryTuning wandering = tune(List.of(7));

        assertEquals(10.0, wandering.get(Setting.FOLLOW_RANGE, 0));
        assertEquals(.25, wandering.get(Setting.MOVEMENT_SPEED, 0), 1.0E-6);
        assertEquals(0.0, wandering.get(Setting.RANGE, 0));
        assertEquals(600.0, wandering.get(Setting.DURATION_TICKS, 0));
    }

    @Test
    void reprisalCapstonesConfigureTheirConsumers() {
        AbyssalSpectralMasteryTuning guardian = tune(List.of(25));
        assertEquals(.5, guardian.get(Setting.REPRISAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.0, guardian.get(Setting.REPRISAL_PUSH_STRENGTH, 0), 1.0E-6);
        assertEquals(60.0, guardian.get(Setting.REPRISAL_GUARD_TICKS, 0));
        assertEquals(80.0, guardian.get(Setting.LOCKOUT_TICKS, 0));
        assertEquals(0.0, guardian.get(Setting.PULL_STRENGTH, 0));

        AbyssalSpectralMasteryTuning vengeful = tune(List.of(26));
        assertEquals(2.25, vengeful.get(Setting.REPRISAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.0, vengeful.get(Setting.REPRISAL_TARGET_CAP, 0));
        assertEquals(30.0, vengeful.get(Setting.RANGE, 0));
        assertEquals(40.0, vengeful.get(Setting.LOCKOUT_TICKS, 0));
    }

    @Test
    void starvedBeastKeepsItsRefundBudget() {
        AbyssalSpectralMasteryTuning starved = tune(List.of(17));

        assertEquals(20.0, starved.get(Setting.COOLDOWN_REFUND_TICKS, 0));
        assertEquals(160.0, starved.get(Setting.COOLDOWN_REFUND_CAP_TICKS, 0));
        assertEquals(64.0, starved.integer(Setting.MODE, 0));
    }

    private static List<Integer> range(int from, int to) {
        return java.util.stream.IntStream.range(from, to).boxed().toList();
    }

    private static AbyssalSpectralMasteryTuning tune(List<Integer> slots) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("the_devourer");
        UniqueAbilityDefinition definition = AbyssalSpectralMasteryAbilities.DEVOURER_MASS;
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        builder.set(AbyssalSpectralMasteryAbilities.COOLDOWN_TICKS, 1200);
        builder.set(AbyssalSpectralMasteryAbilities.TUNING, AbyssalSpectralMasteryTuning.EMPTY
                .with(Setting.COOLDOWN_TICKS, 1200)
                .with(Setting.DURATION_TICKS, 800));
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int slot : slots) {
            effect.tune(null, definition, builder, profile.nodes().get(slot));
        }
        return builder.get(AbyssalSpectralMasteryAbilities.TUNING);
    }
}
