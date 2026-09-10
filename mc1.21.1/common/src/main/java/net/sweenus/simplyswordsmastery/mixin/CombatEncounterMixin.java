package net.sweenus.simplyswordsmastery.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.sweenus.simplyswordsmastery.mastery.combat.CombatEncounter;
import net.sweenus.simplyswordsmastery.mastery.combat.EncounterCarrier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class CombatEncounterMixin implements EncounterCarrier {
    @Unique private CombatEncounter simplyswordsmastery$encounter;
    @Override public CombatEncounter simplyswordsmastery$getEncounter() { return simplyswordsmastery$encounter; }
    @Override public void simplyswordsmastery$setEncounter(CombatEncounter value) { simplyswordsmastery$encounter = value; }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void simplyswordsmastery$save(NbtCompound nbt, CallbackInfo ci) {
        if (simplyswordsmastery$encounter != null) nbt.put("simplyswordsmastery_encounter", simplyswordsmastery$encounter.write());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void simplyswordsmastery$load(NbtCompound nbt, CallbackInfo ci) {
        simplyswordsmastery$encounter = nbt.contains("simplyswordsmastery_encounter")
                ? CombatEncounter.read(nbt.getCompound("simplyswordsmastery_encounter")) : null;
    }
}
