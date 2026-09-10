package net.sweenus.simplyswordsmastery.mastery.effect;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryRuntimeState;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryStateAccess;

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
    static final String SPRINT_HIT = "storms_edge/sprint_hit";
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

    static MasteryRuntimeState.Value value(LivingEntity actor, ItemStack stack, String key, long tick) {
        return PersonalRuntime.read(actor, stack).get(key, tick);
    }

    static void set(LivingEntity actor, ItemStack stack, String key, int amount, long expiresAt, long tick) {
        PersonalRuntime.write(actor, stack,
                PersonalRuntime.read(actor, stack).put(key, amount, expiresAt, tick));
    }

    static void clear(LivingEntity actor, ItemStack stack, String key) {
        PersonalRuntime.write(actor, stack, PersonalRuntime.read(actor, stack).clear(key));
    }

    static int advance(LivingEntity actor, ItemStack stack, String key, long tick, int maximum, int windowTicks) {
        MasteryRuntimeState.Value current = value(actor, stack, key, tick);
        int next = Math.min(maximum, current.amount() + 1);
        set(actor, stack, key, next, tick + windowTicks, tick);
        return next;
    }

    static long extend(LivingEntity actor, ItemStack stack, String key, long tick, int extension, int maximumRemaining) {
        MasteryRuntimeState.Value current = value(actor, stack, key, tick);
        long deadline = extendedDeadline(current.expiresAt(), tick, extension, maximumRemaining);
        set(actor, stack, key, current.amount(), deadline, tick);
        return deadline;
    }

    static long refreshedDeadline(long current, long tick, int duration) {
        return Math.max(current, tick + duration);
    }

    static long extendedDeadline(long current, long tick, int extension, int maximumRemaining) {
        return Math.min(tick + maximumRemaining, Math.max(tick, current) + extension);
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
