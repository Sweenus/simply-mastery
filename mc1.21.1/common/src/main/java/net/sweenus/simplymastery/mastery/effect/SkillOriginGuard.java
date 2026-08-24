package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BooleanSupplier;

public final class SkillOriginGuard {

    private static final ThreadLocal<Origin> ACTIVE = new ThreadLocal<>();

    private SkillOriginGuard() {
    }

    public static boolean active() {
        return ACTIVE.get() != null;
    }

    public static Origin current() {
        return ACTIVE.get();
    }

    public static boolean run(Origin origin, BooleanSupplier action) {
        if (active()) return false;
        ACTIVE.set(origin);
        try {
            return action.getAsBoolean();
        } finally {
            ACTIVE.remove();
        }
    }

    public record Origin(ServerPlayerEntity player, ItemStack stack, String nodeId) {
    }
}
