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

final class DreadwhisperMasterySkillEffectTest {

    private static final List<Integer> ALL = IntStream.range(0, 27).boxed().toList();
    private static final List<Integer> REND = IntStream.range(0, 9).boxed().toList();
    private static final List<Integer> WOUND = IntStream.range(9, 18).boxed().toList();
    private static final List<Integer> GLOAM = IntStream.range(18, 27).boxed().toList();

    @Test
    void everyDreadwhisperNodeRoutesToTheDreadwhisperAbilities() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("dreadwhisper");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(135 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void nothingOutsideReavingFrontCanTouchTheDashGeometry() {
        StormSoulMasteryTuning wound = tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, WOUND);
        StormSoulMasteryTuning gloam = tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(18, 19, 20, 21, 22, 23, 24, 25));

        assertFalse(wound.has(Setting.REND_RANGE));
        assertFalse(wound.has(Setting.REND_DAMAGE_MULTIPLIER));
        assertFalse(wound.has(Setting.REND_TARGET_CAP));
        assertFalse(wound.has(Setting.LEECH_RATIO));
        assertFalse(gloam.has(Setting.REND_RANGE));
        assertFalse(gloam.has(Setting.REND_DAMAGE_MULTIPLIER));
        assertFalse(gloam.has(Setting.REND_TARGET_CAP));
    }

    @Test
    void theOldSharedKeysAreGoneEntirely() {
        StormSoulMasteryTuning reave = tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, ALL);
        StormSoulMasteryTuning wound = tune(StormSoulMasteryAbilities.DREADWHISPER_WOUND, ALL);

        assertFalse(reave.has(Setting.RANGE));
        assertFalse(reave.has(Setting.WIDTH));
        assertFalse(reave.has(Setting.SPEED));
        assertFalse(reave.has(Setting.DAMAGE_MULTIPLIER));
        assertFalse(reave.has(Setting.TARGET_CAP));
        assertFalse(reave.has(Setting.HEAL_RATIO));
        assertFalse(reave.has(Setting.MODE));
        assertFalse(wound.has(Setting.MODE));
        assertFalse(wound.has(Setting.RANGE));
    }

    @Test
    void splinteredPainAndShadowRecallNoLongerShortenTheDash() {
        assertEquals(23, tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(3, 13))
                .get(Setting.REND_RANGE, 20), 1.0E-6);
        assertEquals(23, tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(3, 22))
                .get(Setting.REND_RANGE, 20), 1.0E-6);
        assertEquals(23, tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(3, 16))
                .get(Setting.REND_RANGE, 20), 1.0E-6);
        assertEquals(3, tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(13))
                .get(Setting.SPLINTER_RANGE, 0), 1.0E-6);
        assertEquals(6, tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(22))
                .get(Setting.RECALL_RANGE, 0), 1.0E-6);
    }

    @Test
    void livingShadowAndVoidCrossingNoLongerGutTheDashSilently() {
        StormSoulMasteryTuning living = tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(1, 25));
        StormSoulMasteryTuning voidCrossing = tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(1, 26));

        assertEquals(1.1, living.get(Setting.REND_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.2, living.get(Setting.LIVING_SHADOW_MULTIPLIER, 0), 1.0E-6);
        assertEquals(0, voidCrossing.get(Setting.REND_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.6, voidCrossing.get(Setting.VOID_COLLAPSE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(12, voidCrossing.get(Setting.VOID_COLLAPSE_RADIUS, 0), 1.0E-6);
        assertEquals(30, voidCrossing.get(Setting.REND_RANGE, 20), 1.0E-6);
    }

    @Test
    void reopenAndPlagueNoLongerShortenPersistentCorruption() {
        assertEquals(260, tune(StormSoulMasteryAbilities.DREADWHISPER_WOUND, List.of(9, 14))
                .integer(Setting.WOUND_DURATION_TICKS, 200));
        assertEquals(260, tune(StormSoulMasteryAbilities.DREADWHISPER_WOUND, List.of(9, 16))
                .integer(Setting.WOUND_DURATION_TICKS, 200));
        assertEquals(80, tune(StormSoulMasteryAbilities.DREADWHISPER_WOUND, List.of(9, 14))
                .integer(Setting.REOPEN_DURATION_TICKS, 0));
        assertEquals(120, tune(StormSoulMasteryAbilities.DREADWHISPER_WOUND, List.of(9, 16))
                .integer(Setting.SPREAD_DURATION_TICKS, 0));
    }

    @Test
    void capstonesComposeWithTheirPrerequisites() {
        assertEquals(1.1, tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(1))
                .get(Setting.REND_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.77, tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(1, 7))
                .get(Setting.REND_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.42, tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(1, 8))
                .get(Setting.REND_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.12, tune(StormSoulMasteryAbilities.DREADWHISPER_WOUND, List.of(10))
                .get(Setting.WOUND_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.8, tune(StormSoulMasteryAbilities.DREADWHISPER_WOUND, List.of(10, 17))
                .get(Setting.WOUND_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.784, tune(StormSoulMasteryAbilities.DREADWHISPER_WOUND, List.of(10, 16))
                .get(Setting.WOUND_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void deepLeechCarriesItsAdvertisedCeiling() {
        StormSoulMasteryTuning leech = tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(4));

        assertEquals(.4, leech.get(Setting.LEECH_RATIO, .35), 1.0E-6);
        assertEquals(18, leech.get(Setting.LEECH_CAP, 0), 1.0E-6);
    }

    @Test
    void sweepingFrontNoLongerCapsTheDashUndocumented() {
        StormSoulMasteryTuning front = tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, List.of(0));

        assertEquals(5, front.get(Setting.REND_WIDTH, 4.5), 1.0E-6);
        assertFalse(front.has(Setting.REND_TARGET_CAP));
    }

    @Test
    void everyPreviouslyInertNodeNowCarriesItsOwnConsumedSettings() {
        StormSoulMasteryTuning rend = tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, REND);
        StormSoulMasteryTuning wound = tune(StormSoulMasteryAbilities.DREADWHISPER_WOUND, WOUND);
        StormSoulMasteryTuning gloam = tune(StormSoulMasteryAbilities.DREADWHISPER_REAVE, GLOAM);

        assertEquals(.4, rend.get(Setting.COLLISION_MULTIPLIER, 0), 1.0E-6);
        assertEquals(3, rend.get(Setting.COLLISION_RADIUS, 0), 1.0E-6);
        assertEquals(.04, rend.get(Setting.MOMENTUM_SPEED_BONUS, 0), 1.0E-6);
        assertEquals(.2, rend.get(Setting.MOMENTUM_SPEED_CAP, 0), 1.0E-6);
        assertEquals(.15, rend.get(Setting.MOMENTUM_DAMAGE_CAP, 0), 1.0E-6);
        assertEquals(1, rend.get(Setting.REND_STOP_ON_HIT, 0), 1.0E-6);
        assertEquals(260, wound.integer(Setting.WOUND_WEAKNESS_TICKS, 0));
        assertEquals(.03, wound.get(Setting.WOUND_AGE_BONUS, 0), 1.0E-6);
        assertEquals(.3, wound.get(Setting.WOUND_AGE_CAP, 0), 1.0E-6);
        assertEquals(.35, wound.get(Setting.SPLINTER_MULTIPLIER, 0), 1.0E-6);
        assertEquals(25, wound.integer(Setting.REOPEN_CHANCE, 0));
        assertEquals(120, wound.integer(Setting.REOPEN_LOCKOUT_TICKS, 0));
        assertEquals(.25, wound.get(Setting.MORTAL_THRESHOLD, 0), 1.0E-6);
        assertEquals(.15, wound.get(Setting.MORTAL_BOSS_BONUS, 0), 1.0E-6);
        assertEquals(2, wound.integer(Setting.SPREAD_COUNT, 0));
        assertEquals(1, gloam.integer(Setting.TRAIL_SLOW_LEVEL, 0));
        assertEquals(60, gloam.integer(Setting.TRAIL_SLOW_TICKS, 0));
        assertEquals(8, gloam.integer(Setting.VEIL_DURATION_TICKS, 0));
        assertEquals(.15, gloam.get(Setting.GLOAM_DAMAGE_RIDER, 0), 1.0E-6);
        assertEquals(1.5, gloam.get(Setting.RECALL_STRENGTH, 0), 1.0E-6);
        assertEquals(6, gloam.get(Setting.SHELTER_ABSORPTION_LIMIT, 0), 1.0E-6);
        assertEquals(60, gloam.integer(Setting.FOOTPRINT_TICKS, 0));
        assertEquals(.25, gloam.get(Setting.FOOTPRINT_PROJECTILE_REDUCTION, 0), 1.0E-6);
        assertEquals(100, gloam.integer(Setting.LIVING_SHADOW_TICKS, 0));
        assertEquals(12, gloam.integer(Setting.LIVING_SHADOW_TARGET_CAP, 0));
    }

    private static StormSoulMasteryTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("dreadwhisper");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(StormSoulMasteryAbilities.COOLDOWN_TICKS, 250);
        }
        builder.set(StormSoulMasteryAbilities.TUNING, StormSoulMasteryTuning.EMPTY);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, definition, builder, profile.nodes().get(node));
        }
        return builder.get(StormSoulMasteryAbilities.TUNING);
    }
}
