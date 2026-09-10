package net.sweenus.simplyswordsmastery.client;

import dev.architectury.event.events.client.ClientPlayerEvent;
import net.sweenus.simplyswordsmastery.client.mastery.PersonalMasteryView;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfileRegistry;

public final class SimplySwordsMasteryClient {

    private SimplySwordsMasteryClient() {
    }

    public static void init() {
        MasteryCooldownTooltip.init();
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            MasteryProfileRegistry.clearClient();
            PersonalMasteryView.clear();
        });
    }
}
