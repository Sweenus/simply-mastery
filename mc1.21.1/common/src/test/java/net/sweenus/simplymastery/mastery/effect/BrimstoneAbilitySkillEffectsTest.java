package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

final class BrimstoneAbilitySkillEffectsTest {

    @Test
    void everyBrimstoneNodeRoutesToItsAbilityEffect() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("brimstone_claymore");

        assertEquals(27, profile.nodes().size());
        Map<String, String> expected = expectedEffects();
        profile.nodes().forEach(node -> {
            assertInstanceOf(AbilitySkillEffectType.class,
                    SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(Identifier.of(SimplyMastery.MOD_ID, expected.get(node.id())), node.effect().type(), node.id());
        });
    }

    @Test
    void eruptionChanceNodesTuneTheProcRoll() {
        assertEquals(5, parameter("signature_opening", "chance_bonus"));
        assertEquals(5, parameter("signature_reversal", "chance_per_stack"));
        assertEquals(3, parameter("signature_reversal", "max_stacks"));
        assertEquals(80, parameter("signature_reversal", "window_ticks"));
        assertEquals(3, parameter("signature_reserve", "required_stacks"));
    }

    @Test
    void eruptionDamageAndControlNodesMatchTheirDescriptions() {
        assertEquals(100, parameter("signature_cadence", "fire_ticks"));
        assertEquals(115, parameter("signature_cadence", "burning_damage_percent"));
        assertEquals(10, parameter("signature_pressure", "radius_tenths"));
        assertEquals(115, parameter("signature_pressure", "damage_percent"));
        assertEquals(3, parameter("signature_threshold", "count"));
        assertEquals(20, parameter("signature_threshold", "damage_percent"));
        assertEquals(8, parameter("signature_convergence", "pull_tenths"));
        assertEquals(40, parameter("signature_convergence", "slowness_ticks"));
        assertEquals(50, parameter("signature_focus", "damage_percent"));
        assertEquals(8, parameter("signature_focus", "max_detonations"));
        assertEquals(60, parameter("signature_release", "radius_percent"));
        assertEquals(200, parameter("signature_release", "primary_damage_percent"));
    }

    @Test
    void riteNodesMatchTheirDescriptions() {
        assertEquals(40, parameter("combat_opening", "range_tenths"));
        assertEquals(16, parameter("combat_cadence", "interval_ticks"));
        assertEquals(15, parameter("combat_pressure", "growth_hundredths"));
        assertEquals(10, parameter("combat_pressure", "max_radius_tenths"));
        assertEquals(4, parameter("combat_reversal", "pull_tenths"));
        assertEquals(40, parameter("combat_reversal", "slowness_ticks"));
        assertEquals(10, parameter("combat_reserve", "per_pulse_percent"));
        assertEquals(50, parameter("combat_reserve", "cap_percent"));
        assertEquals(35, parameter("combat_threshold", "damage_percent"));
        assertEquals(60, parameter("combat_focus", "duration_ticks"));
        assertEquals(225, parameter("combat_focus", "final_damage_percent"));
    }

    @Test
    void moltenWakeSpacingStaysWithinASinglePulseOfTravel() {
        assertEquals(40, parameter("combat_convergence", "duration_ticks"));
        assertEquals(20, parameter("combat_convergence", "interval_ticks"));
        assertEquals(20, parameter("combat_convergence", "damage_percent"));
        assertEquals(15, parameter("combat_convergence", "min_move_tenths"));
    }

    @Test
    void perpetualFurnaceCapIsReachableWithinItsDuration() {
        int duration = parameter("combat_release", "duration_ticks");
        int interval = parameter("combat_cadence", "interval_ticks");
        int start = parameter("combat_release", "start_percent");
        int perPulse = parameter("combat_release", "per_pulse_percent");
        int cap = parameter("combat_release", "cap_percent");

        assertEquals(200, duration);
        assertEquals(75, start);
        assertEquals(150, cap);
        assertEquals(cap, peakPercent(start, perPulse, cap, duration / 20));
        assertEquals(cap, peakPercent(start, perPulse, cap, duration / interval));
    }

