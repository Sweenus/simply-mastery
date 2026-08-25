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
                    Optional.empty(), family.profilePath().equals("storms_edge") ? 100 : 10));
        }
        for (String stage : family.formStages()) {
            selectors.add(new MasteryProfile.Selector(Optional.empty(), Optional.of(id("simplyswords", stage)),
                    Optional.empty(), 100));
        }
        if (family.formFamily() != null) {
            selectors.add(new MasteryProfile.Selector(Optional.empty(), Optional.empty(),
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
        List<MasteryProfile.Migration> migrations = new ArrayList<>();
        migrations.add(new MasteryProfile.Migration(1, 2, migration(family), Map.of()));
        int version = 2;
        if (family.profilePath().equals("storms_edge")) {
            version = 4;
            migrations.add(new MasteryProfile.Migration(2, 3, Map.of(), Map.of()));
            migrations.add(new MasteryProfile.Migration(3, 4, Map.of(), Map.of()));
        } else if (family.profilePath().equals("brimstone_claymore")) {
            version = 3;
            migrations.add(new MasteryProfile.Migration(2, 3, Map.of(), Map.of()));
        } else if (phase2Profile(family.profilePath()) || phase3Profile(family.profilePath())) {
            version = 3;
            migrations.add(new MasteryProfile.Migration(2, 3, Map.of(), Map.of()));
        }
        Optional<Identifier> progressionGroup = Optional.ofNullable(family.progressionGroup())
                .map(path -> id("simplymastery", path));
        return new MasteryProfile(1, profileId, version, selectors, progressionGroup,
                branches, nodes, migrations);
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
        Optional<MasteryProfile.Effect> authored = authoredEffect(family, branch.id(), slot);
        MasteryProfile.Effect effect = authored.orElseGet(() -> effect(branch.style(), slot));
        boolean exactText = phase2Profile(family.profilePath()) || phase3Profile(family.profilePath());
        String nameKey = authored.isPresent()
                ? "skill.simplymastery." + family.profilePath() + "." + branch.id() + "." + NODE_SLOTS[slot]
                : "skill.simplymastery.style." + style + "." + NODE_SLOTS[slot];
        return new MasteryProfile.Node(nodeId, branch.id(), x, y, cost, capstone, group, requires,
                effect, nameKey, exactText ? nameKey + ".description"
                        : "effect.simplymastery." + effect.type().getPath() + ".description",
                Optional.of(id("simplymastery", "skills/" + family.profilePath() + "/" + branch.id()
                        + "_" + NODE_SLOTS[slot])));
    }

    private static Optional<MasteryProfile.Effect> authoredEffect(Family family, String branch, int slot) {
        Optional<MasteryProfile.Effect> storm = authoredStormEffect(family, branch, slot);
        if (storm.isPresent()) return storm;
        Optional<MasteryProfile.Effect> phase2 = authoredPhase2Effect(family, branch, slot);
        if (phase2.isPresent()) return phase2;
        Optional<MasteryProfile.Effect> phase3 = authoredPhase3Effect(family, branch, slot);
        if (phase3.isPresent()) return phase3;
        if (!family.profilePath().equals("brimstone_claymore")) return Optional.empty();
        return Optional.of(switch (branch + ":" + slot) {
            case "signature:0" -> effect("sulfurous_edge", "chance_bonus", 5);
            case "signature:1" -> effect("scorching_brand", "fire_ticks", 100,
                    "burning_damage_percent", 115);
            case "signature:2" -> effect("blast_furnace", "radius_tenths", 10,
                    "damage_percent", 115);
            case "signature:3" -> effect("kindling_blows", "chance_per_stack", 5,
                    "max_stacks", 3, "window_ticks", 80);
            case "signature:4" -> effect("flashpoint", "required_stacks", 3);
            case "signature:5" -> effect("cinder_scatter", "count", 3,
                    "range_tenths", 60, "damage_percent", 20, "fire_ticks", 40);
            case "signature:6" -> effect("backdraft", "pull_tenths", 8, "slowness_ticks", 40);
            case "signature:7" -> effect("chain_reaction", "damage_percent", 50,
                    "radius_tenths", 30, "max_detonations", 8);
            case "signature:8" -> effect("crucible_strike", "radius_percent", 60,
                    "primary_damage_percent", 200);
            case "combat:0" -> effect("lengthened_chain", "range_tenths", 40);
            case "combat:1" -> effect("furnace_bellows", "interval_ticks", 16);
            case "combat:2" -> effect("stoked_furnace", "growth_hundredths", 15,
                    "max_radius_tenths", 10);
            case "combat:3" -> effect("shackling_heat", "pull_tenths", 4,
                    "slowness_ticks", 40);
            case "combat:4" -> effect("overpressure", "per_pulse_percent", 10,
                    "cap_percent", 50);
            case "combat:5" -> effect("snapback", "damage_percent", 35, "radius_tenths", 30);
            case "combat:6" -> effect("molten_wake", "duration_ticks", 40,
                    "interval_ticks", 20, "damage_percent", 20, "min_move_tenths", 15);
            case "combat:7" -> effect("executioners_drop", "duration_ticks", 60,
                    "final_damage_percent", 225);
            case "combat:8" -> effect("perpetual_furnace", "duration_ticks", 200,
                    "start_percent", 75, "per_pulse_percent", 5, "cap_percent", 150,
                    "final_damage_percent", 50);
            case "transformation:0" -> effect("cinder_mantle", "duration_ticks", 80,
                    "amplifier", 0);
            case "transformation:1" -> effect("tempered_flesh", "padding_ticks", 18);
            case "transformation:2" -> effect("heat_sink", "max_stacks", 2,
                    "window_ticks", 80, "duration_ticks", 80);
            case "transformation:3" -> effect("furnace_reprisal", "damage_percent", 20,
                    "cooldown_ticks", 20, "fire_ticks", 40);
            case "transformation:4" -> effect("forged_resolve", "health_percent", 40,
                    "resistance_ticks", 60, "absorption_ticks", 80);
            case "transformation:5" -> effect("ashen_step", "duration_ticks", 60,
                    "cooldown_ticks", 100);
            case "transformation:6" -> effect("bulwark_pulse", "target_threshold", 3,
                    "duration_ticks", 60);
            case "transformation:7" -> effect("walking_furnace", "radius_percent", 85,
                    "pull_tenths", 5);
            case "transformation:8" -> effect("last_reprisal", "health_percent", 35,
                    "final_damage_percent", 175, "duration_ticks", 60);
            default -> throw new IllegalArgumentException("Unknown Brimstone Claymore node " + branch + ":" + slot);
        });
    }

    private static Optional<MasteryProfile.Effect> authoredPhase2Effect(Family family, String branch, int slot) {
        List<String> profiles = List.of("watcher_claymore", "the_devourer", "wickpiercer",
                "gloampiercer", "wraithfang", "wraithmaw");
        int profile = profiles.indexOf(family.profilePath());
        if (profile < 0) return Optional.empty();
        int branchIndex = switch (branch) {
            case "signature" -> 0;
            case "combat" -> 1;
            case "transformation" -> 2;
            default -> throw new IllegalArgumentException("Unknown branch " + branch);
        };
        return Optional.of(effect("phase2_mastery", "kind", profile * 27 + branchIndex * 9 + slot));
    }

    private static boolean phase2Profile(String profile) {
        return profile.equals("watcher_claymore") || profile.equals("the_devourer")
                || profile.equals("wickpiercer") || profile.equals("gloampiercer")
                || profile.equals("wraithfang") || profile.equals("wraithmaw");
    }

    private static Optional<MasteryProfile.Effect> authoredPhase3Effect(Family family, String branch, int slot) {
        List<String> profiles = List.of("stormscale", "ionbound_stormscale", "soulrender",
                "soulstalker", "whisperwind", "dreadwhisper");
        int profile = profiles.indexOf(family.profilePath());
        if (profile < 0) return Optional.empty();
        int branchIndex = switch (branch) {
            case "signature" -> 0;
            case "combat" -> 1;
            case "transformation" -> 2;
            default -> throw new IllegalArgumentException("Unknown branch " + branch);
        };
        return Optional.of(effect("phase3_mastery", "kind", profile * 27 + branchIndex * 9 + slot));
    }

    private static boolean phase3Profile(String profile) {
        return profile.equals("stormscale") || profile.equals("ionbound_stormscale")
                || profile.equals("soulrender") || profile.equals("soulstalker")
                || profile.equals("whisperwind") || profile.equals("dreadwhisper");
    }

    private static Optional<MasteryProfile.Effect> authoredStormEffect(Family family, String branch, int slot) {
        if (!family.profilePath().equals("storms_edge")) return Optional.empty();
        return Optional.of(switch (branch + ":" + slot) {
            case "signature:0" -> effect("stormbreak_conduit", "width_tenths", 10,
                    "slowness_ticks", 30, "amplifier", 0);
            case "signature:1" -> effect("slipstream", "distance_percent", 120, "speed_percent", 110);
            case "signature:2" -> effect("crosswind");
            case "signature:3" -> effect("capacitor", "per_hit_percent", 8, "cap_percent", 40);
            case "signature:4" -> effect("storm_chaser", "cooldown_percent", 85);
            case "signature:5" -> effect("flashguard", "duration_ticks", 40, "amplifier", 0);
            case "signature:6" -> effect("afterimage", "duration_ticks", 40, "damage_percent", 30);
            case "signature:7" -> effect("eye_of_storm", "radius_percent", 50, "damage_percent", 175);
            case "signature:8" -> effect("thunderhead", "radius_tenths", 20);
            case "combat:0" -> effect("static_reserve", "sprint_chance", 40);
            case "combat:1" -> effect("charged_pursuit", "duration_ticks", 40, "amplifier", 0);
            case "combat:2" -> effect("building_voltage", "chance_per_stack", 5,
                    "max_stacks", 4, "window_ticks", 60);
            case "combat:3" -> effect("live_wire", "damage_percent", 20, "cooldown_ticks", 10);
            case "combat:4" -> effect("feedback_loop", "window_ticks", 100, "damage_percent", 120);
            case "combat:5" -> effect("quickening_current", "extension_ticks", 40,
                    "max_remaining_ticks", 120);
            case "combat:6" -> effect("unbroken_pace", "duration_ticks", 30, "speed_amplifier", 1,
                    "resistance_amplifier", 0, "cooldown_ticks", 120);
            case "combat:7" -> effect("perpetual_motion", "window_ticks", 120, "cooldown_percent", 70);
            case "combat:8" -> effect("flashover", "targets", 3, "range_tenths", 50, "damage_percent", 35);
            case "transformation:0" -> effect("ionize", "duration_ticks", 100);
            case "transformation:1" -> effect("arc_lash", "damage_percent", 30,
                    "cooldown_ticks", 20, "range_tenths", 50);
            case "transformation:2" -> effect("pressure_drop", "duration_ticks", 60, "amplifier", 0);
            case "transformation:3" -> effect("updraft", "knock_up_percent", 150, "slow_falling_ticks", 40);
            case "transformation:4" -> effect("fulmination", "damage_percent", 25,
                    "radius_tenths", 30, "max_targets", 8);
            case "transformation:5" -> effect("stormshield", "duration_ticks", 80, "strong_threshold", 4);
            case "transformation:6" -> effect("reverberation", "delay_ticks", 15, "damage_percent", 30);
            case "transformation:7" -> effect("judgment_bolt", "damage_percent", 100);
            case "transformation:8" -> effect("supercell", "duration_ticks", 80,
                    "interval_ticks", 20, "damage_percent", 20, "pull_tenths", 8);
            default -> throw new IllegalArgumentException("Unknown Storm's Edge node " + branch + ":" + slot);
        });
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
        grouped(result, "watcher_claymore", "watcher_claymore", "The Watcher", "watcher_claymore",
                List.of(), b("Dread Ledger", EXECUTION), b("Final Omen", CONTROL),
                b("Vitality Theft", SUSTAIN), "watcher_claymore");
        grouped(result, "the_devourer", null, "The Devourer", "watcher_claymore",
                List.of("the_devourer"), b("Devouring Mass", CONTROL), b("Consumption", SUSTAIN),
                b("Reprisal", GUARD), "the_devourer");
        grouped(result, "stormscale", null, "Stormscale", "stormscale",
                List.of("stormscale", "awakened_stormscale"), b("Lightning Rod", CONTROL),
                b("Gathering Charge", COMBO), b("Magnetic Storm", IMPACT), "stormscale");
        grouped(result, "ionbound_stormscale", null, "Ionbound Stormscale", "stormscale",
                List.of("ionbound_stormscale"), b("Ion Reserve", GUARD), b("Ion Crusher", IMPACT),
                b("Paralysis Beam", CONTROL), "ionbound_stormscale");
        grouped(result, "awakened_lichblade", null, "Awakened Lichblade", "lichblade",
                List.of("awakened_lichblade"), b("Soul Anguish", CONTROL), b("Siphoned Vitality", SUSTAIN),
                b("Phylactery Command", EXECUTION), "awakened_lichblade");
        grouped(result, "sunfire", null, "Sunfire", "relic", List.of("sunfire"),
                b("Solar Dominion", IMPACT), b("Banner of Renewal", SUSTAIN), b("Undying Ember", GUARD),
                "sunfire");
        grouped(result, "harbinger", null, "Harbinger", "relic", List.of("harbinger"),
                b("Dread Dominion", CONTROL), b("War's Portent", COMBO), b("Foretold Ruin", EXECUTION),
                "harbinger");
        grouped(result, "soulrender", null, "Soulrender", "soulrender", List.of("soulrender"),
                b("Rendmarks", CONTROL), b("The Reaping", EXECUTION), b("Gravebound", GUARD),
                "soulrender");
        grouped(result, "soulstalker", null, "Soulstalker", "soulrender", List.of("soulstalker"),
                b("Hunting Tendrils", EXECUTION), b("Gloam Stride", MOBILITY), b("Cleave and Crash", IMPACT),
                "soulstalker");
        grouped(result, "whisperwind", null, "Whisperwind", "whisperwind", List.of("whisperwind"),
                b("Petal Step", MOBILITY), b("Bloom Cut", EXECUTION), b("Zephyr Rhythm", COMBO),
                "whisperwind");
        grouped(result, "dreadwhisper", null, "Dreadwhisper", "whisperwind", List.of("dreadwhisper"),
                b("Reaving Front", MOBILITY), b("Corrupted Wound", EXECUTION), b("Gloam Passage", CONTROL),
                "dreadwhisper");
        grouped(result, "wickpiercer", null, "Wickpiercer", "wickpiercer", List.of("wickpiercer"),
                b("Waxen Flight", IMPACT), b("Frenzy Flame", COMBO), b("Deathless Candle", SUSTAIN),
                "wickpiercer");
        grouped(result, "gloampiercer", null, "Gloampiercer", "wickpiercer", List.of("gloampiercer"),
                b("Phantom Ambush", EXECUTION), b("Gloam Barrage", IMPACT), b("Stained Ground", CONTROL),
                "gloampiercer");
        grouped(result, "wraithfang", null, "Wraithfang", "wraithfang", List.of("wraithfang"),
                b("Spectral Throw", IMPACT), b("Wraith Pursuit", MOBILITY), b("Soul Tempo", COMBO),
                "wraithfang");
        grouped(result, "wraithmaw", null, "Wraithmaw", "wraithfang", List.of("wraithmaw"),
                b("Cutlass Muster", IMPACT), b("Orbiting Maw", COMBO), b("Gloam Graveyard", CONTROL),
                "wraithmaw");
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
        groupedSingle(result, "magiscythe", "Magiscythe", "decaying_relic", "Magistorm", IMPACT,
                "Reaping Arc", EXECUTION, "Storm Renewal", SUSTAIN);
        groupedSingle(result, "magiblade", "Magiblade", "decaying_relic", "Magisonic", CONTROL,
                "Orbiting Warden", GUARD, "Sonic Release", IMPACT);
        groupedSingle(result, "magispear", "Magispear", "decaying_relic", "Magislam", IMPACT,
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

    private static void groupedSingle(Map<String, Family> families, String profile, String display,
                                      String progressionGroup, String signature, Style signatureStyle,
                                      String combat, Style combatStyle, String transformation,
                                      Style transformationStyle) {
        grouped(families, profile, null, display, progressionGroup, List.of(),
                b(signature, signatureStyle), b(combat, combatStyle), b(transformation, transformationStyle),
                profile);
    }

    private static void add(Map<String, Family> families, String profile, String formFamily, String display,
                            BranchPlan signature, BranchPlan combat, BranchPlan transformation, String... items) {
        grouped(families, profile, formFamily, display, null, List.of(), signature, combat, transformation, items);
    }

    private static void grouped(Map<String, Family> families, String profile, String formFamily, String display,
                                String progressionGroup, List<String> formStages, BranchPlan signature,
                                BranchPlan combat, BranchPlan transformation, String... items) {
        families.put(profile, new Family(profile, formFamily, progressionGroup, display,
                List.of(signature.withId("signature"), combat.withId("combat"),
                        transformation.withId("transformation")), List.of(items), formStages));
    }

    public static Optional<Identifier> bankingGroup(Identifier itemId) {
        return id("simplyswords", "decaying_relic").equals(itemId)
                ? Optional.of(id("simplymastery", "decaying_relic")) : Optional.empty();
    }

    public static List<Identifier> bankingProfiles(Identifier groupId) {
        if (!id("simplymastery", "decaying_relic").equals(groupId)) return List.of();
        return List.of(id("simplymastery", "magiscythe"), id("simplymastery", "magiblade"),
                id("simplymastery", "magispear"));
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

    public record Family(String profilePath, String formFamily, String progressionGroup, String displayName,
                         List<BranchPlan> branches, List<String> items, List<String> formStages) {
        public Family {
            branches = List.copyOf(branches);
            items = List.copyOf(items);
            formStages = List.copyOf(formStages);
        }
    }
}
