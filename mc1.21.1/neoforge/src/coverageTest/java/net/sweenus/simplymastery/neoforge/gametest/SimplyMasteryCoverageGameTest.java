package net.sweenus.simplymastery.neoforge.gametest;

import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.sweenus.simplymastery.mastery.effect.Phase3ContractChecks;

@GameTestHolder("simplymastery_coverage_test")
@PrefixGameTestTemplate(false)
public final class SimplyMasteryCoverageGameTest {

    @GameTest(templateName = "coverage_empty", tickLimit = 20)
    public static void loadedRegistryCoverage(TestContext context) {
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
        context.complete();
    }
}
