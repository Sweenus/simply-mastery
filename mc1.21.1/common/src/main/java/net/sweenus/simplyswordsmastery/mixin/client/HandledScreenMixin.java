package net.sweenus.simplyswordsmastery.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.sweenus.simplyswordsmastery.client.mastery.MasteryScreenSwitch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Inject(method = "removed", at = @At("HEAD"), cancellable = true)
    private void simplyswordsmastery$preserveHandlerDuringSwitch(CallbackInfo callback) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        if (MasteryScreenSwitch.isSwitching(screen.getScreenHandler())) {
            callback.cancel();
        }
    }
}
