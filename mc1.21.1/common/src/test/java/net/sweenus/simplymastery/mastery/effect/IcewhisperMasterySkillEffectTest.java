package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.StormFrostWaterMasteryTuning;
import net.sweenus.simplyswords.api.ability.StormFrostWaterMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import net.sweenus.simplyswords.item.custom.IcewhisperSwordItem;
import net.sweenus.simplyswords.world.IcewhisperCometManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IcewhisperMasterySkillEffectTest {

    @Test
    void auraAndCometNodesRouteOnlyToTheDefinitionTheyDescribe() {
        for (int node = 0; node < 9; node++) {
            assertTrue(tune(StormFrostWaterMasteryAbilities.ICEWHISPER_AURA, List.of(node)).has(s("MODE")), "aura " + node);
            assertFalse(tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(node)).has(s("MODE")), "storm " + node);
        }
        for (int node = 9; node < 18; node++) {
            assertTrue(tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(node)).has(s("MODE")), "storm " + node);
            assertFalse(tune(StormFrostWaterMasteryAbilities.ICEWHISPER_AURA, List.of(node)).has(s("MODE")), "aura " + node);
        }
        // Ice Armor and Veiled Ground are the only transformation nodes Permafrost consumes.
        for (int node = 18; node < 27; node++) {
            boolean aura = node == 20 || node == 23;
            assertEquals(aura, tune(StormFrostWaterMasteryAbilities.ICEWHISPER_AURA, List.of(node)).has(s("MODE")),
                    "aura " + node);
            assertEquals(!aura, tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(node)).has(s("MODE")),
                    "storm " + node);
        }
    }

    @Test
    void everyNodeWritesDedicatedTuningAndItsModeBit() {
        for (int node = 0; node < 27; node++) {
            UniqueAbilityDefinition definition = node < 9 || node == 20 || node == 23
                    ? StormFrostWaterMasteryAbilities.ICEWHISPER_AURA : StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS;
            StormFrostWaterMasteryTuning tuning = tune(definition, List.of(node));
            assertTrue(java.util.Arrays.stream(StormFrostWaterMasteryTuning.Setting.values())
                    .filter(setting -> setting.name().startsWith("ICEWHISPER_"))
                    .anyMatch(tuning::has), "node " + node);
            assertEquals(1 << node, tuning.integer(s("MODE"), 0), "node " + node);
        }
    }

    @Test
    void noIcewhisperNodeWritesSharedGenericKeys() {
        StormFrostWaterMasteryTuning aura = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_AURA,
                IntStream.range(0, 27).boxed().toList());
        StormFrostWaterMasteryTuning storm = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS,
                IntStream.range(0, 27).boxed().toList());
        for (String generic : List.of("COOLDOWN_TICKS", "DURATION_TICKS", "INTERVAL_TICKS", "LOCKOUT_TICKS",
                "DAMAGE_MULTIPLIER", "SECONDARY_DAMAGE_MULTIPLIER", "INCOMING_MULTIPLIER",
                "OUTGOING_MULTIPLIER", "PER_STACK_MULTIPLIER", "RADIUS", "RANGE", "WIDTH", "TARGET_CAP",
                "COUNT", "STACK_CAP", "FREEZE_TICKS", "FREEZE_CAP_TICKS", "STATUS_DURATION_TICKS",
                "STATUS_AMPLIFIER", "ABSORPTION")) {
            assertFalse(aura.has(s(generic)), "aura " + generic);
            assertFalse(storm.has(s(generic)), "storm " + generic);
        }
    }

    @Test
    void signatureDamageNodesLeaveCometDamageAloneAndTheReverse() {
        StormFrostWaterMasteryTuning aura = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_AURA, List.of(0, 7));
        assertEquals(1.1 * 1.8, aura.get(s("ICEWHISPER_AURA_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertFalse(aura.has(s("ICEWHISPER_COMET_DAMAGE_MULTIPLIER")));

        StormFrostWaterMasteryTuning storm = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(11, 17));
        assertEquals(1.12 * 2.2, storm.get(s("ICEWHISPER_COMET_DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        assertFalse(storm.has(s("ICEWHISPER_AURA_DAMAGE_MULTIPLIER")));
    }

    @Test
    void spreadingColdAddsExactlyOneBlockAndDoesNotShrinkTheStorm() {
        StormFrostWaterMasteryTuning aura = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_AURA, List.of(1));
        assertEquals(5, IcewhisperSwordItem.auraRadius(aura, 4), 1.0E-6);

        StormFrostWaterMasteryTuning storm = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(1));
        assertEquals(8, IcewhisperSwordItem.stormRadius(storm, 8), 1.0E-6);
    }

    @Test
    void lastingChillLengthensTheSlowInsteadOfHalvingIt() {
        StormFrostWaterMasteryTuning aura = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_AURA, List.of(2));
        assertEquals(140, IcewhisperSwordItem.BASE_SLOW_TICKS
                + aura.integer(s("ICEWHISPER_AURA_SLOW_BONUS_TICKS"), 0));
    }

    @Test
    void snowblindDoesNotCollapseThePermafrostSlow() {
        StormFrostWaterMasteryTuning storm = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(19));
        assertEquals(30, storm.integer(s("ICEWHISPER_BLIND_TICKS"), 0));

        StormFrostWaterMasteryTuning aura = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_AURA, List.of(19));
        assertFalse(aura.has(s("ICEWHISPER_AURA_SLOW_BONUS_TICKS")));
        assertFalse(aura.has(s("STATUS_DURATION_TICKS")));
    }

    @Test
    void frozenPreyAndLastSnowNoLongerChangeTheCometCount() {
        StormFrostWaterMasteryTuning frozenPrey = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_AURA, List.of(5));
        assertEquals(60, frozenPrey.integer(s("ICEWHISPER_FROZEN_THRESHOLD_TICKS"), 0));

        StormFrostWaterMasteryTuning lastSnow = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(24));
        assertEquals(35, lastSnow.integer(s("ICEWHISPER_LAST_SNOW_HEALTH_PERCENT"), 0));
        assertEquals(2, IcewhisperCometManager.cometCount(lastSnow, 2, 0));
    }

    @Test
    void huntersSkyAndFractureNoLongerRetuneFallTimeOrTargetCaps() {
        StormFrostWaterMasteryTuning hunters = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(14));
        assertEquals(8, hunters.get(s("ICEWHISPER_HUNTER_RANGE"), 0), 1.0E-6);
        assertEquals(20, IcewhisperCometManager.fallTicks(hunters, 20));
        assertFalse(hunters.has(s("ICEWHISPER_SPLASH_TARGET_CAP")));

        StormFrostWaterMasteryTuning fracture = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(15));
        assertEquals(20, IcewhisperCometManager.fallTicks(fracture, 20));
        assertEquals(3, fracture.integer(s("ICEWHISPER_FRACTURE_STACK_CAP"), 0));
    }

    @Test
    void coldSnapAndFrozenRebukeNoLongerChangeCometFallTime() {
        StormFrostWaterMasteryTuning coldSnap = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_AURA, List.of(3));
        assertEquals(60, coldSnap.integer(s("ICEWHISPER_DWELL_TICKS"), 0));

        StormFrostWaterMasteryTuning rebuke = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(22));
        assertEquals(20, IcewhisperCometManager.fallTicks(rebuke, 20));
        assertEquals(20, rebuke.integer(s("ICEWHISPER_REBUKE_FREEZE_TICKS"), 0));
        assertFalse(rebuke.has(s("ICEWHISPER_FREEZE_PER_PULSE_TICKS")));
    }

    @Test
    void frostWardIsBoundedAndBlackIceSuppressesEveryDefensiveGrant() {
        StormFrostWaterMasteryTuning ward = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(18));
        assertEquals(4, ward.integer(s("ICEWHISPER_WARD_ABSORPTION"), 0));
        assertEquals(80, ward.integer(s("ICEWHISPER_WARD_DURATION_TICKS"), 0));

        StormFrostWaterMasteryTuning blackIce = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(18, 24, 26));
        assertEquals(1.6, blackIce.get(s("ICEWHISPER_BLACK_ICE_MULTIPLIER"), 1), 1.0E-6);
        assertTrue((blackIce.integer(s("MODE"), 0) & (1 << 26)) != 0);
    }

    @Test
    void capstoneCadenceAndDurationComposeAgainstTheConfiguration() {
        StormFrostWaterMasteryTuning hailstorm = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(13, 16));
        assertEquals(4, IcewhisperCometManager.cometCount(hailstorm, 2, 0));
        assertEquals(5, IcewhisperCometManager.cometCount(hailstorm, 2, 2));
        assertEquals(240, IcewhisperCometManager.stormDuration(hailstorm, 200));
        assertEquals(1.5, IcewhisperCometManager.splashRadius(hailstorm, 2.5), 1.0E-6);

        StormFrostWaterMasteryTuning extinction = tune(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, List.of(13, 17));
        assertEquals(1, IcewhisperCometManager.cometCount(extinction, 2, 2));
        assertEquals(24, IcewhisperCometManager.waveInterval(extinction, 14));
        assertEquals(4.0, IcewhisperCometManager.splashRadius(extinction, 2.5), 1.0E-6);
    }

    @Test
    void icewhisperLeavesItsExecutionCooldownAlone() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("icewhisper");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning
                .builder(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS)
                .set(StormFrostWaterMasteryAbilities.TUNING, StormFrostWaterMasteryTuning.EMPTY)
                .set(StormFrostWaterMasteryAbilities.COOLDOWN_TICKS, 450);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node = 0; node < 27; node++) {
            effect.tune(null, StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS, builder, profile.nodes().get(node));
        }
        assertEquals(450, builder.get(StormFrostWaterMasteryAbilities.COOLDOWN_TICKS));
    }

    private static StormFrostWaterMasteryTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("icewhisper");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(StormFrostWaterMasteryAbilities.TUNING, StormFrostWaterMasteryTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) builder.set(StormFrostWaterMasteryAbilities.COOLDOWN_TICKS, 450);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(StormFrostWaterMasteryAbilities.TUNING);
    }

    private static StormFrostWaterMasteryTuning.Setting s(String name) {
        return StormFrostWaterMasteryTuning.Setting.valueOf(name);
    }
}
