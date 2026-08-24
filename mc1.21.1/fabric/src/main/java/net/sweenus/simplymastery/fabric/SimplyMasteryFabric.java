package net.sweenus.simplymastery.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.sweenus.simplymastery.SimplyMastery;

public final class SimplyMasteryFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SimplyMastery.init();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FabricProfileReloadListener());
    }
}
