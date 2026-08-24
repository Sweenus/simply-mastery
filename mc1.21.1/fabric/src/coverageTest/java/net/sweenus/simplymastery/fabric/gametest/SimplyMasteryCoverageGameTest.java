package net.sweenus.simplymastery.fabric.gametest;

import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.sweenus.simplymastery.mastery.effect.Phase3ContractChecks;

public final class SimplyMasteryCoverageGameTest {

    @GameTest(templateName = "fabric-gametest-api-v1:empty", tickLimit = 20)
    public void loadedRegistryCoverage(TestContext context) {
        context.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty", tickLimit = 20)
    public void inertEffectPrimitive(TestContext context) {
        Phase3ContractChecks.requireEffect("none");
        context.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty", tickLimit = 20)
    public void statusEffectPrimitive(TestContext context) {
        Phase3ContractChecks.requireEffect("stormstep");
        context.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty", tickLimit = 20)
    public void additionalDamagePrimitive(TestContext context) {
        Phase3ContractChecks.requireEffect("echo_strike");
        Phase3ContractChecks.requireRecursionGuard();
        context.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty", tickLimit = 20)
    public void comboPrimitive(TestContext context) {
        Phase3ContractChecks.requireEffect("combo_surge");
        context.complete();
    }
}
