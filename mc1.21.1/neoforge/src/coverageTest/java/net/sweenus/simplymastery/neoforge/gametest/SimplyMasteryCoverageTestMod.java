package net.sweenus.simplymastery.neoforge.gametest;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@Mod("simplymastery_coverage_test")
public final class SimplyMasteryCoverageTestMod {

    public SimplyMasteryCoverageTestMod(IEventBus eventBus, ModContainer container) {
        eventBus.addListener(this::registerTests);
    }

    private void registerTests(RegisterGameTestsEvent event) {
        event.register(SimplyMasteryCoverageGameTest.class);
    }
}
