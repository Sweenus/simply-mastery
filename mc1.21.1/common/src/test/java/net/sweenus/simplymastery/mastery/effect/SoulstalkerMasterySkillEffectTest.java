package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.StormSoulMasteryTuning;
import net.sweenus.simplyswords.api.ability.StormSoulMasteryTuning.Setting;
import net.sweenus.simplyswords.api.ability.StormSoulMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SoulstalkerMasterySkillEffectTest {

    private static final List<Integer> ALL = IntStream.range(0, 27).boxed().toList();
    private static final List<Integer> TENDRILS = IntStream.range(0, 9).boxed().toList();
    private static final List<Integer> STRIDE = IntStream.range(9, 18).boxed().toList();
    private static final List<Integer> CLEAVE = IntStream.range(18, 27).boxed().toList();

    @Test
    void everySoulstalkerNodeRoutesToTheSoulstalkerAbilities() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("soulstalker");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(81 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void theTendrilBranchAndTheStrideBranchesStayApart() {
        StormSoulMasteryTuning tendril = tune(StormSoulMasteryAbilities.SOULSTALKER_TENDRIL, ALL);
        StormSoulMasteryTuning stride = tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, ALL);

        assertFalse(tendril.has(Setting.CLEAVE_RANGE));
        assertFalse(tendril.has(Setting.STRIDE_DURATION_TICKS));
        assertFalse(tendril.has(Setting.FOOTFALL_RADIUS));
        assertFalse(stride.has(Setting.TENDRIL_RANGE));
        assertFalse(stride.has(Setting.CHANCE));
        assertFalse(stride.has(Setting.GLOAM_DAMAGE_BONUS));
    }

    @Test
    void gloamStrideAndCleaveAndCrashNoLongerShareKeys() {
        StormSoulMasteryTuning gloamStride = tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, STRIDE);
        StormSoulMasteryTuning cleaveAndCrash = tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, CLEAVE);

        assertFalse(gloamStride.has(Setting.CLEAVE_RANGE));
        assertFalse(gloamStride.has(Setting.CLEAVE_FINAL_WIDTH));
        assertFalse(gloamStride.has(Setting.CLEAVE_DAMAGE_MULTIPLIER));
        assertFalse(cleaveAndCrash.has(Setting.FOOTFALL_DAMAGE_MULTIPLIER));
        assertFalse(cleaveAndCrash.has(Setting.TRAIL_STAIN_WIDTH));
        assertFalse(cleaveAndCrash.has(Setting.STRIDE_DURATION_TICKS));
        assertFalse(tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, ALL).has(Setting.RANGE));
        assertFalse(tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, ALL).has(Setting.FINAL_WIDTH));
    }

    @Test
    void riftStrideNoLongerShrinksShadowCleave() {
        StormSoulMasteryTuning both = tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, List.of(17, 18));

        assertEquals(18, both.get(Setting.CLEAVE_RANGE, 16), 1.0E-6);
        assertEquals(12, both.get(Setting.RIFT_RANGE, 0), 1.0E-6);
        assertEquals(.95, both.get(Setting.LEAP_CHARGE_THRESHOLD, 1), 1.0E-6);
        assertEquals(600, both.integer(Setting.STRIDE_DURATION_TICKS, 800));
    }

    @Test
    void tangledPreyNoLongerRevertsItsOwnPrerequisites() {
        StormSoulMasteryTuning tendril = tune(StormSoulMasteryAbilities.SOULSTALKER_TENDRIL, List.of(0, 2, 3, 6));

        assertEquals(30, tendril.integer(Setting.CHANCE, 25));
        assertEquals(50, tendril.integer(Setting.TENDRIL_LOCKOUT_TICKS, 60));
        assertEquals(40, tendril.integer(Setting.TENDRIL_SLOW_TICKS, 0));
        assertEquals(20, tendril.integer(Setting.SNARE_SLOW_TICKS, 0));
        assertEquals(3, tendril.integer(Setting.SNARE_SLOW_AMPLIFIER, 0));
        assertEquals(80, tendril.integer(Setting.SNARE_LOCKOUT_TICKS, 0));
    }

    @Test
    void tendrilCapstonesComposeWithBarbedEmergence() {
        assertEquals(1.12, tune(StormSoulMasteryAbilities.SOULSTALKER_TENDRIL, List.of(3))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.728, tune(StormSoulMasteryAbilities.SOULSTALKER_TENDRIL, List.of(3, 7))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.96, tune(StormSoulMasteryAbilities.SOULSTALKER_TENDRIL, List.of(3, 8))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void hungeringTendrilCarriesItsAdvertisedDrawback() {
        StormSoulMasteryTuning tendril = tune(StormSoulMasteryAbilities.SOULSTALKER_TENDRIL, List.of(8));

        assertTrue((tendril.integer(Setting.MODE, 0) & 16) != 0);
        assertEquals(30, tendril.integer(Setting.INTERVAL_TICKS, 20));
        assertEquals(20, tendril.integer(Setting.TENDRIL_REFUND_TICKS, 0));
        assertFalse(tendril.has(Setting.TENDRIL_LOCKOUT_TICKS));
    }

    @Test
    void cleaveCapstonesComposeWithSeveringArcAndHeavyFootfall() {
        assertEquals(1.12, tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, List.of(20))
                .get(Setting.CLEAVE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.896, tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, List.of(20, 25))
                .get(Setting.CLEAVE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.784, tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, List.of(20, 26))
                .get(Setting.CLEAVE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.1, tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, List.of(11))
                .get(Setting.FOOTFALL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.825, tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, List.of(11, 16))
                .get(Setting.FOOTFALL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void bothCooldownCapstonesSurviveEachOther() {
        assertEquals(1.2, tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, List.of(16))
                .get(Setting.COOLDOWN_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2, tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, List.of(25))
                .get(Setting.COOLDOWN_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.4, tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, List.of(16, 25))
                .get(Setting.COOLDOWN_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void swiftSummonsAndItsCapstonesReachTheCooldownKey() {
        UniqueAbilityTuning.Builder builder = builder(StormSoulMasteryAbilities.SOULSTALKER_STRIDE);
        apply(builder, StormSoulMasteryAbilities.SOULSTALKER_STRIDE, List.of(15, 16, 25));

        assertEquals(2448, builder.get(StormSoulMasteryAbilities.COOLDOWN_TICKS));
    }

    @Test
    void everyPreviouslyInertNodeNowCarriesItsOwnConsumedSettings() {
        StormSoulMasteryTuning tendril = tune(StormSoulMasteryAbilities.SOULSTALKER_TENDRIL, TENDRILS);
        StormSoulMasteryTuning stride = tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, ALL);

        assertEquals(.25, tendril.get(Setting.GLOAM_DAMAGE_BONUS, 0), 1.0E-6);
        assertEquals(4, tendril.get(Setting.SEEKING_ROOT_RANGE, 0), 1.0E-6);
        assertEquals(3, tendril.integer(Setting.TENDRIL_COUNT, 1));
        assertEquals(1.1, stride.get(Setting.CLIMB_SPEED, .3) / .3, 1.0E-6);
        assertEquals(1, stride.get(Setting.FOOTFALL_RADIUS, 0), 1.0E-6);
        assertEquals(320, stride.integer(Setting.TRAIL_STAIN_DURATION_TICKS, 0));
        assertEquals(1, stride.integer(Setting.TRAIL_SLOW_AMPLIFIER, 0));
        assertEquals(60, stride.integer(Setting.TRAIL_SLOW_DURATION_TICKS, 0));
        assertEquals(8, stride.get(Setting.MOMENTUM_DISTANCE, 0), 1.0E-6);
        assertEquals(.15, stride.get(Setting.MOMENTUM_BONUS, 0), 1.0E-6);
        assertEquals(1, stride.integer(Setting.CLEAVE_SWING_COOLDOWN_TICKS, 2));
        assertEquals(3, stride.get(Setting.LEAP_IMPACT_RADIUS, 0), 1.0E-6);
        assertEquals(1.2, stride.get(Setting.LEAP_IMPACT_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.28, stride.get(Setting.LEAP_IMPACT_LIFT, 0), 1.0E-6);
        assertEquals(.04, stride.get(Setting.CLEAVE_HIT_BONUS, 0), 1.0E-6);
        assertEquals(.2, stride.get(Setting.CLEAVE_HIT_BONUS_CAP, 0), 1.0E-6);
        assertEquals(16, stride.integer(Setting.CLEAVE_TARGET_CAP, 32));
        assertEquals(2, stride.get(Setting.LEAP_CHARGED_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(4, stride.get(Setting.LEAP_CHARGED_RADIUS, 0), 1.0E-6);
        assertEquals(2, stride.get(Setting.LEAP_STAIN_RADIUS, 0), 1.0E-6);
        assertEquals(2, stride.get(Setting.RIFT_STAIN_RADIUS, 0), 1.0E-6);
    }

    @Test
    void theOnlyRemainingModeBitHasAConsumer() {
        assertEquals(16, tune(StormSoulMasteryAbilities.SOULSTALKER_TENDRIL, ALL).integer(Setting.MODE, 0));
        assertFalse(tune(StormSoulMasteryAbilities.SOULSTALKER_STRIDE, ALL).has(Setting.MODE));
    }

    private static UniqueAbilityTuning.Builder builder(UniqueAbilityDefinition definition) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(StormSoulMasteryAbilities.COOLDOWN_TICKS, 1200);
        }
        builder.set(StormSoulMasteryAbilities.TUNING, StormSoulMasteryTuning.EMPTY);
        return builder;
    }

    private static void apply(UniqueAbilityTuning.Builder builder, UniqueAbilityDefinition definition,
                              List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("soulstalker");
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, definition, builder, profile.nodes().get(node));
        }
    }

    private static StormSoulMasteryTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = builder(definition);
        apply(builder, definition, nodes);
        return builder.get(StormSoulMasteryAbilities.TUNING);
    }
}
