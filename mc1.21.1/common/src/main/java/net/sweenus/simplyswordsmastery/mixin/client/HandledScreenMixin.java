package net.sweenus.simplyswordsmastery.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.sweenus.simplyswordsmastery.client.mastery.MasteryScreenSwitch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @WrapOperation(method = "removed", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/screen/ScreenHandler;onClosed(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void simplyswordsmastery$keepHandlerDuringSwitch(
            ScreenHandler handler, PlayerEntity player, Operation<Void> original) {
        if (!MasteryScreenSwitch.isSwitching(handler)) {
            original.call(handler, player);
        }
    }
}
