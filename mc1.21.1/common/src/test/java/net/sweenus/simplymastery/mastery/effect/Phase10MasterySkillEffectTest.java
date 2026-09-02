package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase10AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase10UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import net.sweenus.simplyswords.world.DawnquiverAbilityManager;
import net.sweenus.simplyswords.world.Phase10WeaponManager;
import net.sweenus.simplyswords.world.RiftmaneAbilityManager;
import net.sweenus.simplyswords.world.WatcherAbilityManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Phase10MasterySkillEffectTest {

    private static final Phase10AbilityTuning MARK =
            WatcherAbilityManager.warglaiveMarkBase(200, 5, 5, 12.0);
    private static final Phase10AbilityTuning HUNT =
            WatcherAbilityManager.warglaiveHuntBase(10.0, 5);
    private static final Phase10AbilityTuning SANGUINE =
            WatcherAbilityManager.warglaiveSanguineBase(0.40);
    private static final Phase10AbilityTuning CHARGER =
            RiftmaneAbilityManager.chargerBase(20.0, 1.2, 1.1, 1.0, 16);
    private static final Phase10AbilityTuning HARRIER =
            RiftmaneAbilityManager.harrierBase(CHARGER, 70, 60, 16.0, 4.0, 110.0);
    private static final Phase10AbilityTuning RANK =
            RiftmaneAbilityManager.rankBase(CHARGER, 5, 7.0);
    private static final Phase10AbilityTuning RIDER =
            RiftmaneAbilityManager.riderBase(CHARGER, 2.0);

    @Test
    void everyWarglaiveBranchReachesItsOwnDefinition() {
        for (int node = 0; node < 9; node++) {
            assertTrue(owns(warglaive("watching_warglaive", Phase10UniqueAbilities.WARG_MARK, MARK, node), node));
            assertFalse(owns(warglaive("watching_warglaive", Phase10UniqueAbilities.WARG_HUNT, HUNT, node), node));
            assertFalse(owns(warglaive("watching_warglaive", Phase10UniqueAbilities.WARG_SANGUINE, SANGUINE, node), node));
        }
        for (int node = 9; node < 18; node++) {
            assertTrue(owns(warglaive("watching_warglaive", Phase10UniqueAbilities.WARG_HUNT, HUNT, node), node));
            assertFalse(owns(warglaive("watching_warglaive", Phase10UniqueAbilities.WARG_SANGUINE, SANGUINE, node), node));
            assertEquals(node == 11,
                    owns(warglaive("watching_warglaive", Phase10UniqueAbilities.WARG_MARK, MARK, node), node),
                    "only Long Gaze also tunes the mark definition");
        }
        for (int node = 18; node < 27; node++) {
            assertTrue(owns(warglaive("watching_warglaive", Phase10UniqueAbilities.WARG_SANGUINE, SANGUINE, node), node));
            assertFalse(owns(warglaive("watching_warglaive", Phase10UniqueAbilities.WARG_HUNT, HUNT, node), node));
            assertFalse(owns(warglaive("watching_warglaive", Phase10UniqueAbilities.WARG_MARK, MARK, node), node));
        }
    }

    @Test
    void everyRiftmaneBranchReachesItsOwnDefinition() {
        for (int node = 0; node < 9; node++) {
            assertTrue(owns(warglaive("riftmane", Phase10UniqueAbilities.RIFTMANE_HARRIER, HARRIER, node), node));
            assertFalse(owns(warglaive("riftmane", Phase10UniqueAbilities.RIFTMANE_RANK, RANK, node), node));
            assertFalse(owns(warglaive("riftmane", Phase10UniqueAbilities.RIFTMANE_RIDER, RIDER, node), node));
        }
        for (int node = 9; node < 18; node++) {
            assertTrue(owns(warglaive("riftmane", Phase10UniqueAbilities.RIFTMANE_RANK, RANK, node), node));
            assertFalse(owns(warglaive("riftmane", Phase10UniqueAbilities.RIFTMANE_RIDER, RIDER, node), node));
        }
        for (int node = 18; node < 27; node++) {
            assertTrue(owns(warglaive("riftmane", Phase10UniqueAbilities.RIFTMANE_RIDER, RIDER, node), node));
            assertFalse(owns(warglaive("riftmane", Phase10UniqueAbilities.RIFTMANE_RANK, RANK, node), node));
        }
    }

    @Test
    void relativeWarglaiveNodesComposeAgainstTheConfiguredBase() {
        assertEquals(240, tune("watching_warglaive", Phase10UniqueAbilities.WARG_MARK, MARK, List.of(0))
                .integer(s("DURATION_TICKS"), 0), "Lingering Gaze adds two seconds");
        assertEquals(6, tune("watching_warglaive", Phase10UniqueAbilities.WARG_MARK, MARK, List.of(1))
                .integer(s("STACK_CAP"), 0), "Deeper Dread adds one stack");
        assertEquals(11.5, tune("watching_warglaive", Phase10UniqueAbilities.WARG_HUNT, HUNT, List.of(10))
                .get(s("RADIUS"), 0), 1.0E-6, "Broad Hunt widens rather than shrinks the hunt");
        assertEquals(14.0, tune("watching_warglaive", Phase10UniqueAbilities.WARG_MARK, MARK, List.of(11))
                .get(s("RANGE"), 0), 1.0E-6, "Long Gaze extends the activation range");
        assertEquals(0.45, tune("watching_warglaive", Phase10UniqueAbilities.WARG_SANGUINE, SANGUINE, List.of(20))
                .get(s("HEALTH_THRESHOLD"), 0), 1.0E-6, "Deep Draught adds five points of heal cap");
    }

    @Test
    void warglaiveNodesNoLongerOverwriteEachOther() {
        Phase10AbilityTuning hunt = tune("watching_warglaive", Phase10UniqueAbilities.WARG_HUNT, HUNT,
                List.of(12, 13, 14, 15));
        assertEquals(20, hunt.integer(s("STATUS_DURATION_TICKS"), 0), "Wing Buffet keeps its own duration");
        assertEquals(0.05, hunt.get(s("PER_STACK_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(5, hunt.integer(s("STACK_CAP"), 0), "Murder Flight keeps its cap");
        assertEquals(0.6, hunt.get(s("SECONDARY_DAMAGE_MULTIPLIER"), 0), 1.0E-6, "Second Pass owns this key");
        assertEquals(0.4, hunt.get(s("FINAL_DAMAGE_MULTIPLIER"), 0), 1.0E-6, "Returning Shadow owns this key");

        Phase10AbilityTuning sanguine = tune("watching_warglaive", Phase10UniqueAbilities.WARG_SANGUINE, SANGUINE,
                List.of(21, 23, 24, 26));
        assertEquals(0.04, sanguine.get(s("PER_STACK_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(7, sanguine.integer(s("STACK_CAP"), 0), "Killing Gaze no longer destroys the cap");
        assertEquals(50, sanguine.integer(s("STATUS_DURATION_TICKS"), 0), "Hollow Watcher keeps its Wither duration");
        assertEquals(60, sanguine.integer(s("SECONDARY_DURATION_TICKS"), 0), "Hunter's Reprieve owns its own key");
        assertFalse(sanguine.has(s("TARGET_CAP")), "no transformation node caps the hunt");

        Phase10AbilityTuning falcon = tune("watching_warglaive", Phase10UniqueAbilities.WARG_HUNT, HUNT,
                List.of(17));
        assertEquals(8, falcon.integer(s("TARGET_CAP"), 0));
    }

    @Test
    void relativeRiftmaneNodesComposeAgainstTheConfiguredBase() {
        assertEquals(78, tune("riftmane", Phase10UniqueAbilities.RIFTMANE_HARRIER, HARRIER, List.of(0))
                .integer(s("CHANCE"), 0), "Thin Veil raises rather than collapses the proc chance");
        assertEquals(18.0, tune("riftmane", Phase10UniqueAbilities.RIFTMANE_HARRIER, HARRIER, List.of(2))
                .get(s("SEARCH_RANGE"), 0), 1.0E-6);
        assertEquals(20.0, tune("riftmane", Phase10UniqueAbilities.RIFTMANE_HARRIER, HARRIER, List.of(2))
                .get(s("RANGE"), 0), 1.0E-6, "the search range no longer collides with charge distance");
        assertEquals(125.0, tune("riftmane", Phase10UniqueAbilities.RIFTMANE_HARRIER, HARRIER, List.of(4))
                .get(s("ANGLE"), 0), 1.0E-6, "Wide Sight widens the cone");
        assertEquals(58, tune("riftmane", Phase10UniqueAbilities.RIFTMANE_HARRIER, HARRIER, List.of(0, 7))
                .integer(s("CHANCE"), 0), "Haunted Volley stays reachable");
        assertEquals(8.0, tune("riftmane", Phase10UniqueAbilities.RIFTMANE_RANK, RANK, List.of(10))
                .get(s("WIDTH"), 0), 1.0E-6, "Broader Front actually widens the rank");
        assertEquals(23.0, tune("riftmane", Phase10UniqueAbilities.RIFTMANE_RANK, RANK, List.of(11))
                .get(s("RANGE"), 0), 1.0E-6);
        assertEquals(1.45, tune("riftmane", Phase10UniqueAbilities.RIFTMANE_RANK, RANK, List.of(14))
                .get(s("KNOCKBACK"), 0), 1.0E-6, "Scattering Line keeps awakening-scaled knockback");
        assertEquals(2.35, tune("riftmane", Phase10UniqueAbilities.RIFTMANE_RIDER, RIDER, List.of(19))
                .get(s("SECONDARY_RADIUS"), 0), 1.0E-6, "Far Ride lengthens the ride");
        assertEquals(12, tune("riftmane", Phase10UniqueAbilities.RIFTMANE_RIDER, RIDER, List.of(18))
                .integer(s("WINDUP_TICKS"), 0), "Quick Mount removes exactly 0.2 seconds");
    }

    @Test
    void ghostRoadAndCataphractOnlyRetuneTheMount() {
        Phase10AbilityTuning rider = tune("riftmane", Phase10UniqueAbilities.RIFTMANE_RIDER, RIDER, List.of(25));
        assertEquals(60.0, rider.get(s("RANGE"), 0), 1.0E-6, "Ghost Road triples the configured distance");
        assertEquals(0.0, rider.get(s("DAMAGE_MULTIPLIER"), 1), 1.0E-6);
        Phase10AbilityTuning rank = tune("riftmane", Phase10UniqueAbilities.RIFTMANE_RANK, RANK, List.of(25));
        assertEquals(20.0, rank.get(s("RANGE"), 0), 1.0E-6, "the rank keeps its distance");
        assertEquals(1.0, rank.get(s("DAMAGE_MULTIPLIER"), 0), 1.0E-6, "the rank keeps its damage");

        Phase10AbilityTuning cataphract = tune("riftmane", Phase10UniqueAbilities.RIFTMANE_RIDER, RIDER, List.of(26));
        assertEquals(2.4, cataphract.get(s("KNOCKBACK"), 0), 1.0E-6, "double the configured knockback");
        assertEquals(0.75, cataphract.get(s("SPEED"), 0), 1.0E-6);
        assertEquals(1.0, tune("riftmane", Phase10UniqueAbilities.RIFTMANE_RANK, RANK, List.of(26))
                .get(s("SPEED"), 0), 1.0E-6, "the rank is not slowed");
    }

    @Test
    void ribbonNodesWriteKeysTheirConsumersRead() {
        Phase10AbilityTuning heavy = tune("ribboncleaver", Phase10UniqueAbilities.RIBBON_HEAVY,
                Phase10WeaponManager.ribbonHeavyBase(), List.of(0, 4, 5, 6));
        assertEquals(0.97, heavy.get(s("SPEED"), 0), 1.0E-6, "Unyielding no longer clobbers Balanced Grip");
        assertEquals(1.0, heavy.get(s("PER_STACK_MULTIPLIER"), 0), 1.0E-6, "Unyielding owns its own relief key");
        assertEquals(1.1, heavy.get(s("OUTGOING_MULTIPLIER"), 0), 1.0E-6, "Heavy Retort keeps its own key");
        assertEquals(60, heavy.integer(s("SECONDARY_DURATION_TICKS"), 0), "Bound Wounds owns its own lifetime");

        Phase10AbilityTuning flowing = tune("ribboncleaver", Phase10UniqueAbilities.RIBBON_HEAVY,
                Phase10WeaponManager.ribbonHeavyBase(), List.of(4, 8));
        assertEquals(0.88, flowing.get(s("SECONDARY_DAMAGE_MULTIPLIER"), 0), 1.0E-6,
                "Flowing Ribbon no longer collides with Heavy Retort");
        assertEquals(1.1, flowing.get(s("OUTGOING_MULTIPLIER"), 0), 1.0E-6);

        Phase10AbilityTuning rush = tune("ribboncleaver", Phase10UniqueAbilities.RIBBON_RUSH,
                Phase10WeaponManager.ribbonRushBase(), List.of(16));
        assertEquals(16, rush.integer(s("RANGE"), 0), 1.0E-6, "Juggernaut doubles the rush travel window");

        Phase10AbilityTuning step = tune("ribboncleaver", Phase10UniqueAbilities.RIBBON_RUSH,
                Phase10WeaponManager.ribbonRushBase(), List.of(17));
        assertEquals(5.0, step.get(s("SECONDARY_RADIUS"), 0), 1.0E-6, "Ribbon Step carries a blink distance");
        assertEquals(8, step.integer(s("RANGE"), 0), "and leaves the rush travel window alone");
    }

    @Test
    void thousandRibbonsStillProducesDamage() {
        double base = 0.95;
        Phase10AbilityTuning promise = tune("ribboncleaver", Phase10UniqueAbilities.RIBBON_PROMISE,
                Phase10WeaponManager.ribbonPromiseBase(base), List.of(26));
        double bonus = Math.max(0, promise.get(s("DAMAGE_MULTIPLIER"), base) - base);
        double sweep = (1 + base + bonus) * promise.get(s("SECONDARY_DAMAGE_MULTIPLIER"), 0);
        assertTrue(sweep > 1.0, "the sweep derives from the full empowered hit, not the mastery delta");
        assertEquals(10, promise.integer(s("TARGET_CAP"), 0));

        Phase10AbilityTuning sharper = tune("ribboncleaver", Phase10UniqueAbilities.RIBBON_PROMISE,
                Phase10WeaponManager.ribbonPromiseBase(base), List.of(19));
        assertEquals(1.10, sharper.get(s("DAMAGE_MULTIPLIER"), 0), 1.0E-6);

        Phase10AbilityTuning delayed = tune("ribboncleaver", Phase10UniqueAbilities.RIBBON_PROMISE,
                Phase10WeaponManager.ribbonPromiseBase(base), List.of(23));
        assertEquals(base, delayed.get(s("DAMAGE_MULTIPLIER"), 0), 1.0E-6,
                "Delayed Wrath no longer grants its bonus unconditionally");
        assertEquals(0.2, delayed.get(s("PER_STACK_MULTIPLIER"), 0), 1.0E-6);
    }

    private static final Phase10AbilityTuning LESSER =
            DawnquiverAbilityManager.lesserBase(100, 80, 24.0, 35.0);
    private static final Phase10AbilityTuning CHORUS =
            DawnquiverAbilityManager.chorusBase(3, 35.0);
    private static final Phase10AbilityTuning DRAW =
            DawnquiverAbilityManager.drawBase(80, .35, 4, .85, 6, 2, 2.0);
    private static final Phase10AbilityTuning CLOAK = Phase10WeaponManager.dreadCloakBase();
    private static final Phase10AbilityTuning PACT = Phase10WeaponManager.dreadPactBase(60, 1200);
    private static final Phase10AbilityTuning ASSAULT = Phase10WeaponManager.dreadAssaultBase(250, 12);

    @Test
    void everyDawnquiverAndDreadtideBranchReachesItsOwnDefinition() {
        for (int node = 0; node < 9; node++) {
            assertTrue(owns(warglaive("dawnquiver", Phase10UniqueAbilities.DAWN_LESSER, LESSER, node), node));
            assertFalse(owns(warglaive("dawnquiver", Phase10UniqueAbilities.DAWN_CHORUS, CHORUS, node), node));
            assertTrue(owns(warglaive("dreadtide", Phase10UniqueAbilities.DREAD_CLOAK, CLOAK, node), node));
            assertFalse(owns(warglaive("dreadtide", Phase10UniqueAbilities.DREAD_PACT, PACT, node), node));
        }
        for (int node = 9; node < 18; node++) {
            assertTrue(owns(warglaive("dawnquiver", Phase10UniqueAbilities.DAWN_CHORUS, CHORUS, node), node));
            assertFalse(owns(warglaive("dawnquiver", Phase10UniqueAbilities.DAWN_DRAW, DRAW, node), node));
            assertTrue(owns(warglaive("dreadtide", Phase10UniqueAbilities.DREAD_ASSAULT, ASSAULT, node), node));
            assertFalse(owns(warglaive("dreadtide", Phase10UniqueAbilities.DREAD_CLOAK, CLOAK, node), node));
            assertFalse(owns(warglaive("dreadtide", Phase10UniqueAbilities.DREAD_PACT, PACT, node), node));
        }
        for (int node = 18; node < 27; node++) {
            assertTrue(owns(warglaive("dawnquiver", Phase10UniqueAbilities.DAWN_DRAW, DRAW, node), node));
            assertFalse(owns(warglaive("dawnquiver", Phase10UniqueAbilities.DAWN_LESSER, LESSER, node), node));
            assertTrue(owns(warglaive("dreadtide", Phase10UniqueAbilities.DREAD_PACT, PACT, node), node));
            assertFalse(owns(warglaive("dreadtide", Phase10UniqueAbilities.DREAD_CLOAK, CLOAK, node), node),
                    "the transformation branch no longer retunes Voidcloak");
        }
    }

    @Test
    void dawnquiverNodesWriteKeysTheirConsumersRead() {
        Phase10AbilityTuning lesser = tune("dawnquiver", Phase10UniqueAbilities.DAWN_LESSER, LESSER,
                List.of(5, 6, 8));
        assertEquals(4, lesser.integer(s("SEARCH_CAP"), 0), "Twin Hymn owns its own cadence key");
        assertEquals(3, lesser.integer(s("STACK_CAP"), 0), "Solo Cantor owns the guaranteed-chorus cadence");
        assertEquals(130, lesser.integer(s("INTERVAL_TICKS"), 0));

        Phase10AbilityTuning choir = tune("dawnquiver", Phase10UniqueAbilities.DAWN_LESSER, LESSER, List.of(7));
        assertEquals(3, choir.integer(s("COUNT"), 0), "Dawn Choir publishes a bow count that is now read");

        Phase10AbilityTuning chorus = tune("dawnquiver", Phase10UniqueAbilities.DAWN_CHORUS, CHORUS,
                List.of(9, 12, 14, 15));
        assertEquals(4, chorus.integer(s("STACK_CAP"), 0), "Swelling Hymn no longer clobbers Fourth Voice");
        assertEquals(.03, chorus.get(s("PER_STACK_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(35.0, chorus.get(s("CHANCE"), 0), 1.0E-6, "Refrain no longer lowers the Chorus chance");
        assertEquals(.2, chorus.get(s("HEALTH_THRESHOLD"), 0), 1.0E-6);
        assertEquals(.8, chorus.get(s("INCOMING_MULTIPLIER"), 0), 1.0E-6);
    }

    @Test
    void dreadtideCorruptionNodesNoLongerInvertTheCloak() {
        Phase10AbilityTuning cloak = tune("dreadtide", Phase10UniqueAbilities.DREAD_CLOAK, CLOAK,
                List.of(2, 4, 6, 8));
        assertEquals(.95, cloak.get(s("INCOMING_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(15, cloak.integer(s("LOCKOUT_TICKS"), 0), "Emergency Veil no longer breaks Lasting Shroud");
        assertEquals(200, cloak.integer(s("DELAY_TICKS"), 0));
        assertEquals(0.0, cloak.get(s("SPEED"), 1), 1.0E-6, "Ravenous Cloak grants no movement speed");
        assertEquals(.08, cloak.get(s("PER_STACK_MULTIPLIER"), 0), 1.0E-6);

        Phase10AbilityTuning pact = tune("dreadtide", Phase10UniqueAbilities.DREAD_PACT, PACT,
                List.of(20, 21, 23, 24, 25, 26));
        assertEquals(.92, pact.get(s("INCOMING_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(1.08, pact.get(s("HEAL_MULTIPLIER"), 0), 1.0E-6,
                "Edge of Madness no longer overwrites the per-stack reduction");
        assertEquals(40, pact.integer(s("CORRUPTION"), 0));
        assertEquals(60, pact.integer(s("SECONDARY_CORRUPTION"), 0));
        assertEquals(80, pact.integer(s("FINAL_CORRUPTION"), 0));
        assertEquals(75, pact.integer(s("PITY_CHANCE"), 0));
        assertEquals(4.0, pact.get(s("ABSORPTION"), 0), 1.0E-6, "Dark Recovery keeps its own grant size");
        assertEquals(8.0, pact.get(s("HEIGHT"), 0), 1.0E-6);
        assertEquals(200, pact.integer(s("LOCKOUT_TICKS"), 0));
        assertEquals(1200, pact.integer(s("SECONDARY_DURATION_TICKS"), 0));

        Phase10AbilityTuning assault = tune("dreadtide", Phase10UniqueAbilities.DREAD_ASSAULT, ASSAULT,
                List.of(14, 15));
        assertEquals(5.0, assault.get(s("RANGE"), 0), 1.0E-6, "Leaping Darkness publishes a jump radius");
        assertEquals(2, assault.integer(s("COUNT"), 0));
        assertEquals(6, assault.integer(s("TARGET_CAP"), 0), "Final Scream owns its own target cap");
        assertEquals(1.8, assault.get(s("FINAL_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
    }

    private static boolean owns(Phase10AbilityTuning tuning, int node) {
        return (tuning.integer(s("MODE"), 0) & 1 << node % 27) != 0;
    }

    private static Phase10AbilityTuning warglaive(String profile, UniqueAbilityDefinition definition,
                                                  Phase10AbilityTuning base, int node) {
        return tune(profile, definition, base, List.of(node));
    }

    private static Phase10AbilityTuning tune(String profile, UniqueAbilityDefinition definition,
                                             Phase10AbilityTuning base, List<Integer> nodes) {
        MasteryProfile masteryProfile = BuiltInFamilyProfiles.profile(profile);
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(Phase10UniqueAbilities.TUNING, base);
        if (definition.cooldownKey().isPresent()) builder.set(Phase10UniqueAbilities.COOLDOWN_TICKS, 100);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                masteryProfile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, masteryProfile.nodes().get(node));
        return builder.get(Phase10UniqueAbilities.TUNING);
    }

    private static Phase10AbilityTuning.Setting s(String name) {
        return Phase10AbilityTuning.Setting.valueOf(name);
    }
}
