package net.sweenus.simplyswordsmastery.mixin;

import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.ServerAdvancementLoader;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfileReloadListener;
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
    @Unique private MasteryProfileReloadListener simplyswordsmastery$reload;

    @Inject(method = "getContents", at = @At("RETURN"), cancellable = true)
    private void simplyswordsmastery$appendReload(CallbackInfoReturnable<List<ResourceReloader>> cir) {
        if (simplyswordsmastery$reload == null) {
            simplyswordsmastery$reload = new MasteryProfileReloadListener(registryTagManager, serverAdvancementLoader);
        }
        List<ResourceReloader> reloaders = new ArrayList<>(cir.getReturnValue());
        reloaders.add(simplyswordsmastery$reload);
        cir.setReturnValue(List.copyOf(reloaders));
    }

    @Inject(method = "refresh", at = @At("TAIL"))
    private void simplyswordsmastery$publishReload(CallbackInfo ci) {
        if (simplyswordsmastery$reload != null) simplyswordsmastery$reload.publish();
    }
}
