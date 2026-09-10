package net.sweenus.simplyswordsmastery.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.sweenus.simplyswordsmastery.mastery.combat.CombatAccounting;
import net.sweenus.simplyswordsmastery.mastery.effect.SkillRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    @Inject(method = "damage", at = @At("RETURN"))
    private void simplyswordsmastery$afterDamage(DamageSource source, float amount,
                                           CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ()) SkillRuntime.onDamageApplied((LivingEntity) (Object) this, source, amount);
    }

    @WrapMethod(method = "damage")
    private boolean simplyswordsmastery$account(DamageSource source, float amount,
            Operation<Boolean> original) {
        LivingEntity target = (LivingEntity) (Object) this;
        if (target.getWorld().isClient()) return original.call(source, amount);
        var frame = CombatAccounting.begin(target, source);
        try {
            return original.call(source, amount);
        } finally {
            CombatAccounting.finish(frame);
        }
    }

    @WrapMethod(method = "setHealth")
    private void simplyswordsmastery$health(float health, Operation<Void> original) {
        LivingEntity target = (LivingEntity) (Object) this;
        float before = target.getHealth();
        original.call(health);
        if (!target.getWorld().isClient()) CombatAccounting
                .healthChanged(target, before, target.getHealth());
    }
}
