package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase8AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase8UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SoulstealerMasterySkillEffectTest {
    @Test
    void nodesRouteToDebtApproachAndReapDefinitions() {
        for (int node = 0; node < 27; node++) {
            Phase8AbilityTuning debt = tune(Phase8UniqueAbilities.SOULSTEALER_DEBT, List.of(node));
            Phase8AbilityTuning approach = tune(Phase8UniqueAbilities.SOULSTEALER_APPROACH, List.of(node));
            Phase8AbilityTuning reap = tune(Phase8UniqueAbilities.SOULSTEALER_REAP, List.of(node));
            assertEquals(node < 9, hasSoulstealerSetting(debt), "debt:" + node);
            assertEquals(node >= 9 && node < 18, hasSoulstealerSetting(approach), "approach:" + node);
            assertEquals(node < 9 || node >= 18, hasSoulstealerSetting(reap), "reap:" + node);
        }
    }

    @Test
    void nodesDoNotWriteGenericNumericSettings() {
        for (UniqueAbilityDefinition definition : List.of(Phase8UniqueAbilities.SOULSTEALER_DEBT,
                Phase8UniqueAbilities.SOULSTEALER_APPROACH, Phase8UniqueAbilities.SOULSTEALER_REAP)) {
            Phase8AbilityTuning tuning = tune(definition, IntStream.range(0, 27).boxed().toList());
            for (String generic : List.of("COOLDOWN_TICKS", "DURATION_TICKS", "INTERVAL_TICKS",
                    "LOCKOUT_TICKS", "REFUND_TICKS", "DELAY_TICKS", "CHANCE", "DAMAGE_MULTIPLIER",
                    "SECONDARY_DAMAGE_MULTIPLIER", "SPELL_MULTIPLIER", "INCOMING_MULTIPLIER",
                    "PER_STACK_MULTIPLIER", "RANGE", "ANGLE", "TARGET_CAP", "SEARCH_CAP", "COUNT",
                    "STACK_CAP", "STATUS_DURATION_TICKS", "STATUS_AMPLIFIER", "ARMOR_IGNORE")) {
                assertFalse(tuning.has(setting(generic)), definition.id() + " leaked " + generic);
            }
        }
    }

    @Test
    void pathBonusesAndCapstonesComposeWithoutColliding() {
        Phase8AbilityTuning debt = tune(Phase8UniqueAbilities.SOULSTEALER_DEBT,
                List.of(0, 1, 2, 5, 6, 8));
        assertEquals(8, debt.integer(setting("SOULSTEALER_CHANCE_BONUS"), 0));
        assertEquals(.5, debt.get(setting("SOULSTEALER_CHANCE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(7, debt.integer(setting("SOULSTEALER_MAX_DEBT_BONUS"), 0));
        assertEquals(1, debt.integer(setting("SOULSTEALER_KILL_DEBT_BONUS"), 0));
        assertEquals(3, debt.integer(setting("SOULSTEALER_KILL_DEBT_OVERRIDE"), 0));
        assertEquals(2, debt.integer(setting("SOULSTEALER_MARK_HITS"), 0));
        assertEquals(120, debt.integer(setting("SOULSTEALER_COLLECTOR_DURATION_CAP_TICKS"), 0));

        Phase8AbilityTuning reap = tune(Phase8UniqueAbilities.SOULSTEALER_REAP,
                List.of(1, 2, 18, 22, 24, 26));
        assertEquals(2, reap.integer(setting("SOULSTEALER_MAX_DEBT_BONUS"), 0));
        assertEquals(1, reap.integer(setting("SOULSTEALER_KILL_DEBT_BONUS"), 0));
        assertEquals(.08, reap.get(setting("SOULSTEALER_DAMAGE_PER_DEBT_BONUS"), 0), 1.0E-6);
        assertEquals(1.25, reap.get(setting("SOULSTEALER_EXACT_PAYMENT_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(2, reap.integer(setting("SOULSTEALER_DEATH_TAX_DEBT"), 0));
        assertEquals(3, reap.integer(setting("SOULSTEALER_INSTALLMENT_MAX_SPEND"), 0));
    }

    private static boolean hasSoulstealerSetting(Phase8AbilityTuning tuning) {
        return Arrays.stream(Phase8AbilityTuning.Setting.values())
                .filter(value -> value.name().startsWith("SOULSTEALER_"))
                .anyMatch(tuning::has);
    }

    private static Phase8AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(Phase8UniqueAbilities.TUNING, Phase8AbilityTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(Phase8UniqueAbilities.COOLDOWN_TICKS, 400);
        MasteryProfile profile = BuiltInFamilyProfiles.profile("soulstealer");
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(Phase8UniqueAbilities.TUNING);
    }

    private static Phase8AbilityTuning.Setting setting(String name) {
        return Phase8AbilityTuning.Setting.valueOf(name);
    }
}
