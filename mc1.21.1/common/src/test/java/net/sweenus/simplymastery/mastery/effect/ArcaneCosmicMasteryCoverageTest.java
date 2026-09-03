package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.ArcaneCosmicMasteryTuning;
import net.sweenus.simplyswords.api.ability.ArcaneCosmicMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import net.sweenus.simplyswords.world.MagibladeAbilityManager;
import net.sweenus.simplyswords.world.MagispearAbilityManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArcaneCosmicMasteryCoverageTest {

    private static final ArcaneCosmicMasteryTuning REPULSION = MagibladeAbilityManager.repulsionBase(8, 55, 4.0);
    private static final ArcaneCosmicMasteryTuning WARDEN = MagibladeAbilityManager.wardenBase(10, 300, 0.9, 16.0);
    private static final ArcaneCosmicMasteryTuning JUDGMENT = MagibladeAbilityManager.judgmentBase(100, 12, 1.0);
    private static final ArcaneCosmicMasteryTuning SPELLPOINT = MagispearAbilityManager.spellpointBase(35);
    private static final ArcaneCosmicMasteryTuning RAIN = MagispearAbilityManager.rainBase(6, 4.0, 0.35);
    private static final ArcaneCosmicMasteryTuning SLAM = MagispearAbilityManager.slamBase(4.0, 6.0);

    @Test
    void magibladeAndMagispearBranchesReachTheirOwnDefinitions() {
        for (int node = 9; node < 18; node++) {
            assertTrue(owns(one("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_WARDEN, WARDEN, node), node));
            assertFalse(owns(one("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_JUDGMENT, JUDGMENT, node), node));
            assertTrue(owns(one("magispear", ArcaneCosmicMasteryAbilities.MAGISPEAR_RAIN, RAIN, node), node));
            assertFalse(owns(one("magispear", ArcaneCosmicMasteryAbilities.MAGISPEAR_SLAM, SLAM, node), node));
        }
        for (int node = 18; node < 27; node++) {
            assertTrue(owns(one("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_JUDGMENT, JUDGMENT, node), node));
            assertFalse(owns(one("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_WARDEN, WARDEN, node), node));
            assertTrue(owns(one("magispear", ArcaneCosmicMasteryAbilities.MAGISPEAR_SLAM, SLAM, node), node));
            assertFalse(owns(one("magispear", ArcaneCosmicMasteryAbilities.MAGISPEAR_RAIN, RAIN, node), node));
        }
    }

    @Test
    void enigmaBranchesReachTheirOwnDefinitions() {
        for (int node = 0; node < 9; node++)
            assertTrue(owns(one("enigma", ArcaneCosmicMasteryAbilities.ENIGMA_STORMCHASER,
                    ArcaneCosmicMasteryTuning.EMPTY, node), node));
        for (int node = 9; node < 18; node++) {
            assertTrue(owns(one("enigma", ArcaneCosmicMasteryAbilities.ENIGMA_VORTEX,
                    ArcaneCosmicMasteryTuning.EMPTY, node), node));
            assertFalse(owns(one("enigma", ArcaneCosmicMasteryAbilities.ENIGMA_STORMCHASER,
                    ArcaneCosmicMasteryTuning.EMPTY, node), node));
        }
        for (int node = 18; node < 27; node++) {
            assertTrue(owns(one("enigma", ArcaneCosmicMasteryAbilities.ENIGMA_TAILWIND,
                    ArcaneCosmicMasteryTuning.EMPTY, node), node));
            assertFalse(owns(one("enigma", ArcaneCosmicMasteryAbilities.ENIGMA_STORMCHASER,
                    ArcaneCosmicMasteryTuning.EMPTY, node), node),
                    "the wielder-buff branch no longer touches the twister's controls");
        }
    }

    @Test
    void magibladeComposesAgainstItsConfiguredBase() {
        assertEquals(63, tune("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_REPULSION, REPULSION, List.of(0))
                .integer(s("CHANCE"), 0), "Alert Blade raises the configured 55% chance");
        assertEquals(6, tune("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_REPULSION, REPULSION, List.of(1))
                .integer(s("INTERVAL_TICKS"), 0));
        assertEquals(5.0, tune("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_REPULSION, REPULSION, List.of(2))
                .get(s("RADIUS"), 0), 1.0E-6);

        assertEquals(0, tune("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_WARDEN, WARDEN, List.of(9))
                .integer(s("WINDUP_TICKS"), 99) - 0, "Eager Summons cannot make the charge slower");
        assertEquals(350, tune("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_WARDEN, WARDEN, List.of(10))
                .integer(s("DURATION_TICKS"), 0));
        assertEquals(19.0, tune("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_WARDEN, WARDEN, List.of(13))
                .get(s("RANGE"), 0), 1.0E-6);
        assertEquals(92, tune("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_JUDGMENT, JUDGMENT, List.of(20))
                .integer(s("INTERVAL_TICKS"), 0), "Restless Call fires 8 ticks sooner, not 68");
        assertEquals(0.9, tune("magiblade", ArcaneCosmicMasteryAbilities.MAGIBLADE_JUDGMENT, JUDGMENT, List.of(25))
                .get(s("RADIUS"), 0.9), 1.0E-6, "Warden's Roar no longer parks the head five blocks out");
    }

    @Test
    void magispearComposesAgainstItsConfiguredBase() {
        assertEquals(43, tune("magispear", ArcaneCosmicMasteryAbilities.MAGISPEAR_SPELLPOINT, SPELLPOINT, List.of(0))
                .integer(s("CHANCE"), 0), "Keen Focus raises the configured 35% chance");
        assertEquals(4.75, tune("magispear", ArcaneCosmicMasteryAbilities.MAGISPEAR_RAIN, RAIN, List.of(9))
                .get(s("RADIUS"), 0), 1.0E-6);
        assertEquals(.55, tune("magispear", ArcaneCosmicMasteryAbilities.MAGISPEAR_RAIN, RAIN, List.of(13))
                .get(s("PULL_STRENGTH"), 0), 1.0E-6, "Magnetized Field strengthens the configured pull");
        assertEquals(7, tune("magispear", ArcaneCosmicMasteryAbilities.MAGISPEAR_RAIN, RAIN, List.of(14))
                .integer(s("STACK_CAP"), 0), "Arcane Downpour adds a wave instead of removing two");
        assertEquals(12, tune("magispear", ArcaneCosmicMasteryAbilities.MAGISPEAR_RAIN, RAIN, List.of(16))
                .integer(s("STACK_CAP"), 0), "Spear Monsoon actually doubles the configured wave count");

        ArcaneCosmicMasteryTuning guardian = tune("magispear", ArcaneCosmicMasteryAbilities.MAGISPEAR_SLAM, SLAM, List.of(26));
        assertEquals(5.0, guardian.get(s("SECONDARY_RADIUS"), 0), 1.0E-6);
        ArcaneCosmicMasteryTuning rainUnderGuardian = tune("magispear", ArcaneCosmicMasteryAbilities.MAGISPEAR_RAIN,
                RAIN, List.of(26));
        assertEquals(4.0, rainUnderGuardian.get(s("RADIUS"), 0), 1.0E-6,
                "Guardian Descent no longer retunes the rain radius");
        assertEquals(1.0, rainUnderGuardian.get(s("DAMAGE_MULTIPLIER"), 0), 1.0E-6,
                "nor the rain wave damage");
    }

    @Test
    void decayingRelicGroupBanksProgressionAcrossAllThreeForms() {
        MasteryProfile scythe = BuiltInFamilyProfiles.profile("magiscythe");
        MasteryProfile blade = BuiltInFamilyProfiles.profile("magiblade");
        MasteryProfile spear = BuiltInFamilyProfiles.profile("magispear");
        for (MasteryProfile profile : List.of(scythe, blade, spear)) {
            assertTrue(profile.progressionGroup().isPresent(), profile.id().toString());
            assertEquals("simplymastery:decaying_relic", profile.progressionGroupId().toString());
            assertEquals(27, profile.nodes().size());
        }
        assertEquals(scythe.progressionGroupId(), blade.progressionGroupId());
        assertEquals(blade.progressionGroupId(), spear.progressionGroupId());
        assertFalse(scythe.id().equals(blade.id()), "the three forms remain distinct profiles");
    }

    @Test
    void caelestisKeyAllocationHasNoCollisions() {
        ArcaneCosmicMasteryTuning all = tune("caelestis", ArcaneCosmicMasteryAbilities.CAELESTIS_HOST,
                ArcaneCosmicMasteryTuning.EMPTY, java.util.stream.IntStream.range(0, 27).boxed().toList());

        assertEquals(5, all.integer(s("TARGET_CAP"), 0),
                "the bound-creature cap ends on Chosen Horror, untouched by the focus cap");
        assertEquals(8, all.integer(s("SECONDARY_TARGET_CAP"), 0),
                "Directed Hunger's focus cap is its own channel");
        assertEquals(16, all.integer(s("TERTIARY_TARGET_CAP"), 0), "the grasp cap is its own channel");
        assertEquals(2, all.integer(s("STACK_CAP"), 0), "the Dreadglare cap is its own channel");
        assertEquals(100, all.integer(s("TERTIARY_DURATION_TICKS"), 0), "focus duration");
        assertEquals(120, all.integer(s("SECONDARY_STATUS_DURATION_TICKS"), 0), "mark duration");
        assertEquals(20, all.integer(s("TERTIARY_STATUS_DURATION_TICKS"), 0), "ward duration");
        assertEquals(60, all.integer(s("REFUND_TICKS"), 0));
        assertEquals(120, all.integer(s("SECONDARY_REFUND_TICKS"), 0));
        assertEquals(180, all.integer(s("TERTIARY_REFUND_TICKS"), 0));
        assertEquals(20, all.integer(s("RANGE"), 0), "Forced Recall range");
        assertEquals(50, all.integer(s("PITY_CHANCE"), 0), "Forced Recall chance");
        assertEquals(8.0, all.get(s("SECONDARY_RADIUS"), 0), 1.0E-6, "ward radius");
    }

    @Test
    void caelestisComposesAgainstItsConfiguredBase() {
        assertEquals(120, one("caelestis", ArcaneCosmicMasteryAbilities.CAELESTIS_HOST,
                ArcaneCosmicMasteryTuning.EMPTY, 0).integer(s("WINDUP_TICKS"), 0),
                "Eager Rift shortens the configured 140-tick expansion by one second");
        assertEquals(26, one("caelestis", ArcaneCosmicMasteryAbilities.CAELESTIS_HOST,
                ArcaneCosmicMasteryTuning.EMPTY, 1).integer(s("INTERVAL_TICKS"), 0),
                "Teeming Rift composes against the configured 30-tick spawn interval");
        assertEquals(14, one("caelestis", ArcaneCosmicMasteryAbilities.CAELESTIS_HOST,
                ArcaneCosmicMasteryTuning.EMPTY, 2).integer(s("TARGET_CAP"), 0),
                "Thronging Muster adds two to the configured cap of 12");
        assertEquals(22.0, one("caelestis", ArcaneCosmicMasteryAbilities.CAELESTIS_HOST,
                ArcaneCosmicMasteryTuning.EMPTY, 9).get(s("RADIUS"), 0), 1.0E-6,
                "Gaping Tear widens the configured 20-block radius");
        assertEquals(980, one("caelestis", ArcaneCosmicMasteryAbilities.CAELESTIS_HOST,
                ArcaneCosmicMasteryTuning.EMPTY, 10).integer(s("DURATION_TICKS"), 0),
                "Enduring Rift lengthens the configured 900-tick breach");
        assertEquals(14, one("caelestis", ArcaneCosmicMasteryAbilities.CAELESTIS_HOST,
                ArcaneCosmicMasteryTuning.EMPTY, 11).integer(s("SECONDARY_COUNT"), 0),
                "Grasping Fringe adds two to the configured 12 tentacles");
        assertEquals(25, one("caelestis", ArcaneCosmicMasteryAbilities.CAELESTIS_HOST,
                ArcaneCosmicMasteryTuning.EMPTY, 12).integer(s("SECONDARY_INTERVAL_TICKS"), 0));
        assertEquals(40, one("caelestis", ArcaneCosmicMasteryAbilities.CAELESTIS_HOST,
                ArcaneCosmicMasteryTuning.EMPTY, 13).integer(s("STATUS_DURATION_TICKS"), 0),
                "Crushing Limbs doubles the configured 20-tick tentacle slow");
        assertEquals(780, one("caelestis", ArcaneCosmicMasteryAbilities.CAELESTIS_HOST,
                ArcaneCosmicMasteryTuning.EMPTY, 17).integer(s("DURATION_TICKS"), 0),
                "Stable Gate shortens the configured breach rather than a literal 300");
        assertEquals(4, one("caelestis", ArcaneCosmicMasteryAbilities.CAELESTIS_HOST,
                ArcaneCosmicMasteryTuning.EMPTY, 18).integer(s("CHANCE"), 0),
                "Firm Binding reduces the configured betrayal chance of 5");
    }

    private static boolean owns(ArcaneCosmicMasteryTuning tuning, int node) {
        return (tuning.integer(s("MODE"), 0) & 1 << node % 27) != 0;
    }

    private static ArcaneCosmicMasteryTuning one(String profile, UniqueAbilityDefinition definition,
                                           ArcaneCosmicMasteryTuning base, int node) {
        return tune(profile, definition, base, List.of(node));
    }

    private static ArcaneCosmicMasteryTuning tune(String profile, UniqueAbilityDefinition definition,
                                            ArcaneCosmicMasteryTuning base, List<Integer> nodes) {
        MasteryProfile masteryProfile = BuiltInFamilyProfiles.profile(profile);
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(ArcaneCosmicMasteryAbilities.TUNING, base);
        if (definition.cooldownKey().isPresent()) builder.set(ArcaneCosmicMasteryAbilities.COOLDOWN_TICKS, 120);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                masteryProfile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, masteryProfile.nodes().get(node));
        return builder.get(ArcaneCosmicMasteryAbilities.TUNING);
    }

    private static ArcaneCosmicMasteryTuning.Setting s(String name) {
        return ArcaneCosmicMasteryTuning.Setting.valueOf(name);
    }
}
