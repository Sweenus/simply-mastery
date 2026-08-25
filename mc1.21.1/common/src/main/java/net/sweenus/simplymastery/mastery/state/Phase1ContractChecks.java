package net.sweenus.simplymastery.mastery.state;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;

public final class Phase1ContractChecks {

    private Phase1ContractChecks() {
    }

    public static void requireFormProgressionFoundation() {
        if (BuiltInFamilyProfiles.familiesForCoverage().size() != 52) {
            throw new IllegalStateException("Expected 52 visible mastery profiles");
        }
        MasteryProfile base = BuiltInFamilyProfiles.profile("watcher_claymore");
        MasteryProfile evolved = BuiltInFamilyProfiles.profile("the_devourer");
        if (!base.progressionGroupId().equals(evolved.progressionGroupId())) {
            throw new IllegalStateException("Split forms must share progression");
        }
        MasteryPortfolio portfolio = MasteryPortfolio.initial(base.progressionGroupId(), 7)
                .reconcile(base, 7).reconcile(evolved, 7);
        portfolio = portfolio.withState(base,
                portfolio.activeView(base, 7).unlock(base.nodes().getFirst().id()), 7);
        if (!portfolio.activeView(evolved, 7).unlockedNodeIds().isEmpty()
                || portfolio.activeView(evolved, 7).earnedPoints() != 7) {
            throw new IllegalStateException("Split form loadouts must remain independent");
        }
        if (BuiltInFamilyProfiles.bankingGroup(Identifier.of("simplyswords", "decaying_relic")).isEmpty()) {
            throw new IllegalStateException("Decaying Relic must bank progression invisibly");
        }
    }
}
