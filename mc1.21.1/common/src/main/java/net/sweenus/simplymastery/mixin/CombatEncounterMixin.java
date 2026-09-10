package net.sweenus.simplymastery.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.sweenus.simplymastery.mastery.combat.CombatEncounter;
import net.sweenus.simplymastery.mastery.combat.EncounterCarrier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class CombatEncounterMixin implements EncounterCarrier {
    @Unique private CombatEncounter simplymastery$encounter;
    @Override public CombatEncounter simplymastery$getEncounter() { return simplymastery$encounter; }
    @Override public void simplymastery$setEncounter(CombatEncounter value) { simplymastery$encounter = value; }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void simplymastery$save(NbtCompound nbt, CallbackInfo ci) {
        if (simplymastery$encounter != null) nbt.put("simplymastery_encounter", simplymastery$encounter.write());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void simplymastery$load(NbtCompound nbt, CallbackInfo ci) {
        simplymastery$encounter = nbt.contains("simplymastery_encounter")
                ? CombatEncounter.read(nbt.getCompound("simplymastery_encounter")) : null;
    }
}
