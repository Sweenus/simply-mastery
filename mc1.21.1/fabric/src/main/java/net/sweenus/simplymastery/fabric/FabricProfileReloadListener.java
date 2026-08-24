package net.sweenus.simplymastery.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileReloadListener;

public final class FabricProfileReloadListener implements IdentifiableResourceReloadListener, SynchronousResourceReloader {

    private final MasteryProfileReloadListener delegate = new MasteryProfileReloadListener();

    @Override
    public Identifier getFabricId() {
        return Identifier.of("simplymastery", "weapon_skills");
    }

    @Override
    public void reload(ResourceManager manager) {
        delegate.reload(manager);
    }
}