    @Test
    void cinderGuardNodesMatchTheirDescriptions() {
        assertEquals(80, parameter("transformation_opening", "duration_ticks"));
        assertEquals(0, parameter("transformation_opening", "amplifier"));
        assertEquals(18, parameter("transformation_cadence", "padding_ticks"));
        assertEquals(2, parameter("transformation_pressure", "max_stacks"));
        assertEquals(80, parameter("transformation_pressure", "window_ticks"));
        assertEquals(20, parameter("transformation_reversal", "damage_percent"));
        assertEquals(20, parameter("transformation_reversal", "cooldown_ticks"));
        assertEquals(40, parameter("transformation_reserve", "health_percent"));
        assertEquals(60, parameter("transformation_threshold", "duration_ticks"));
        assertEquals(100, parameter("transformation_threshold", "cooldown_ticks"));
        assertEquals(3, parameter("transformation_convergence", "target_threshold"));
        assertEquals(85, parameter("transformation_focus", "radius_percent"));
        assertEquals(35, parameter("transformation_release", "health_percent"));
        assertEquals(175, parameter("transformation_release", "final_damage_percent"));
    }

    @Test
    void capstonesRemainMutuallyExclusive() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("brimstone_claymore");
        for (String branch : new String[]{"signature", "combat", "transformation"}) {
            MasteryProfile.Node focus = node(profile, branch + "_focus");
            MasteryProfile.Node release = node(profile, branch + "_release");
            assertEquals(focus.choiceGroup(), release.choiceGroup(), branch);
            assertEquals("brimstone_claymore_" + branch + "_capstone", focus.choiceGroup(), branch);
        }
    }

    private static int peakPercent(int start, int perPulse, int cap, int pulses) {
        int percent = start;
        for (int pulse = 1; pulse < pulses; pulse++) percent = Math.min(cap, percent + perPulse);
        return percent;
    }

    private static int parameter(String node, String key) {
        return node(BuiltInFamilyProfiles.profile("brimstone_claymore"), node)
                .effect().parameters().get(key);
    }

    private static MasteryProfile.Node node(MasteryProfile profile, String suffix) {
        return profile.node("brimstone_claymore_" + suffix).orElseThrow();
    }

    private static Map<String, String> expectedEffects() {
        Map<String, String> effects = new LinkedHashMap<>();
        effects.put("signature_opening", "sulfurous_edge");
        effects.put("signature_cadence", "scorching_brand");
        effects.put("signature_pressure", "blast_furnace");
        effects.put("signature_reversal", "kindling_blows");
        effects.put("signature_reserve", "flashpoint");
        effects.put("signature_threshold", "cinder_scatter");
        effects.put("signature_convergence", "backdraft");
        effects.put("signature_focus", "chain_reaction");
        effects.put("signature_release", "crucible_strike");
        effects.put("combat_opening", "lengthened_chain");
        effects.put("combat_cadence", "furnace_bellows");
        effects.put("combat_pressure", "stoked_furnace");
        effects.put("combat_reversal", "shackling_heat");
        effects.put("combat_reserve", "overpressure");
        effects.put("combat_threshold", "snapback");
        effects.put("combat_convergence", "molten_wake");
        effects.put("combat_focus", "executioners_drop");
        effects.put("combat_release", "perpetual_furnace");
        effects.put("transformation_opening", "cinder_mantle");
        effects.put("transformation_cadence", "tempered_flesh");
        effects.put("transformation_pressure", "heat_sink");
        effects.put("transformation_reversal", "furnace_reprisal");
        effects.put("transformation_reserve", "forged_resolve");
        effects.put("transformation_threshold", "ashen_step");
        effects.put("transformation_convergence", "bulwark_pulse");
        effects.put("transformation_focus", "walking_furnace");
        effects.put("transformation_release", "last_reprisal");
        Map<String, String> byNodeId = new LinkedHashMap<>();
        effects.forEach((suffix, effect) -> byNodeId.put("brimstone_claymore_" + suffix, effect));
        return byNodeId;
    }
}
