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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GloampiercerMasterySkillEffectTest {

    @Test
    void everyGloampiercerNodeRoutesToThePhase2AbilityEffect() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("gloampiercer");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class,
                    SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(81 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void phantomAmbushNodesDoNotRewriteTheBarrage() {
        List<Integer> nodes = List.of(1, 2, 3, 4);
        Phase2AbilityTuning ambush = tune(Phase2UniqueAbilities.GLOAMPIERCER_AMBUSH, nodes);
        Phase2AbilityTuning barrage = tune(Phase2UniqueAbilities.GLOAMPIERCER_BARRAGE, nodes);

        assertEquals(7, ambush.integer(Setting.FIRE_DELAY_TICKS, 9));
        assertEquals(125, ambush.integer(Setting.CONE_DEGREES, 110));
        assertEquals(14, ambush.integer(Setting.RANGE, 12));
        assertEquals(1.1, ambush.get(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2, ambush.integer(Setting.CLONE_COUNT, 1));
        assertEquals(9, barrage.integer(Setting.FIRE_DELAY_TICKS, 9));
        assertEquals(5, barrage.integer(Setting.CLONE_COUNT, 5));
        assertEquals(1, barrage.get(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void barrageNodesDoNotRewritePhantomAmbush() {
        List<Integer> nodes = List.of(10, 11, 12, 13);
        Phase2AbilityTuning barrage = tune(Phase2UniqueAbilities.GLOAMPIERCER_BARRAGE, nodes);
        Phase2AbilityTuning ambush = tune(Phase2UniqueAbilities.GLOAMPIERCER_AMBUSH, nodes);

        assertEquals(6, barrage.integer(Setting.CLONE_COUNT, 5));
        assertEquals(10, barrage.integer(Setting.FIRE_DELAY_TICKS, 12));
        assertEquals(10, barrage.integer(Setting.THRESHOLD, 12));
        assertEquals(1.7, barrage.get(Setting.PROJECTILE_SPEED, 1.55), 1.0E-6);
        assertEquals(1.08, barrage.get(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1, ambush.integer(Setting.CLONE_COUNT, 1));
        assertEquals(9, ambush.integer(Setting.FIRE_DELAY_TICKS, 9));
        assertEquals(1, ambush.get(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void transformationNodesNoLongerCollideWithPassiveOrBarrageKeys() {
        List<Integer> nodes = List.of(1, 2, 11, 23, 25);
        Phase2AbilityTuning ambush = tune(Phase2UniqueAbilities.GLOAMPIERCER_AMBUSH, nodes);
        Phase2AbilityTuning barrage = tune(Phase2UniqueAbilities.GLOAMPIERCER_BARRAGE, nodes);

        assertEquals(7, ambush.integer(Setting.FIRE_DELAY_TICKS, 9));
        assertEquals(14, ambush.integer(Setting.RANGE, 12));
        assertEquals(10, barrage.integer(Setting.FIRE_DELAY_TICKS, 12));
        assertEquals(5, ambush.integer(Setting.CHAIN_RANGE, 0));
        assertEquals(4, ambush.integer(Setting.CHAIN_DELAY_TICKS, 0));
        assertEquals(6, ambush.integer(Setting.GLOAM_MOVE_RANGE, 0));
        assertEquals(5, barrage.integer(Setting.CHAIN_RANGE, 0));
        assertEquals(6, barrage.integer(Setting.GLOAM_MOVE_RANGE, 0));
    }

    @Test
    void capstonesConfigureOnlyTheirAdvertisedAbility() {
        Phase2AbilityTuning hall = tune(Phase2UniqueAbilities.GLOAMPIERCER_AMBUSH, List.of(7));
        Phase2AbilityTuning royal = tune(Phase2UniqueAbilities.GLOAMPIERCER_BARRAGE, List.of(17));
        Phase2AbilityTuning shortRoyal = tune(Phase2UniqueAbilities.GLOAMPIERCER_BARRAGE, List.of(15, 17));

        assertEquals(3, hall.integer(Setting.CLONE_COUNT, 1));
        assertEquals(.55, hall.get(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(24, hall.integer(Setting.PASSIVE_COOLDOWN_TICKS, 12));
        assertTrue((royal.integer(Setting.MODE, 0) & 32) != 0);
        assertEquals(6, royal.integer(Setting.SPEAR_COUNT, 18));
        assertEquals(2.4, royal.get(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(540, royal.integer(Setting.COOLDOWN_TICKS, 450));
        assertEquals(510, shortRoyal.integer(Setting.COOLDOWN_TICKS, 450));
    }

    @Test
    void stainedGroundNodesConfigureEveryRuntimeConsumer() {
        Phase2AbilityTuning tuning = tune(Phase2UniqueAbilities.GLOAMPIERCER_AMBUSH,
                java.util.stream.IntStream.range(18, 27).boxed().toList());

        assertEquals(1.8, tuning.get(Setting.STAIN_RADIUS, 0), 1.0E-6);
        assertEquals(100, tuning.integer(Setting.STAIN_DURATION_TICKS, 0));
        assertEquals(60, tuning.integer(Setting.STATUS_DURATION_TICKS, 0));
        assertEquals(.15, tuning.get(Setting.BONUS_PER_TRIGGER, 0), 1.0E-6);
        assertEquals(140, tuning.integer(Setting.EMBEDDED_DURATION_TICKS, 0));
        assertEquals(5, tuning.integer(Setting.CHAIN_RANGE, 0));
        assertEquals(4, tuning.integer(Setting.CHAIN_DELAY_TICKS, 0));
        assertEquals(6, tuning.integer(Setting.TARGET_CAP, 0));
        assertEquals(1, tuning.get(Setting.PULL_STRENGTH, 0), 1.0E-6);
        assertEquals(6, tuning.integer(Setting.GLOAM_MOVE_RANGE, 0));
        assertEquals(.15, tuning.get(Setting.MOVEMENT_SPEED, 0), 1.0E-6);
        assertEquals(.75, tuning.get(Setting.DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.8, tuning.get(Setting.IMPACT_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(2.5, tuning.get(Setting.IMPACT_RADIUS, 0), 1.0E-6);
        assertEquals(6, tuning.integer(Setting.IMPACT_TARGET_CAP, 0));
        assertTrue((tuning.integer(Setting.MODE, 0) & (64 | 128 | 256 | 512))
                == (64 | 128 | 256 | 512));
    }

    private static Phase2AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("gloampiercer");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(Phase2UniqueAbilities.COOLDOWN_TICKS, 450);
        }
        builder.set(Phase2UniqueAbilities.TUNING, Phase2AbilityTuning.EMPTY
                .with(Setting.COOLDOWN_TICKS, 450)
                .with(Setting.CHANNEL_DURATION_TICKS, 60)
                .with(Setting.SPEAR_COUNT, 18)
                .with(Setting.CLONE_COUNT,
                        definition == Phase2UniqueAbilities.GLOAMPIERCER_AMBUSH ? 1 : 5)
                .with(Setting.PROJECTILE_SPEED, 1.55)
                .with(Setting.PROJECTILE_DAMAGE_MULTIPLIER, 1));
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(Phase2UniqueAbilities.TUNING);
    }
}
