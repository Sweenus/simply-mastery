package net.sweenus.simplyswordsmastery.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.sweenus.simplyswordsmastery.client.mastery.MasteryItemComparison;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftClient.class, priority = 900)
public abstract class BetterCombatItemComparisonMixin {
    @Dynamic
    @Inject(method = "areItemStackEqual", at = @At("RETURN"), remap = false, require = 0, cancellable = true)
    private static void simplyswordsmastery$ignoreBookkeeping(ItemStack previous, ItemStack current,
                                                            CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ() && MasteryItemComparison.equivalent(previous, current)) {
            callback.setReturnValue(true);
        }
    }
}
