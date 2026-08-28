package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase4AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase4AbilityTuning.Setting;
import net.sweenus.simplyswords.api.ability.Phase4UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SunfireMasterySkillEffectTest {

    private static final List<Integer> SIGNATURE = IntStream.range(0, 9).boxed().toList();
    private static final List<Integer> COMBAT = IntStream.range(9, 18).boxed().toList();
    private static final List<Integer> ALL = IntStream.range(0, 27).boxed().toList();

    @Test
    void everySunfireNodeRoutesToThePhase4Abilities() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("sunfire");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(27 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void solarPillarAndSanctuaryComposeInsteadOfDiscardingTheirPrerequisites() {
        assertEquals(1.1, standard(List.of(0)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.76, standard(List.of(0, 8)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.825, standard(List.of(0, 7)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.408, standard(List.of(0, 8, 25)).get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.12, standard(List.of(9)).get(Setting.HEAL_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.68, standard(List.of(9, 16)).get(Setting.HEAL_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void flareCycleKeepsItsFourthPulseBesideRallyingStandard() {
        Phase4AbilityTuning both = standard(List.of(5, 15));

        assertEquals(4, both.integer(Setting.CYCLE_PULSE_COUNT, 0));
        assertEquals(3, both.integer(Setting.RALLY_ALLY_COUNT, 0));
        assertEquals(40, both.integer(Setting.RALLY_REFUND_TICKS, 0));
        assertFalse(both.has(Setting.COUNT));
    }

    @Test
    void scorchingGroundKeepsItsOwnWindowAndWeaknessAgainstEverySupportNode() {
        Phase4AbilityTuning all = standard(List.of(3, 5, 11, 17));

        assertEquals(3, all.integer(Setting.WEAKNESS_PULSE_COUNT, 0));
        assertEquals(40, all.integer(Setting.WEAKNESS_WINDOW_TICKS, 0));
        assertEquals(60, all.integer(Setting.WEAKNESS_DURATION_TICKS, 0));
        assertEquals(100, all.integer(Setting.STRENGTH_DURATION_TICKS, 0));
        assertFalse(all.has(Setting.STATUS_DURATION_TICKS));
        assertFalse(all.has(Setting.LOCKOUT_TICKS));
    }

    @Test
    void theSupportBranchNoLongerSharesOneDurationKey() {
        Phase4AbilityTuning all = standard(List.of(6, 12, 13, 14, 16, 17));

        assertEquals(100, all.integer(Setting.EARLY_WINDOW_TICKS, 0));
        assertEquals(160, all.integer(Setting.CLEANSE_LOCKOUT_TICKS, 0));
        assertEquals(60, all.integer(Setting.ALLY_REGEN_TICKS, 0));
        assertEquals(100, all.integer(Setting.GUARDIAN_ABSORPTION_TICKS, 0));
        assertEquals(200, all.integer(Setting.GUARDIAN_LOCKOUT_TICKS, 0));
        assertEquals(60, all.integer(Setting.SANCTUARY_RESISTANCE_TICKS, 0));
        assertEquals(60, all.integer(Setting.ALLY_CHARGE_TICKS, 0));
        assertFalse(all.has(Setting.DURATION_TICKS));
        assertFalse(all.has(Setting.THRESHOLD));
        assertFalse(all.has(Setting.ABSORPTION));
    }

    @Test
    void radiantReprisalAndRekindlingAndConsumingSunKeepTheirOwnLockouts() {
        Phase4AbilityTuning all = regen(List.of(22, 24, 26));

        assertEquals(40, all.integer(Setting.REPRISAL_LOCKOUT_TICKS, 0));
        assertEquals(40, all.integer(Setting.REPRISAL_FIRE_TICKS, 0));
        assertEquals(600, all.integer(Setting.REKINDLE_LOCKOUT_TICKS, 0));
        assertEquals(40, all.integer(Setting.FLARE_LOCKOUT_TICKS, 0));
        assertFalse(all.has(Setting.LOCKOUT_TICKS));
    }

    @Test
    void emberReserveAndRekindlingCarryRealValuesAndExpiries() {
        Phase4AbilityTuning reserve = regen(List.of(21));
        Phase4AbilityTuning rekindle = regen(List.of(24));

        assertTrue((reserve.integer(Setting.MODE, 0) & 4096) != 0);
        assertEquals(4, reserve.integer(Setting.RESERVE_CAP, 0));
        assertEquals(.5, reserve.get(Setting.RESERVE_THRESHOLD, 0), 1.0E-6);
        assertEquals(100, reserve.integer(Setting.RESERVE_ABSORPTION_TICKS, 0));
        assertEquals(.3, rekindle.get(Setting.REKINDLE_THRESHOLD, 0), 1.0E-6);
        assertEquals(4, rekindle.integer(Setting.REKINDLE_ABSORPTION, 0));
        assertEquals(100, rekindle.integer(Setting.REKINDLE_DURATION_TICKS, 0));
    }

    @Test
    void standardBearerCarriesBothHalvesOfItsDescription() {
        Phase4AbilityTuning guard = standard(List.of(23));

        assertEquals(7, guard.get(Setting.GUARD_RANGE, 0), 1.0E-6);
        assertEquals(.15, guard.get(Setting.DAMAGE_REDUCTION, 0), 1.0E-6);
        assertEquals(.2, guard.get(Setting.KNOCKBACK_RESISTANCE, 0), 1.0E-6);
        assertFalse(guard.has(Setting.RANGE));
    }

    @Test
    void phoenixStandardTunesItsOwnDurationAndCooldownSurcharge() {
        Phase4AbilityTuning phoenix = standard(List.of(25));

        assertEquals(80, phoenix.integer(Setting.PHOENIX_DURATION_TICKS, 0));
        assertEquals(300, phoenix.integer(Setting.PHOENIX_COOLDOWN_TICKS, 0));
        assertEquals(.8, phoenix.get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void everyModeBitStillReachesItsOwnDefinition() {
        int standardMode = standard(ALL).integer(Setting.MODE, 0);
        int regenMode = regen(ALL).integer(Setting.MODE, 0);

        for (int bit = 1; bit <= 1024; bit <<= 1) assertTrue((standardMode & bit) != 0, "standard bit " + bit);
        assertTrue((standardMode & 16384) != 0);
        assertTrue((standardMode & 65536) != 0);
        for (int bit : new int[]{2048, 4096, 8192, 32768, 131072}) {
            assertTrue((regenMode & bit) != 0, "regen bit " + bit);
        }
    }

    @Test
    void theReserveScopingHoldsInBothDirections() {
        assertTrue(regen(SIGNATURE).isEmpty());
        assertTrue(regen(COMBAT).isEmpty());
        assertFalse(regen(List.of(23)).has(Setting.GUARD_RANGE));
        assertFalse(regen(List.of(25)).has(Setting.PHOENIX_DURATION_TICKS));
        assertFalse(standard(List.of(18)).has(Setting.CHANCE));
        assertFalse(standard(List.of(26)).has(Setting.FLARE_DAMAGE_MULTIPLIER));
    }

    private static Phase4AbilityTuning standard(List<Integer> nodes) {
        return tune(Phase4UniqueAbilities.SUNFIRE_STANDARD, nodes);
    }

    private static Phase4AbilityTuning regen(List<Integer> nodes) {
        return tune(Phase4UniqueAbilities.SUNFIRE_REGEN, nodes);
    }

    private static Phase4AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("sunfire");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(Phase4UniqueAbilities.COOLDOWN_TICKS, 0);
        }
        builder.set(Phase4UniqueAbilities.TUNING, Phase4AbilityTuning.EMPTY);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, definition, builder, profile.nodes().get(node));
        }
        return builder.get(Phase4UniqueAbilities.TUNING);
    }
}
