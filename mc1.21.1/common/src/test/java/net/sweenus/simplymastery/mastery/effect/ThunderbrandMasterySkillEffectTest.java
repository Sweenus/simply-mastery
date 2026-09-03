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

final class ThunderbrandMasterySkillEffectTest {
    @Test
    void refreshNodeRoutesOnlyToThePassiveDefinition() {
        StormFrostWaterMasteryTuning refresh = tune(StormFrostWaterMasteryAbilities.THUNDERBRAND_REFRESH, List.of(0));
        StormFrostWaterMasteryTuning blitz = tune(StormFrostWaterMasteryAbilities.THUNDERBRAND_BLITZ, List.of(0));
        assertEquals(5, refresh.integer(s("THUNDERBRAND_REFRESH_CHANCE_BONUS"), 0));
        assertFalse(blitz.has(s("THUNDERBRAND_REFRESH_CHANCE_BONUS")));
        assertEquals(1, refresh.integer(s("MODE"), 0));
    }

    @Test
    void everyBlitzNodeWritesAScopedConsumerKeyAndItsModeBit() {
        for (int node = 1; node < 27; node++) {
            StormFrostWaterMasteryTuning tuning = tune(StormFrostWaterMasteryAbilities.THUNDERBRAND_BLITZ, List.of(node));
            assertTrue(java.util.Arrays.stream(StormFrostWaterMasteryTuning.Setting.values())
                    .filter(setting -> setting.name().startsWith("THUNDERBRAND_"))
                    .anyMatch(tuning::has), "node " + node);
            assertEquals(1 << node, tuning.integer(s("MODE"), 0), "node " + node);
        }
    }

    @Test
    void noThunderbrandNodeWritesAFormerlySharedGenericKey() {
        StormFrostWaterMasteryTuning all = tune(StormFrostWaterMasteryAbilities.THUNDERBRAND_BLITZ,
                IntStream.range(1, 27).boxed().toList());
        for (String generic : List.of("DURATION_TICKS", "COUNT", "SPEED", "RADIUS", "TARGET_CAP",
                "LOCKOUT_TICKS", "CHARGE_CAP", "ABSORPTION", "STATUS_DURATION_TICKS", "REFUND_TICKS",
                "DAMAGE_MULTIPLIER", "SECONDARY_DAMAGE_MULTIPLIER", "FINAL_DAMAGE_MULTIPLIER",
                "INCOMING_MULTIPLIER", "OUTGOING_MULTIPLIER", "PER_STACK_MULTIPLIER", "RANGE",
                "PULL_STRENGTH")) assertFalse(all.has(s(generic)), generic);
    }

    @Test
    void cooldownReductionUsesTheConfiguredExecutionCooldown() {
        assertEquals(275, tunedCooldown(300, List.of(1)));
        assertEquals(225, tunedCooldown(250, List.of(1)));
        assertEquals(275, tunedCooldown(300, List.of(1, 2, 3, 4, 14, 24)));
        assertEquals(300, tunedCooldown(300, List.of(2, 3, 4)));
    }

    @Test
    void durationRadiusAndTargetMeaningsStayIndependent() {
        StormFrostWaterMasteryTuning tuning = tune(StormFrostWaterMasteryAbilities.THUNDERBRAND_BLITZ,
                List.of(3, 5, 6, 14, 18, 23, 24, 26));
        assertEquals(3, tuning.integer(s("THUNDERBRAND_DASH_BONUS_TICKS"), 0));
        assertEquals(.5, tuning.get(s("THUNDERBRAND_COLLISION_RADIUS_BONUS"), 0), 1.0E-6);
        assertEquals(3, tuning.get(s("THUNDERBRAND_BLITZ_BURST_RADIUS"), 0), 1.0E-6);
        assertEquals(8, tuning.integer(s("THUNDERBRAND_OVERLOAD_THRESHOLD"), 0));
        assertEquals(1, tuning.integer(s("THUNDERBRAND_CHAIN_EXTRA_TARGETS"), 0));
        assertEquals(4, tuning.integer(s("THUNDERBRAND_ARC_WAKE_TARGET_CAP"), 0));
        assertEquals(12, tuning.integer(s("THUNDERBRAND_FINAL_TARGET_CAP"), 0));
        assertEquals(2, tuning.integer(s("THUNDERBRAND_WEB_CHAIN_COUNT"), 0));
    }

    @Test
    void capstonesCarryTheirExactIndependentTradeoffs() {
        StormFrostWaterMasteryTuning rush = tune(StormFrostWaterMasteryAbilities.THUNDERBRAND_BLITZ, List.of(7));
        assertEquals(8, rush.integer(s("THUNDERBRAND_DASH_DURATION_TICKS"), 0));
        assertEquals(1.5, rush.get(s("THUNDERBRAND_DASH_SPEED_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(0, rush.get(s("THUNDERBRAND_STEERING_MULTIPLIER"), 1), 1.0E-6);

        StormFrostWaterMasteryTuning rolling = tune(StormFrostWaterMasteryAbilities.THUNDERBRAND_BLITZ, List.of(8));
        assertEquals(2, rolling.get(s("THUNDERBRAND_DASH_DURATION_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(.65, rolling.get(s("THUNDERBRAND_COLLISION_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(1.5, rolling.get(s("THUNDERBRAND_STEERING_MULTIPLIER"), 1), 1.0E-6);

        StormFrostWaterMasteryTuning rail = tune(StormFrostWaterMasteryAbilities.THUNDERBRAND_BLITZ, List.of(25));
        assertEquals(6, rail.integer(s("THUNDERBRAND_RAIL_HIT_CAP"), 0));
        assertFalse(rail.has(s("THUNDERBRAND_WEB_CHAIN_COUNT")));
    }

    private static int tunedCooldown(int base, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("thunderbrand");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(StormFrostWaterMasteryAbilities.THUNDERBRAND_BLITZ)
                .set(StormFrostWaterMasteryAbilities.TUNING, StormFrostWaterMasteryTuning.EMPTY)
                .set(StormFrostWaterMasteryAbilities.COOLDOWN_TICKS, base);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, StormFrostWaterMasteryAbilities.THUNDERBRAND_BLITZ,
                builder, profile.nodes().get(node));
        return builder.get(StormFrostWaterMasteryAbilities.COOLDOWN_TICKS);
    }

    private static StormFrostWaterMasteryTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("thunderbrand");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(StormFrostWaterMasteryAbilities.TUNING, StormFrostWaterMasteryTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(StormFrostWaterMasteryAbilities.COOLDOWN_TICKS, 250);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(StormFrostWaterMasteryAbilities.TUNING);
    }

    private static StormFrostWaterMasteryTuning.Setting s(String name) {
        return StormFrostWaterMasteryTuning.Setting.valueOf(name);
    }
}
