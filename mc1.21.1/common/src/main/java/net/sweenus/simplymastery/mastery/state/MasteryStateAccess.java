package net.sweenus.simplymastery.mastery.state;

import net.minecraft.item.ItemStack;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;

import net.minecraft.util.Identifier;

public final class MasteryStateAccess {

    private MasteryStateAccess() {
    }

    public static MasteryState read(ItemStack stack, MasteryProfile profile, int initialPoints) {
        MasteryPortfolio portfolio = stack.get(MasteryComponents.MASTERY_PORTFOLIO.get());
        if (portfolio == null) {
            portfolio = MasteryPortfolio.importLegacy(stack.get(MasteryComponents.MASTERY_STATE.get()),
                    profile.progressionGroupId(), initialPoints);
        }
        MasteryPortfolio reconciled = portfolio.reconcile(profile, initialPoints);
        MasteryState state = reconciled.activeView(profile, initialPoints);
        if (!reconciled.equals(stack.get(MasteryComponents.MASTERY_PORTFOLIO.get()))) {
            stack.set(MasteryComponents.MASTERY_PORTFOLIO.get(), reconciled);
        }
        if (!state.equals(stack.get(MasteryComponents.MASTERY_STATE.get()))) {
            stack.set(MasteryComponents.MASTERY_STATE.get(), state);
        }
        return state;
    }

    public static void write(ItemStack stack, MasteryState state) {
        MasteryProfile profile = MasteryProfileRegistry.server().profiles().get(state.profileId());
        if (profile == null) profile = MasteryProfileRegistry.client().profiles().get(state.profileId());
        if (profile == null) {
            stack.set(MasteryComponents.MASTERY_STATE.get(), state);
            return;
        }
        write(stack, profile, state);
    }

    public static void write(ItemStack stack, MasteryProfile profile, MasteryState state) {
        MasteryPortfolio portfolio = stack.get(MasteryComponents.MASTERY_PORTFOLIO.get());
        if (portfolio == null) {
            portfolio = MasteryPortfolio.importLegacy(stack.get(MasteryComponents.MASTERY_STATE.get()),
                    profile.progressionGroupId(), state.earnedPoints());
        }
        MasteryPortfolio updated = portfolio.withState(profile, state, state.earnedPoints());
        stack.set(MasteryComponents.MASTERY_PORTFOLIO.get(), updated);
        stack.set(MasteryComponents.MASTERY_STATE.get(), updated.activeView(profile, state.earnedPoints()));
    }

    public static boolean bankXp(ItemStack stack, Identifier progressionGroup, int initialPoints, int amount,
                                 int maximumPoints, int baseRequirement, int requirementGrowth) {
        MasteryPortfolio portfolio = ensurePortfolio(stack, progressionGroup, initialPoints);
        return applyPortfolio(stack, portfolio, portfolio.awardXp(progressionGroup, initialPoints, amount,
                maximumPoints, baseRequirement, requirementGrowth));
    }

    public static boolean grantPoints(ItemStack stack, Identifier progressionGroup, int initialPoints,
                                     int amount, int maximumPoints) {
        MasteryPortfolio portfolio = ensurePortfolio(stack, progressionGroup, initialPoints);
        return applyPortfolio(stack, portfolio,
                portfolio.grantPoints(progressionGroup, initialPoints, amount, maximumPoints));
    }

    public static boolean setProgress(ItemStack stack, Identifier progressionGroup, int initialPoints,
                                      int masteryXp, int earnedPoints, int maximumPoints) {
        MasteryPortfolio portfolio = ensurePortfolio(stack, progressionGroup, initialPoints);
        return applyPortfolio(stack, portfolio, portfolio.withProgress(progressionGroup, initialPoints,
                masteryXp, earnedPoints, maximumPoints));
    }

    public static MasteryPortfolio portfolio(ItemStack stack) {
        return stack.get(MasteryComponents.MASTERY_PORTFOLIO.get());
    }

    public static MasteryCooldownState cooldowns(ItemStack stack) {
        return stack.getOrDefault(MasteryComponents.MASTERY_COOLDOWNS.get(), MasteryCooldownState.EMPTY);
    }

    public static void writeCooldowns(ItemStack stack, MasteryCooldownState state) {
        if (state.deadlines().isEmpty()) stack.remove(MasteryComponents.MASTERY_COOLDOWNS.get());
        else stack.set(MasteryComponents.MASTERY_COOLDOWNS.get(), state);
    }

    public static MasteryRuntimeState runtime(ItemStack stack) {
        return stack.getOrDefault(MasteryComponents.MASTERY_RUNTIME.get(), MasteryRuntimeState.EMPTY);
    }

    public static void writeRuntime(ItemStack stack, MasteryRuntimeState state) {
        if (state.values().isEmpty()) stack.remove(MasteryComponents.MASTERY_RUNTIME.get());
        else stack.set(MasteryComponents.MASTERY_RUNTIME.get(), state);
    }

    private static MasteryPortfolio ensurePortfolio(ItemStack stack, Identifier progressionGroup,
                                                    int initialPoints) {
        MasteryPortfolio portfolio = stack.get(MasteryComponents.MASTERY_PORTFOLIO.get());
        return portfolio != null ? portfolio
                : MasteryPortfolio.importLegacy(stack.get(MasteryComponents.MASTERY_STATE.get()),
                        progressionGroup, initialPoints);
    }

    private static boolean applyPortfolio(ItemStack stack, MasteryPortfolio previous, MasteryPortfolio updated) {
        stack.set(MasteryComponents.MASTERY_PORTFOLIO.get(), updated);
        stack.remove(MasteryComponents.MASTERY_STATE.get());
        return !updated.equals(previous);
    }
}
