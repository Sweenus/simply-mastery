package net.sweenus.simplymastery.neoforge.gametest;

import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.sweenus.simplymastery.mastery.effect.Phase3ContractChecks;
import net.sweenus.simplymastery.mastery.state.Phase1ContractChecks;

@GameTestHolder("simplymastery_coverage_test")
@PrefixGameTestTemplate(false)
public final class SimplyMasteryCoverageGameTest {

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void loadedRegistryCoverage(TestContext context) {
        Phase1ContractChecks.requireFormProgressionFoundation();
        context.complete();
    }

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void inertEffectPrimitive(TestContext context) {
        Phase3ContractChecks.requireEffect("none");
        context.complete();
    }

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void statusEffectPrimitive(TestContext context) {
        Phase3ContractChecks.requireEffect("stormstep");
        context.complete();
    }

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void additionalDamagePrimitive(TestContext context) {
        Phase3ContractChecks.requireEffect("echo_strike");
        Phase3ContractChecks.requireRecursionGuard();
        context.complete();
    }

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void comboPrimitive(TestContext context) {
        Phase3ContractChecks.requireEffect("combo_surge");
        Phase3ContractChecks.requireReleasedEffects();
        context.complete();
    }

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void uniqueAbilityIntegration(TestContext context) {
        Phase3ContractChecks.requireUniqueAbilityIntegration();
        context.complete();
    }
}
