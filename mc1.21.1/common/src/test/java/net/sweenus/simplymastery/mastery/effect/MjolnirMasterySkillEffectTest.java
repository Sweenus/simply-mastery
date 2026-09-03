package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.StormFrostWaterMasteryTuning;
import net.sweenus.simplyswords.api.ability.StormFrostWaterMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import net.sweenus.simplyswords.world.MjolnirStormManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MjolnirMasterySkillEffectTest {

    private static final int CONFIG_DURATION = 200;
    private static final int CONFIG_RADIUS = 10;
    private static final int CONFIG_CONDUCTIVE = 120;
    private static final int CONFIG_FINAL_BOLTS = 3;
    private static final double CONFIG_FINAL_RADIUS = 6.0;

    @Test
    void noNodeWritesAGenericKeyAnyMore() {
        StormFrostWaterMasteryTuning all = tune(allNodes());
        for (String generic : List.of("DURATION_TICKS", "INTERVAL_TICKS", "RADIUS", "RANGE", "COUNT",
                "TARGET_CAP", "LOCKOUT_TICKS", "REFUND_TICKS", "STATUS_DURATION_TICKS", "STATUS_AMPLIFIER",
                "STACK_CAP", "PER_STACK_MULTIPLIER", "DAMAGE_MULTIPLIER", "SECONDARY_DAMAGE_MULTIPLIER",
                "FINAL_DAMAGE_MULTIPLIER", "INCOMING_MULTIPLIER", "OUTGOING_MULTIPLIER", "KNOCKBACK",
                "PULL_STRENGTH", "ABSORPTION")) {
            assertFalse(all.has(s(generic)), generic + " is still written by a Mjolnir node");
        }
    }

    @Test
    void everyNodeWritesAtLeastOneKeyItsConsumerReads() {
        for (int node = 0; node < 27; node++) {
            StormFrostWaterMasteryTuning tuning = tune(List.of(node));
            long written = java.util.Arrays.stream(StormFrostWaterMasteryTuning.Setting.values())
                    .filter(setting -> setting.name().startsWith("MJOLNIR_"))
                    .filter(tuning::has)
                    .count();
            assertTrue(written > 0, "node " + node + " writes no scoped Mjolnir key");
        }
    }

    @Test
    void everyOwnedNodeSetsItsOwnModeBit() {
        for (int node = 0; node < 27; node++) {
            int bit = 1 << node;
            assertEquals(bit, tune(List.of(node)).integer(s("MODE"), 0), "node " + node);
        }
    }

    @Test
    void swellingFrontNoLongerCancelsItselfOutAgainstTheConfiguredRadius() {
        StormFrostWaterMasteryTuning tuning = tune(List.of(2));
        assertEquals(2, tuning.get(s("MJOLNIR_STORM_RADIUS_BONUS"), 0), 1.0E-6);
        assertEquals(12, MjolnirStormManager.stormRadius(tuning, CONFIG_RADIUS), 1.0E-6);
        assertEquals(20, tuning.integer(s("MJOLNIR_STORM_TARGET_CAP"), 0));
        assertEquals(CONFIG_FINAL_RADIUS, MjolnirStormManager.finalRadius(tuning, CONFIG_FINAL_RADIUS), 1.0E-6);
    }

    @Test
    void hammerfallThunderWakeAndStormAnchorNoLongerCrippleTheStormRadius() {
        StormFrostWaterMasteryTuning tuning = tune(List.of(10, 13, 17));
        assertEquals(CONFIG_RADIUS, MjolnirStormManager.stormRadius(tuning, CONFIG_RADIUS), 1.0E-6);
        assertEquals(2.5, tuning.get(s("MJOLNIR_ENTRY_RADIUS"), 0), 1.0E-6);
        assertEquals(2, tuning.get(s("MJOLNIR_WAKE_RADIUS"), 0), 1.0E-6);
        assertEquals(3, tuning.get(s("MJOLNIR_ANCHOR_RADIUS"), 0), 1.0E-6);
    }

    @Test
    void forkedBoltAndLightningRecallNoLongerRetuneTheFinalSequence() {
        StormFrostWaterMasteryTuning tuning = tune(List.of(5, 12));
        assertEquals(CONFIG_FINAL_BOLTS, MjolnirStormManager.finalBoltCount(tuning, CONFIG_FINAL_BOLTS));
        assertEquals(4, tuning.integer(s("MJOLNIR_FORK_COUNT"), 0));
        assertEquals(60, tuning.integer(s("MJOLNIR_RECALL_ARM_TICKS"), 0));
        assertEquals(4, MjolnirStormManager.finalBoltCount(tune(List.of(23)), CONFIG_FINAL_BOLTS));
    }

    @Test
    void conductiveDurationIsUntouchedByEveryOtherStatusNode() {
        StormFrostWaterMasteryTuning others = tune(List.of(9, 14, 15, 16, 17, 18, 25));
        assertEquals(CONFIG_CONDUCTIVE, MjolnirStormManager.conductiveDuration(others, CONFIG_CONDUCTIVE));
        assertEquals(160, MjolnirStormManager.conductiveDuration(tune(List.of(4)), CONFIG_CONDUCTIVE));
    }

    @Test
    void skybreakerRaisesOnlyTheClapAndComposesWithWrathOfThunder() {
        StormFrostWaterMasteryTuning skybreaker = tune(List.of(24));
        assertEquals(7.5, MjolnirStormManager.finalRadius(skybreaker, CONFIG_FINAL_RADIUS), 1.0E-6);
        assertEquals(CONFIG_RADIUS, MjolnirStormManager.stormRadius(skybreaker, CONFIG_RADIUS), 1.0E-6);
        assertEquals(24, skybreaker.integer(s("MJOLNIR_FINAL_TARGET_CAP"), 0));

        StormFrostWaterMasteryTuning both = tune(List.of(24, 26));
        assertEquals(2.5, both.get(s("MJOLNIR_FINAL_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(1.5, both.get(s("MJOLNIR_FINAL_KNOCKBACK_MULTIPLIER"), 1), 1.0E-6);
        assertFalse(MjolnirStormManager.grantsDefensiveBuffs(both));
    }

    @Test
    void endlessSquallSuppressesTheClapWithoutZeroingAnotherNodesMultiplier() {
        StormFrostWaterMasteryTuning squall = tune(List.of(7, 24));
        assertTrue(MjolnirStormManager.suppressesFinalClap(squall));
        assertEquals(1.25, squall.get(s("MJOLNIR_FINAL_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(400, MjolnirStormManager.resolveDuration(squall, CONFIG_DURATION));
        assertEquals(.8, squall.get(s("MJOLNIR_BOLT_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
    }

    @Test
    void suddenTempestOwnsAnExactFiveSecondStormAndAQuarterRadiusCut() {
        StormFrostWaterMasteryTuning tempest = tune(List.of(0, 8));
        assertEquals(100, MjolnirStormManager.resolveDuration(tempest, CONFIG_DURATION));
        assertEquals(6, tempest.integer(s("MJOLNIR_PULSE_INTERVAL_TICKS"), 0));
        assertEquals(1.35, tempest.get(s("MJOLNIR_BOLT_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(7.5, MjolnirStormManager.stormRadius(tempest, CONFIG_RADIUS), 1.0E-6);
    }

    @Test
    void chargedFinaleOwnsThirtyPercentAtSixConductiveTargets() {
        StormFrostWaterMasteryTuning finale = tune(List.of(6));
        assertEquals(130, MjolnirStormManager.finaleDamage(finale, 100, 6), 1.0E-4);
        assertEquals(130, MjolnirStormManager.finaleDamage(finale, 100, 12), 1.0E-4);
    }

    @Test
    void amplifiersMatchTheirZeroBasedDescriptions() {
        assertEquals(0, tune(List.of(9)).integer(s("MJOLNIR_SPEED_AMPLIFIER"), -1));
        assertEquals(1, tune(List.of(14)).integer(s("MJOLNIR_SKYBOUND_AMPLIFIER"), -1));
        assertEquals(1, tune(List.of(15)).integer(s("MJOLNIR_FLASH_SPEED_AMPLIFIER"), -1));
        assertEquals(1, tune(List.of(22)).integer(s("MJOLNIR_SHELTER_AMPLIFIER"), -1));
        assertEquals(1, tune(List.of(25)).integer(s("MJOLNIR_AEGIS_AMPLIFIER"), -1));
    }

    @Test
    void absorptionGrantsCarryTheDurationsTheyAdvertise() {
        StormFrostWaterMasteryTuning shell = tune(List.of(18));
        assertEquals(4, shell.get(s("MJOLNIR_SHELL_ABSORPTION"), 0), 1.0E-6);
        assertEquals(80, shell.integer(s("MJOLNIR_SHELL_DURATION_TICKS"), 0));

        StormFrostWaterMasteryTuning aegis = tune(List.of(25));
        assertEquals(8, aegis.get(s("MJOLNIR_AEGIS_ABSORPTION"), 0), 1.0E-6);
        assertEquals(120, aegis.integer(s("MJOLNIR_AEGIS_DURATION_TICKS"), 0));
    }

    @Test
    void updraftReachesTheConductiveBurstRatherThanTheFinalClap() {
        StormFrostWaterMasteryTuning updraft = tune(List.of(21));
        assertEquals(1.15, updraft.get(s("MJOLNIR_BURST_KNOCKBACK_MULTIPLIER"), 1), 1.0E-6);
        assertEquals(1.25, updraft.get(s("MJOLNIR_BURST_KNOCKUP_MULTIPLIER"), 1), 1.0E-6);
        assertFalse(updraft.has(s("MJOLNIR_FINAL_KNOCKBACK_MULTIPLIER")));
    }

    private static List<Integer> allNodes() {
        return IntStream.range(0, 27).boxed().toList();
    }

    private static StormFrostWaterMasteryTuning tune(List<Integer> nodes) {
        UniqueAbilityDefinition definition = StormFrostWaterMasteryAbilities.MJOLNIR_STORM;
        MasteryProfile profile = BuiltInFamilyProfiles.profile("mjolnir");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(StormFrostWaterMasteryAbilities.TUNING, StormFrostWaterMasteryTuning.EMPTY)
                .set(StormFrostWaterMasteryAbilities.COOLDOWN_TICKS, 700);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(StormFrostWaterMasteryAbilities.TUNING);
    }

    private static StormFrostWaterMasteryTuning.Setting s(String name) {
        return StormFrostWaterMasteryTuning.Setting.valueOf(name);
    }
}
