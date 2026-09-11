package net.sweenus.simplyswordsmastery.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import net.sweenus.simplyswordsmastery.client.mastery.MasteryItemComparison;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private ItemStack mainHand;
    @Shadow private ItemStack offHand;

    @Inject(method = "updateHeldItems", at = @At("HEAD"))
    private void simplyswordsmastery$refreshBookkeeping(CallbackInfo callback) {
        if (client.player == null) return;
        mainHand = MasteryItemComparison.refreshBookkeeping(mainHand, client.player.getMainHandStack());
        offHand = MasteryItemComparison.refreshBookkeeping(offHand, client.player.getOffHandStack());
    }
}
