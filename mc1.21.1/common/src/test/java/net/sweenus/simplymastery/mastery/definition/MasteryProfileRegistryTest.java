package net.sweenus.simplymastery.mastery.definition;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MasteryProfileRegistryTest {

    private static final Identifier SOURCE = Identifier.of("simplymastery",
            "simplymastery/weapon_skills/storms_edge.json");

    @Test
    void exactSelectorWinsBeforeHigherPriorityFamilySelector() {
        MasteryProfile source = ProfileTestFixtures.stormsEdge();
        Identifier family = Identifier.of("simplyswords", "storm_family");
        MasteryProfile familyProfile = new MasteryProfile(source.schema(), Identifier.of("simplymastery", "family"),
                source.version(), List.of(new MasteryProfile.Selector(Optional.empty(), Optional.of(family), 999)),
                source.branches(), source.nodes(), source.migrations());
        MasteryProfileRegistry.Snapshot snapshot = new MasteryProfileRegistry.Snapshot(4,
                List.of(source, familyProfile));
        assertEquals(source.id(), snapshot.resolveIds(Identifier.of("simplyswords", "storms_edge"), family)
                .orElseThrow().profile().id());
    }

    @Test
    void invalidReloadRetainsLastKnownGoodProfile() {
        MasteryProfileRegistry.ReloadResult first = MasteryProfileRegistry.reload(
                Map.of(SOURCE, ProfileTestFixtures.stormsEdgeJson()));
        int epoch = first.snapshot().epoch();
        JsonObject invalid = ProfileTestFixtures.stormsEdgeJson().getAsJsonObject().deepCopy();
        invalid.getAsJsonArray("nodes").get(0).getAsJsonObject().addProperty("x", 2.0);
        MasteryProfileRegistry.ReloadResult second = MasteryProfileRegistry.reload(Map.of(SOURCE, invalid));
        assertTrue(second.installed());
        assertTrue(second.errors().stream().anyMatch(error -> error.contains("coordinates")));
        assertEquals(epoch + 1, second.snapshot().epoch());
        assertEquals(0.88, second.snapshot().profiles().get(Identifier.of("simplymastery", "storms_edge"))
                .node("guard_root").orElseThrow().x());
    }

    @Test
    void noLastGoodDisablesOnlyInvalidResource() {
        JsonObject invalid = ProfileTestFixtures.stormsEdgeJson().getAsJsonObject().deepCopy();
        invalid.addProperty("id", "simplymastery:new_invalid");
        invalid.getAsJsonArray("branches").remove(0);
        MasteryProfileRegistry.ReloadResult result = MasteryProfileRegistry.reload(
                Map.of(Identifier.of("simplymastery", "simplymastery/weapon_skills/new_invalid.json"), invalid));
        assertTrue(result.installed());
        assertFalse(result.snapshot().profiles().containsKey(Identifier.of("simplymastery", "new_invalid")));
    }

    @Test
    void currentExactCatalogHasOneWinnerAndOrdinaryWeaponsHaveNone() {
        Map<Identifier, MasteryProfile> profiles = new LinkedHashMap<>();
        MasteryProfile storms = ProfileTestFixtures.stormsEdge();
        profiles.put(storms.id(), storms);
        BuiltInFamilyProfiles.addMissing(profiles);
        MasteryProfileRegistry.Snapshot snapshot = new MasteryProfileRegistry.Snapshot(1, profiles.values());
        assertEquals(storms.id(), snapshot.resolveIds(Identifier.of("simplyswords", "storms_edge"), null)
                .orElseThrow().profile().id());
        for (BuiltInFamilyProfiles.Family family : BuiltInFamilyProfiles.familiesForCoverage()) {
            Identifier expected = Identifier.of("simplymastery", family.profilePath());
            Identifier familyId = family.formFamily() == null ? null
                    : Identifier.of("simplyswords", family.formFamily());
            for (String item : family.items()) {
                assertEquals(expected, snapshot.resolveIds(Identifier.of("simplyswords", item), familyId)
                        .orElseThrow().profile().id());
            }
        }
        assertTrue(snapshot.resolveIds(Identifier.of("simplyswords", "iron_longsword"), null).isEmpty());
        assertTrue(snapshot.resolveIds(Identifier.of("simplyswords", "runic_longsword"), null).isEmpty());
        assertTrue(snapshot.resolveIds(Identifier.of("simplyswords", "diamond_claymore"), null).isEmpty());
    }
}
