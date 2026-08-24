package net.sweenus.simplymastery.mastery.definition;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BuiltInFamilyProfiles {

    private static final List<Family> FAMILIES = families();

    private BuiltInFamilyProfiles() {
    }

    public static void addMissing(Map<Identifier, MasteryProfile> profiles) {
        for (Family family : FAMILIES) {
            Identifier profileId = id("simplymastery", family.profilePath());
            profiles.putIfAbsent(profileId, create(profileId, family));
        }
    }

    public static Collection<Family> familiesForCoverage() {
        return FAMILIES;
    }

    private static MasteryProfile create(Identifier profileId, Family family) {
        List<MasteryProfile.Selector> selectors = new ArrayList<>();
        for (String item : family.items()) {
            selectors.add(new MasteryProfile.Selector(Optional.of(id("simplyswords", item)), Optional.empty(), 10));
        }
        if (family.formFamily() != null) {
            selectors.add(new MasteryProfile.Selector(Optional.empty(),
                    Optional.of(id("simplyswords", family.formFamily())), 0));
        }
        List<MasteryProfile.Branch> branches = List.of(
                new MasteryProfile.Branch("guard", "branch.simplymastery.guard", 0xFF72D9E8),
                new MasteryProfile.Branch("tempo", "branch.simplymastery.tempo", 0xFFA984F4),
                new MasteryProfile.Branch("storm", "branch.simplymastery.storm", 0xFFE8B65A)
        );
        List<MasteryProfile.Node> nodes = new ArrayList<>();
        addBranch(nodes, "guard", .22);
        addBranch(nodes, "tempo", .50);
        addBranch(nodes, "storm", .78);
        return new MasteryProfile(1, profileId, 1, selectors, branches, nodes, List.of());
    }

    private static void addBranch(List<MasteryProfile.Node> nodes, String branch, double y) {
        String root = branch + "_root";
        String path = branch + "_path";
        String group = branch + "_capstone";
        nodes.add(node(root, branch, .88, y, 1, false, "", List.of()));
        nodes.add(node(path, branch, .56, y, 1, false, "", List.of(root)));
        nodes.add(node(branch + "_capstone_a", branch, .16, y - .05, 2, true, group, List.of(path)));
        nodes.add(node(branch + "_capstone_b", branch, .16, y + .05, 2, true, group, List.of(path)));
    }

    private static MasteryProfile.Node node(String id, String branch, double x, double y, int cost,
                                            boolean capstone, String group, List<String> requires) {
        return new MasteryProfile.Node(id, branch, x, y, cost, capstone, group, requires,
                new MasteryProfile.Effect(id("simplymastery", "none"), Map.of()),
                "skill.simplymastery.placeholder", "skill.simplymastery.placeholder.description", Optional.empty());
    }

    private static List<Family> families() {
        Map<String, Family> grouped = new LinkedHashMap<>();
        add(grouped, "watcher_claymore", "watcher_claymore", "watcher_claymore", "the_devourer");
        add(grouped, "stormscale", "stormscale", "stormscale", "ionbound_stormscale");
        add(grouped, "lichblade", "slumbering_lichblade", "slumbering_lichblade", "waking_lichblade", "awakened_lichblade");
        add(grouped, "relic", "dormant_relic", "dormant_relic", "tainted_relic", "righteous_relic", "sunfire", "harbinger");
        add(grouped, "soulrender", "soulrender", "soulrender", "soulstalker");
        add(grouped, "whisperwind", "whisperwind", "whisperwind", "dreadwhisper");
        add(grouped, "wickpiercer", "wickpiercer", "wickpiercer", "gloampiercer");
        add(grouped, "wraithfang", "wraithfang", "wraithfang", "wraithmaw");
        List<String> singles = List.of(
                "brimstone_claymore", "stormbringer", "bramblethorn", "watching_warglaive", "toxic_longsword",
                "emberblade", "frostfall", "soulpyre", "molten_edge", "livyatan", "icewhisper", "arcanethyst",
                "thunderbrand", "hearthflame", "twisted_blade", "soulkeeper", "soulstealer", "mjolnir",
                "shadowsting", "emberlash", "waxweaver", "hiveheart", "stars_edge", "tempest", "flamewind",
                "ribboncleaver", "riftmane", "dawnquiver", "decaying_relic", "magiscythe", "magiblade",
                "magispear", "enigma", "caelestis", "bloodwake", "chompolotl", "dreadtide"
        );
        for (String item : singles) add(grouped, item, null, item);
        return List.copyOf(grouped.values());
    }

    private static void add(Map<String, Family> families, String profile, String formFamily, String... items) {
        families.put(profile, new Family(profile, formFamily, List.of(items)));
    }

    private static Identifier id(String namespace, String path) {
        return Identifier.of(namespace, path);
    }

    public record Family(String profilePath, String formFamily, List<String> items) {
        public Family {
            items = List.copyOf(items);
        }
    }
}
