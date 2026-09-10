package net.sweenus.simplyswordsmastery.mixin;

import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.api.WeaponAbilityContext;
import net.sweenus.simplyswordsmastery.mastery.progression.MasteryRewardService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimplySwordsAPI.class)
public abstract class WeaponAbilityIdentityMixin {
    @Inject(method = "tryActivateWeaponAbility", at = @At("HEAD"), remap = false)
    private static void simplyswordsmastery$beforeAbility(WeaponAbilityContext context, CallbackInfoReturnable<Boolean> ci) {
        if (context == null || context.sourcePlayer() == null) return;
        var player = context.sourcePlayer();
        if (context.stack() == player.getMainHandStack() || context.stack() == player.getOffHandStack()) {
            MasteryRewardService.synchronize(player.getServer(), context.stack());
        }
    }
}
