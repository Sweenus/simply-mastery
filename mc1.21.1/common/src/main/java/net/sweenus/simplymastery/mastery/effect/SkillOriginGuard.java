package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplyswords.api.combat.CombatProvenance;
import net.sweenus.simplyswords.api.combat.CombatProvenanceApi;

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
        try (var ignored = CombatProvenanceApi.origin(origin.player(), origin.stack(),
                CombatProvenance.ABILITY)) {
            return action.getAsBoolean();
        } finally {
            ACTIVE.remove();
        }
    }

    public record Origin(ServerPlayerEntity player, ItemStack stack, String nodeId) {
    }
}
