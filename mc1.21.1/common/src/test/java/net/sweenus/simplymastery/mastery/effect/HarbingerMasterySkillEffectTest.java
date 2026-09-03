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

final class HarbingerMasterySkillEffectTest {

    private static final List<Integer> SIGNATURE = IntStream.range(0, 9).boxed().toList();
    private static final List<Integer> COMBAT = IntStream.range(9, 18).boxed().toList();
    private static final List<Integer> ALL = IntStream.range(0, 27).boxed().toList();

    @Test
    void everyHarbingerNodeRoutesToTheLongPathFinalFormsAbilities() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("harbinger");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(54 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void bothCapstonesComposeInsteadOfDiscardingTheSignatureBranch() {
        assertEquals(1.1, standard(List.of(0)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.76, standard(List.of(0, 8)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.88, standard(List.of(0, 7)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.375, standard(List.of(0, 17)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.1, standard(List.of(0, 7, 17)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void executionersPortentNoLongerCripplesTheAuraTargetCap() {
        assertFalse(standard(List.of(26)).has(Setting.TARGET_CAP));
        assertFalse(omen(List.of(26)).has(Setting.TARGET_CAP));
        assertFalse(standard(List.of(6)).has(Setting.TARGET_CAP));
        assertFalse(standard(ALL).has(Setting.TARGET_CAP));
    }

    @Test
    void graspingVoidSurvivesBothPullCapstones() {
        assertEquals(.3125, standard(List.of(2)).get(Setting.PULL_STRENGTH, .25), 1.0E-6);
        assertEquals(.3125, standard(List.of(2, 6)).get(Setting.PULL_STRENGTH, .25), 1.0E-6);
        assertEquals(.3125, standard(List.of(2, 8)).get(Setting.PULL_STRENGTH, .25), 1.0E-6);
        assertEquals(1.5, standard(List.of(2, 6)).get(Setting.CYCLE_PULL_STRENGTH, 0), 1.0E-6);
        assertEquals(1.5, standard(List.of(2, 8)).get(Setting.CONSTANT_PULL_STRENGTH, 0), 1.0E-6);
    }

    @Test
    void everyRangeKeepsItsOwnMeaning() {
        LongPathFinalFormsMasteryTuning all = standard(List.of(5, 7, 11));

        assertEquals(2, all.get(Setting.CORE_RANGE, 0), 1.0E-6);
        assertEquals(12, all.get(Setting.PURSUIT_RANGE, 0), 1.0E-6);
        assertEquals(7, all.get(Setting.OWNER_AURA_RANGE, 0), 1.0E-6);
        assertFalse(all.has(Setting.RANGE));
    }

    @Test
    void crushingGloomKeepsItsWindowAndDurationAgainstEverySupportNode() {
        LongPathFinalFormsMasteryTuning all = standard(List.of(3, 9, 17));

        assertEquals(3, all.integer(Setting.WEAKNESS_PULSE_COUNT, 0));
        assertEquals(40, all.integer(Setting.WEAKNESS_WINDOW_TICKS, 0));
        assertEquals(80, all.integer(Setting.WEAKNESS_DURATION_TICKS, 0));
        assertEquals(120, all.integer(Setting.HASTE_DURATION_TICKS, 0));
        assertFalse(all.has(Setting.STATUS_DURATION_TICKS));
    }

    @Test
    void theSupportBranchNoLongerSharesOneDurationKey() {
        LongPathFinalFormsMasteryTuning all = standard(List.of(12, 13, 14, 15));

        assertEquals(80, all.integer(Setting.ALLY_SPEED_TICKS, 0));
        assertEquals(80, all.integer(Setting.ALLY_CHARGE_TICKS, 0));
        assertEquals(60, all.integer(Setting.ALLY_WEAKNESS_TICKS, 0));
        assertEquals(3, all.integer(Setting.RALLY_ALLY_COUNT, 0));
        assertEquals(.15, all.get(Setting.KNOCKBACK_RESISTANCE, 0), 1.0E-6);
        assertFalse(all.has(Setting.DURATION_TICKS));
        assertFalse(all.has(Setting.COUNT));
    }

    @Test
    void theOmenLockoutsNoLongerOverwriteEachOther() {
        LongPathFinalFormsMasteryTuning all = omen(List.of(20, 22, 23));

        assertEquals(200, all.integer(Setting.OMEN_WINDOW_TICKS, 0));
        assertEquals(80, all.integer(Setting.OMEN_UPGRADE_TICKS, 0));
        assertEquals(20, all.integer(Setting.DOOM_PULL_LOCKOUT_TICKS, 0));
        assertEquals(100, all.integer(Setting.PROPHECY_REFUND_CAP_TICKS, 0));
        assertEquals(10, all.get(Setting.DOOM_PULL_RANGE, 0), 1.0E-6);
        assertFalse(all.has(Setting.LOCKOUT_TICKS));
    }

    @Test
    void finalOmenCarriesItsThresholdAndItsBossClause() {
        LongPathFinalFormsMasteryTuning omen = standard(List.of(24));

        assertEquals(.25, omen.get(Setting.LOW_HEALTH_THRESHOLD, 0), 1.0E-6);
        assertEquals(1.2, omen.get(Setting.LOW_HEALTH_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(1.1, omen.get(Setting.BOSS_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertFalse(omen.has(Setting.THRESHOLD));
    }

    @Test
    void thePlagueAndExecutionMultipliersNoLongerShareOneKey() {
        assertEquals(.8, standard(List.of(25)).get(Setting.PLAGUE_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(120, standard(List.of(25)).integer(Setting.PLAGUE_WEAKNESS_TICKS, 0));
        assertEquals(1.4, standard(List.of(26)).get(Setting.EXECUTION_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertFalse(standard(List.of(25)).has(Setting.WEAKENED_DAMAGE_MULTIPLIER));
        assertFalse(standard(List.of(26)).has(Setting.WEAKENED_DAMAGE_MULTIPLIER));
        assertEquals(220, omen(List.of(19)).integer(Setting.STATUS_DURATION_TICKS, 0));
    }

    @Test
    void bothCapstonesStillReachBothDefinitions() {
        for (int node : new int[]{25, 26}) {
            assertFalse(standard(List.of(node)).isEmpty(), "standard " + node);
            assertFalse(omen(List.of(node)).isEmpty(), "omen " + node);
        }
        assertEquals(780, omen(List.of(26)).integer(Setting.COOLDOWN_TICKS, 0));
    }

    @Test
    void everyModeBitStillReachesItsOwnDefinition() {
        int standardMode = standard(ALL).integer(Setting.MODE, 0);
        int omenMode = omen(ALL).integer(Setting.MODE, 0);

        for (int bit = 1; bit <= 2048; bit <<= 1) assertTrue((standardMode & bit) != 0, "standard bit " + bit);
        assertTrue((standardMode & 32768) != 0);
        for (int bit : new int[]{4096, 8192, 16384}) assertTrue((omenMode & bit) != 0, "omen bit " + bit);
        for (int bit : new int[]{65536, 131072}) {
            assertTrue((standardMode & bit) != 0, "standard bit " + bit);
            assertTrue((omenMode & bit) != 0, "omen bit " + bit);
        }
    }

    @Test
    void theReserveScopingHoldsInEveryDirection() {
        assertTrue(omen(SIGNATURE).isEmpty());
        assertTrue(omen(COMBAT).isEmpty());
        assertTrue(omen(List.of(24)).isEmpty());
        assertTrue(standard(List.of(18, 19, 20, 21, 22, 23)).isEmpty());
    }

    private static LongPathFinalFormsMasteryTuning standard(List<Integer> nodes) {
        return tune(LongPathFinalFormsMasteryAbilities.HARBINGER_STANDARD, nodes);
    }

    private static LongPathFinalFormsMasteryTuning omen(List<Integer> nodes) {
        return tune(LongPathFinalFormsMasteryAbilities.HARBINGER_OMEN, nodes);
    }

    private static LongPathFinalFormsMasteryTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("harbinger");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(LongPathFinalFormsMasteryAbilities.COOLDOWN_TICKS, 700);
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
