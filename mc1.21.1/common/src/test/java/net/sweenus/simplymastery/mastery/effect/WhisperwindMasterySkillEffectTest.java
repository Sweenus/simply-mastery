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

final class WhisperwindMasterySkillEffectTest {

    private static final List<Integer> ALL = IntStream.range(0, 27).boxed().toList();
    private static final List<Integer> PETAL = IntStream.range(0, 9).boxed().toList();
    private static final List<Integer> BLOOM = IntStream.range(9, 18).boxed().toList();
    private static final List<Integer> RHYTHM = IntStream.range(18, 27).boxed().toList();

    @Test
    void everyWhisperwindNodeRoutesToTheWhisperwindAbilities() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("whisperwind");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(108 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void noNodeWritesTheOldSharedKeysAnyMore() {
        StormSoulMasteryTuning dash = tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, ALL);
        StormSoulMasteryTuning reset = tune(StormSoulMasteryAbilities.WHISPERWIND_RESET, ALL);

        assertFalse(dash.has(Setting.TARGET_CAP));
        assertFalse(dash.has(Setting.DAMAGE_MULTIPLIER));
        assertFalse(dash.has(Setting.SPEED));
        assertFalse(dash.has(Setting.RANGE));
        assertFalse(dash.has(Setting.MODE));
        assertFalse(dash.has(Setting.FLICKER_DURATION_TICKS));
        assertFalse(dash.has(Setting.ABSORPTION));
        assertFalse(reset.has(Setting.MODE));
        assertFalse(reset.has(Setting.DAMAGE_MULTIPLIER));
    }

    @Test
    void stillWindNoLongerHijacksEveryDelayedStrike() {
        StormSoulMasteryTuning withBloom = tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(9, 10, 26));

        assertEquals(1.232, withBloom.get(Setting.STRIKE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.75, withBloom.get(Setting.STILL_WIND_MULTIPLIER, 1), 1.0E-6);
        assertEquals(3, withBloom.integer(Setting.STILL_WIND_THRESHOLD, 0));
        assertFalse(withBloom.has(Setting.STRIKE_TARGET_CAP));
    }

