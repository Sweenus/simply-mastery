package net.sweenus.simplymastery.config;

import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;

public final class MasteryServerConfig extends me.fzzyhmstrs.fzzy_config.config.Config {

    public boolean enabled = true;
    @ValidatedInt.Restrict(min = 1, max = 64)
    public int maximumEarnedPoints = 21;
    @ValidatedInt.Restrict(min = 0, max = 21)
    public int verticalSliceStartingPoints = 6;
    @ValidatedInt.Restrict(min = 5, max = 200)
    public int stormstepDurationTicks = 40;
    @ValidatedInt.Restrict(min = 0, max = 4)
    public int stormstepAmplifier = 1;

    public MasteryServerConfig() {
        super(Identifier.of(SimplyMastery.MOD_ID, "server"));
    }
}
