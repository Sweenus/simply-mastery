package net.sweenus.simplymastery.mixin;

import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.ServerAdvancementLoader;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(DataPackContents.class)
public abstract class DataPackContentsMasteryMixin {
    @Shadow @Final private TagManagerLoader registryTagManager;
    @Shadow @Final private ServerAdvancementLoader serverAdvancementLoader;
    @Unique private MasteryProfileReloadListener simplymastery$reload;

    @Inject(method = "getContents", at = @At("RETURN"), cancellable = true)
    private void simplymastery$appendReload(CallbackInfoReturnable<List<ResourceReloader>> cir) {
        if (simplymastery$reload == null) {
            simplymastery$reload = new MasteryProfileReloadListener(registryTagManager, serverAdvancementLoader);
        }
        List<ResourceReloader> reloaders = new ArrayList<>(cir.getReturnValue());
        reloaders.add(simplymastery$reload);
        cir.setReturnValue(List.copyOf(reloaders));
    }

    @Inject(method = "refresh", at = @At("TAIL"))
    private void simplymastery$publishReload(CallbackInfo ci) {
        if (simplymastery$reload != null) simplymastery$reload.publish();
    }
}
