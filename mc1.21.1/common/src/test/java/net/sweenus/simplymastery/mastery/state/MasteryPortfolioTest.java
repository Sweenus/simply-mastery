package net.sweenus.simplymastery.mastery.state;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.network.UnlockResult;
import net.sweenus.simplymastery.mastery.network.UnlockRules;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MasteryPortfolioTest {

    @Test
    void codecAndPacketRoundTripsPreservePortfolio() {
        MasteryPortfolio original = new MasteryPortfolio(1,
                List.of(new MasteryPortfolio.GroupProgress(id("watcher_claymore"), 240, 8)),
                List.of(new MasteryPortfolio.ProfileLoadout(id("watcher_claymore"), 2,
                        List.of("known", "unknown_from_newer_version"))), 12L);
        JsonElement encoded = MasteryPortfolio.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        assertEquals(original, MasteryPortfolio.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());

        RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), DynamicRegistryManager.EMPTY);
        try {
            MasteryPortfolio.PACKET_CODEC.encode(buffer, original);
            assertEquals(original, MasteryPortfolio.PACKET_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void legacyImportPreservesBaseOwnershipAndStartsFinalLoadoutEmpty() {
        MasteryProfile base = BuiltInFamilyProfiles.profile("watcher_claymore");
        MasteryProfile evolved = BuiltInFamilyProfiles.profile("the_devourer");
        String owned = base.nodes().getFirst().id();
        MasteryState legacy = new MasteryState(1, base.id(), 1, 175, 7,
                List.of(owned, "unknown_from_newer_version"), 9L);

        MasteryPortfolio portfolio = MasteryPortfolio.importLegacy(legacy, base.progressionGroupId(), 0)
                .reconcile(evolved, 0).reconcile(base, 0);
        MasteryState finalView = portfolio.activeView(evolved, 0);
        MasteryState baseView = portfolio.activeView(base, 0);

        assertEquals(175, finalView.masteryXp());
        assertEquals(7, finalView.earnedPoints());
        assertTrue(finalView.unlockedNodeIds().isEmpty());
        assertTrue(baseView.owns(owned));
        assertTrue(baseView.owns("unknown_from_newer_version"));
    }

    @Test
    void sharedXpAndIndependentRespecSurviveFormSwitching() {
        MasteryProfile base = BuiltInFamilyProfiles.profile("soulrender");
        MasteryProfile evolved = BuiltInFamilyProfiles.profile("soulstalker");
        MasteryPortfolio portfolio = MasteryPortfolio.initial(base.progressionGroupId(), 0)
                .reconcile(base, 0).reconcile(evolved, 0)
                .awardXp(base.progressionGroupId(), 0, 360, 21, 100, 25);

        MasteryState baseState = portfolio.activeView(base, 0).unlock(base.nodes().getFirst().id());
        portfolio = portfolio.withState(base, baseState, 0);
        MasteryState evolvedState = portfolio.activeView(evolved, 0).unlock(evolved.nodes().getFirst().id());
        portfolio = portfolio.withState(evolved, evolvedState, 0);
        long beforeRespec = portfolio.mutationRevision();
        portfolio = portfolio.withState(evolved, portfolio.activeView(evolved, 0).respec(), 0);

        assertEquals(portfolio.activeView(base, 0).masteryXp(), portfolio.activeView(evolved, 0).masteryXp());
        assertEquals(portfolio.activeView(base, 0).earnedPoints(), portfolio.activeView(evolved, 0).earnedPoints());
        assertTrue(portfolio.activeView(base, 0).owns(base.nodes().getFirst().id()));
        assertTrue(portfolio.activeView(evolved, 0).unlockedNodeIds().isEmpty());
        assertEquals(beforeRespec + 1L, portfolio.mutationRevision());
    }

    @Test
    void routeOutcomesKeepIndependentBuildsAndRejectStaleRevision() {
        MasteryProfile sunfire = BuiltInFamilyProfiles.profile("sunfire");
        MasteryProfile harbinger = BuiltInFamilyProfiles.profile("harbinger");
        MasteryPortfolio portfolio = MasteryPortfolio.initial(sunfire.progressionGroupId(), 9)
                .reconcile(sunfire, 9).reconcile(harbinger, 9);
        MasteryState sunState = portfolio.activeView(sunfire, 9).unlock(sunfire.nodes().getFirst().id());
        portfolio = portfolio.withState(sunfire, sunState, 9);
        long staleRevision = portfolio.mutationRevision();
        MasteryState harbingerState = portfolio.activeView(harbinger, 9)
                .unlock(harbinger.nodes().getFirst().id());
        portfolio = portfolio.withState(harbinger, harbingerState, 9);

        MasteryState restoredSun = portfolio.activeView(sunfire, 9);
        assertTrue(restoredSun.owns(sunfire.nodes().getFirst().id()));
        assertFalse(restoredSun.owns(harbinger.nodes().getFirst().id()));
        assertEquals(UnlockResult.STALE_STATE, UnlockRules.validate(sunfire, restoredSun,
                sunfire.nodes().get(1), staleRevision));
    }

    @Test
    void constructorBoundsGroupsAndLoadouts() {
        List<MasteryPortfolio.GroupProgress> groups = IntStream.range(0, MasteryPortfolio.MAX_GROUPS + 4)
                .mapToObj(index -> new MasteryPortfolio.GroupProgress(id("group_" + index), index, index))
                .toList();
        List<MasteryPortfolio.ProfileLoadout> loadouts = IntStream.range(0, MasteryPortfolio.MAX_LOADOUTS + 4)
                .mapToObj(index -> new MasteryPortfolio.ProfileLoadout(id("profile_" + index), 1, List.of()))
                .toList();
        MasteryPortfolio portfolio = new MasteryPortfolio(1, groups, loadouts, 0L);
        assertEquals(MasteryPortfolio.MAX_GROUPS, portfolio.groups().size());
        assertEquals(MasteryPortfolio.MAX_LOADOUTS, portfolio.loadouts().size());
    }

    @Test
    void grantPointsClampsAtMaximumAndZeroesBankedXp() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("soulrender");
        Identifier group = profile.progressionGroupId();
        MasteryPortfolio portfolio = MasteryPortfolio.initial(group, 0)
                .awardXp(group, 0, 250, 21, 100, 25);
        int bankedXp = portfolio.group(group).orElseThrow().masteryXp();
        assertTrue(bankedXp > 0);

        MasteryPortfolio granted = portfolio.grantPoints(group, 0, 3, 21);
        assertEquals(portfolio.group(group).orElseThrow().earnedPoints() + 3,
                granted.group(group).orElseThrow().earnedPoints());
        assertEquals(bankedXp, granted.group(group).orElseThrow().masteryXp());
        assertEquals(portfolio.mutationRevision() + 1L, granted.mutationRevision());

        MasteryPortfolio capped = portfolio.grantPoints(group, 0, 500, 21);
        assertEquals(21, capped.group(group).orElseThrow().earnedPoints());
        assertEquals(0, capped.group(group).orElseThrow().masteryXp());
    }

    @Test
    void withProgressReturnsSameInstanceWhenNothingMoves() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("sunfire");
        Identifier group = profile.progressionGroupId();
        MasteryPortfolio portfolio = MasteryPortfolio.initial(group, 4);

        assertSame(portfolio, portfolio.withProgress(group, 4, 0, 4, 21));
        MasteryPortfolio moved = portfolio.withProgress(group, 4, 180, 9, 21);
        assertEquals(180, moved.group(group).orElseThrow().masteryXp());
        assertEquals(9, moved.group(group).orElseThrow().earnedPoints());
        assertEquals(portfolio.mutationRevision() + 1L, moved.mutationRevision());
    }

    @Test
    void clearedProgressNeverLeavesUnaffordableNodesUnlocked() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("watcher_claymore");
        Identifier group = profile.progressionGroupId();
        MasteryPortfolio portfolio = MasteryPortfolio.initial(group, 0).reconcile(profile, 0)
                .awardXp(group, 0, 900, 21, 100, 25);
        MasteryState unlocked = portfolio.activeView(profile, 0).unlock(profile.nodes().getFirst().id());
        portfolio = portfolio.withState(profile, unlocked, 0);

        MasteryState cleared = portfolio.activeView(profile, 0).respec().withProgress(0, 0, 21);
        portfolio = portfolio.withState(profile, cleared, 0);
        MasteryState view = portfolio.activeView(profile, 0);

        assertEquals(0, view.masteryXp());
        assertEquals(0, view.earnedPoints());
        assertTrue(view.unlockedNodeIds().isEmpty());
        assertTrue(view.spentPoints(profile) <= view.earnedPoints());
    }

    private static Identifier id(String path) {
        return Identifier.of("simplymastery", path);
    }
}
