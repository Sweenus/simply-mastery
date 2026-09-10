package net.sweenus.simplyswordsmastery.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplyswordsmastery.mastery.RunicForgeMasteryContext;
import net.sweenus.simplyswordsmastery.mastery.progression.MasteryRewardService;
import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RunicForgeScreenHandler.class)
public abstract class RunicForgeProgressionMixin {
    @Shadow(remap = false) @Final private PlayerEntity owner;

    @Inject(method = {"loadInsertedWeapon", "commitWeapon", "refreshPreview"}, at = @At("HEAD"), remap = false)
    private void simplyswordsmastery$synchronizeWeapon(CallbackInfo ci) {
        if (owner instanceof ServerPlayerEntity player) {
            MasteryRewardService.synchronize(player.getServer(),
                    RunicForgeMasteryContext.stateStack((RunicForgeScreenHandler) (Object) this));
        }
    }
}
