package net.sweenus.simplymastery.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.sweenus.simplymastery.mastery.effect.SkillRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    @Inject(method = "damage", at = @At("RETURN"))
    private void simplymastery$afterDamage(DamageSource source, float amount,
                                           CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ()) SkillRuntime.onDamageApplied((LivingEntity) (Object) this, source, amount);
    }
}
