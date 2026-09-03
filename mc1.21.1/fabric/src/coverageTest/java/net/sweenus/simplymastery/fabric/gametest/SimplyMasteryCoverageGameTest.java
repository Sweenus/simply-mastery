package net.sweenus.simplymastery.fabric.gametest;

import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.sweenus.simplymastery.mastery.effect.MasteryIntegrationContractChecks;
import net.sweenus.simplymastery.mastery.state.FormProgressionContractChecks;

public final class SimplyMasteryCoverageGameTest {

    @GameTest(templateName = "fabric-gametest-api-v1:empty", tickLimit = 20)
    public void loadedRegistryCoverage(TestContext context) {
        FormProgressionContractChecks.requireFormProgressionFoundation();
        context.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty", tickLimit = 20)
    public void inertEffectPrimitive(TestContext context) {
        MasteryIntegrationContractChecks.requireEffect("none");
        context.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty", tickLimit = 20)
    public void statusEffectPrimitive(TestContext context) {
        MasteryIntegrationContractChecks.requireEffect("stormstep");
        context.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty", tickLimit = 20)
    public void additionalDamagePrimitive(TestContext context) {
        MasteryIntegrationContractChecks.requireEffect("echo_strike");
        MasteryIntegrationContractChecks.requireRecursionGuard();
        context.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty", tickLimit = 20)
    public void comboPrimitive(TestContext context) {
        MasteryIntegrationContractChecks.requireEffect("combo_surge");
        MasteryIntegrationContractChecks.requireReleasedEffects();
        context.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty", tickLimit = 20)
    public void uniqueAbilityIntegration(TestContext context) {
        MasteryIntegrationContractChecks.requireUniqueAbilityIntegration();
        context.complete();
    }
}
