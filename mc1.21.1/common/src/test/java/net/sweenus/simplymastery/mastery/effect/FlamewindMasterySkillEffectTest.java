package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryTuning;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryTuning.Setting;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlamewindMasterySkillEffectTest {

    private static final List<Integer> ALL = IntStream.range(0, 27).boxed().toList();

    @Test
    void theBackburnReleaseCooldownsStillCompose() {
        assertEquals(100, seed(List.of(21)).integer(Setting.COOLDOWN_TICKS, 350));
        assertEquals(140, seed(List.of(21, 25)).integer(Setting.COOLDOWN_TICKS, 350));
        assertEquals(160, seed(List.of(21, 26)).integer(Setting.COOLDOWN_TICKS, 350));
    }

    @Test
    void theReleaseCapstonesNoLongerDiscardTheSignatureBranch() {
        assertEquals(1.25, seed(List.of(4)).get(Setting.FLAMEWIND_DEATH_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.5, seed(List.of(8)).get(Setting.FINAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.05, seed(List.of(8, 21)).get(Setting.FINAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.1, seed(List.of(8, 25)).get(Setting.FINAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.5, seed(List.of(8, 26)).get(Setting.FINAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void everyPreviouslyInertNodeNowCarriesItsOwnKeys() {
        assertEquals(140, seed(List.of(3)).integer(Setting.FLAMEWIND_HASTE_DURATION_TICKS, 0));
        assertEquals(.03, seed(List.of(5)).get(Setting.FLAMEWIND_DRAFT_PER_SEED, 0), 1.0E-6);
        assertEquals(14, seed(List.of(9)).get(Setting.FLAMEWIND_SEED_RANGE, 0), 1.0E-6);
        assertEquals(6, seed(List.of(12)).get(Setting.FLAMEWIND_RETARGET_RANGE, 0), 1.0E-6);
        assertEquals(7, seed(List.of(14)).get(Setting.FLAMEWIND_SPREAD_RANGE, 0), 1.0E-6);
        assertEquals(1.25, seed(List.of(16)).get(Setting.FLAMEWIND_GENERATION_MULTIPLIER, 0), 1.0E-6);
        assertEquals(6, seed(List.of(18)).integer(Setting.FLAMEWIND_HARVEST_REFUND_TICKS, 0));
        assertEquals(1.15, seed(List.of(19)).get(Setting.FLAMEWIND_CHAIN_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(3, seed(List.of(20)).integer(Setting.FLAMEWIND_FLASH_COUNT, 0));
        assertEquals(2, seed(List.of(23)).get(Setting.FLAMEWIND_RESERVE_ABSORPTION, 0), 1.0E-6);
        assertEquals(3, seed(List.of(24)).integer(Setting.FLAMEWIND_RESET_KILL_COUNT, 0));
    }

    @Test
    void freshFuelTunesDurationRatherThanDamage() {
        FireForgeMasteryTuning fresh = seed(List.of(15));

        assertEquals(.8, fresh.get(Setting.FLAMEWIND_SPREAD_DURATION_FRACTION, 0), 1.0E-6);
        assertEquals(1, fresh.get(Setting.PERIODIC_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.188, seed(List.of(0, 11, 15)).get(Setting.PERIODIC_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void cinderReserveNoLongerShrinksTheDetonationCap() {
        assertFalse(seed(List.of(23)).has(Setting.TARGET_CAP));
        assertEquals(12, seed(List.of(8, 23)).integer(Setting.TARGET_CAP, 10));
    }

    @Test
    void bothRecastLockoutsAreRealValues() {
        assertEquals(100, seed(List.of(21)).integer(Setting.FLAMEWIND_RECAST_LOCKOUT_TICKS, 0));
        assertEquals(160, seed(List.of(21, 26)).integer(Setting.FLAMEWIND_RECAST_LOCKOUT_TICKS, 0));
    }

    @Test
    void everyNodeContributesItsOwnOwnershipBit() {
        int mode = seed(ALL).integer(Setting.MODE, 0);

        for (int slot = 0; slot < 27; slot++) {
            assertTrue((mode & (1 << slot)) != 0, "ownership bit " + slot);
        }
    }

    private static FireForgeMasteryTuning seed(List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("flamewind");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(FireForgeMasteryAbilities.FLAMEWIND_SEED);
        builder.set(FireForgeMasteryAbilities.COOLDOWN_TICKS, 350);
        builder.set(FireForgeMasteryAbilities.TUNING, FireForgeMasteryTuning.EMPTY);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, FireForgeMasteryAbilities.FLAMEWIND_SEED, builder, profile.nodes().get(node));
        }
        return builder.get(FireForgeMasteryAbilities.TUNING);
    }
}
