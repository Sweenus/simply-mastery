package net.sweenus.simplymastery.client;

import dev.architectury.event.events.client.ClientPlayerEvent;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;

public final class SimplyMasteryClient {

    private SimplyMasteryClient() {
    }

    public static void init() {
        MasteryCooldownTooltip.init();
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> MasteryProfileRegistry.clearClient());
    }
}
