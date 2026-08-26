package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase2AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase2AbilityTuning.Setting;
import net.sweenus.simplyswords.api.ability.Phase2UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WatcherMasterySkillEffectTest {

    @Test
    void everyWatcherNodeRoutesToThePhase2AbilityEffect() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("watcher_claymore");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class,
                    SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void owningEveryNodeLeavesEachSharedSettingAtItsIntendedValue() {
        Phase2AbilityTuning tuning = tuneAll();

        assertEquals(4.0, tuning.get(Setting.THRESHOLD, 0));
        assertEquals(4.0, tuning.get(Setting.SCAN_RADIUS, 0));
        assertEquals(40.0, tuning.get(Setting.LOCKOUT_TICKS, 0));
        assertEquals(30.0, tuning.get(Setting.REPEAT_WINDOW_TICKS, 0));
        assertEquals(80.0, tuning.get(Setting.REPEAT_LOCKOUT_TICKS, 0));
        assertEquals(4.0, tuning.get(Setting.INCOMING_DREAD_THRESHOLD, 0));
        assertEquals(30.0, tuning.get(Setting.LOW_HEALTH_PERCENT, 0));
        assertEquals(12.0, tuning.get(Setting.TRIGGER_RADIUS, 0));
        assertEquals(200.0, tuning.get(Setting.PASSIVE_COOLDOWN_TICKS, 0));
        assertEquals(6.0, tuning.get(Setting.EXECUTE_DREAD_THRESHOLD, 0));
        assertEquals(12.0, tuning.get(Setting.RANGE, 0));
    }

    @Test
    void guardNodesNoLongerRewriteTheOmenSlownessOrDuration() {
        Phase2AbilityTuning slowness = tune(List.of(12, 20, 21, 25));

        assertEquals(90.0, slowness.get(Setting.STATUS_DURATION_TICKS, 0));
        assertEquals(60.0, slowness.get(Setting.EMBEDDED_DURATION_TICKS, 0));

        Phase2AbilityTuning duration = tune(List.of(9, 24));

        assertEquals(54.0, duration.get(Setting.DURATION_TICKS, 0));
        assertEquals(120.0, duration.get(Setting.CLAIM_DURATION_TICKS, 0));
    }

    @Test
    void gatheringDoomKeepsItsOwnRampWhenClaimedVitalityIsOwned() {
        Phase2AbilityTuning tuning = tune(List.of(13, 24));

        assertEquals(.03, tuning.get(Setting.BONUS_PER_TRIGGER, 0), 1.0E-6);
        assertEquals(.36, tuning.get(Setting.BONUS_CAP, 0), 1.0E-6);
        assertEquals(.05, tuning.get(Setting.CLAIM_BONUS_PER_POINT, 0), 1.0E-6);
        assertEquals(.4, tuning.get(Setting.CLAIM_BONUS_CAP, 0), 1.0E-6);
    }

    @Test
    void capstonesConfigureTheirOwnConsumers() {
        Phase2AbilityTuning manyEyed = tune(List.of(7));
        assertEquals(3.0, manyEyed.get(Setting.SECONDARY_TARGET_CAP, 0));
        assertEquals(12.0, manyEyed.get(Setting.IMPACT_RADIUS, 0));
        assertEquals(.7, manyEyed.get(Setting.SECONDARY_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(225.0, manyEyed.get(Setting.COOLDOWN_TICKS, 0));

        Phase2AbilityTuning endless = tune(List.of(16));
        assertEquals(20.0, endless.get(Setting.SPEAR_COUNT, 0));
        assertEquals(100.0, endless.get(Setting.DURATION_TICKS, 0));

        Phase2AbilityTuning sudden = tune(List.of(17));
        assertEquals(0.0, sudden.get(Setting.SPEAR_COUNT, 5));
        assertEquals(0.0, sudden.get(Setting.STATUS_DURATION_TICKS, 70));

        Phase2AbilityTuning mercy = tune(List.of(25));
        assertNotEquals(0.0, mercy.get(Setting.EXECUTE_THRESHOLD, 0));
        assertEquals(1.5, mercy.get(Setting.ABSORPTION_MULTIPLIER, 1), 1.0E-6);

        Phase2AbilityTuning absolute = tune(List.of(26));
        assertEquals(180.0, absolute.get(Setting.COOLDOWN_TICKS, 0));
        assertTrue((absolute.integer(Setting.MODE, 0) & 16384) != 0);
    }

    private static Phase2AbilityTuning tuneAll() {
        return tune(java.util.stream.IntStream.range(0, 27).boxed().toList());
    }

    private static Phase2AbilityTuning tune(List<Integer> slots) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("watcher_claymore");
        UniqueAbilityDefinition definition = Phase2UniqueAbilities.WATCHER_OMEN;
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        builder.set(Phase2UniqueAbilities.COOLDOWN_TICKS, 180);
        builder.set(Phase2UniqueAbilities.TUNING, Phase2AbilityTuning.EMPTY
                .with(Setting.COOLDOWN_TICKS, 180)
                .with(Setting.DURATION_TICKS, 60)
                .with(Setting.STATUS_DURATION_TICKS, 70));
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int slot : slots) {
            effect.tune(null, definition, builder, profile.nodes().get(slot));
        }
        return builder.get(Phase2UniqueAbilities.TUNING);
    }
}
