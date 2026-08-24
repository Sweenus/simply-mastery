package net.sweenus.simplymastery.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileReloadListener;

@Mod(SimplyMastery.MOD_ID)
public final class SimplyMasteryNeoForge {

    public SimplyMasteryNeoForge() {
        SimplyMastery.init();
        NeoForge.EVENT_BUS.addListener(this::registerReloadListener);
    }

    private void registerReloadListener(AddReloadListenerEvent event) {
        event.addListener(new MasteryProfileReloadListener());
    }
}
