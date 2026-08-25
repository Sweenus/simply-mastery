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
    void exactStageWinsBeforeExactItemAndFamilySelectors() {
        MasteryProfile source = ProfileTestFixtures.stormsEdge();
        Identifier item = Identifier.of("simplyswords", "shared_item");
        Identifier stage = Identifier.of("simplyswords", "final_stage");
        Identifier family = Identifier.of("simplyswords", "shared_family");
        MasteryProfile itemProfile = new MasteryProfile(source.schema(), Identifier.of("simplymastery", "item"),
                source.version(), List.of(new MasteryProfile.Selector(Optional.of(item), Optional.empty(), 999)),
                source.branches(), source.nodes(), source.migrations());
        MasteryProfile stageProfile = new MasteryProfile(source.schema(), Identifier.of("simplymastery", "stage"),
                source.version(), List.of(new MasteryProfile.Selector(Optional.empty(), Optional.of(stage),
                Optional.empty(), -999)), source.branches(), source.nodes(), source.migrations());
        MasteryProfile familyProfile = new MasteryProfile(source.schema(), Identifier.of("simplymastery", "family"),
                source.version(), List.of(new MasteryProfile.Selector(Optional.empty(), Optional.of(family), 9999)),
                source.branches(), source.nodes(), source.migrations());
        MasteryProfileRegistry.Snapshot snapshot = new MasteryProfileRegistry.Snapshot(1,
                List.of(itemProfile, stageProfile, familyProfile));
        assertEquals(stageProfile.id(), snapshot.resolveIds(item, stage, family).orElseThrow().profile().id());
        assertEquals(MasteryProfileRegistry.SelectorKind.EXACT_FORM_STAGE,
                snapshot.resolveIds(item, stage, family).orElseThrow().kind());
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
        assertEquals(0.90, second.snapshot().profiles().get(Identifier.of("simplymastery", "storms_edge"))
                .node("storms_edge_signature_opening").orElseThrow().x());
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
        assertEquals(52, snapshot.profiles().size());
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
        assertTrue(snapshot.resolveIds(Identifier.of("simplyswords", "slumbering_lichblade"),
                Identifier.of("simplyswords", "slumbering_lichblade"),
                Identifier.of("simplyswords", "slumbering_lichblade")).isEmpty());
        assertTrue(snapshot.resolveIds(Identifier.of("simplyswords", "waking_lichblade"),
                Identifier.of("simplyswords", "waking_lichblade"),
                Identifier.of("simplyswords", "slumbering_lichblade")).isEmpty());
        assertTrue(snapshot.resolveIds(Identifier.of("simplyswords", "dormant_relic"),
                Identifier.of("simplyswords", "dormant_relic"),
                Identifier.of("simplyswords", "dormant_relic")).isEmpty());
        assertTrue(snapshot.resolveIds(Identifier.of("simplyswords", "righteous_relic"),
                Identifier.of("simplyswords", "righteous_relic"),
                Identifier.of("simplyswords", "dormant_relic")).isEmpty());
        assertTrue(snapshot.resolveIds(Identifier.of("simplyswords", "tainted_relic"),
                Identifier.of("simplyswords", "tainted_relic"),
                Identifier.of("simplyswords", "dormant_relic")).isEmpty());
        assertTrue(snapshot.resolveIds(Identifier.of("simplyswords", "decaying_relic"), null).isEmpty());
        assertEquals(Identifier.of("simplymastery", "awakened_lichblade"),
                snapshot.resolveIds(Identifier.of("simplyswords", "slumbering_lichblade"),
                        Identifier.of("simplyswords", "awakened_lichblade"),
                        Identifier.of("simplyswords", "slumbering_lichblade"))
                        .orElseThrow().profile().id());
        assertEquals(Identifier.of("simplymastery", "sunfire"),
                snapshot.resolveIds(Identifier.of("simplyswords", "dormant_relic"),
                        Identifier.of("simplyswords", "sunfire"),
                        Identifier.of("simplyswords", "dormant_relic"))
                        .orElseThrow().profile().id());
        assertEquals(Identifier.of("simplymastery", "harbinger"),
                snapshot.resolveIds(Identifier.of("simplyswords", "dormant_relic"),
                        Identifier.of("simplyswords", "harbinger"),
                        Identifier.of("simplyswords", "dormant_relic"))
                        .orElseThrow().profile().id());
    }

    @Test
    void stormsEdgeVersionFourContainsTheCompleteAuthoredTree() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("storms_edge");

        assertEquals(4, profile.version());
        assertTrue(profile.migrations().stream().anyMatch(migration -> migration.fromVersion() == 3
                && migration.toVersion() == 4));
        List<String> expected = List.of("stormbreak_conduit", "slipstream", "crosswind", "capacitor",
                "storm_chaser", "flashguard", "afterimage", "eye_of_storm", "thunderhead",
                "static_reserve", "charged_pursuit", "building_voltage", "live_wire", "feedback_loop",
                "quickening_current", "unbroken_pace", "perpetual_motion", "flashover", "ionize",
                "arc_lash", "pressure_drop", "updraft", "fulmination", "stormshield", "reverberation",
                "judgment_bolt", "supercell");
        assertEquals(expected, profile.nodes().stream().map(node -> node.effect().type().getPath()).toList());
    }

    @Test
    void brimstoneVersionThreeContainsTheCompleteAuthoredTree() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("brimstone_claymore");

        assertEquals(3, profile.version());
        assertTrue(profile.migrations().stream().anyMatch(migration -> migration.fromVersion() == 2
                && migration.toVersion() == 3));
        List<String> expected = List.of("sulfurous_edge", "scorching_brand", "blast_furnace",
                "kindling_blows", "flashpoint", "cinder_scatter", "backdraft", "chain_reaction",
                "crucible_strike", "lengthened_chain", "furnace_bellows", "stoked_furnace",
                "shackling_heat", "overpressure", "snapback", "molten_wake", "executioners_drop",
                "perpetual_furnace", "cinder_mantle", "tempered_flesh", "heat_sink",
                "furnace_reprisal", "forged_resolve", "ashen_step", "bulwark_pulse", "walking_furnace",
                "last_reprisal");
        assertEquals(expected, profile.nodes().stream().map(node -> node.effect().type().getPath()).toList());
    }
}
