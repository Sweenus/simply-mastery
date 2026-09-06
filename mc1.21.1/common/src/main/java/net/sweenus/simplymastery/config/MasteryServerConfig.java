package net.sweenus.simplymastery.config;

import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedSet;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;

import java.util.List;

public final class MasteryServerConfig extends me.fzzyhmstrs.fzzy_config.config.Config {

    public boolean enabled = true;
    @ValidatedInt.Restrict(min = 1, max = 64)
    public int maximumEarnedPoints = 20;
    @ValidatedInt.Restrict(min = 0, max = 18)
    public int verticalSliceStartingPoints = 0;
    @ValidatedInt.Restrict(min = 1, max = 10000)
    public int xpBaseRequirement = 100;
    @ValidatedInt.Restrict(min = 0, max = 10000)
    public int xpRequirementGrowth = 35;
    @ValidatedInt.Restrict(min = 0, max = 1000)
    public int killBaseXp = 10;
    @ValidatedInt.Restrict(min = 0, max = 100)
    public int maxHealthXpPerTwenty = 2;
    @ValidatedInt.Restrict(min = 1, max = 1000)
    public int maximumKillXp = 100;
    @ValidatedInt.Restrict(min = 0, max = 1000)
    public int hostilePercent = 125;
    @ValidatedInt.Restrict(min = 0, max = 1000)
    public int bossPercent = 250;
    public boolean allowPassiveMobXp = false;
    public boolean allowPvpXp = false;
    @ValidatedInt.Restrict(min = 0, max = 72000)
    public int antiFarmWindowTicks = 1200;
    @ValidatedInt.Restrict(min = 1, max = 100)
    public int antiFarmFullValueKills = 4;
    @ValidatedInt.Restrict(min = 0, max = 100)
    public int antiFarmRepeatedPercent = 20;
    public ValidatedSet<Identifier> excludedEntityIds = new ValidatedIdentifier().toSet(
            List.of(Identifier.of("simplyswords", "training_dummy")));
    public boolean respecEnabled = true;
    public boolean respecUsesTag = false;
    public ValidatedIdentifier respecCostItem = new ValidatedIdentifier("simplyswords:runic_tablet");
    public ValidatedIdentifier respecCostTag = new ValidatedIdentifier(Identifier.of("c", "runic_tablets"));
    @ValidatedInt.Restrict(min = 1, max = 64)
    public int respecCostCount = 3;
    public boolean creativeRespecIsFree = true;
    public ValidatedSet<Identifier> disabledProfiles = new ValidatedIdentifier().toSet(List.of());
    public ValidatedSet<Identifier> disabledEffects = new ValidatedIdentifier().toSet(List.of());
    @ValidatedInt.Restrict(min = 5, max = 200)
    public int stormstepDurationTicks = 40;
    @ValidatedInt.Restrict(min = 0, max = 4)
    public int stormstepAmplifier = 1;

    public MasteryServerConfig() {
        super(Identifier.of(SimplyMastery.MOD_ID, "server"));
    }
}
