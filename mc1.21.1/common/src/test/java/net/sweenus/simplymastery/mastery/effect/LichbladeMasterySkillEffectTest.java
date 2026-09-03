package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.LongPathFinalFormsMasteryTuning;
import net.sweenus.simplyswords.api.ability.LongPathFinalFormsMasteryTuning.Setting;
import net.sweenus.simplyswords.api.ability.LongPathFinalFormsMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LichbladeMasterySkillEffectTest {

    private static final List<Integer> COMBAT = IntStream.range(9, 18).boxed().toList();
    private static final List<Integer> TRANSFORMATION = IntStream.range(18, 27).boxed().toList();

    @Test
    void everyLichbladeNodeRoutesToTheLongPathFinalFormsAbilities() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("awakened_lichblade");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void resonantSoulsNoLongerShrinksTheExpandingAnguishTargetCap() {
        LongPathFinalFormsMasteryTuning expanding = channel(List.of(1));
        LongPathFinalFormsMasteryTuning both = channel(List.of(1, 4));

        assertEquals(24, expanding.integer(Setting.TARGET_CAP, 0));
        assertEquals(24, both.integer(Setting.TARGET_CAP, 0));
        assertEquals(3.5, both.get(Setting.RADIUS, 0), 1.0E-6);
        assertEquals(.03, both.get(Setting.PER_TARGET_BONUS, 0), 1.0E-6);
        assertEquals(.24, both.get(Setting.BONUS_CAP, 0), 1.0E-6);
    }

    @Test
    void clingingMiseryKeepsItsOwnWindowAndSlowAgainstEveryOtherNode() {
        LongPathFinalFormsMasteryTuning all = channel(List.of(3, 5, 12, 16, 17, 21));

        assertEquals(40, all.integer(Setting.REPEAT_WINDOW_TICKS, 0));
        assertEquals(60, all.integer(Setting.SLOW_DURATION_TICKS, 0));
        assertFalse(all.has(Setting.LOCKOUT_TICKS));
        assertFalse(all.has(Setting.STATUS_DURATION_TICKS));
    }

    @Test
    void resonantSoulsAndGraveInterestAndWanderingPhylacteryNoLongerShareKeys() {
        LongPathFinalFormsMasteryTuning all = channel(List.of(4, 15, 25));

        assertEquals(.03, all.get(Setting.PER_TARGET_BONUS, 0), 1.0E-6);
        assertEquals(.24, all.get(Setting.BONUS_CAP, 0), 1.0E-6);
        assertEquals(10, all.integer(Setting.INTEREST_CHARGE_STEP, 0));
        assertEquals(.1, all.get(Setting.INTEREST_PER_STEP, 0), 1.0E-6);
        assertEquals(.4, all.get(Setting.INTEREST_CAP, 0), 1.0E-6);
        assertEquals(.15, all.get(Setting.RETARGET_DAMAGE_PENALTY, 0), 1.0E-6);
        assertEquals(.4, all.get(Setting.RETARGET_DAMAGE_FLOOR, 0), 1.0E-6);
    }

    @Test
    void measuredIntakeKeepsTwoChargePerHitBesideOverflowingSpiritAndGraveInterest() {
        LongPathFinalFormsMasteryTuning all = channel(List.of(12, 13, 15, 16));

        assertEquals(2, all.integer(Setting.CHARGE_PER_HIT, 0));
        assertEquals(10, all.integer(Setting.CHARGE_LOCKOUT_TICKS, 0));
        assertEquals(4, all.integer(Setting.RESISTANCE_CHARGE_STEP, 0));
        assertEquals(2, all.integer(Setting.BASTION_CHARGE_PER_ABSORPTION, 0));
        assertFalse(all.has(Setting.COUNT));
    }

    @Test
    void temporaryAbsorptionNodesNoLongerTruncateTheChannel() {
        assertFalse(channel(List.of(14)).has(Setting.DURATION_TICKS));
        assertFalse(channel(List.of(16)).has(Setting.DURATION_TICKS));
        assertEquals(80, channel(List.of(14)).integer(Setting.OVERHEAL_ABSORPTION_TICKS, 0));
        assertEquals(160, channel(List.of(16)).integer(Setting.BASTION_ABSORPTION_TICKS, 0));
        assertEquals(240, channel(List.of(14, 16, 20)).integer(Setting.DURATION_TICKS, 0));
    }

    @Test
    void unceasingCryAndSwiftHauntingNoLongerSuppressEachOther() {
        LongPathFinalFormsMasteryTuning both = channel(List.of(6, 19));

        assertEquals(4, both.integer(Setting.FAST_PULSE_INTERVAL_TICKS, 0));
        assertEquals(60, both.integer(Setting.FAST_PULSE_AFTER_TICKS, 0));
        assertEquals(4, both.integer(Setting.CLOUD_MOVE_INTERVAL_TICKS, 0));
        assertFalse(both.has(Setting.INTERVAL_TICKS));
        assertFalse(both.has(Setting.THRESHOLD));
    }

    @Test
    void choirOfTheDamnedTunesMovementAndComposesItsCooldownWithRestlessPhylactery() {
        LongPathFinalFormsMasteryTuning choir = channel(List.of(7));
        LongPathFinalFormsMasteryTuning restless = channel(List.of(24));
        LongPathFinalFormsMasteryTuning both = channel(List.of(7, 24));

        assertEquals(.6, choir.get(Setting.MOVEMENT_MULTIPLIER, 1), 1.0E-6);
        assertFalse(choir.has(Setting.MOVEMENT_SPEED));
        assertEquals(1.2, choir.get(Setting.COOLDOWN_MULTIPLIER, 1), 1.0E-6);
        assertFalse(choir.has(Setting.COOLDOWN_TICKS));
        assertEquals(600, restless.integer(Setting.COOLDOWN_TICKS, 0));
        assertEquals(600, both.integer(Setting.COOLDOWN_TICKS, 0));
        assertEquals(1.2, both.get(Setting.COOLDOWN_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void ravenousPhylacteryTunesItsOwnCooldownPenaltyAndNothingElse() {
        LongPathFinalFormsMasteryTuning ravenous = channel(List.of(17));

        assertEquals(10, ravenous.integer(Setting.COOLDOWN_PER_SIPHON_TICKS, 0));
        assertEquals(200, ravenous.integer(Setting.COOLDOWN_PENALTY_CAP_TICKS, 0));
        assertFalse(ravenous.has(Setting.LOCKOUT_TICKS));
        assertFalse(ravenous.has(Setting.ABSORPTION_CAP));
        assertTrue((ravenous.integer(Setting.MODE, 0) & 2048) != 0);
    }

    @Test
    void soulRecallCarriesItsOwnCooldownPenalty() {
        LongPathFinalFormsMasteryTuning recall = channel(List.of(26));

        assertEquals(100, recall.integer(Setting.RECALL_COOLDOWN_TICKS, 0));
        assertEquals(4, recall.get(Setting.RECALL_RADIUS, 0), 1.0E-6);
        assertEquals(1.25, recall.get(Setting.RECALL_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertTrue((recall.integer(Setting.MODE, 0) & 65536) != 0);
    }

    @Test
    void signatureCapstonesStillComposeWithAgonizingCurrent() {
        assertEquals(1.1, channel(List.of(2)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.715, channel(List.of(2, 7)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.2, channel(List.of(2, 8)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.77, channel(List.of(2, 16)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void everyModeBitStillReachesTheChannel() {
        int mode = channel(IntStream.range(0, 27).boxed().toList()).integer(Setting.MODE, 0);

        for (int bit = 1; bit <= 65536; bit <<= 1) {
            assertTrue((mode & bit) != 0, "mode bit " + bit);
        }
    }

    @Test
    void theAuraOnlySeesItsOwnSignatureNodes() {
        LongPathFinalFormsMasteryTuning aura = aura(IntStream.range(0, 27).boxed().toList());

        assertEquals(30, aura.integer(Setting.AURA_INTERVAL_TICKS, 0));
        assertEquals(3.5, aura.get(Setting.RADIUS, 0), 1.0E-6);
        assertEquals(1.1, aura.get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertFalse(aura.has(Setting.CHANCE));
        assertFalse(aura.has(Setting.HEAL_AMOUNT));
        assertFalse(aura.has(Setting.ACQUISITION_RANGE));
        assertFalse(aura.has(Setting.MOVEMENT_MULTIPLIER));
    }

    @Test
    void combatAndTransformationNodesNeverReachTheAura() {
        assertTrue(aura(COMBAT).isEmpty());
        assertTrue(aura(TRANSFORMATION).isEmpty());
    }

    private static LongPathFinalFormsMasteryTuning channel(List<Integer> nodes) {
        return tune(LongPathFinalFormsMasteryAbilities.LICHBLADE_CHANNEL, nodes);
    }

    private static LongPathFinalFormsMasteryTuning aura(List<Integer> nodes) {
        return tune(LongPathFinalFormsMasteryAbilities.LICHBLADE_AURA, nodes);
    }

    private static LongPathFinalFormsMasteryTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("awakened_lichblade");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(LongPathFinalFormsMasteryAbilities.COOLDOWN_TICKS, 0);
        }
        builder.set(LongPathFinalFormsMasteryAbilities.TUNING, LongPathFinalFormsMasteryTuning.EMPTY);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, definition, builder, profile.nodes().get(node));
        }
        return builder.get(LongPathFinalFormsMasteryAbilities.TUNING);
    }
}
