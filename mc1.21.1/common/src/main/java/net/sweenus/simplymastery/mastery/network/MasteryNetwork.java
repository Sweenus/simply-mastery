package net.sweenus.simplymastery.mastery.network;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MasteryNetwork {

    public static final int PROTOCOL_VERSION = 3;
    public static final SimpleNetworkManager NETWORK = SimpleNetworkManager.create(SimplyMastery.MOD_ID);
    public static final MessageType UNLOCK_NODE = NETWORK.registerC2S("unlock_node", UnlockNodePacket::new);
    public static final MessageType UNLOCK_RESULT = NETWORK.registerS2C("unlock_result", UnlockResultPacket::new);
    public static final MessageType REQUEST_PROFILE_SYNC = NETWORK.registerC2S("request_profile_sync", RequestProfileSyncPacket::new);
    public static final MessageType PROFILE_SYNC = NETWORK.registerS2C("profile_sync", ProfileSyncPacket::new);
    private static volatile MinecraftServer server;
    private static final long REQUEST_INTERVAL_NANOS = 2_000_000_000L;
    private static final Map<UUID, Long> LAST_SYNCS = new HashMap<>();
    private static MasteryProfileRegistry.Snapshot cachedSnapshot;
    private static ProfileSyncPacket cachedPacket;

    private MasteryNetwork() {
    }

    public static void init() {
        UnlockService.init();
        LifecycleEvent.SERVER_BEFORE_START.register(value -> server = value);
        LifecycleEvent.SERVER_STOPPED.register(value -> {
            if (server == value) {
                server = null;
                clearSession();
            }
        });
        PlayerEvent.PLAYER_JOIN.register(MasteryNetwork::sync);
        PlayerEvent.PLAYER_QUIT.register(player -> {
            synchronized (MasteryNetwork.class) {
                LAST_SYNCS.remove(player.getUuid());
            }
        });
    }

    public static void sync(ServerPlayerEntity player) {
        ProfileSyncPacket packet;
        synchronized (MasteryNetwork.class) {
            LAST_SYNCS.put(player.getUuid(), System.nanoTime());
            packet = currentPacket();
        }
        packet.sendTo(player);
    }

    public static void requestSync(ServerPlayerEntity player) {
        ProfileSyncPacket packet;
        synchronized (MasteryNetwork.class) {
            long now = System.nanoTime();
            Long previous = LAST_SYNCS.get(player.getUuid());
            if (previous != null && now - previous < REQUEST_INTERVAL_NANOS) return;
            LAST_SYNCS.put(player.getUuid(), now);
            packet = currentPacket();
        }
        packet.sendTo(player);
    }

    public static synchronized void prepareProfileReload() {
        if (server == null) clearSession();
    }

    private static synchronized void clearSession() {
        LAST_SYNCS.clear();
        cachedSnapshot = null;
        cachedPacket = null;
        MasteryProfileRegistry.clearServer();
    }

    private static ProfileSyncPacket currentPacket() {
        MasteryProfileRegistry.Snapshot snapshot = MasteryProfileRegistry.server();
        if (cachedSnapshot != snapshot) {
            cachedPacket = new ProfileSyncPacket(snapshot);
            cachedSnapshot = snapshot;
        }
        return cachedPacket;
    }

    public static void syncAllPlayers() {
        MinecraftServer current = server;
        if (current == null || current.getPlayerManager() == null) return;
        current.execute(() -> {
            if (server != current) return;
            for (ServerPlayerEntity player : current.getPlayerManager().getPlayerList()) sync(player);
        });
    }
}
