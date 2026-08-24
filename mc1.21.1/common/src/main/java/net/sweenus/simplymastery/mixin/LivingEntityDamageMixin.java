package net.sweenus.simplymastery.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplymastery.mastery.effect.StormstepEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    @Inject(method = "damage", at = @At("RETURN"))
    private void simplymastery$afterDamage(DamageSource source, float amount,
                                           CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ()
                || !source.isOf(DamageTypes.PLAYER_ATTACK)
                || !(source.getAttacker() instanceof ServerPlayerEntity player)) {
            return;
        }
        ItemStack stack = source.getWeaponStack();
        if (stack == null || stack.isEmpty()) {
            stack = player.getMainHandStack();
        }
        StormstepEffect.onSuccessfulMeleeHit(player, stack);
    }
}
