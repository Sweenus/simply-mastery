package net.sweenus.simplymastery.mastery.definition;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;

import java.util.List;
import java.util.Optional;

public enum MasteryCohort {
    ABYSSAL_SPECTRAL("abyssal_spectral", List.of(
            "watcher_claymore", "the_devourer", "wickpiercer", "gloampiercer", "wraithfang", "wraithmaw")),
    STORM_SOUL("storm_soul", List.of(
            "stormscale", "ionbound_stormscale", "soulrender", "soulstalker", "whisperwind", "dreadwhisper")),
    LONG_PATH_FINAL_FORMS("long_path_final_forms", List.of(
            "awakened_lichblade", "sunfire", "harbinger")),
    FIRE_FORGE("fire_forge", List.of(
            "hearthflame", "emberblade", "emberlash", "flamewind", "molten_edge", "soulpyre")),
    STORM_FROST_WATER("storm_frost_water", List.of(
            "stormbringer", "mjolnir", "thunderbrand", "tempest", "frostfall", "icewhisper", "livyatan")),
    NATURE_SWARM("nature_swarm", List.of(
            "bramblethorn", "waxweaver", "hiveheart", "chompolotl")),
    DEATH_SHADOW_BLOOD("death_shadow_blood", List.of(
            "toxic_longsword", "soulkeeper", "soulstealer", "twisted_blade", "shadowsting", "bloodwake")),
    ARCANE_COSMIC("arcane_cosmic", List.of(
            "arcanethyst", "stars_edge", "magiscythe", "magiblade", "magispear", "enigma", "caelestis")),
    MARTIAL_COMMAND_ELDRITCH("martial_command_eldritch", List.of(
            "watching_warglaive", "ribboncleaver", "riftmane", "dawnquiver", "dreadtide"));

    public static final int NODES_PER_PROFILE = 27;

    private final String id;
    private final List<String> profilePaths;
    private final Identifier effectId;

    MasteryCohort(String id, List<String> profilePaths) {
        this.id = id;
        this.profilePaths = List.copyOf(profilePaths);
        this.effectId = Identifier.of(SimplyMastery.MOD_ID, "cohort/" + id);
    }

    public String id() {
        return id;
    }

    public List<String> profilePaths() {
        return profilePaths;
    }

    public int expectedNodeCount() {
        return profilePaths.size() * NODES_PER_PROFILE;
    }

    public Identifier effectId() {
        return effectId;
    }

    public int kind(String profilePath, String branch, int slot) {
        int profileIndex = profilePaths.indexOf(profilePath);
        if (profileIndex < 0) throw new IllegalArgumentException(profilePath + " is not in cohort " + id);
        int branchIndex = switch (branch) {
            case "signature" -> 0;
            case "combat" -> 1;
            case "transformation" -> 2;
            default -> throw new IllegalArgumentException("Unknown branch " + branch);
        };
        if (slot < 0 || slot >= 9) throw new IllegalArgumentException("Unknown node slot " + slot);
        return profileIndex * NODES_PER_PROFILE + branchIndex * 9 + slot;
    }

    public static Optional<MasteryCohort> forProfile(String profilePath) {
        for (MasteryCohort cohort : values()) {
            if (cohort.profilePaths.contains(profilePath)) return Optional.of(cohort);
        }
        return Optional.empty();
    }

    public static Optional<MasteryCohort> forEffect(Identifier effectId) {
        for (MasteryCohort cohort : values()) {
            if (cohort.effectId.equals(effectId)) return Optional.of(cohort);
        }
        return Optional.empty();
    }
}
