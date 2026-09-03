package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.AbyssalSpectralMasteryTuning;
import net.sweenus.simplyswords.api.ability.AbyssalSpectralMasteryTuning.Setting;
import net.sweenus.simplyswords.api.ability.AbyssalSpectralMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WickpiercerMasterySkillEffectTest {

    @Test
    void everyWickpiercerNodeRoutesToTheAbyssalSpectralAbilityEffect() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("wickpiercer");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class,
                    SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(54 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void owningEveryNodeLeavesEachSharedSettingAtItsIntendedValue() {
        AbyssalSpectralMasteryTuning tuning = tuneAll();

        assertEquals(60.0, tuning.get(Setting.PROJECTILE_FIRE_TICKS, 0));
        assertEquals(40.0, tuning.get(Setting.FIRE_TICKS, 0));
        assertEquals(100.0, tuning.get(Setting.REVIVE_FIRE_TICKS, 0));

        assertEquals(.7, tuning.get(Setting.PIERCE_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.45, tuning.get(Setting.SECONDARY_DAMAGE_MULTIPLIER, 0), 1.0E-6);

        assertEquals(2.5, tuning.get(Setting.IMPACT_RADIUS, 0), 1.0E-6);
        assertEquals(3.0, tuning.get(Setting.MELEE_IMPACT_RADIUS, 0), 1.0E-6);
        assertEquals(5.0, tuning.get(Setting.PYRE_RADIUS, 0), 1.0E-6);
        assertEquals(.35, tuning.get(Setting.IMPACT_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.45, tuning.get(Setting.MELEE_IMPACT_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(2.0, tuning.get(Setting.PYRE_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(5.0, tuning.get(Setting.IMPACT_TARGET_CAP, 0));
        assertEquals(6.0, tuning.get(Setting.MELEE_IMPACT_TARGET_CAP, 0));
        assertEquals(12.0, tuning.get(Setting.PYRE_TARGET_CAP, 0));

        assertEquals(20.0, tuning.get(Setting.LOCKOUT_TICKS, 0));
        assertEquals(40.0, tuning.get(Setting.TWIN_LOCKOUT_TICKS, 0));
        assertEquals(300.0, tuning.get(Setting.GUARD_LOCKOUT_TICKS, 0));

        assertEquals(40.0, tuning.get(Setting.DURATION_TICKS, 0));
        assertEquals(60.0, tuning.get(Setting.RETURN_DELAY_TICKS, 0));
        assertEquals(60.0, tuning.get(Setting.ORBIT_DURATION_TICKS, 0));
        assertEquals(100.0, tuning.get(Setting.REVIVE_WINDOW_TICKS, 0));

        assertEquals(.05, tuning.get(Setting.BONUS_PER_TRIGGER, 0), 1.0E-6);
        assertEquals(.2, tuning.get(Setting.BURN_DAMAGE_BONUS, 0), 1.0E-6);

        assertEquals(2.0, tuning.get(Setting.TWIN_STACK_GRANT, 0));
        assertEquals(4.0, tuning.get(Setting.ARMOR_IGNORE, 0));
        assertEquals(30.0, tuning.get(Setting.LOW_HEALTH_PERCENT, 0));
        assertEquals(0.0, tuning.get(Setting.THRESHOLD, 0));

        assertEquals(80.0, tuning.get(Setting.STATUS_DURATION_TICKS, 0));
        assertEquals(60.0, tuning.get(Setting.GUARD_STATUS_TICKS, 0));
        assertEquals(80.0, tuning.get(Setting.ALLY_STATUS_TICKS, 0));
    }

    @Test
    void revivalNodesNoLongerRewriteTheThrowCooldown() {
        AbyssalSpectralMasteryTuning revival = tune(List.of(18, 19, 20, 21, 22, 23, 24, 25, 26));

        assertEquals(33.0, revival.get(Setting.COOLDOWN_TICKS, 0));
        assertEquals(600.0, revival.get(Setting.REVIVE_COOLDOWN_FLOOR_TICKS, 0));

        assertEquals(100.0, tune(List.of(4, 8)).get(Setting.COOLDOWN_TICKS, 0));
        assertEquals(26.0, tune(List.of(4)).get(Setting.COOLDOWN_TICKS, 0));
    }

    @Test
    void revivalCooldownReductionsCompound() {
        assertEquals(.85, tune(List.of(24)).get(Setting.REVIVE_COOLDOWN_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.6, tune(List.of(25)).get(Setting.REVIVE_COOLDOWN_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.51, tune(List.of(24, 25)).get(Setting.REVIVE_COOLDOWN_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void prerequisiteBonusesSurviveTheirCapstones() {
        assertEquals(1.1, tune(List.of(0)).get(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.98, tune(List.of(0, 7)).get(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.66, tune(List.of(0, 8)).get(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1), 1.0E-6);

        assertEquals(1.1, tune(List.of(11)).get(Setting.MELEE_BONUS_PER_STACK, 1), 1.0E-6);
        assertEquals(1.75, tune(List.of(17)).get(Setting.MELEE_BONUS_PER_STACK, 1), 1.0E-6);
        assertEquals(1.925, tune(List.of(11, 17)).get(Setting.MELEE_BONUS_PER_STACK, 1), 1.0E-6);
    }

    @Test
    void capstonesConfigureTheirOwnConsumers() {
        AbyssalSpectralMasteryTuning comet = tune(List.of(7));
        assertTrue((comet.integer(Setting.MODE, 0) & 1) != 0);
        assertEquals(0.0, comet.get(Setting.LOYALTY, 3));
        assertEquals(60.0, comet.get(Setting.RETURN_DELAY_TICKS, 0));

        AbyssalSpectralMasteryTuning orbit = tune(List.of(8));
        assertTrue((orbit.integer(Setting.MODE, 0) & 2) != 0);
        assertEquals(60.0, orbit.get(Setting.ORBIT_DURATION_TICKS, 0));
        assertEquals(20.0, orbit.get(Setting.INTERVAL_TICKS, 1));

        AbyssalSpectralMasteryTuning wildfire = tune(List.of(16));
        assertTrue((wildfire.integer(Setting.MODE, 0) & 64) != 0);
        assertEquals(0.0, wildfire.get(Setting.IMPACT_RADIUS, 0));

        AbyssalSpectralMasteryTuning pyre = tune(List.of(26));
        assertTrue((pyre.integer(Setting.MODE, 0) & 4096) != 0);
        assertEquals(0.0, pyre.get(Setting.REVIVE_HEALTH_MULTIPLIER, 1));
        assertEquals(0.0, pyre.get(Setting.IMPACT_RADIUS, 0));
    }

    @Test
    void returnPathAndLoyaltyNodesTuneTheirOwnConsumers() {
        AbyssalSpectralMasteryTuning trail = tune(List.of(2));
        assertEquals(2.5, trail.get(Setting.RETURN_TRAIL_RADIUS, 0), 1.0E-6);
        assertEquals(.5, trail.get(Setting.RETURN_TRAIL_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(100.0, trail.get(Setting.PROJECTILE_LIFETIME, 80));

        assertEquals(5.0, tune(List.of(4)).get(Setting.LOYALTY, 3));
        assertEquals(0.0, tune(List.of(4, 7)).get(Setting.LOYALTY, 3));
    }

    private static AbyssalSpectralMasteryTuning tuneAll() {
        return tune(java.util.stream.IntStream.range(0, 27).boxed().toList());
    }

    private static AbyssalSpectralMasteryTuning tune(List<Integer> slots) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("wickpiercer");
        UniqueAbilityDefinition definition = AbyssalSpectralMasteryAbilities.WICKPIERCER_THROW;
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        builder.set(AbyssalSpectralMasteryAbilities.COOLDOWN_TICKS, 33);
        builder.set(AbyssalSpectralMasteryAbilities.TUNING, AbyssalSpectralMasteryTuning.EMPTY
                .with(Setting.COOLDOWN_TICKS, 33)
                .with(Setting.PROJECTILE_SPEED, 1.5)
                .with(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1)
                .with(Setting.PROJECTILE_LIFETIME, 80)
                .with(Setting.LOYALTY, 3)
                .with(Setting.STACK_DURATION_TICKS, 80)
                .with(Setting.STACK_CAP, 4));
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int slot : slots) {
            effect.tune(null, definition, builder, profile.nodes().get(slot));
        }
        return builder.get(AbyssalSpectralMasteryAbilities.TUNING);
    }
}
