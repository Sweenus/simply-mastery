package net.sweenus.simplymastery.mastery.definition;

import static net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles.Style.*;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BuiltInFamilyProfiles {

    private static final List<Family> FAMILIES = families();
    private static final String[] NODE_SLOTS = {
            "opening", "cadence", "pressure", "reversal", "reserve", "threshold", "convergence",
            "focus", "release"
    };

    private BuiltInFamilyProfiles() {
    }

    public static void addMissing(Map<Identifier, MasteryProfile> profiles) {
        for (Family family : FAMILIES) {
            profiles.putIfAbsent(id("simplymastery", family.profilePath()), create(family));
        }
    }

    public static Collection<Family> familiesForCoverage() {
        return FAMILIES;
    }

    public static MasteryProfile profile(String path) {
        return FAMILIES.stream().filter(family -> family.profilePath().equals(path)).findFirst()
                .map(BuiltInFamilyProfiles::create).orElseThrow();
    }

    private static MasteryProfile create(Family family) {
        Identifier profileId = id("simplymastery", family.profilePath());
        List<MasteryProfile.Selector> selectors = new ArrayList<>();
        for (String item : family.items()) {
            selectors.add(new MasteryProfile.Selector(Optional.of(id("simplyswords", item)), Optional.empty(),
                    family.profilePath().equals("storms_edge") ? 100 : 10));
        }
        if (family.formFamily() != null) {
            selectors.add(new MasteryProfile.Selector(Optional.empty(),
                    Optional.of(id("simplyswords", family.formFamily())), 0));
        }
        List<MasteryProfile.Branch> branches = family.branches().stream()
                .map(branch -> new MasteryProfile.Branch(branch.id(),
                        "branch.simplymastery." + family.profilePath() + "." + branch.id(), color(branch.style())))
                .toList();
        List<MasteryProfile.Node> nodes = new ArrayList<>();
        addBranch(nodes, family, family.branches().get(0), .22);
        addBranch(nodes, family, family.branches().get(1), .50);
        addBranch(nodes, family, family.branches().get(2), .78);
        return new MasteryProfile(1, profileId, 2, selectors, branches, nodes,
                List.of(new MasteryProfile.Migration(1, 2, migration(family), Map.of())));
    }

    private static void addBranch(List<MasteryProfile.Node> nodes, Family family, BranchPlan branch, double y) {
        String prefix = family.profilePath() + "_" + branch.id() + "_";
        String[] ids = new String[NODE_SLOTS.length];
        for (int i = 0; i < ids.length; i++) ids[i] = prefix + NODE_SLOTS[i];
        nodes.add(node(family, branch, ids[0], 0, .90, y, 1, false, "", List.of()));
        nodes.add(node(family, branch, ids[1], 1, .78, y, 1, false, "", List.of(ids[0])));
        nodes.add(node(family, branch, ids[2], 2, .66, y, 1, false, "", List.of(ids[1])));
        nodes.add(node(family, branch, ids[3], 3, .54, y, 1, false, "", List.of(ids[2])));
        nodes.add(node(family, branch, ids[4], 4, .42, y, 1, false, "", List.of(ids[3])));
        nodes.add(node(family, branch, ids[5], 5, .66, y - .045, 1, false, "", List.of(ids[1])));
        nodes.add(node(family, branch, ids[6], 6, .54, y + .045, 1, false, "", List.of(ids[2])));
        String group = family.profilePath() + "_" + branch.id() + "_capstone";
        nodes.add(node(family, branch, ids[7], 7, .16, y - .055, 2, true, group, List.of(ids[4])));
        nodes.add(node(family, branch, ids[8], 8, .16, y + .055, 2, true, group, List.of(ids[4])));
    }

    private static MasteryProfile.Node node(Family family, BranchPlan branch, String nodeId, int slot,
                                            double x, double y, int cost, boolean capstone, String group,
                                            List<String> requires) {
        String style = branch.style().name().toLowerCase();
        return new MasteryProfile.Node(nodeId, branch.id(), x, y, cost, capstone, group, requires,
                effect(branch.style(), slot), "skill.simplymastery.style." + style + "." + NODE_SLOTS[slot],
                "effect.simplymastery." + effect(branch.style(), slot).type().getPath() + ".description",
                Optional.of(id("simplymastery", "skills/" + family.profilePath() + "/" + branch.id()
                        + "_" + NODE_SLOTS[slot])));
    }

    private static MasteryProfile.Effect effect(Style style, int slot) {
        int mechanic = style.pattern[slot];
        int mode = style.ordinal() % 5;
        return switch (mechanic) {
            case 0 -> effect("stormstep", "duration_ticks", 35, "amplifier", 0, "cooldown_ticks", 100);
            case 1 -> effect("battle_flow", "hits", 3, "window_ticks", 70, "duration_ticks", 60,
                    "amplifier", 0, "cooldown_ticks", 120, "mode", mode);
            case 2 -> effect("combo_strike", "hits", 3, "window_ticks", 70,
                    "damage_tenths", slot >= 7 ? 20 : 8, "cooldown_ticks", slot >= 7 ? 100 : 80);
            case 3 -> effect("hindering_strike", "duration_ticks", slot >= 7 ? 80 : 40,
                    "amplifier", 0, "cooldown_ticks", 100, "mode", style.ordinal() % 4);
            case 4 -> effect("finishing_strike", "health_percent", 30,
                    "damage_tenths", slot >= 7 ? 30 : 12, "cooldown_ticks", 100);
            case 5 -> effect("soul_mend", "heal_tenths", slot >= 7 ? 20 : 8, "cooldown_ticks", 120);
            case 6 -> effect("kill_flow", "duration_ticks", 80, "amplifier", 0,
                    "cooldown_ticks", 120, "mode", mode);
            case 7 -> effect("counterstrike", "damage_tenths", slot >= 7 ? 25 : 10,
                    "cooldown_ticks", 140);
            case 8 -> effect("guarded_recovery", "duration_ticks", slot >= 7 ? 70 : 40,
                    "amplifier", 0, "cooldown_ticks", 180);
            case 9 -> effect("leeching_strike", "heal_tenths", slot >= 7 ? 15 : 5,
                    "cooldown_ticks", 140);
            case 10 -> effect("cleaving_echo", "hits", 3, "window_ticks", 70,
                    "damage_tenths", slot >= 7 ? 18 : 7, "radius_tenths", slot >= 7 ? 50 : 35,
                    "cooldown_ticks", 120);
            default -> effect("echo_strike", "damage_tenths", slot >= 7 ? 20 : 8,
                    "cooldown_ticks", 100);
        };
    }

    private static MasteryProfile.Effect effect(String path, Object... values) {
        Map<String, Integer> parameters = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            parameters.put((String) values[i], (Integer) values[i + 1]);
        }
        return new MasteryProfile.Effect(id("simplymastery", path), parameters);
    }

    private static Map<String, String> migration(Family family) {
        Map<String, String> renamed = new LinkedHashMap<>();
        renameBranch(renamed, family, "guard", "signature");
        renameBranch(renamed, family, "tempo", "combat");
        renameBranch(renamed, family, "storm", "transformation");
        if (family.profilePath().equals("storms_edge")) {
            renamed.put("charged_pursuit", nodeId(family, "signature", 1));
            renamed.put("eye_of_storm", nodeId(family, "signature", 7));
            renamed.put("thunderhead", nodeId(family, "signature", 8));
            renamed.put("afterimage", nodeId(family, "combat", 1));
            renamed.put("flashpoint", nodeId(family, "combat", 7));
            renamed.put("conduction", nodeId(family, "combat", 8));
            renamed.put("pressure_front", nodeId(family, "transformation", 1));
            renamed.put("skybreaker", nodeId(family, "transformation", 7));
            renamed.put("tempest_wake", nodeId(family, "transformation", 8));
        }
        return Map.copyOf(renamed);
    }

    private static void renameBranch(Map<String, String> renamed, Family family, String old, String replacement) {
        renamed.put(old + "_root", nodeId(family, replacement, 0));
        renamed.put(old + "_path", nodeId(family, replacement, 1));
        renamed.put(old + "_capstone_a", nodeId(family, replacement, 7));
        renamed.put(old + "_capstone_b", nodeId(family, replacement, 8));
    }

    private static String nodeId(Family family, String branch, int slot) {
        return family.profilePath() + "_" + branch + "_" + NODE_SLOTS[slot];
    }

    private static int color(Style style) {
        return switch (style) {
            case MOBILITY -> 0xFF72D9E8;
            case COMBO -> 0xFFA984F4;
            case CONTROL -> 0xFF78B7E8;
            case GUARD -> 0xFF80C8A0;
            case SUSTAIN -> 0xFFE0A5C8;
            case EXECUTION -> 0xFFE87972;
            case IMPACT -> 0xFFE8B65A;
            case FRENZY -> 0xFFE88652;
            case ARCANE -> 0xFFB58AF2;
        };
    }

    private static List<Family> families() {
        Map<String, Family> result = new LinkedHashMap<>();
        add(result, "storms_edge", null, "Storm's Edge", b("Stormbreak", MOBILITY),
                b("Thunderstep", COMBO), b("Skywrath", IMPACT), "storms_edge");
        add(result, "watcher_claymore", "watcher_claymore", "The Watcher", b("Unblinking Hunt", EXECUTION),
                b("Sentinel's Weight", GUARD), b("Devouring Abyss", SUSTAIN), "watcher_claymore", "the_devourer");
        add(result, "stormscale", "stormscale", "Stormscale", b("Lightning Rod", CONTROL),
                b("Charged Pursuit", COMBO), b("Ion Containment", IMPACT), "stormscale", "ionbound_stormscale");
        add(result, "lichblade", "slumbering_lichblade", "Lichblade", b("Soul Anguish", CONTROL),
                b("Grave Harvest", SUSTAIN), b("Lich Ascendance", EXECUTION), "slumbering_lichblade",
                "waking_lichblade", "awakened_lichblade");
        add(result, "relic", "dormant_relic", "Dormant Relic", b("Relic Attunement", GUARD),
                b("Righteous Standard", SUSTAIN), b("Abyssal Standard", EXECUTION), "dormant_relic",
                "tainted_relic", "righteous_relic", "sunfire", "harbinger");
        add(result, "soulrender", "soulrender", "Soulrender", b("Soul Rend", SUSTAIN),
                b("Stygian Stride", MOBILITY), b("Reaper's Claim", EXECUTION), "soulrender", "soulstalker");
        add(result, "whisperwind", "whisperwind", "Whisperwind", b("Fatal Flicker", MOBILITY),
                b("Whispered Tempo", COMBO), b("Dread Rend", EXECUTION), "whisperwind", "dreadwhisper");
        add(result, "wickpiercer", "wickpiercer", "Wickpiercer", b("Flicker Fury", COMBO),
                b("Spectral Feint", MOBILITY), b("Phantom Phalanx", CONTROL), "wickpiercer", "gloampiercer");
        add(result, "wraithfang", "wraithfang", "Wraithfang", b("Spectral Leap", MOBILITY),
                b("Wraith Hunt", EXECUTION), b("Spectral Downpour", IMPACT), "wraithfang", "wraithmaw");
        single(result, "brimstone_claymore", "Brimstone Claymore", "Brimstone", IMPACT,
                "Furnace Chain", CONTROL, "Cinder Guard", GUARD);
        single(result, "stormbringer", "Stormbringer", "Shock Deflect", GUARD,
                "Storm Charge", COMBO, "Chain Tempest", IMPACT);
        single(result, "bramblethorn", "Bramblethorn", "Wild Grasp", CONTROL,
                "Thorn Dance", COMBO, "Verdant Renewal", SUSTAIN);
        single(result, "watching_warglaive", "Watching Warglaive", "Unblinking Hunt", EXECUTION,
                "Warglaive Orbit", COMBO, "Omen Guard", CONTROL);
        single(result, "toxic_longsword", "Longsword of the Plague", "Death Knell", CONTROL,
                "Plague Tempo", COMBO, "Pestilent Harvest", SUSTAIN);
        single(result, "emberblade", "Emberblade", "Ember Ire", FRENZY,
                "Cinder Step", MOBILITY, "Wildfire", IMPACT);
        single(result, "frostfall", "Frostfall", "Frost Fury", CONTROL,
                "Returning Arc", COMBO, "Glacial Impact", IMPACT);
        single(result, "soulpyre", "Soul Pyre", "Soul Tether", SUSTAIN,
                "Pyre Wisp", CONTROL, "Nether Dominion", GUARD);
        single(result, "molten_edge", "Molten Edge", "Furnace Heart", SUSTAIN,
                "Molten Pursuit", MOBILITY, "Rupture", IMPACT);
        single(result, "livyatan", "Livyatan", "Leviathan's Wake", IMPACT,
                "Tidal Return", COMBO, "Tempest Current", CONTROL);
        single(result, "icewhisper", "Icewhisper", "Permafrost", CONTROL,
                "Comet Cadence", IMPACT, "Winter Veil", GUARD);
        single(result, "arcanethyst", "Arcanethyst", "Arcane Assault", CONTROL,
                "Levitation Rhythm", COMBO, "Gravity Well", IMPACT);
        single(result, "thunderbrand", "Thunderbrand", "Thunder Blitz", MOBILITY,
                "Stored Charge", GUARD, "Chain Release", IMPACT);
        single(result, "hearthflame", "Hearthflame", "Furnace Chains", CONTROL,
                "Hearth Guard", GUARD, "Molten Reprisal", IMPACT);
        single(result, "twisted_blade", "Twisted Blade", "Twisted Crescendo", COMBO,
                "Death Knell", EXECUTION, "Discordant Echo", CONTROL);
        single(result, "soulkeeper", "Soulkeeper", "Soul Meld", SUSTAIN,
                "Lantern Ward", GUARD, "Wisp Procession", CONTROL);
        single(result, "soulstealer", "Soulstealer", "Soul Steal", SUSTAIN,
                "Reaping Tempo", EXECUTION, "Spirit Rush", MOBILITY);
        single(result, "mjolnir", "Mjolnir", "Storm", IMPACT,
                "Thunder Step", MOBILITY, "Sky Guard", GUARD);
        single(result, "shadowsting", "Shadowsting", "Shadow Dance", MOBILITY,
                "Backstab Rhythm", EXECUTION, "Mist Veil", GUARD);
        single(result, "emberlash", "Emberlash", "Smoulder", CONTROL,
                "Cauterizing Step", SUSTAIN, "Ember Reprisal", FRENZY);
        single(result, "waxweaver", "Waxweaver", "Waxweave", CONTROL,
                "Kindled Tempo", FRENZY, "Chrysalis Guard", GUARD);
        single(result, "hiveheart", "Hiveheart", "Hivemind", CONTROL,
                "Swarm Cadence", COMBO, "Royal Guard", SUSTAIN);
        single(result, "stars_edge", "Star's Edge", "Astral Reprise", COMBO,
                "Constellation Step", MOBILITY, "Falling Star", IMPACT);
        single(result, "tempest", "Tempest", "Elemental Vortex", CONTROL,
                "Elemental Cadence", COMBO, "Convergence", IMPACT);
        single(result, "flamewind", "Flamewind", "Emberstorm", CONTROL,
                "Seed Cycle", COMBO, "Wildfire Release", IMPACT);
        single(result, "ribboncleaver", "Ribboncleaver", "Ribbonwrath", GUARD,
                "Cleaving Rush", MOBILITY, "Resilient Fury", FRENZY);
        single(result, "riftmane", "Riftmane", "Vanguard", MOBILITY,
                "Charger Rank", IMPACT, "Rift Command", CONTROL);
        single(result, "dawnquiver", "Dawnquiver", "Seraph's Draw", EXECUTION,
                "Dawn Chorus", COMBO, "Sunlance", IMPACT);
        single(result, "decaying_relic", "Decaying Relic", "Decay", CONTROL,
                "Relic Pulse", ARCANE, "Unbound Fate", EXECUTION);
        single(result, "magiscythe", "Magiscythe", "Magistorm", IMPACT,
                "Reaping Arc", EXECUTION, "Storm Renewal", SUSTAIN);
        single(result, "magiblade", "Magiblade", "Magisonic", CONTROL,
                "Orbiting Warden", GUARD, "Sonic Release", IMPACT);
        single(result, "magispear", "Magispear", "Magislam", IMPACT,
                "Skyward Vault", MOBILITY, "Spear Rain", COMBO);
        single(result, "enigma", "Enigma", "Galeforce", MOBILITY,
                "Twister Snare", CONTROL, "Eye of Enigma", GUARD);
        single(result, "caelestis", "Caelestis", "Astral Breach", ARCANE,
                "Riftling Command", CONTROL, "Unbound Pact", EXECUTION);
        single(result, "bloodwake", "Bloodwake", "Crimson Revelry", FRENZY,
                "Blood Rite", SUSTAIN, "Red Tide", IMPACT);
        single(result, "chompolotl", "Chomp'olotl", "Chompocalypse", CONTROL,
                "Axolotl Rally", COMBO, "Blue Guardian", SUSTAIN);
        single(result, "dreadtide", "Dreadtide", "Voidcaller", CONTROL,
                "Voidcloak", GUARD, "Eldritch Release", EXECUTION);
        return List.copyOf(result.values());
    }

    private static void single(Map<String, Family> families, String profile, String display,
                               String signature, Style signatureStyle, String combat, Style combatStyle,
                               String transformation, Style transformationStyle) {
        add(families, profile, null, display, b(signature, signatureStyle), b(combat, combatStyle),
                b(transformation, transformationStyle), profile);
    }

    private static void add(Map<String, Family> families, String profile, String formFamily, String display,
                            BranchPlan signature, BranchPlan combat, BranchPlan transformation, String... items) {
        families.put(profile, new Family(profile, formFamily, display,
                List.of(signature.withId("signature"), combat.withId("combat"),
                        transformation.withId("transformation")), List.of(items)));
    }

    private static BranchPlan b(String title, Style style) {
        return new BranchPlan("", title, style);
    }

    private static Identifier id(String namespace, String path) {
        return Identifier.of(namespace, path);
    }

    public enum Style {
        MOBILITY(0, 1, 2, 3, 9, 4, 6, 2, 10),
        COMBO(1, 2, 0, 3, 6, 9, 11, 2, 10),
        CONTROL(3, 2, 1, 7, 8, 4, 6, 4, 10),
        GUARD(8, 7, 3, 1, 5, 9, 6, 7, 8),
        SUSTAIN(9, 5, 1, 8, 3, 4, 6, 5, 8),
        EXECUTION(4, 2, 3, 0, 6, 9, 5, 4, 10),
        IMPACT(11, 10, 2, 3, 7, 4, 6, 2, 10),
        FRENZY(1, 2, 0, 9, 3, 4, 6, 2, 10),
        ARCANE(3, 11, 1, 10, 8, 4, 5, 2, 10);

        private final int[] pattern;

        Style(int... pattern) {
            this.pattern = pattern;
        }
    }

    public record BranchPlan(String id, String title, Style style) {
        BranchPlan withId(String id) {
            return new BranchPlan(id, title, style);
        }
    }

    public record Family(String profilePath, String formFamily, String displayName,
                         List<BranchPlan> branches, List<String> items) {
        public Family {
            branches = List.copyOf(branches);
            items = List.copyOf(items);
        }
    }
}
