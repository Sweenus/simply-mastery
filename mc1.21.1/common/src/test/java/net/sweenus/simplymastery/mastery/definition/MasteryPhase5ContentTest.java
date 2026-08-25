package net.sweenus.simplymastery.mastery.definition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MasteryPhase5ContentTest {

    @Test
    void completeCatalogMeetsReleasedTreeGate() {
        Map<Identifier, MasteryProfile> catalog = catalog();
        assertEquals(52, catalog.size());
        MasteryProfileValidator.validateRegistry(catalog.values().stream().toList(), 21);

        Set<Identifier> icons = new HashSet<>();
        Set<String> roots = new HashSet<>();
        for (MasteryProfile profile : catalog.values()) {
            int expectedVersion = profile.id().getPath().equals("storms_edge") ? 4
                    : profile.id().getPath().equals("brimstone_claymore")
                    || List.of("watcher_claymore", "the_devourer", "wickpiercer", "gloampiercer",
                    "wraithfang", "wraithmaw", "stormscale", "ionbound_stormscale", "soulrender",
                    "soulstalker", "whisperwind", "dreadwhisper", "awakened_lichblade", "sunfire",
                    "harbinger", "hearthflame", "emberblade", "emberlash", "flamewind",
                    "molten_edge", "soulpyre").contains(profile.id().getPath()) ? 3 : 2;
            assertEquals(expectedVersion, profile.version());
            assertEquals(3, profile.branches().size());
            assertEquals(27, profile.nodes().size());
            assertTrue(profile.migrations().stream().anyMatch(migration -> migration.fromVersion() == 1
                    && migration.toVersion() == 2 && migration.renamedNodes().size() >= 12));
            for (MasteryProfile.Branch branch : profile.branches()) {
                List<MasteryProfile.Node> nodes = profile.nodes().stream()
                        .filter(node -> node.branch().equals(branch.id())).toList();
                assertEquals(7, nodes.stream().filter(node -> !node.capstone()).count());
                assertEquals(2, nodes.stream().filter(MasteryProfile.Node::capstone).count());
                nodes.stream().filter(MasteryProfile.Node::capstone)
                        .forEach(node -> assertEquals(7, routeCost(profile, node)));
            }
            assertTrue(roots.add(profile.nodes().getFirst().id()));
            for (MasteryProfile.Node node : profile.nodes()) {
                assertFalse(node.effect().type().equals(Identifier.of("simplymastery", "none")));
                assertTrue(icons.add(node.icon().orElseThrow()));
            }
        }
        assertEquals(1_404, icons.size());
    }

    @Test
    void selectorsGiveEveryDeclaredFormOneStableProfile() {
        Map<Identifier, MasteryProfile> catalog = catalog();
        MasteryProfileRegistry.Snapshot snapshot = new MasteryProfileRegistry.Snapshot(1, catalog.values());
        Set<Identifier> itemSelectors = new HashSet<>();
        for (BuiltInFamilyProfiles.Family family : BuiltInFamilyProfiles.familiesForCoverage()) {
            Identifier expected = Identifier.of("simplymastery", family.profilePath());
            for (String item : family.items()) {
                Identifier itemId = Identifier.of("simplyswords", item);
                assertTrue(itemSelectors.add(itemId));
                assertEquals(expected, snapshot.resolveIds(itemId, null).orElseThrow().profile().id());
            }
            if (family.formFamily() != null) {
                Identifier familyId = Identifier.of("simplyswords", family.formFamily());
                assertEquals(expected, snapshot.resolveIds(Identifier.of("simplyswords", "unregistered_form"),
                        familyId).orElseThrow().profile().id());
            }
        }
        assertTrue(snapshot.resolveIds(Identifier.of("simplyswords", "iron_longsword"), null).isEmpty());
        assertTrue(snapshot.resolveIds(Identifier.of("simplyswords", "runic_longsword"), null).isEmpty());
    }

    @Test
    void everyReleasedTextKeyIsTranslated() throws Exception {
        JsonObject language;
        try (var stream = getClass().getResourceAsStream("/assets/simplymastery/lang/en_us.json")) {
            assertNotNull(stream);
            language = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
        for (MasteryProfile profile : catalog().values()) {
            assertTrue(language.has("profile.simplymastery." + profile.id().getPath()));
            profile.branches().forEach(branch -> assertTrue(language.has(branch.nameKey()), branch.nameKey()));
            profile.nodes().forEach(node -> {
                assertTrue(language.has(node.nameKey()), node.nameKey());
                assertTrue(language.has(node.descriptionKey()), node.descriptionKey());
            });
        }
    }

    @Test
    void balanceLedgerAccountsForEveryNode() {
        String ledger = MasteryBalanceReport.render(catalog().values().stream().toList());
        assertEquals(1_404, ledger.lines().filter(line -> line.startsWith("| `simplymastery:")).count());
        assertTrue(ledger.contains("Trigger frequency"));
        assertTrue(ledger.contains("Combat role"));
    }

    private static Map<Identifier, MasteryProfile> catalog() {
        Map<Identifier, MasteryProfile> profiles = new LinkedHashMap<>();
        BuiltInFamilyProfiles.addMissing(profiles);
        return profiles;
    }

    private static int routeCost(MasteryProfile profile, MasteryProfile.Node node) {
        Map<String, MasteryProfile.Node> nodes = new HashMap<>();
        profile.nodes().forEach(candidate -> nodes.put(candidate.id(), candidate));
        return routeCost(node, nodes, new HashSet<>());
    }

    private static int routeCost(MasteryProfile.Node node, Map<String, MasteryProfile.Node> nodes, Set<String> seen) {
        if (!seen.add(node.id())) return 0;
        int result = node.cost();
        for (String requirement : node.requires()) result += routeCost(nodes.get(requirement), nodes, seen);
        return result;
    }
}
