package net.sweenus.simplymastery.config;

import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;

public final class MasteryClientConfig extends me.fzzyhmstrs.fzzy_config.config.Config {

    public boolean reducedMotion = false;
    @ValidatedFloat.Restrict(min = 0.0F, max = 1.0F)
    public float motionIntensity = 1.0F;
    @ValidatedFloat.Restrict(min = 0.2F, max = 0.95F)
    public float backgroundDim = 0.72F;
    @ValidatedFloat.Restrict(min = 0.0F, max = 1.0F)
    public float uiSoundVolume = 0.6F;
    @ValidatedFloat.Restrict(min = 0.0F, max = 1.0F)
    public float auraParticleDensity = 0.75F;
    @ValidatedFloat.Restrict(min = 0.25F, max = 2.0F)
    public float zoomSensitivity = 1.0F;
    public boolean confirmUnlocks = false;

    public MasteryClientConfig() {
        super(Identifier.of(SimplyMastery.MOD_ID, "client"));
    }
}
