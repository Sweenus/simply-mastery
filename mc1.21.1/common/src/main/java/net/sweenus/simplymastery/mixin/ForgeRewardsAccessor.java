package net.sweenus.simplymastery.mixin;

import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RunicForgeScreenHandler.class)
public interface ForgeRewardsAccessor {
    @Invoker(value = "refreshPreview", remap = false)
    void simplymastery$refreshRewardsPreview();
}
