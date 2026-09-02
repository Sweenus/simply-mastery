package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase6AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase6UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import net.sweenus.simplyswords.world.LivyatanAbilityManager;
import net.sweenus.simplyswords.world.LivyatanWaveManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivyatanMasterySkillEffectTest {
    @Test
    void nodesRouteOnlyToDefinitionsThatConsumeThem() {
        for (int node = 0; node < 27; node++) {
            assertEquals(reachesThrow(node), tune(Phase6UniqueAbilities.LIVYATAN_THROW, List.of(node)).has(s("MODE")),
                    "throw " + node);
            assertEquals(reachesReturn(node), tune(Phase6UniqueAbilities.LIVYATAN_RETURN, List.of(node)).has(s("MODE")),
                    "return " + node);
            assertEquals(reachesWave(node), tune(Phase6UniqueAbilities.LIVYATAN_WAVE, List.of(node)).has(s("MODE")),
                    "wave " + node);
        }
    }

    @Test
    void everyNodeWritesDedicatedTuningAndItsModeBit() {
        for (int node = 0; node < 27; node++) {
            UniqueAbilityDefinition definition = reachesThrow(node) ? Phase6UniqueAbilities.LIVYATAN_THROW
                    : reachesReturn(node) ? Phase6UniqueAbilities.LIVYATAN_RETURN
                    : Phase6UniqueAbilities.LIVYATAN_WAVE;
            Phase6AbilityTuning tuning = tune(definition, List.of(node));
            assertTrue(java.util.Arrays.stream(Phase6AbilityTuning.Setting.values())
                    .filter(setting -> setting.name().startsWith("LIVYATAN_"))
                    .anyMatch(tuning::has), "node " + node);
            assertEquals(1 << node, tuning.integer(s("MODE"), 0), "node " + node);
        }
    }

    @Test
    void noLivyatanNodeWritesSharedGenericKeys() {
        for (UniqueAbilityDefinition definition : List.of(Phase6UniqueAbilities.LIVYATAN_THROW,
                Phase6UniqueAbilities.LIVYATAN_RETURN, Phase6UniqueAbilities.LIVYATAN_WAVE)) {
            Phase6AbilityTuning tuning = tune(definition, IntStream.range(0, 27).boxed().toList());
            for (String generic : List.of("COOLDOWN_TICKS", "DURATION_TICKS", "INTERVAL_TICKS",
                    "LOCKOUT_TICKS", "REFUND_TICKS", "CHANCE", "DAMAGE_MULTIPLIER",
                    "SECONDARY_DAMAGE_MULTIPLIER", "FINAL_DAMAGE_MULTIPLIER", "OUTGOING_MULTIPLIER",
                    "RADIUS", "RANGE", "WIDTH", "LENGTH", "SPEED", "PULL_STRENGTH", "KNOCKBACK",
                    "TARGET_CAP", "COUNT", "STATUS_DURATION_TICKS", "STATUS_AMPLIFIER")) {
                assertFalse(tuning.has(s(generic)), definition.id() + " " + generic);
            }
        }
    }

    @Test
    void signatureGeometryUsesBonusesAndMultipliersAgainstConfiguration() {
        Phase6AbilityTuning tuning = tune(Phase6UniqueAbilities.LIVYATAN_WAVE,
                List.of(0, 1, 2, 3, 4, 5, 7));
        assertEquals(11, LivyatanWaveManager.waveWidth(8, tuning), 1.0E-6);
        assertEquals(12, LivyatanWaveManager.waveLength(10, tuning));
        assertEquals(1.08, LivyatanWaveManager.waveKnockback(.5, tuning), 1.0E-6);
        assertEquals(.66, LivyatanWaveManager.waveDamageMultiplier(tuning), 1.0E-6);
        assertEquals(1.25, tuning.get(s("LIVYATAN_WAVE_FINAL_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(30, tuning.integer(s("LIVYATAN_WAVE_SLOW_TICKS"), 0));
    }

    @Test
    void doubleBreakAndRiptideLanceKeepIndependentChannels() {
        Phase6AbilityTuning doubleBreak = tune(Phase6UniqueAbilities.LIVYATAN_WAVE, List.of(6));
        assertEquals(4, doubleBreak.integer(s("LIVYATAN_DOUBLE_SWING_COUNT"), 0));
        assertEquals(6, doubleBreak.integer(s("LIVYATAN_DOUBLE_DELAY_TICKS"), 0));
        assertEquals(.55, doubleBreak.get(s("LIVYATAN_DOUBLE_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertFalse(doubleBreak.has(s("LIVYATAN_RETURN_LIGHTNING_DAMAGE_MULTIPLIER")));

        Phase6AbilityTuning lance = tune(Phase6UniqueAbilities.LIVYATAN_WAVE, List.of(1, 2, 8));
        assertEquals(2, LivyatanWaveManager.waveWidth(5, lance), 1.0E-6);
        assertEquals(14, LivyatanWaveManager.waveLength(7, lance));
        assertEquals(0, LivyatanWaveManager.waveKnockback(.52, lance), 1.0E-6);
    }

    @Test
    void returnBranchDoesNotRetuneThrowOrWaveDamage() {
        Phase6AbilityTuning charged = tune(Phase6UniqueAbilities.LIVYATAN_RETURN, List.of(13, 16));
        assertEquals(1.15, charged.get(s("LIVYATAN_RETURN_LIGHTNING_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(.35, charged.get(s("LIVYATAN_MAELSTROM_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertFalse(charged.has(s("LIVYATAN_WAVE_DAMAGE_MULTIPLIER")));
        assertFalse(charged.has(s("LIVYATAN_THROW_DAMAGE_MULTIPLIER")));
    }

    @Test
    void thunderheadComposesWithChargedTideAndSuppressesPull() {
        Phase6AbilityTuning tuning = tune(Phase6UniqueAbilities.LIVYATAN_RETURN, List.of(10, 13, 17));
        assertEquals(.92, LivyatanAbilityManager.returnLightningMultiplier(tuning), 1.0E-6);
        assertEquals(100, LivyatanAbilityManager.returnLightningChance(20, tuning));
        assertEquals(0, LivyatanAbilityManager.returnPull(.42, tuning), 1.0E-6);
        assertEquals(8, tuning.integer(s("LIVYATAN_THUNDERHEAD_TARGET_CAP"), 0));
    }

    @Test
    void unboundDoesNotCollapseTheActiveCooldown() {
        UniqueAbilityTuning.Builder builder = builder(Phase6UniqueAbilities.LIVYATAN_THROW, 65);
        tuneInto(builder, Phase6UniqueAbilities.LIVYATAN_THROW, List.of(25));
        assertEquals(65, builder.get(Phase6UniqueAbilities.COOLDOWN_TICKS));

        Phase6AbilityTuning wave = tune(Phase6UniqueAbilities.LIVYATAN_WAVE, List.of(25));
        assertEquals(10, LivyatanWaveManager.swingCooldown(5, wave));
        assertEquals(1.3, LivyatanWaveManager.waveDamageMultiplier(wave), 1.0E-6);
    }

    @Test
    void calmBeforeAddsTwentyTicksAndSuppressesOnlyWaves() {
        UniqueAbilityTuning.Builder builder = builder(Phase6UniqueAbilities.LIVYATAN_THROW, 65);
        tuneInto(builder, Phase6UniqueAbilities.LIVYATAN_THROW, List.of(26));
        assertEquals(85, builder.get(Phase6UniqueAbilities.COOLDOWN_TICKS));
        Phase6AbilityTuning thrown = builder.get(Phase6UniqueAbilities.TUNING);
        assertEquals(1.5, thrown.get(s("LIVYATAN_CALM_DAMAGE_MULTIPLIER"), 1), 1.0E-6);

        Phase6AbilityTuning wave = tune(Phase6UniqueAbilities.LIVYATAN_WAVE, List.of(26));
        assertEquals(1, wave.integer(s("LIVYATAN_SUPPRESS_WAVES"), 0));
        assertFalse(wave.has(s("LIVYATAN_WAVE_DAMAGE_MULTIPLIER")));
    }

    private static boolean reachesThrow(int node) {
        return node == 18 || node == 19 || node == 24 || node == 26;
    }

    private static boolean reachesReturn(int node) {
        return node >= 9 && node <= 17 || node == 20 || node == 23 || node == 24 || node == 26;
    }

    private static boolean reachesWave(int node) {
        return node < 9 || node == 13 || node == 17 || node == 20 || node == 21 || node == 22
                || node == 23 || node == 24 || node == 25 || node == 26;
    }

    private static Phase6AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        UniqueAbilityTuning.Builder builder = builder(definition, 65);
        tuneInto(builder, definition, nodes);
        return builder.get(Phase6UniqueAbilities.TUNING);
    }

    private static UniqueAbilityTuning.Builder builder(UniqueAbilityDefinition definition, int cooldown) {
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(Phase6UniqueAbilities.TUNING, Phase6AbilityTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(Phase6UniqueAbilities.COOLDOWN_TICKS, cooldown);
        return builder;
    }

    private static void tuneInto(UniqueAbilityTuning.Builder builder, UniqueAbilityDefinition definition,
                                 List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("livyatan");
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
    }

    private static Phase6AbilityTuning.Setting s(String name) {
        return Phase6AbilityTuning.Setting.valueOf(name);
    }
}
