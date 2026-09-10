package net.sweenus.simplyswordsmastery.mastery.progression;

import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryCooldownState;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryPortfolio;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryState;

public final class PersonalProgression {
    private PersonalProgression() {
    }

    public static MasteryState read(ServerPlayerEntity player, MasteryProfile profile) {
        ProgressionOwner owner = owner(player, profile);
        ProgressionLedger ledger = ProgressionLedger.get(player.getServer());
        ProgressionLedger.Entry entry = ledger.entry(owner, 0, MasteryRewardService.initialPoints(profile.progressionGroupId()));
        MasteryPortfolio portfolio = reconcile(entry, profile);
        ledger.writePersonalState(owner, portfolio, entry.cooldowns());
        return portfolio.activeView(profile, MasteryRewardService.initialPoints(profile.progressionGroupId()));
    }

    public static void writeAllocations(ServerPlayerEntity player, MasteryProfile profile, MasteryState state) {
        if (!state.profileId().equals(profile.id())) throw new IllegalArgumentException("Mismatched mastery profile");
        ProgressionOwner owner = owner(player, profile);
        ProgressionLedger ledger = ProgressionLedger.get(player.getServer());
        ProgressionLedger.Entry entry = ledger.entry(owner, 0, MasteryRewardService.initialPoints(profile.progressionGroupId()));
        MasteryPortfolio portfolio = reconcile(entry, profile);
        MasteryState current = portfolio.activeView(profile, MasteryRewardService.initialPoints(profile.progressionGroupId()));
        MasteryState allocation = new MasteryState(current.schemaVersion(), profile.id(), profile.version(),
                current.masteryXp(), current.earnedPoints(), state.unlockedNodeIds(),
                Math.max(current.mutationRevision() + 1, state.mutationRevision()));
        portfolio = portfolio.withState(profile, allocation, MasteryRewardService.initialPoints(profile.progressionGroupId()))
                .reconcile(profile, MasteryRewardService.initialPoints(profile.progressionGroupId()), MasteryRewardService.cap(profile.progressionGroupId()));
        ledger.writePersonalState(owner, portfolio, entry.cooldowns());
    }

    public static boolean startCooldown(ServerPlayerEntity player, MasteryProfile profile,
                                         String effectKey, long tick, int durationTicks) {
        ProgressionOwner owner = owner(player, profile);
        ProgressionLedger ledger = ProgressionLedger.get(player.getServer());
        ProgressionLedger.Entry entry = ledger.entry(owner, 0, MasteryRewardService.initialPoints(profile.progressionGroupId()));
        if (effectKey == null || effectKey.isBlank() || effectKey.length() > 96) {
            throw new IllegalArgumentException("Invalid personal mastery cooldown");
        }
        if (!entry.cooldowns().ready(effectKey, tick)) return false;
        if (durationTicks <= 0) return true;
        MasteryCooldownState updated = entry.cooldowns().start(effectKey,
                tick > Long.MAX_VALUE - durationTicks ? Long.MAX_VALUE : tick + durationTicks);
        ledger.writePersonalState(owner, reconcile(entry, profile), updated);
        return true;
    }

    private static ProgressionOwner owner(ServerPlayerEntity player, MasteryProfile profile) {
        return ProgressionOwner.player(player.getUuid(), profile.progressionGroupId());
    }

    private static MasteryPortfolio reconcile(ProgressionLedger.Entry entry, MasteryProfile profile) {
        MasteryPortfolio portfolio = entry.personalPortfolio();
        if (portfolio == null) {
            portfolio = MasteryPortfolio.initial(profile.progressionGroupId(), MasteryRewardService.initialPoints(profile.progressionGroupId()));
        }
        return portfolio.withProgress(profile.progressionGroupId(), MasteryRewardService.initialPoints(profile.progressionGroupId()),
                        (int) Math.min(Integer.MAX_VALUE, entry.xp()), entry.points(), MasteryRewardService.cap(profile.progressionGroupId()))
                .reconcile(profile, MasteryRewardService.initialPoints(profile.progressionGroupId()), MasteryRewardService.cap(profile.progressionGroupId()));
    }
}
