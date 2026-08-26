package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class StormAbilitySkillEffectsTest {
    @Test
    void everyStormsEdgeNodeRoutesToAnAbilityEffect() {
        var profile = BuiltInFamilyProfiles.profile("storms_edge");

        assertEquals(27, profile.nodes().size());
        profile.nodes().forEach(node -> assertInstanceOf(AbilitySkillEffectType.class,
                SkillEffectRegistry.get(node.effect().type()), node.id()));
    }

    @Test
    void sprintRefreshUsesThePrePostHitSnapshotForPlayers() {
        assertTrue(StormAbilitySkillEffects.sprintRefreshEligible(true, false, 1));
        assertFalse(StormAbilitySkillEffects.sprintRefreshEligible(true, true, 0));
        assertTrue(StormAbilitySkillEffects.sprintRefreshEligible(false, true, 0));
    }

    @Test
    void pursuitRefreshNeverShortensAndExtensionRemainsCapped() {
        assertEquals(160L, StormMasteryRuntime.refreshedDeadline(160L, 100L, 40));
        assertEquals(140L, StormMasteryRuntime.refreshedDeadline(120L, 100L, 40));
        assertEquals(220L, StormMasteryRuntime.extendedDeadline(200L, 100L, 40, 120));
        assertEquals(180L, StormMasteryRuntime.extendedDeadline(140L, 100L, 40, 120));
    }

    @Test
    void quickeningCountsDamageButNotTheRefreshNotification() {
        assertTrue(StormAbilitySkillEffects.quickeningDamagePath("storms_edge_melee/hit"));
        assertTrue(StormAbilitySkillEffects.quickeningDamagePath("stormbreak/thunderclap_hit"));
        assertFalse(StormAbilitySkillEffects.quickeningDamagePath("storms_edge_refresh/proc"));
    }

    @Test
    void chargedPursuitUsesAVisibleSpeedTier() {
        var node = BuiltInFamilyProfiles.profile("storms_edge")
                .node("storms_edge_combat_cadence").orElseThrow();

        assertEquals(2, node.effect().parameters().get("amplifier"));
        assertEquals(40, node.effect().parameters().get("duration_ticks"));
    }
}
