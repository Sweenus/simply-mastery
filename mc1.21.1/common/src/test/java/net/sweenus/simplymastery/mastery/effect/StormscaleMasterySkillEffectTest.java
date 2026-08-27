package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase3AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase3AbilityTuning.Setting;
import net.sweenus.simplyswords.api.ability.Phase3UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

final class StormscaleMasterySkillEffectTest {

    @Test
    void everyStormscaleNodeRoutesToTheLightningRodAbility() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("stormscale");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void theCooldownMultiplierIsAppliedExactlyOnceRegardlessOfNodeCount() {
        assertEquals(1200, cooldown(List.of(8)));
        assertEquals(1200, cooldown(List.of(8, 9, 10, 11)));
        assertEquals(1200, cooldown(IntStream.range(0, 27).filter(node -> node != 17).boxed().toList()));
    }

    @Test
    void patientSupercellAndStormSpireCooldownsCompose() {
        assertEquals(1080, cooldown(List.of(17)));
        assertEquals(1296, cooldown(List.of(8, 17)));
        assertEquals(1296, cooldown(List.of(8, 17, 20, 21, 22)));
    }

    @Test
    void mobileConductorDrawbacksSurviveTheGatheringChargeBranch() {
        Phase3AbilityTuning tuning = tune(List.of(7, 10, 11));

        assertEquals(3, tuning.get(Setting.RADIUS, 3.5), 1.0E-6);
        assertEquals(.4, tuning.get(Setting.GROWTH_CAP_LIMIT, 0), 1.0E-6);
        assertEquals(1, tuning.get(Setting.GROWTH_CAP, .8), 1.0E-6);
        assertEquals(.015, tuning.get(Setting.GROWTH_PER_HIT, .01), 1.0E-6);
    }

    @Test
    void rapidDynamoCapsRadiusInsteadOfReplacingItAndDoublesTheTunedGrowth() {
        Phase3AbilityTuning withPrerequisite = tune(List.of(8, 10, 16));

        assertEquals(6, withPrerequisite.get(Setting.RADIUS, 3.5), 1.0E-6);
        assertEquals(4.5, withPrerequisite.get(Setting.RADIUS_CAP, 0), 1.0E-6);
        assertEquals(.03, withPrerequisite.get(Setting.GROWTH_PER_HIT, .01), 1.0E-6);
    }

    @Test
    void conductiveTargetNoLongerTouchesThePulseTargetCap() {
        Phase3AbilityTuning tuning = tune(List.of(1, 13));

        assertFalse(tuning.has(Setting.TARGET_CAP));
        assertEquals(12, tuning.integer(Setting.CONDUCTIVE_TARGET_CAP, 0));
        assertEquals(60, tuning.integer(Setting.CONDUCTIVE_DURATION_TICKS, 0));
        assertEquals(4, tuning.get(Setting.RADIUS, 3.5), 1.0E-6);
    }

    @Test
    void everyNodeThatOnceSharedAMultiplierNowOwnsItsOwnSetting() {
        Phase3AbilityTuning tuning = tune(IntStream.range(0, 27).boxed().toList());

        assertEquals(.7, tuning.get(Setting.PLANT_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.5, tuning.get(Setting.SECOND_CHARGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.6, tuning.get(Setting.EXTRA_PULSE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.55, tuning.get(Setting.INSTANT_PULSE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.35, tuning.get(Setting.STORED_PULSE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.25, tuning.get(Setting.CHAIN_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(80, tuning.integer(Setting.REPOSITION_DURATION_COST_TICKS, 0));
        assertEquals(10, tuning.integer(Setting.DOUBLE_CHARGE_LOCKOUT_TICKS, 0));
        assertEquals(60, tuning.integer(Setting.REVERSE_LOCKOUT_TICKS, 0));
        assertEquals(100, tuning.integer(Setting.WARD_LOCKOUT_TICKS, 0));
        assertEquals(60, tuning.integer(Setting.REVERSE_WEAKNESS_TICKS, 0));
        assertEquals(20, tuning.integer(Setting.ROOT_DURATION_TICKS, 0));
        assertEquals(5, tuning.integer(Setting.SURGE_INTERVAL, 0));
        assertEquals(6, tuning.integer(Setting.WARD_TARGET_THRESHOLD, 0));
        assertEquals(6, tuning.get(Setting.STORED_PULSE_RADIUS, 0), 1.0E-6);
    }

    @Test
    void overflowAndWardKeepTheirTunedBudgets() {
        Phase3AbilityTuning tuning = tune(List.of(14, 24));

        assertEquals(.05, tuning.get(Setting.PER_STACK_BONUS, 0), 1.0E-6);
        assertEquals(.2, tuning.get(Setting.BONUS_CAP, 0), 1.0E-6);
        assertEquals(3, tuning.integer(Setting.ABSORPTION, 0));
        assertEquals(80, tuning.integer(Setting.BUFF_DURATION_TICKS, 0));
        assertEquals(20, tuning.integer(Setting.REFUND_TICKS, 0));
    }

    private static int cooldown(List<Integer> nodes) {
        return builder(nodes).get(Phase3UniqueAbilities.COOLDOWN_TICKS);
    }

    private static Phase3AbilityTuning tune(List<Integer> nodes) {
        return builder(nodes).get(Phase3UniqueAbilities.TUNING);
    }

    private static UniqueAbilityTuning.Builder builder(List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("stormscale");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(Phase3UniqueAbilities.STORMSCALE_ROD);
        builder.set(Phase3UniqueAbilities.COOLDOWN_TICKS, 1000);
        builder.set(Phase3UniqueAbilities.TUNING, Phase3AbilityTuning.EMPTY);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, Phase3UniqueAbilities.STORMSCALE_ROD, builder, profile.nodes().get(node));
        }
        return builder;
    }
}
