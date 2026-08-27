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

final class SoulrenderMasterySkillEffectTest {

    private static final List<Integer> ALL = IntStream.range(0, 27).boxed().toList();
    private static final List<Integer> RENDMARKS = IntStream.range(0, 9).boxed().toList();
    private static final List<Integer> REAPING = IntStream.range(9, 18).boxed().toList();
    private static final List<Integer> GRAVEBOUND = IntStream.range(18, 27).boxed().toList();

    @Test
    void everySoulrenderNodeRoutesToTheSoulrenderAbilities() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("soulrender");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(54 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void rendmarksNoLongerLeakIntoTheReaping() {
        Phase3AbilityTuning reap = tune(Phase3UniqueAbilities.SOULRENDER_REAP, RENDMARKS);

        assertFalse(reap.has(Setting.CHANCE));
        assertFalse(reap.has(Setting.ECHO_CHANCE));
        assertFalse(reap.has(Setting.TARGET_CAP));
        assertFalse(reap.has(Setting.REAP_TARGET_CAP));
        assertFalse(reap.has(Setting.MARK_DURATION_TICKS));
        assertFalse(reap.has(Setting.MELEE_BONUS_PER_STACK));
    }

    @Test
    void echoedCurseNoLongerCripplesTheMarkRollOrTheHarvest() {
        Phase3AbilityTuning mark = tune(Phase3UniqueAbilities.SOULRENDER_MARK, List.of(0, 5));
        Phase3AbilityTuning reap = tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(0, 5));

        assertFalse(mark.has(Setting.CHANCE));
        assertEquals(5, mark.integer(Setting.CHANCE_BONUS, 0));
        assertEquals(25, mark.integer(Setting.ECHO_CHANCE, 0));
        assertEquals(4, mark.get(Setting.ECHO_RANGE, 0), 1.0E-6);
        assertEquals(1, mark.integer(Setting.ECHO_TARGET_CAP, 0));
        assertFalse(reap.has(Setting.ECHO_TARGET_CAP));
        assertFalse(reap.has(Setting.REAP_TARGET_CAP));
    }

