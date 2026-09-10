package net.sweenus.simplyswordsmastery.mastery.network;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sweenus.simplyswordsmastery.mastery.RunicForgeMasteryContext;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplyswordsmastery.mastery.progression.PersonalProgression;
import net.sweenus.simplyswordsmastery.mastery.progression.ProgressionOwnership;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryState;
import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PersonalStateSync {
    private static final Map<UUID, Map<Identifier, Sent>> SENT = new HashMap<>();

    private PersonalStateSync() {
    }

    public static void init() {
        LifecycleEvent.SERVER_STOPPED.register(server -> SENT.clear());
        PlayerEvent.PLAYER_QUIT.register(player -> SENT.remove(player.getUuid()));
        TickEvent.SERVER_POST.register(server -> {
            if (!ProgressionOwnership.personal(server)) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                for (int slot = 0; slot < player.getInventory().size(); slot++) {
                    MasteryProfileRegistry.resolveServer(player.getInventory().getStack(slot))
                            .ifPresent(profile -> sync(player, profile));
                }
                if (player.currentScreenHandler instanceof RunicForgeScreenHandler forge) {
                    MasteryProfileRegistry.resolveServer(RunicForgeMasteryContext.identityStack(forge))
                            .ifPresent(profile -> sync(player, profile));
                }
            }
        });
    }

    public static void reset(ServerPlayerEntity player) {
        SENT.remove(player.getUuid());
        new PersonalStatePacket(player.getUuid(), ProgressionOwnership.mode(player.getServer()),
                MasteryProfileRegistry.server().epoch(), null).sendTo(player);
    }

    private static void sync(ServerPlayerEntity player, MasteryProfile profile) {
        MasteryState state = PersonalProgression.read(player, profile);
        Sent updated = new Sent(MasteryProfileRegistry.server().epoch(), state);
        var views = SENT.computeIfAbsent(player.getUuid(), ignored -> new HashMap<>());
        if (!updated.equals(views.put(profile.id(), updated))) {
            new PersonalStatePacket(player.getUuid(), ProgressionOwnership.mode(player.getServer()),
                    updated.epoch, state).sendTo(player);
        }
    }

    private record Sent(int epoch, MasteryState state) {
    }
}
