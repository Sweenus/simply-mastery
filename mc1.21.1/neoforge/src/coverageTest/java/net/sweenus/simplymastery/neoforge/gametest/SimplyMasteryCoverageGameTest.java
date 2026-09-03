package net.sweenus.simplymastery.neoforge.gametest;

import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.sweenus.simplymastery.mastery.effect.MasteryIntegrationContractChecks;
import net.sweenus.simplymastery.mastery.state.FormProgressionContractChecks;

@GameTestHolder("simplymastery_coverage_test")
@PrefixGameTestTemplate(false)
public final class SimplyMasteryCoverageGameTest {

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void loadedRegistryCoverage(TestContext context) {
        FormProgressionContractChecks.requireFormProgressionFoundation();
        context.complete();
    }

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void inertEffectPrimitive(TestContext context) {
        MasteryIntegrationContractChecks.requireEffect("none");
        context.complete();
    }

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void statusEffectPrimitive(TestContext context) {
        MasteryIntegrationContractChecks.requireEffect("stormstep");
        context.complete();
    }

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void additionalDamagePrimitive(TestContext context) {
        MasteryIntegrationContractChecks.requireEffect("echo_strike");
        MasteryIntegrationContractChecks.requireRecursionGuard();
        context.complete();
    }

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void comboPrimitive(TestContext context) {
        MasteryIntegrationContractChecks.requireEffect("combo_surge");
        MasteryIntegrationContractChecks.requireReleasedEffects();
        context.complete();
    }

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void uniqueAbilityIntegration(TestContext context) {
        MasteryIntegrationContractChecks.requireUniqueAbilityIntegration();
        context.complete();
    }
}
