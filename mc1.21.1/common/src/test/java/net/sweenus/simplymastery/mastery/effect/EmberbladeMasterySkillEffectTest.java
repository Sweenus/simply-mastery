package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryTuning;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryTuning.Setting;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmberbladeMasterySkillEffectTest {

    private static final List<Integer> ALL = IntStream.range(0, 27).boxed().toList();

    @Test
    void bankedIreAndFedByFlameKeepIndependentWindows() {
        FireForgeMasteryTuning both = shrapnel(List.of(4, 23));

        assertEquals(60, both.integer(Setting.EMBERBLADE_BANK_DURATION_TICKS, 0));
        assertEquals(100, both.integer(Setting.EMBERBLADE_FLAME_BANK_DURATION_TICKS, 0));
        assertEquals(.5, both.get(Setting.EMBERBLADE_BANK_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.05, both.get(Setting.EMBERBLADE_BANK_GAIN, 0), 1.0E-6);
        assertEquals(.25, both.get(Setting.EMBERBLADE_BANK_CAP, 0), 1.0E-6);
    }

    @Test
    void whiteHeatSendsItsGateAsDataRatherThanPreApplyingIt() {
        FireForgeMasteryTuning whiteHeat = shrapnel(List.of(6));

        assertEquals(10, whiteHeat.integer(Setting.EMBERBLADE_FULL_CHARGE_WINDOW_TICKS, 0));
        assertEquals(1.25, whiteHeat.get(Setting.EMBERBLADE_FULL_CHARGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1, whiteHeat.get(Setting.EMBERBLADE_MAX_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(80, whiteHeat.integer(Setting.EMBERBLADE_FULL_CHARGE_FIRE_TICKS, 0));
    }

    @Test
    void theDamageMultipliersStillComposeAcrossBranches() {
        assertEquals(1.12, shrapnel(List.of(0)).get(Setting.EMBERBLADE_MIN_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.15, shrapnel(List.of(1)).get(Setting.EMBERBLADE_MAX_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.84, shrapnel(List.of(1, 7)).get(Setting.EMBERBLADE_MAX_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.896, shrapnel(List.of(0, 17)).get(Setting.EMBERBLADE_MIN_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.5376, shrapnel(List.of(0, 17, 25)).get(Setting.EMBERBLADE_MIN_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void cooldownComposesAcrossHairTriggerAndIreIncarnate() {
        assertEquals(40, shrapnel(List.of(8)).integer(Setting.COOLDOWN_TICKS, 0));
        assertEquals(140, shrapnel(List.of(26)).integer(Setting.COOLDOWN_TICKS, 0));
        assertEquals(120, shrapnel(List.of(8, 26)).integer(Setting.COOLDOWN_TICKS, 0));
    }

    @Test
    void emberBarrageSupersedesShrapnelBloom() {
        assertEquals(2, shrapnel(List.of(20)).integer(Setting.EMBERBLADE_FRAGMENT_COUNT, 0));
        assertEquals(5, shrapnel(List.of(20, 25)).integer(Setting.EMBERBLADE_FRAGMENT_COUNT, 0));
        assertEquals(.45, shrapnel(List.of(20, 25)).get(Setting.EMBERBLADE_FRAGMENT_DAMAGE_MULTIPLIER, 0), 1.0E-6);
    }

    @Test
    void flashoverCarriesEveryValueItsBlastNeeds() {
        FireForgeMasteryTuning flashover = shrapnel(List.of(22));

        assertEquals(3, flashover.integer(Setting.EMBERBLADE_FLASHOVER_COUNT, 0));
        assertEquals(100, flashover.integer(Setting.EMBERBLADE_FLASHOVER_WINDOW_TICKS, 0));
        assertEquals(3, flashover.get(Setting.EMBERBLADE_FLASHOVER_RADIUS, 0), 1.0E-6);
        assertEquals(8, flashover.integer(Setting.EMBERBLADE_FLASHOVER_TARGET_CAP, 0));
        assertEquals(.4, flashover.get(Setting.EMBERBLADE_FLASHOVER_DAMAGE_MULTIPLIER, 0), 1.0E-6);
    }

    @Test
    void duelistsReleaseIsAPureModeNodeAndBlastbackKeepsItsOwnRecoil() {
        assertTrue((shrapnel(List.of(16)).integer(Setting.MODE, 0) & (1 << 16)) != 0);
        assertEquals(1.5, shrapnel(List.of(11)).get(Setting.EMBERBLADE_RECOIL_DISTANCE, 0), 1.0E-6);
        assertEquals(4, shrapnel(List.of(11, 17)).get(Setting.EMBERBLADE_RECOIL_DISTANCE, 0), 1.0E-6);
        assertEquals(40, shrapnel(List.of(17)).integer(Setting.EMBERBLADE_BLASTBACK_RESISTANCE_TICKS, 0));
    }

    @Test
    void everyNodeContributesItsOwnOwnershipBit() {
        int mode = shrapnel(ALL).integer(Setting.MODE, 0);

        for (int slot = 0; slot < 27; slot++) {
            assertTrue((mode & (1 << slot)) != 0, "ownership bit " + slot);
        }
        assertFalse((shrapnel(List.of(0)).integer(Setting.MODE, 0) & (1 << 16)) != 0);
    }

    private static FireForgeMasteryTuning shrapnel(List<Integer> nodes) {
        return tune(FireForgeMasteryAbilities.EMBERBLADE_SHRAPNEL, nodes);
    }

    private static FireForgeMasteryTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("emberblade");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(FireForgeMasteryAbilities.COOLDOWN_TICKS, 60);
        }
        builder.set(FireForgeMasteryAbilities.TUNING, FireForgeMasteryTuning.EMPTY);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, definition, builder, profile.nodes().get(node));
        }
        return builder.get(FireForgeMasteryAbilities.TUNING);
    }
}
