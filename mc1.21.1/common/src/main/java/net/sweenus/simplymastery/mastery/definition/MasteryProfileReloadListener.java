package net.sweenus.simplymastery.mastery.definition;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.sweenus.simplymastery.mastery.network.MasteryNetwork;

public final class MasteryProfileReloadListener implements SynchronousResourceReloader {

    @Override
    public void reload(ResourceManager manager) {
        MasteryNetwork.prepareProfileReload();
        MasteryProfileRegistry.ReloadResult result = MasteryProfileRegistry.reload(manager);
        if (result.installed()) MasteryNetwork.syncAllPlayers();
    }
}
