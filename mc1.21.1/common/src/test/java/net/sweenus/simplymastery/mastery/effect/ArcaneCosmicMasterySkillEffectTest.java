package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.ArcaneCosmicMasteryTuning;
import net.sweenus.simplyswords.api.ability.ArcaneCosmicMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import net.sweenus.simplyswords.world.ArcanethystAssaultManager;
import net.sweenus.simplyswords.world.MagiscytheMasteryManager;
import net.sweenus.simplyswords.world.StarsEdgeAbilityManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArcaneCosmicMasterySkillEffectTest {

    private static final ArcaneCosmicMasteryTuning SPARK = ArcanethystAssaultManager.sparkBase(25);
    private static final ArcaneCosmicMasteryTuning SUSPENSION =
            ArcanethystAssaultManager.suspensionBase(4.0, 18, 14, 6.0, 100);
    private static final ArcaneCosmicMasteryTuning IMPACT = ArcanethystAssaultManager.impactBase(4.0, 10);
    private static final ArcaneCosmicMasteryTuning SOLAR = StarsEdgeAbilityManager.solarBase();
    private static final ArcaneCosmicMasteryTuning LUNAR = StarsEdgeAbilityManager.lunarBase();
    private static final ArcaneCosmicMasteryTuning CONSTELLATION =
            StarsEdgeAbilityManager.constellationBase(120, 100, 5, 10, 2.5, 1.5, 12);
    private static final ArcaneCosmicMasteryTuning STORM = MagiscytheMasteryManager.stormBase(400, 4.0, 5);
    private static final ArcaneCosmicMasteryTuning STRIKES = MagiscytheMasteryManager.strikesBase(4.0);
    private static final ArcaneCosmicMasteryTuning WRIGHT = MagiscytheMasteryManager.magewrightBase(25);

    @Test
    void arcanethystTransformationReachesTheImpactDefinition() {
        for (int node = 0; node < 9; node++) {
            assertTrue(owns(one("arcanethyst", ArcaneCosmicMasteryAbilities.ARCANETHYST_SPARK, SPARK, node), node));
            assertFalse(owns(one("arcanethyst", ArcaneCosmicMasteryAbilities.ARCANETHYST_IMPACT, IMPACT, node), node));
        }
        for (int node = 9; node < 18; node++) {
            assertTrue(owns(one("arcanethyst", ArcaneCosmicMasteryAbilities.ARCANETHYST_SUSPENSION, SUSPENSION, node), node));
            assertFalse(owns(one("arcanethyst", ArcaneCosmicMasteryAbilities.ARCANETHYST_IMPACT, IMPACT, node), node));
        }
        for (int node = 18; node < 27; node++) {
            assertTrue(owns(one("arcanethyst", ArcaneCosmicMasteryAbilities.ARCANETHYST_IMPACT, IMPACT, node), node));
            assertFalse(owns(one("arcanethyst", ArcaneCosmicMasteryAbilities.ARCANETHYST_SUSPENSION, SUSPENSION, node), node),
                    "the transformation branch no longer retunes the suspension");
        }
    }

    @Test
    void arcanethystStasisGeodeNoLongerCollapsesTheSlam() {
        ArcaneCosmicMasteryTuning suspension = tune("arcanethyst", ArcaneCosmicMasteryAbilities.ARCANETHYST_SUSPENSION,
                SUSPENSION, List.of(16));
        assertEquals(1.4, suspension.get(s("FINAL_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        ArcaneCosmicMasteryTuning impact = tune("arcanethyst", ArcaneCosmicMasteryAbilities.ARCANETHYST_IMPACT,
                IMPACT, List.of(16, 18));
        assertEquals(4.6, impact.get(s("FINAL_DAMAGE_MULTIPLIER"), 0), 1.0E-6,
                "the slam multiplier only sees its own branch");
    }

    @Test
    void arcanethystSharedStatusChannelsAreSeparated() {
        ArcaneCosmicMasteryTuning spark = tune("arcanethyst", ArcaneCosmicMasteryAbilities.ARCANETHYST_SPARK,
                SPARK, List.of(5, 6, 8));
        assertEquals(100, spark.integer(s("STATUS_DURATION_TICKS"), 0), "Levitation keeps Perfect Cut's duration");
        assertEquals(40, spark.integer(s("SECONDARY_STATUS_DURATION_TICKS"), 0), "Weakness owns its own channel");
        assertEquals(60, spark.integer(s("WINDUP_TICKS"), 0), "and so does the absorption grant");

        ArcaneCosmicMasteryTuning impact = tune("arcanethyst", ArcaneCosmicMasteryAbilities.ARCANETHYST_IMPACT,
                IMPACT, List.of(20, 24, 26));
        assertEquals(2.5, impact.get(s("SECONDARY_RADIUS"), 0), 1.0E-6, "Fracture Ring keeps its radius");
        assertEquals(3.0, impact.get(s("TERTIARY_RADIUS"), 0), 1.0E-6, "and the aftershock owns a separate one");
        assertEquals(80, impact.integer(s("TERTIARY_DURATION_TICKS"), 0));
        assertEquals(10, impact.integer(s("INTERVAL_TICKS"), 0), "the field no longer retunes the slam time");
    }

    @Test
    void starsEdgeChannelsAndReachability() {
        ArcaneCosmicMasteryTuning solar = tune("stars_edge", ArcaneCosmicMasteryAbilities.STARS_SOLAR,
                SOLAR, List.of(2, 7));
        assertEquals(4, solar.integer(s("STACK_CAP"), 0), "Supernova Edge leaves Solar Brand's cap alone");
        assertEquals(25, solar.integer(s("FLAT_DAMAGE"), 0));
        assertTrue(owns(one("stars_edge", ArcaneCosmicMasteryAbilities.STARS_SOLAR, SOLAR, 8), 8),
                "Endless Day is published on the solar definition the night branch now reads");

        ArcaneCosmicMasteryTuning lunar = tune("stars_edge", ArcaneCosmicMasteryAbilities.STARS_LUNAR,
                LUNAR, List.of(9, 12, 13));
        assertEquals(1.1, lunar.get(s("HEAL_MULTIPLIER"), 0), 1.0E-6,
                "Blood Moon no longer raises lifesteal unconditionally");
        assertEquals(1.25, lunar.get(s("INCOMING_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(1.08, lunar.get(s("PER_STACK_MULTIPLIER"), 0), 1.0E-6, "Eclipse Brand owns its own key");

        ArcaneCosmicMasteryTuning constellation = tune("stars_edge", ArcaneCosmicMasteryAbilities.STARS_CONSTELLATION,
                CONSTELLATION, List.of(19, 20, 22));
        assertEquals(140, constellation.integer(s("DURATION_TICKS"), 0));
        assertEquals(2.9, constellation.get(s("RADIUS"), 0), 1.0E-6);
        assertEquals(12, constellation.integer(s("COUNT"), 0), "Crowded Heavens no longer changes the star count");
        assertEquals(1.5, constellation.get(s("SECONDARY_RADIUS"), 0), 1.0E-6);

        ArcaneCosmicMasteryTuning living = tune("stars_edge", ArcaneCosmicMasteryAbilities.STARS_CONSTELLATION,
                CONSTELLATION, List.of(26));
        assertEquals(12, living.integer(s("COUNT"), 0), "Living Zodiac no longer collapses to two stars");
    }

    @Test
    void magiscytheChannelsAndSingularBolt() {
        ArcaneCosmicMasteryTuning storm = tune("magiscythe", ArcaneCosmicMasteryAbilities.MAGISCYTHE_STORM,
                STORM, List.of(0, 2, 4, 6, 7));
        assertEquals(430, storm.integer(s("DURATION_TICKS"), 0), "Everstorm no longer shortens the storm");
        assertEquals(80, storm.integer(s("SECONDARY_DURATION_TICKS"), 0));
        assertEquals(13, storm.integer(s("CHANCE"), 0));
        assertEquals(6.0, storm.get(s("RADIUS"), 0), 1.0E-6);
        assertEquals(2, storm.integer(s("DELAY_TICKS"), 0));
        assertEquals(8, MagiscytheMasteryManager.strikeInterval(storm, 0));

        ArcaneCosmicMasteryTuning strikes = tune("magiscythe", ArcaneCosmicMasteryAbilities.MAGISCYTHE_STRIKES,
                STRIKES, List.of(12, 15, 17));
        assertEquals(.05, strikes.get(s("PER_STACK_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(4, strikes.integer(s("STACK_CAP"), 0));
        assertEquals(60, strikes.integer(s("DURATION_TICKS"), 0), "Singular Bolt marks on melee like Storm Mark");
        assertEquals(2.0, strikes.get(s("DAMAGE_MULTIPLIER"), 0), 1.0E-6,
                "and its doubling is published where the strike damage reads it");

        ArcaneCosmicMasteryTuning charge = tune("magiscythe", ArcaneCosmicMasteryAbilities.MAGISCYTHE_MAGEWRIGHT,
                WRIGHT, List.of(18, 22));
        assertEquals(33, charge.integer(s("CHANCE"), 0),
                "Conserved Charge no longer overwrites Careful Mending's chance");

        ArcaneCosmicMasteryTuning wright = tune("magiscythe", ArcaneCosmicMasteryAbilities.MAGISCYTHE_MAGEWRIGHT,
                WRIGHT, List.of(18, 21, 22, 24, 26));
        assertEquals(25, wright.integer(s("CHANCE"), 0),
                "and Sacrificial Edge still caps it at 25 as its own drawback");
        assertEquals(10, wright.integer(s("PITY_CHANCE"), 0));
        assertEquals(40, wright.integer(s("STACK_CAP"), 0));
        assertEquals(1.5, wright.get(s("SECONDARY_DAMAGE_MULTIPLIER"), 0), 1.0E-6,
                "Perfect Temper boosts the fifth repair rather than pinning every repair to 150");
        assertEquals(2.0, wright.get(s("FINAL_DAMAGE_MULTIPLIER"), 0), 1.0E-6);
        assertEquals(0, wright.integer(s("REPAIR_AMOUNT"), 0), "and Arcane Salvage no longer pins the amount");
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
        if (definition.cooldownKey().isPresent()) builder.set(ArcaneCosmicMasteryAbilities.COOLDOWN_TICKS, 220);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                masteryProfile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, masteryProfile.nodes().get(node));
        return builder.get(ArcaneCosmicMasteryAbilities.TUNING);
    }

    private static ArcaneCosmicMasteryTuning.Setting s(String name) {
        return ArcaneCosmicMasteryTuning.Setting.valueOf(name);
    }
}
