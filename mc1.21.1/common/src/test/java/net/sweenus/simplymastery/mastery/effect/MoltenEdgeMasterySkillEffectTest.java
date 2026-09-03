package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryTuning;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MoltenEdgeMasterySkillEffectTest {

    @Test
    void everyMoltenEdgeNodeRoutesToTheFireForgeEffect() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("molten_edge");
        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < 27; index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()));
            assertEquals(108 + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void heatVentAndRuptureNodesAreDefinitionScoped() {
        FireForgeMasteryTuning heat = tune(FireForgeMasteryAbilities.MOLTEN_EDGE_HEAT, allNodes());
        FireForgeMasteryTuning vent = tune(FireForgeMasteryAbilities.MOLTEN_EDGE_VENT, allNodes());
        FireForgeMasteryTuning rupture = tune(FireForgeMasteryAbilities.MOLTEN_EDGE_RUPTURE, allNodes());

        assertTrue(heat.has(FireForgeMasteryTuning.Setting.MOLTEN_MELEE_HEAT_GAIN));
        assertFalse(heat.has(FireForgeMasteryTuning.Setting.MOLTEN_VENT_DRAIN));
        assertFalse(heat.has(FireForgeMasteryTuning.Setting.MOLTEN_RUPTURE_LENGTH));
        assertTrue(vent.has(FireForgeMasteryTuning.Setting.MOLTEN_VENT_DRAIN));
        assertFalse(vent.has(FireForgeMasteryTuning.Setting.MOLTEN_MELEE_HEAT_GAIN));
        assertFalse(vent.has(FireForgeMasteryTuning.Setting.MOLTEN_RUPTURE_LENGTH));
        assertTrue(rupture.has(FireForgeMasteryTuning.Setting.MOLTEN_RUPTURE_LENGTH));
        assertFalse(rupture.has(FireForgeMasteryTuning.Setting.MOLTEN_MELEE_HEAT_GAIN));
        assertFalse(rupture.has(FireForgeMasteryTuning.Setting.MOLTEN_VENT_DRAIN));
    }

    @Test
    void sharedGenericKeysCanNoLongerCrossContaminateMoltenNodes() {
        FireForgeMasteryTuning heat = tune(FireForgeMasteryAbilities.MOLTEN_EDGE_HEAT, allNodes());
        assertEquals(6, heat.integer(FireForgeMasteryTuning.Setting.MOLTEN_MELEE_HEAT_GAIN, 0));
        assertEquals(7, heat.integer(FireForgeMasteryTuning.Setting.MOLTEN_INCOMING_HEAT_GAIN, 0));
        assertFalse(heat.has(FireForgeMasteryTuning.Setting.HEAT_GAIN));
        assertFalse(heat.has(FireForgeMasteryTuning.Setting.COUNT));
        assertFalse(heat.has(FireForgeMasteryTuning.Setting.FIRE_TICKS));
    }

    @Test
    void ruptureDamageComposesWithoutChangingTheVentCooldown() {
        FireForgeMasteryTuning rupture = tune(FireForgeMasteryAbilities.MOLTEN_EDGE_RUPTURE, List.of(20, 25));
        assertEquals(1.904, rupture.get(
                FireForgeMasteryTuning.Setting.MOLTEN_RUPTURE_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2, rupture.get(
                FireForgeMasteryTuning.Setting.MOLTEN_RUPTURE_COOLDOWN_MULTIPLIER, 1), 1.0E-6);

        UniqueAbilityTuning.Builder ventBuilder = tuneBuilder(FireForgeMasteryAbilities.MOLTEN_EDGE_VENT, List.of(25));
        assertEquals(40, ventBuilder.get(FireForgeMasteryAbilities.COOLDOWN_TICKS));
        assertTrue(ventBuilder.get(FireForgeMasteryAbilities.TUNING).isEmpty());
    }

    @Test
    void whiteHotAndPersistentScarHaveIndependentFireDurations() {
        FireForgeMasteryTuning heat = tune(FireForgeMasteryAbilities.MOLTEN_EDGE_HEAT, List.of(6, 22));
        FireForgeMasteryTuning rupture = tune(FireForgeMasteryAbilities.MOLTEN_EDGE_RUPTURE, List.of(6, 22));
        assertEquals(60, heat.integer(FireForgeMasteryTuning.Setting.MOLTEN_WHITE_HOT_FIRE_TICKS, 0));
        assertFalse(heat.has(FireForgeMasteryTuning.Setting.MOLTEN_RUPTURE_FIRE_TICKS));
        assertEquals(80, rupture.integer(FireForgeMasteryTuning.Setting.MOLTEN_RUPTURE_FIRE_TICKS, 0));
        assertFalse(rupture.has(FireForgeMasteryTuning.Setting.MOLTEN_WHITE_HOT_FIRE_TICKS));
    }

    private static List<Integer> allNodes() {
        return java.util.stream.IntStream.range(0, 27).boxed().toList();
    }

    private static FireForgeMasteryTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        return tuneBuilder(definition, nodes).get(FireForgeMasteryAbilities.TUNING);
    }

    private static UniqueAbilityTuning.Builder tuneBuilder(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("molten_edge");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(FireForgeMasteryAbilities.TUNING, FireForgeMasteryTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(FireForgeMasteryAbilities.COOLDOWN_TICKS, 40);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder;
    }
}
