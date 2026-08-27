package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase2AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase2AbilityTuning.Setting;
import net.sweenus.simplyswords.api.ability.Phase2UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class WraithmawMasterySkillEffectTest {

    @Test
    void everyWraithmawNodeRoutesToTheMusterAbility() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("wraithmaw");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(135 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void hauntedSteelNoLongerOverwritesTheFallCadence() {
        Phase2AbilityTuning tuning = tune(List.of(5, 22));

        assertEquals(1, tuning.integer(Setting.INTERVAL_TICKS, 2));
        assertEquals(40, tuning.integer(Setting.HAUNT_INTERVAL_TICKS, 0));
        assertEquals(3, tuning.get(Setting.HAUNT_RANGE, 0), 1.0E-6);
        assertEquals(.25, tuning.get(Setting.HAUNT_DAMAGE_MULTIPLIER, 0), 1.0E-6);
    }

    @Test
    void graveWoundNoLongerOverwritesStoredMalice() {
        Phase2AbilityTuning tuning = tune(List.of(15, 21));

        assertEquals(.04, tuning.get(Setting.BONUS_PER_TRIGGER, 0), 1.0E-6);
        assertEquals(.24, tuning.get(Setting.BONUS_CAP, 0), 1.0E-6);
        assertEquals(.15, tuning.get(Setting.GLOAM_VULNERABILITY_BONUS, 0), 1.0E-6);
    }

    @Test
    void everyGloamGraveyardNodeUsesItsOwnRangeAndTargetCap() {
        Phase2AbilityTuning tuning = tune(IntStream.range(18, 27).boxed().toList());

        assertEquals(3, tuning.get(Setting.HAUNT_RANGE, 0), 1.0E-6);
        assertEquals(5, tuning.get(Setting.BURIAL_RANGE, 0), 1.0E-6);
        assertEquals(4, tuning.get(Setting.RECALL_RANGE, 0), 1.0E-6);
        assertEquals(6, tuning.get(Setting.GRAVEWALK_RANGE, 0), 1.0E-6);
        assertEquals(12, tuning.get(Setting.BURST_RANGE, 0), 1.0E-6);
        assertEquals(8, tuning.integer(Setting.HAUNT_TARGET_CAP, 0));
        assertEquals(5, tuning.integer(Setting.RECALL_TARGET_CAP, 0));
        assertEquals(8, tuning.integer(Setting.BURST_TARGET_CAP, 0));
        assertEquals(.25, tuning.get(Setting.HAUNT_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.6, tuning.get(Setting.GRAVEWALK_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.9, tuning.get(Setting.BURST_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(16 | 32 | 64 | 128 | 256,
                tuning.integer(Setting.MODE, 0) & (16 | 32 | 64 | 128 | 256));
    }

    @Test
    void mausoleumBurstDoesNotTouchTheCastDamageMultiplier() {
        Phase2AbilityTuning tuning = tune(List.of(2, 26));

        assertEquals(1.1, tuning.get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.9, tuning.get(Setting.BURST_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(200, tuning.integer(Setting.BURST_WINDOW_TICKS, 0));
    }

    @Test
    void heavyFallComposesWithBothSignatureCapstones() {
        Phase2AbilityTuning tempest = tune(List.of(2, 7));
        Phase2AbilityTuning guillotine = tune(List.of(2, 8));

        assertEquals(.715, tempest.get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.75, guillotine.get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertNotEquals(.65, tempest.get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void capstoneCooldownDrawbacksMatchTheirDescriptions() {
        assertEquals(720, tune(List.of(7)).integer(Setting.COOLDOWN_TICKS, 600));
        assertEquals(700, tune(List.of(8)).integer(Setting.COOLDOWN_TICKS, 600));
        assertEquals(720, tune(List.of(26)).integer(Setting.COOLDOWN_TICKS, 600));
        assertEquals(840, tune(List.of(7, 26)).integer(Setting.COOLDOWN_TICKS, 600));
    }

    @Test
    void crownOfBladesTunesItsLaunchCountAndLoneExecutionerItsLockout() {
        Phase2AbilityTuning crown = tune(List.of(16));
        Phase2AbilityTuning lone = tune(List.of(17));

        assertEquals(2, crown.integer(Setting.LAUNCH_COUNT, 1));
        assertEquals(12, crown.integer(Setting.ORBIT_CAP, 6));
        assertEquals(1, lone.integer(Setting.ORBIT_CAP, 6));
        assertEquals(80, lone.integer(Setting.LOCKOUT_TICKS, 0));
        assertEquals(1, lone.integer(Setting.LAUNCH_COUNT, 1));
    }

    private static Phase2AbilityTuning tune(List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("wraithmaw");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(Phase2UniqueAbilities.WRAITHMAW_MUSTER);
        builder.set(Phase2UniqueAbilities.COOLDOWN_TICKS, 600);
        builder.set(Phase2UniqueAbilities.TUNING, Phase2AbilityTuning.EMPTY
                .with(Setting.COOLDOWN_TICKS, 600)
                .with(Setting.DAMAGE_MULTIPLIER, 1)
                .with(Setting.MATERIALIZE_TICKS, 12)
                .with(Setting.FALL_SPEED, 1.25));
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, Phase2UniqueAbilities.WRAITHMAW_MUSTER, builder, profile.nodes().get(node));
        }
        return builder.get(Phase2UniqueAbilities.TUNING);
    }
}
