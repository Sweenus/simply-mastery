package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplymastery.mastery.progression.ProgressionOwnership;
import net.sweenus.simplymastery.mastery.state.MasteryRuntimeState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class PersonalRuntime {
    private static final Map<MinecraftServer, Map<Key, MasteryRuntimeState>> VALUES = new WeakHashMap<>();

    private PersonalRuntime() {
    }

    static MasteryRuntimeState read(LivingEntity actor, ItemStack stack) {
        if (actor.getServer() == null || !ProgressionOwnership.personal(actor.getServer())) {
            return MasteryStateAccess.runtime(stack);
        }
        Key key = key(actor, stack);
        return key == null ? MasteryRuntimeState.EMPTY
                : VALUES.getOrDefault(actor.getServer(), Map.of()).getOrDefault(key, MasteryRuntimeState.EMPTY);
    }

    static void write(LivingEntity actor, ItemStack stack, MasteryRuntimeState state) {
        if (actor.getServer() == null || !ProgressionOwnership.personal(actor.getServer())) {
            MasteryStateAccess.writeRuntime(stack, state);
            return;
        }
        Key key = key(actor, stack);
        if (key == null) return;
        var entries = VALUES.computeIfAbsent(actor.getServer(), ignored -> new HashMap<>());
        if (state.values().isEmpty()) entries.remove(key);
        else entries.put(key, state);
    }

    public static void clear(MinecraftServer server, UUID actor) {
        var entries = VALUES.get(server);
        if (entries != null) entries.keySet().removeIf(key -> key.actor.equals(actor));
    }

    private static Key key(LivingEntity actor, ItemStack stack) {
        return MasteryProfileRegistry.resolveProgressionServer(stack)
                .map(resolution -> new Key(actor.getUuid(), resolution.groupId())).orElse(null);
    }

    private record Key(UUID actor, Identifier group) {
    }
}
