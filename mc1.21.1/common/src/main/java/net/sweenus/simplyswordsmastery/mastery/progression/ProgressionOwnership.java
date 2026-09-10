package net.sweenus.simplyswordsmastery.mastery.progression;

import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.server.MinecraftServer;
import net.sweenus.simplyswordsmastery.config.MasteryConfig;

import java.util.Map;
import java.util.WeakHashMap;

public final class ProgressionOwnership {
    private static final Map<MinecraftServer, ProgressionOwner.Kind> MODES = new WeakHashMap<>();

    private ProgressionOwnership() {
    }

    public static void init() {
        LifecycleEvent.SERVER_BEFORE_START.register(server -> MODES.put(server,
                MasteryConfig.SERVER.progressionOwnership));
        LifecycleEvent.SERVER_STOPPED.register(MODES::remove);
    }

    public static ProgressionOwner.Kind mode(MinecraftServer server) {
        return MODES.getOrDefault(server, ProgressionOwner.Kind.WEAPON);
    }

    public static boolean personal(MinecraftServer server) {
        return mode(server) == ProgressionOwner.Kind.PLAYER;
    }
}
