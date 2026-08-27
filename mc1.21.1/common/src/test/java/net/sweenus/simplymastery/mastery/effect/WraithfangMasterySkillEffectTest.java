package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase2AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase2AbilityTuning.Setting;
import net.sweenus.simplyswords.api.ability.Phase2UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WraithfangMasterySkillEffectTest {

    @Test
    void everyWraithfangNodeRoutesToTheThrowAbility() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("wraithfang");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(108 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void unrelatedDamageAndDurationNodesNeverOverwriteOneAnother() {
        Phase2AbilityTuning tuning = tune(List.of(3, 4, 5, 6, 12, 14, 16, 23));

        assertEquals(60, tuning.integer(Setting.STATUS_DURATION_TICKS, 0));
        assertEquals(20, tuning.integer(Setting.DASH_STATUS_DURATION_TICKS, 0));
        assertEquals(.65, tuning.get(Setting.FLIGHT_DAMAGE_PER_TICK, 0), 1.0E-6);
        assertEquals(.05, tuning.get(Setting.ALTERNATION_BONUS_PER_STACK, 0), 1.0E-6);
        assertEquals(.65, tuning.get(Setting.PIERCE_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.5, tuning.get(Setting.DASH_CONTACT_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.35, tuning.get(Setting.IMPACT_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.7, tuning.get(Setting.ARRIVAL_DAMAGE_MULTIPLIER, 0), 1.0E-6);
    }

    @Test
    void prerequisiteDamageAndSpeedComposeWithCapstones() {
        Phase2AbilityTuning damage = tune(List.of(0, 8));
        Phase2AbilityTuning speed = tune(List.of(1, 7, 17));

        assertEquals(2.475, damage.get(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.1275, speed.get(Setting.PROJECTILE_SPEED, 1.5), 1.0E-4);
    }

    @Test
    void cooldownAdjustmentsComposeInStableNodeOrder() {
        Phase2AbilityTuning frenzied = tune(List.of(7, 15, 16, 25));
        Phase2AbilityTuning silent = tune(List.of(7, 15, 16, 26));

        assertEquals(37, frenzied.integer(Setting.COOLDOWN_TICKS, 20));
        assertEquals(94, silent.integer(Setting.COOLDOWN_TICKS, 20));
    }

    @Test
    void soulTempoUsesDedicatedRuntimeSettings() {
        Phase2AbilityTuning tuning = tune(java.util.stream.IntStream.range(18, 25).boxed().toList());

        assertEquals(170, tuning.integer(Setting.HASTE_DURATION_TICKS, 0));
        assertEquals(.08, tuning.get(Setting.MELEE_BONUS_PER_STACK, 0), 1.0E-6);
        assertEquals(40, tuning.integer(Setting.PROJECTILE_GUARD_TICKS, 0));
        assertEquals(.2, tuning.get(Setting.PROJECTILE_DAMAGE_REDUCTION, 0), 1.0E-6);
        assertEquals(100, tuning.integer(Setting.KILL_HASTE_DURATION_TICKS, 0));
        assertEquals(20, tuning.integer(Setting.KILL_COOLDOWN_REFUND_TICKS, 0));
        assertEquals(60, tuning.integer(Setting.ALTERNATION_WINDOW_TICKS, 0));
        assertEquals(40, tuning.integer(Setting.COOLDOWN_REFUND_CAP_TICKS, 0));
        assertEquals(128 | 256 | 512 | 1024 | 2048,
                tuning.integer(Setting.MODE, 0) & (128 | 256 | 512 | 1024 | 2048));
    }

    private static Phase2AbilityTuning tune(List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("wraithfang");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(Phase2UniqueAbilities.WRAITHFANG_THROW);
        builder.set(Phase2UniqueAbilities.COOLDOWN_TICKS, 20);
        builder.set(Phase2UniqueAbilities.TUNING, Phase2AbilityTuning.EMPTY
                .with(Setting.COOLDOWN_TICKS, 20)
                .with(Setting.PROJECTILE_SPEED, 1.5)
                .with(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1)
                .with(Setting.HASTE_DURATION_TICKS, 80));
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, Phase2UniqueAbilities.WRAITHFANG_THROW, builder, profile.nodes().get(node));
        }
        return builder.get(Phase2UniqueAbilities.TUNING);
    }
}
