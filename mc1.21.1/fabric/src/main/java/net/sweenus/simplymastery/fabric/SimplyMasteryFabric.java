package net.sweenus.simplymastery.fabric;

import net.fabricmc.api.ModInitializer;
import net.sweenus.simplymastery.SimplyMastery;

public final class SimplyMasteryFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SimplyMastery.init();
    }
}
