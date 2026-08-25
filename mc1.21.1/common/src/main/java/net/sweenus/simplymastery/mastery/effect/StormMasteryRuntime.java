package net.sweenus.simplymastery.mastery.effect;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.sweenus.simplymastery.mastery.state.MasteryRuntimeState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class StormMasteryRuntime {
    static final String VOLTAGE = "storms_edge/voltage";
    static final String PURSUIT = "storms_edge/pursuit";
    static final String FEEDBACK = "storms_edge/feedback";
    static final String OVERDRIVE = "storms_edge/overdrive";
    static final String LIVE_WIRE = "storms_edge/live_wire";
    static final String ARC_LASH = "storms_edge/arc_lash";
    private static final Map<ServerWorld, Map<Mark, Long>> IONIZED = new HashMap<>();

    private StormMasteryRuntime() {
    }

    public static void init() {
        TickEvent.SERVER_LEVEL_POST.register(world -> {
            if (!(world instanceof ServerWorld serverWorld) || serverWorld.getTime() % 20L != 0L) return;
            Map<Mark, Long> marks = IONIZED.get(serverWorld);
            if (marks == null) return;
            long tick = serverWorld.getTime();
            marks.entrySet().removeIf(entry -> entry.getValue() <= tick
                    || serverWorld.getEntity(entry.getKey().actor()) == null
                    || serverWorld.getEntity(entry.getKey().target()) == null);
            if (marks.isEmpty()) IONIZED.remove(serverWorld);
        });
    }

    static MasteryRuntimeState.Value value(ItemStack stack, String key, long tick) {
        return MasteryStateAccess.runtime(stack).get(key, tick);
    }

    static void set(ItemStack stack, String key, int amount, long expiresAt, long tick) {
        MasteryStateAccess.writeRuntime(stack,
                MasteryStateAccess.runtime(stack).put(key, amount, expiresAt, tick));
    }

    static void clear(ItemStack stack, String key) {
        MasteryStateAccess.writeRuntime(stack, MasteryStateAccess.runtime(stack).clear(key));
    }

    static int advance(ItemStack stack, String key, long tick, int maximum, int windowTicks) {
        MasteryRuntimeState.Value current = value(stack, key, tick);
        int next = Math.min(maximum, current.amount() + 1);
        set(stack, key, next, tick + windowTicks, tick);
        return next;
    }

    static long extend(ItemStack stack, String key, long tick, int extension, int maximumRemaining) {
        long current = value(stack, key, tick).expiresAt();
        long deadline = Math.min(tick + maximumRemaining, Math.max(tick, current) + extension);
        set(stack, key, 1, deadline, tick);
        return deadline;
    }

    static void ionize(ServerWorld world, UUID actor, UUID target, long expiresAt) {
        IONIZED.computeIfAbsent(world, ignored -> new HashMap<>()).put(new Mark(actor, target), expiresAt);
    }

    static boolean ionized(ServerWorld world, UUID actor, UUID target) {
        Map<Mark, Long> marks = IONIZED.get(world);
        return marks != null && marks.getOrDefault(new Mark(actor, target), 0L) > world.getTime();
    }

    static boolean consumeIonized(ServerWorld world, UUID actor, UUID target) {
        Map<Mark, Long> marks = IONIZED.get(world);
        return marks != null && marks.remove(new Mark(actor, target)) != null;
    }

    public static void clear(UUID actor) {
        IONIZED.values().forEach(marks -> marks.keySet().removeIf(mark -> mark.actor().equals(actor)));
        IONIZED.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private record Mark(UUID actor, UUID target) {
    }
}
