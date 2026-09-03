package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.StormFrostWaterMasteryTuning;
import net.sweenus.simplyswords.api.ability.StormFrostWaterMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class StormbringerMasterySkillEffectTest {

    @Test
    void branchesRouteOnlyToTheirOwningDefinitions() {
        StormFrostWaterMasteryTuning guard = tune(StormFrostWaterMasteryAbilities.STORMBRINGER_GUARD, allNodes());
        StormFrostWaterMasteryTuning chain = tune(StormFrostWaterMasteryAbilities.STORMBRINGER_CHAIN, allNodes());

        assertTrue(guard.has(s("STORMBRINGER_PARRY_WINDOW_TICKS")));
        assertTrue(guard.has(s("STORMBRINGER_CHARGE_CAP")));
        assertFalse(guard.has(s("STORMBRINGER_CHAIN_RANGE")));
        assertFalse(guard.has(s("STORMBRINGER_MARK_DURATION_TICKS")));

        assertFalse(chain.has(s("STORMBRINGER_PARRY_WINDOW_TICKS")));
        assertTrue(chain.has(s("STORMBRINGER_CHARGE_CAP")));
        assertTrue(chain.has(s("STORMBRINGER_CHAIN_RANGE")));
        assertTrue(chain.has(s("STORMBRINGER_MARK_DURATION_TICKS")));
    }

    @Test
    void guardValuesComposeWithoutGenericCollisions() {
        StormFrostWaterMasteryTuning tuning = tune(StormFrostWaterMasteryAbilities.STORMBRINGER_GUARD,
                List.of(0, 1, 2, 4, 7, 9, 17));

        assertEquals(8, tuning.integer(s("STORMBRINGER_PARRY_WINDOW_TICKS"), 0));
        assertEquals(7, tuning.integer(s("STORMBRINGER_PARRY_CHARGE_GAIN"), 0));
        assertEquals(0, tuning.integer(s("STORMBRINGER_NORMAL_CHARGE_GAIN"), -1));
        assertEquals(2.24, tuning.get(s("STORMBRINGER_COUNTER_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(6, tuning.integer(s("STORMBRINGER_CHARGE_CAP"), 0));
        assertEquals(.5, tuning.get(s("STORMBRINGER_CHARGE_GAIN_MULTIPLIER"), 1), 1.0E-6);
        assertFalse(tuning.has(s("COUNT")));
        assertFalse(tuning.has(s("DAMAGE_MULTIPLIER")));
        assertFalse(tuning.has(s("INTERVAL_TICKS")));
    }

    @Test
    void fullBatteryAndHotArcHaveIndependentDamageChannels() {
        StormFrostWaterMasteryTuning guard = tune(StormFrostWaterMasteryAbilities.STORMBRINGER_GUARD, List.of(2, 15, 19));
        StormFrostWaterMasteryTuning chain = tune(StormFrostWaterMasteryAbilities.STORMBRINGER_CHAIN, List.of(2, 15, 19));

        assertEquals(1.12, guard.get(s("STORMBRINGER_COUNTER_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertFalse(guard.has(s("STORMBRINGER_FULL_DAMAGE_MULTIPLIER")));
        assertFalse(guard.has(s("STORMBRINGER_CHAIN_DAMAGE_MULTIPLIER")));
        assertEquals(1.25, chain.get(s("STORMBRINGER_FULL_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(1.12, chain.get(s("STORMBRINGER_CHAIN_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertFalse(chain.has(s("STORMBRINGER_COUNTER_DAMAGE_MULTIPLIER")));
    }

    @Test
    void focusedAndRollingPayloadsStayIndependent() {
        StormFrostWaterMasteryTuning focused = tune(StormFrostWaterMasteryAbilities.STORMBRINGER_CHAIN, List.of(25));
        StormFrostWaterMasteryTuning rolling = tune(StormFrostWaterMasteryAbilities.STORMBRINGER_CHAIN, List.of(26));

        assertEquals(3, focused.integer(s("STORMBRINGER_FOCUSED_HIT_COUNT"), 0));
        assertEquals(.55, focused.get(s("STORMBRINGER_FOCUSED_HIT_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertFalse(focused.has(s("STORMBRINGER_ROLLING_TARGET_CAP")));
        assertEquals(10, rolling.integer(s("STORMBRINGER_ROLLING_TARGET_CAP"), 0));
        assertEquals(.82, rolling.get(s("STORMBRINGER_ROLLING_JUMP_MULTIPLIER"), 0), 1.0E-6);
        assertFalse(rolling.has(s("STORMBRINGER_FOCUSED_HIT_COUNT")));
    }

    @Test
    void guardCapstonesOwnTheirExactTradeoffs() {
        StormFrostWaterMasteryTuning razor = tune(StormFrostWaterMasteryAbilities.STORMBRINGER_GUARD, List.of(2, 7));
        StormFrostWaterMasteryTuning bulwark = tune(StormFrostWaterMasteryAbilities.STORMBRINGER_GUARD, List.of(8));

        assertEquals(8, razor.integer(s("STORMBRINGER_PARRY_WINDOW_TICKS"), 0));
        assertEquals(2.24, razor.get(s("STORMBRINGER_COUNTER_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(0, razor.integer(s("STORMBRINGER_NORMAL_CHARGE_GAIN"), -1));
        assertEquals(70, bulwark.integer(s("STORMBRINGER_BLOCK_DURATION_TICKS"), 0));
        assertEquals(3, bulwark.integer(s("STORMBRINGER_NORMAL_CHARGE_GAIN"), 0));
        assertEquals(.5, bulwark.get(s("STORMBRINGER_COUNTER_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
    }

    private static List<Integer> allNodes() {
        return IntStream.range(0, 27).boxed().toList();
    }

    private static StormFrostWaterMasteryTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("stormbringer");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(StormFrostWaterMasteryAbilities.TUNING, StormFrostWaterMasteryTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(StormFrostWaterMasteryAbilities.COOLDOWN_TICKS, 240);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(StormFrostWaterMasteryAbilities.TUNING);
    }

    private static StormFrostWaterMasteryTuning.Setting s(String name) {
        return StormFrostWaterMasteryTuning.Setting.valueOf(name);
    }
}
