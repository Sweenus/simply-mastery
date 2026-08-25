package net.sweenus.simplymastery.mastery.state;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.RunicForgeMasteryContext;
import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplyswords.api.AwakeningApi;
import net.sweenus.simplyswords.api.AwakeningFormRegistry;
import net.sweenus.simplyswords.item.component.AwakeningRouteComponent;
import net.sweenus.simplyswords.registry.ComponentTypeRegistry;
import net.sweenus.simplyswords.registry.ItemsRegistry;

import java.util.List;

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
        requireForgePreviewIdentity();
    }

    private static void requireForgePreviewIdentity() {
        MasteryProfileRegistry.Snapshot snapshot = new MasteryProfileRegistry.Snapshot(1, List.of(
                BuiltInFamilyProfiles.profile("awakened_lichblade"),
                BuiltInFamilyProfiles.profile("sunfire"),
                BuiltInFamilyProfiles.profile("harbinger")
        ));
        requireForgePreviewIdentity(snapshot, ItemsRegistry.SLUMBERING_LICHBLADE.get(),
                AwakeningFormRegistry.LICHBLADE_ROUTE, "awakened_lichblade");
        requireForgePreviewIdentity(snapshot, ItemsRegistry.DORMANT_RELIC.get(),
                AwakeningFormRegistry.SUN_ROUTE, "sunfire");
        requireForgePreviewIdentity(snapshot, ItemsRegistry.DORMANT_RELIC.get(),
                AwakeningFormRegistry.HARBINGER_ROUTE, "harbinger");
    }

    private static void requireForgePreviewIdentity(MasteryProfileRegistry.Snapshot snapshot,
                                                    Item item, Identifier route, String expectedProfile) {
        ItemStack stateStack = new ItemStack(item);
        stateStack.set(ComponentTypeRegistry.AWAKENING_ROUTE.get(), new AwakeningRouteComponent(route));
        AwakeningApi.setLevel(stateStack, 0);
        ItemStack previewStack = stateStack.copy();
        AwakeningApi.setLevel(previewStack, 8);
        if (snapshot.resolve(stateStack).isPresent()) {
            throw new IllegalStateException("Temporary forge stack must not expose " + expectedProfile);
        }
        Identifier actual = snapshot.resolve(RunicForgeMasteryContext.identityStack(stateStack, previewStack))
                .map(MasteryProfileRegistry.Resolution::profile)
                .map(MasteryProfile::id)
                .orElseThrow(() -> new IllegalStateException("Forge preview did not expose " + expectedProfile));
        Identifier expected = Identifier.of("simplymastery", expectedProfile);
        if (!expected.equals(actual)) {
            throw new IllegalStateException("Expected " + expected + " but resolved " + actual);
        }
    }
}