    @Test
    void crosswindAndSecondFloweringNoLongerCapTheStrike() {
        StormSoulMasteryTuning crosswind = tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(6));
        StormSoulMasteryTuning flowering = tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(13));
        StormSoulMasteryTuning passing = tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(4));

        assertFalse(crosswind.has(Setting.STRIKE_TARGET_CAP));
        assertEquals(1, crosswind.get(Setting.DASH_EXTENSION_PER_TARGET, 0), 1.0E-6);
        assertEquals(4, crosswind.get(Setting.DASH_EXTENSION_CAP, 0), 1.0E-6);
        assertFalse(flowering.has(Setting.STRIKE_TARGET_CAP));
        assertEquals(3, flowering.integer(Setting.FLOWERING_TARGET_CAP, 0));
        assertFalse(passing.has(Setting.STRIKE_TARGET_CAP));
        assertEquals(8, passing.integer(Setting.PASSING_CUT_TARGET_CAP, 0));
    }

    @Test
    void repriseNoLongerForcesEveryRefresh() {
        StormSoulMasteryTuning reset = tune(StormSoulMasteryAbilities.WHISPERWIND_RESET, List.of(18, 23));

        assertEquals(20, reset.integer(Setting.CHANCE, 15));
        assertEquals(80, reset.integer(Setting.REPRISE_WINDOW_TICKS, 0));
    }

    @Test
    void thePlayerPathValuesReachTheDash() {
        StormSoulMasteryTuning dash = tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(0, 2, 19));

        assertEquals(3.3, dash.get(Setting.DASH_SPEED, 3), 1.0E-6);
        assertEquals(18, dash.integer(Setting.DASH_TICKS, 12));
        assertEquals(120, dash.integer(Setting.DASH_ABSORPTION_TICKS, 100));
        assertEquals(4, dash.integer(Setting.DASH_ABSORPTION, 2));
    }

    @Test
    void gentleGuardNoLongerShortensLastingFlicker() {
        assertEquals(120, tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(2, 19))
                .integer(Setting.DASH_ABSORPTION_TICKS, 100));
    }

    @Test
    void strikeCapstonesComposeWithTheirPrerequisites() {
        assertEquals(1.1, tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(9))
                .get(Setting.STRIKE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.232, tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(9, 10))
                .get(Setting.STRIKE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.5544, tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(9, 10, 16))
                .get(Setting.STRIKE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.772, tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(9, 10, 17))
                .get(Setting.STRIKE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.924, tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(9, 10, 7))
                .get(Setting.STRIKE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void everyPreviouslyInertNodeNowCarriesItsOwnConsumedSettings() {
        StormSoulMasteryTuning petal = tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, PETAL);
        StormSoulMasteryTuning bloom = tune(StormSoulMasteryAbilities.WHISPERWIND_DASH, BLOOM);
        StormSoulMasteryTuning rhythm = tune(StormSoulMasteryAbilities.WHISPERWIND_RESET, RHYTHM);

        assertEquals(30, petal.integer(Setting.WAKE_DURATION_TICKS, 0));
        assertEquals(.2, petal.get(Setting.WAKE_KNOCKBACK_RESISTANCE, 0), 1.0E-6);
        assertEquals(.2, petal.get(Setting.PASSING_CUT_MULTIPLIER, 0), 1.0E-6);
        assertEquals(3, petal.integer(Setting.RETURN_THRESHOLD, 0));
        assertEquals(25, petal.integer(Setting.RETURN_REFUND_TICKS, 0));
        assertEquals(1.5, petal.get(Setting.DASH_RANGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2, petal.integer(Setting.STRIKE_COUNT, 1));
        assertEquals(.2, bloom.get(Setting.BOUQUET_PER_TARGET_BONUS, 0), 1.0E-6);
        assertEquals(.35, bloom.get(Setting.FLOWERING_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.15, bloom.get(Setting.ARMOR_IGNORE_RATIO, 0), 1.0E-6);
        assertEquals(4, bloom.get(Setting.ARMOR_IGNORE_CAP, 0), 1.0E-6);
        assertEquals(.3, bloom.get(Setting.SOLO_DAMAGE_BONUS, 0), 1.0E-6);
        assertEquals(5, bloom.integer(Setting.CROWD_THRESHOLD, 0));
        assertEquals(3, bloom.integer(Setting.STRIKE_COUNT, 1));
        assertEquals(3, rhythm.get(Setting.RHYTHM_CHANCE_PER_FAILURE, 0), 1.0E-6);
        assertEquals(15, rhythm.get(Setting.RHYTHM_CHANCE_CAP, 0), 1.0E-6);
        assertEquals(.1, rhythm.get(Setting.FALL_DAMAGE_REDUCTION, 0), 1.0E-6);
        assertEquals(20, rhythm.integer(Setting.WINDBREAK_TICKS, 0));
        assertEquals(.3, rhythm.get(Setting.WINDBREAK_PROJECTILE_REDUCTION, 0), 1.0E-6);
        assertEquals(40, rhythm.integer(Setting.TEMPO_WINDOW_TICKS, 0));
        assertEquals(60, rhythm.integer(Setting.REFRESH_REFUND_TICKS, 0));
        assertEquals(20, rhythm.integer(Setting.REFRESH_INTERVAL_TICKS, 0));
    }

    @Test
    void galePassageAndSingleFallingLeafReachTheCooldown() {
        UniqueAbilityTuning.Builder gale = builder(StormSoulMasteryAbilities.WHISPERWIND_DASH);
        apply(gale, StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(1, 7));
        UniqueAbilityTuning.Builder leaf = builder(StormSoulMasteryAbilities.WHISPERWIND_DASH);
        apply(leaf, StormSoulMasteryAbilities.WHISPERWIND_DASH, List.of(1, 17));

        assertEquals(186, gale.get(StormSoulMasteryAbilities.COOLDOWN_TICKS));
        assertEquals(185, leaf.get(StormSoulMasteryAbilities.COOLDOWN_TICKS));
    }

    private static UniqueAbilityTuning.Builder builder(UniqueAbilityDefinition definition) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(StormSoulMasteryAbilities.COOLDOWN_TICKS, 175);
        }
        builder.set(StormSoulMasteryAbilities.TUNING, StormSoulMasteryTuning.EMPTY);
        return builder;
    }

    private static void apply(UniqueAbilityTuning.Builder builder, UniqueAbilityDefinition definition,
                              List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("whisperwind");
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