    @Test
    void condemnationAndWitheringPalimpsestStillReachTheHarvest() {
        Phase3AbilityTuning reap = tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(6));
        Phase3AbilityTuning capstone = tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(6, 8));
        Phase3AbilityTuning mark = tune(Phase3UniqueAbilities.SOULRENDER_MARK, List.of(2, 8));

        assertEquals(.06, reap.get(Setting.REAP_STACK_BONUS, 0), 1.0E-6);
        assertEquals(.36, reap.get(Setting.REAP_STACK_BONUS_CAP, 0), 1.0E-6);
        assertEquals(.15, capstone.get(Setting.REAP_STACK_BONUS, 0), 1.0E-6);
        assertEquals(.75, capstone.get(Setting.REAP_STACK_BONUS_CAP, 0), 1.0E-6);
        assertEquals(5, mark.integer(Setting.STACK_CAP, 8));
    }

    @Test
    void pallbearersLedgerCarriesItsDrawback() {
        Phase3AbilityTuning mark = tune(Phase3UniqueAbilities.SOULRENDER_MARK, List.of(7));

        assertTrue((mark.integer(Setting.MODE, 0) & 4) != 0);
        assertEquals(20, mark.integer(Setting.REPEAT_CHANCE_PENALTY, 0));
        assertFalse(mark.has(Setting.CHANCE));
        assertFalse(mark.has(Setting.BONUS_CAP));
    }

    @Test
    void theReapingBranchNeverShrinksItsOwnRadiusOrCap() {
        Phase3AbilityTuning sweeping = tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(9));
        Phase3AbilityTuning sharedEnding = tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(9, 14));
        Phase3AbilityTuning reach = tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(9, 13));

        assertEquals(12, sweeping.get(Setting.RADIUS, 10), 1.0E-6);
        assertFalse(sweeping.has(Setting.REAP_TARGET_CAP));
        assertEquals(12, sharedEnding.get(Setting.RADIUS, 10), 1.0E-6);
        assertEquals(5, sharedEnding.get(Setting.SHARED_END_RANGE, 0), 1.0E-6);
        assertEquals(6, sharedEnding.integer(Setting.SHARED_END_TARGET_CAP, 0));
        assertEquals(12, reach.get(Setting.RADIUS, 10), 1.0E-6);
        assertEquals(4, reach.get(Setting.REACH_BONUS_RANGE, 0), 1.0E-6);
        assertEquals(8, reach.integer(Setting.REACH_TARGET_CAP, 0));
    }

    @Test
    void graveboundNeverShrinksTheHarvestRadiusOrCap() {
        Phase3AbilityTuning reap = tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(9, 19, 21, 24));

        assertEquals(12, reap.get(Setting.RADIUS, 10), 1.0E-6);
        assertFalse(reap.has(Setting.REAP_TARGET_CAP));
        assertFalse(reap.has(Setting.TARGET_CAP));
        assertEquals(6, reap.get(Setting.PATIENCE_RANGE, 0), 1.0E-6);
        assertEquals(8, reap.integer(Setting.COLD_GRIP_TARGET_CAP, 0));
        assertEquals(5, reap.integer(Setting.UNBROKEN_TARGET_CAP, 0));
    }

    @Test
    void harvestCapstonesComposeWithKeenHarvestAndSoulShelter() {
        assertEquals(1.1, tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(10))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.77, tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(10, 16))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.65, tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(10, 17))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.825, tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(10, 25))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.5775, tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(10, 16, 25))
                .get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void healingComposesInsteadOfBeingOverwritten() {
        Phase3AbilityTuning dividend = tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(12));
        Phase3AbilityTuning harvestMoon = tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(12, 16));
        Phase3AbilityTuning tithe = tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(12, 17));
        Phase3AbilityTuning deathsDue = tune(Phase3UniqueAbilities.SOULRENDER_REAP, List.of(12, 26));

        assertEquals(.5, dividend.get(Setting.HEAL_RATIO_BONUS, 0), 1.0E-6);
        assertEquals(9, dividend.get(Setting.HEAL_CAP, 6), 1.0E-6);
        assertEquals(.5, harvestMoon.get(Setting.HEAL_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.5, harvestMoon.get(Setting.HEAL_RATIO_BONUS, 0), 1.0E-6);
        assertEquals(1.25, tithe.get(Setting.HEAL_MULTIPLIER, 1), 1.0E-6);
        assertEquals(0, deathsDue.get(Setting.HEAL_MULTIPLIER, 1), 1.0E-6);
        assertFalse(deathsDue.has(Setting.HEAL_RATIO));
    }

    @Test
    void graveboundNodesReachTheirOutOfExecutionHook() {
        Phase3AbilityTuning grave = tune(Phase3UniqueAbilities.SOULRENDER_GRAVE, GRAVEBOUND);

        assertEquals(6, grave.get(Setting.PATIENCE_RANGE, 0), 1.0E-6);
        assertEquals(.1, grave.get(Setting.KNOCKBACK_RESISTANCE_BONUS, 0), 1.0E-6);
        assertEquals(40, grave.integer(Setting.BORROWED_TIME_TICKS, 0));
        assertEquals(40, grave.integer(Setting.BORROWED_TIME_LOCKOUT_TICKS, 0));
        assertEquals(40, grave.integer(Setting.COLD_GRIP_SLOW_TICKS, 0));
        assertEquals(5, grave.integer(Setting.UNBROKEN_MARK_THRESHOLD, 0));
        assertEquals(10, grave.get(Setting.UNBROKEN_RANGE, 0), 1.0E-6);
        assertEquals(1200, grave.integer(Setting.UNBROKEN_LOCKOUT_TICKS, 0));
    }

    @Test
    void theMarkAndTheGraveboundHookShareNoSettings() {
        Phase3AbilityTuning mark = tune(Phase3UniqueAbilities.SOULRENDER_MARK, ALL);
        Phase3AbilityTuning grave = tune(Phase3UniqueAbilities.SOULRENDER_GRAVE, ALL);

        assertFalse(mark.has(Setting.RADIUS));
        assertFalse(mark.has(Setting.DAMAGE_MULTIPLIER));
        assertFalse(mark.has(Setting.HEAL_MULTIPLIER));
        assertFalse(grave.has(Setting.CHANCE_BONUS));
        assertFalse(grave.has(Setting.STACK_CAP));
        assertFalse(grave.has(Setting.MELEE_BONUS_CAP));
        assertFalse(grave.has(Setting.REAP_STACK_BONUS));
    }

    @Test
    void everyPreviouslyInertNodeNowCarriesItsOwnConsumedSettings() {
        Phase3AbilityTuning mark = tune(Phase3UniqueAbilities.SOULRENDER_MARK, RENDMARKS);
        Phase3AbilityTuning reap = tune(Phase3UniqueAbilities.SOULRENDER_REAP, REAPING);
        Phase3AbilityTuning grave = tune(Phase3UniqueAbilities.SOULRENDER_GRAVE, GRAVEBOUND);
        Phase3AbilityTuning graveReap = tune(Phase3UniqueAbilities.SOULRENDER_REAP, GRAVEBOUND);

        assertEquals(2, mark.integer(Setting.FRESH_INK_STACKS, 1));
        assertEquals(80, mark.integer(Setting.FRESH_INK_LOCKOUT_TICKS, 0));
        assertEquals(3, reap.integer(Setting.REAP_SPEED_THRESHOLD, 0));
        assertEquals(60, reap.integer(Setting.REAP_SPEED_DURATION_TICKS, 0));
        assertEquals(6, reap.integer(Setting.REAP_HASTE_THRESHOLD, 0));
        assertEquals(1, reap.integer(Setting.REAP_HASTE_AMPLIFIER, 0));
        assertEquals(1.5, reap.get(Setting.REACH_PULL_STRENGTH, 0), 1.0E-6);
        assertEquals(.35, reap.get(Setting.SHARED_END_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(2, graveReap.integer(Setting.SHEATH_ABSORPTION, 0));
        assertEquals(.1, graveReap.get(Setting.GRAVE_RESERVE_RATIO, 0), 1.0E-6);
        assertEquals(6, graveReap.get(Setting.GRAVE_RESERVE_CAP, 0), 1.0E-6);
        assertEquals(.35, graveReap.get(Setting.GRAVE_RESERVE_HEALTH_THRESHOLD, 0), 1.0E-6);
        assertEquals(.2, graveReap.get(Setting.QUIETUS_HEALTH_THRESHOLD, 0), 1.0E-6);
        assertEquals(6, graveReap.integer(Setting.QUIETUS_ABSORPTION_CAP, 0));
        assertEquals(.4, graveReap.get(Setting.SHELTER_ABSORPTION_RATIO, 0), 1.0E-6);
        assertEquals(8, graveReap.get(Setting.SHELTER_ABSORPTION_CAP, 0), 1.0E-6);
        assertEquals(.1, graveReap.get(Setting.TITHE_KILL_BONUS, 0), 1.0E-6);
        assertEquals(.5, graveReap.get(Setting.TITHE_BONUS_CAP, 0), 1.0E-6);
        assertEquals(100, grave.integer(Setting.TITHE_DURATION_TICKS, 0));
    }

    @Test
    void theOnlyRemainingModeBitHasAConsumer() {
        assertEquals(4, tune(Phase3UniqueAbilities.SOULRENDER_MARK, ALL).integer(Setting.MODE, 0));
        assertFalse(tune(Phase3UniqueAbilities.SOULRENDER_REAP, ALL).has(Setting.MODE));
        assertFalse(tune(Phase3UniqueAbilities.SOULRENDER_GRAVE, ALL).has(Setting.MODE));
    }

    @Test
    void theHarvestKeepsItsZeroCooldownUnderAFullTree() {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(Phase3UniqueAbilities.SOULRENDER_REAP);
        builder.set(Phase3UniqueAbilities.COOLDOWN_TICKS, 0);
        builder.set(Phase3UniqueAbilities.TUNING, Phase3AbilityTuning.EMPTY);
        MasteryProfile profile = BuiltInFamilyProfiles.profile("soulrender");
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : ALL) {
            effect.tune(null, Phase3UniqueAbilities.SOULRENDER_REAP, builder, profile.nodes().get(node));
        }

        assertEquals(0, builder.get(Phase3UniqueAbilities.COOLDOWN_TICKS));
    }

    private static Phase3AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("soulrender");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(Phase3UniqueAbilities.COOLDOWN_TICKS, 0);
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
