package net.sweenus.simplyswordsmastery.fabric;

import net.fabricmc.api.ModInitializer;
import net.sweenus.simplyswordsmastery.SimplySwordsMastery;

public final class SimplySwordsMasteryFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SimplySwordsMastery.init();
    }
}
