package net.sweenus.simplymastery.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplymastery.mastery.reward.AdvancementEntitlements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerAdvancementTracker.class)
public abstract class AdvancementRewardMixin {
    @Shadow private ServerPlayerEntity owner;

    @Unique private boolean simplymastery$loading;

    @WrapMethod(method = "load")
    private void simplymastery$load(ServerAdvancementLoader loader, Operation<Void> original) {
        boolean previous = simplymastery$loading;
        simplymastery$loading = true;
        try { original.call(loader); }
        finally { simplymastery$loading = previous; }
    }

    @WrapMethod(method = "grantCriterion")
    private boolean simplymastery$grant(AdvancementEntry advancement, String criterion, Operation<Boolean> original) {
        if (simplymastery$loading) return original.call(advancement, criterion);
        PlayerAdvancementTracker tracker = (PlayerAdvancementTracker) (Object) this;
        AdvancementEntitlements.baseline(owner, tracker);
        boolean completed = tracker.getProgress(advancement).isDone();
        boolean changed = original.call(advancement, criterion);
        if (changed && !completed && tracker.getProgress(advancement).isDone()) {
            AdvancementEntitlements.completed(owner, advancement);
        }
        return changed;
    }
}
