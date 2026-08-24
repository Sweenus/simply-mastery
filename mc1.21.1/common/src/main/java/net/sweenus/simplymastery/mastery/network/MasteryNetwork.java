package net.sweenus.simplymastery.mastery.network;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;

public final class MasteryNetwork {

    public static final int PROTOCOL_VERSION = 3;
    public static final SimpleNetworkManager NETWORK = SimpleNetworkManager.create(SimplyMastery.MOD_ID);
    public static final MessageType UNLOCK_NODE = NETWORK.registerC2S("unlock_node", UnlockNodePacket::new);
    public static final MessageType UNLOCK_RESULT = NETWORK.registerS2C("unlock_result", UnlockResultPacket::new);
    public static final MessageType REQUEST_PROFILE_SYNC = NETWORK.registerC2S("request_profile_sync", RequestProfileSyncPacket::new);
    public static final MessageType PROFILE_SYNC = NETWORK.registerS2C("profile_sync", ProfileSyncPacket::new);
    private static volatile MinecraftServer server;

    private MasteryNetwork() {
    }

    public static void init() {
        UnlockService.init();
        LifecycleEvent.SERVER_BEFORE_START.register(value -> server = value);
        LifecycleEvent.SERVER_STOPPED.register(value -> server = null);
        PlayerEvent.PLAYER_JOIN.register(MasteryNetwork::sync);
    }

    public static void sync(ServerPlayerEntity player) {
        new ProfileSyncPacket(MasteryProfileRegistry.server()).sendTo(player);
    }

    public static void syncAllPlayers() {
        MinecraftServer current = server;
        if (current == null || current.getPlayerManager() == null) return;
        for (ServerPlayerEntity player : current.getPlayerManager().getPlayerList()) sync(player);
    }
}
