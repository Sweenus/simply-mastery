package net.sweenus.simplymastery.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplymastery.mastery.progression.MasteryRewardService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerWeaponIdentityMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void simplymastery$beforeAttack(Entity target, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            MasteryRewardService.synchronize(player.getServer(), player.getMainHandStack());
            MasteryRewardService.synchronize(player.getServer(), player.getOffHandStack());
        }
    }
}
