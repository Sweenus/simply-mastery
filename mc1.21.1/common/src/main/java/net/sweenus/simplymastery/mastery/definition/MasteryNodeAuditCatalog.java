package net.sweenus.simplymastery.mastery.definition;

import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

final class MasteryNodeAuditCatalog {
    private static final Map<String, MasteryNodeAuditReport.Evidence> EVIDENCE = evidence();

    private MasteryNodeAuditCatalog() {
    }

    static Optional<MasteryNodeAuditReport.Evidence> find(Identifier profile, String node) {
        return Optional.ofNullable(EVIDENCE.get(profile.getPath() + "/" + node));
    }

    private static Map<String, MasteryNodeAuditReport.Evidence> evidence() {
        Map<String, MasteryNodeAuditReport.Evidence> evidence = new LinkedHashMap<>();
        storm(evidence, "signature_opening", "Stormbreak corridor and thunderclap",
                "Corridor hit", "stormbreak", "Corridor width +1; Slowness I 30t",
                "StormsEdgeAbilityManager corridor query and CORRIDOR_HIT observer", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Tuning and hit observer match the description");
        storm(evidence, "signature_cadence", "Stormbreak dash",
                "Ability preparation", "stormbreak", "Distance x1.2; speed x1.1",
                "StormsEdgeAbilityManager dash distance, velocity, and timeout", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Both modifiers are consumed by the dash");
        storm(evidence, "signature_pressure", "Stormbreak corridor force",
                "Corridor hit", "stormbreak", "Force mode inward",
                "StormsEdgeAbilityManager knockToward corridor path", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Replaces lateral knockback with an inward pull");
        storm(evidence, "signature_reversal", "Stormbreak corridor into thunderclap",
                "Successful corridor hit", "stormbreak", "+8% thunderclap damage per hit; 40% cap",
                "StormsEdgeAbilityManager successfulCorridorHits damage multiplier", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Counts successful damage events and enforces the cap");
        storm(evidence, "signature_reserve", "Stormbreak item cooldown",
                "Ability preparation", "stormbreak", "Cooldown x0.85",
                "UniqueAbilityExecution cooldown key consumed by SimplySwordsAPI", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The tuned cooldown reaches player and entity cooldown managers");
        storm(evidence, "signature_threshold", "Stormbreak completion",
                "Dash end", "stormbreak", "Resistance I 40t",
                "DASH_END observer", "Status-effect duration",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Runs for normal, terrain, and focused dash completion");
        storm(evidence, "signature_convergence", "Stormbreak traveled path",
                "Residual tick after completion", "stormbreak", "40t duration; 30% corridor damage",
                "StormsEdgeAbilityManager ResidualStorm path scanner", "Execution finishes at residual maximum age",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Each target can be damaged by the charged path once");
        storm(evidence, "signature_focus", "Stormbreak focused capstone",
                "First successful corridor hit", "stormbreak", "Half-radius 175% focused thunderclap",
                "StormsEdgeAbilityManager focused stop and single-target filter", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Focused thunderclap suppresses horizontal force while retaining its vertical launch for Rising Gust");
        storm(evidence, "signature_release", "Stormbreak area capstone",
                "Dash completion", "stormbreak", "+2 thunderclap radius; inward force",
                "StormsEdgeAbilityManager thunderhead radius and knockToward", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Full-path mode and inward thunderclap are both consumed");
        storm(evidence, "combat_opening", "Storm's Edge melee cooldown refresh",
                "Sprinting melee hit", "storms_edge_refresh", "Refresh chance 40%",
                "StormsEdgeSwordItem postHit refresh roll", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "The successful attack hook snapshots sprinting before vanilla clears it and the refresh consumes that snapshot");
        storm(evidence, "combat_cadence", "Successful Stormbreak refresh",
                "Refresh proc", "storms_edge_refresh", "Pursuit 40t; Speed I 40t",
                "REFRESH_PROC observer and stack runtime", "Stack TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Speed III remains visible over Stormbreak's Speed II and refreshes preserve the longer Pursuit deadline");
        storm(evidence, "combat_pressure", "Storm's Edge melee refresh chance",
                "Melee hit then refresh preparation", "storms_edge_melee + storms_edge_refresh",
                "+5 points per stack; 4 stacks; 60t window",
                "MELEE_HIT observer and REFRESH_CHANCE tuning", "Stack TTL; clear on refresh",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Current hit builds before its roll; stacks expire and clear as described");
        storm(evidence, "combat_reversal", "Charged Pursuit melee rider",
                "Melee hit during Pursuit", "storms_edge_melee", "20% corridor scaling; 10t lockout",
                "LIVE_WIRE stack gate and secondary lightning damage", "Stack TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Damage and lockout are reachable while Pursuit is active");
        storm(evidence, "combat_reserve", "Next Stormbreak after refresh",
                "Refresh proc then Stormbreak preparation", "storms_edge_refresh + stormbreak",
                "120% corridor and thunderclap scaling; 100t window",
                "FEEDBACK runtime consumed at Stormbreak START", "Stack TTL; consumed on start",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The prepared execution retains the bonus before the state is consumed");
        storm(evidence, "combat_threshold", "Charged Pursuit duration",
                "Kill during Pursuit", "storms_edge_melee + stormbreak", "+40t; max 120t remaining",
                "Dead-target observer and PURSUIT runtime extension", "Stack TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Only damage events extend Pursuit, refresh notifications are excluded, and the six-second cap is preserved");
        storm(evidence, "combat_convergence", "Sprinting defensive response",
                "Successful incoming damage while sprinting", "damage hook",
                "Speed II + Resistance I 30t; 120t cooldown",
                "SkillRuntime damage-received dispatch", "Per-stack mastery cooldown",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Damage callback observes the victim's sprint state and starts the node cooldown");
        storm(evidence, "combat_focus", "Refresh overdrive capstone",
                "Refresh proc then Stormbreak preparation", "storms_edge_refresh + stormbreak",
                "120t window; cooldown x0.7",
                "OVERDRIVE runtime and UniqueAbilityExecution cooldown key", "Stack TTL; consumed on start",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Works during the original cooldown because refresh clears that cooldown first");
        storm(evidence, "combat_release", "Refresh lightning capstone",
                "Refresh proc", "storms_edge_refresh", "3 targets; 5 blocks; 35% thunderclap scaling",
                "REFRESH_PROC chain-target observer", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Lethal refreshes use the additive position-origin chain query while the existing entity-origin API remains unchanged");
        storm(evidence, "transformation_opening", "Stormbreak damage mark",
                "Stormbreak damage event", "stormbreak", "Ionized 100t",
                "Actor-target world mark table", "TTL and periodic entity pruning",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "SkillRuntime lifecycle clearing now removes actor marks in addition to TTL and entity pruning");
        storm(evidence, "transformation_cadence", "Ionized melee chain",
                "Melee hit against Ionized target", "storms_edge_melee", "30% corridor scaling; 20t lockout; 5 blocks",
                "ARC_LASH mark gate and chain-target observer", "Stack TTL plus Ionized TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Killing blows use position-origin target discovery and retain the defeated enemy as the visual origin");
        storm(evidence, "transformation_pressure", "Ionized thunderclap debuff",
                "Thunderclap hit on Ionized target", "stormbreak", "Weakness I 60t",
                "THUNDERCLAP_HIT observer", "Status and Ionized TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Ionize is applied earlier in node order and the Weakness gate is reachable");
        storm(evidence, "transformation_reversal", "Thunderclap vertical control",
                "Thunderclap hit", "stormbreak", "Knock-up x1.5; Slow Falling 40t",
                "Thunderclap tuning and THUNDERCLAP_HIT observer", "Execution and status duration",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Focused thunderclaps retain vertical launch, so both the multiplier and Slow Falling remain effective");
        storm(evidence, "transformation_reserve", "Ionized kill discharge",
                "Dead Ionized event target", "storms_edge_melee + stormbreak", "25% thunderclap scaling; 3 blocks; 8 targets",
                "FULMINATION mark consume and area damage", "Ionized mark consumed",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Melee and every Stormbreak damage event can discharge the mark once");
        storm(evidence, "transformation_threshold", "Thunderclap defensive reward",
                "Thunderclap finish with at least one hit", "stormbreak", "Absorption 80t; amplifier II at 4 hits",
                "THUNDERCLAP_FINISH affected-target count", "Status duration",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Affected-target threshold maps to the advertised stronger shield");
        storm(evidence, "transformation_convergence", "Delayed thunderclap echo",
                "15t after thunderclap", "stormbreak", "30% thunderclap damage; no force",
                "ResidualStorm aftershock pulse", "Execution finishes after pulse",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Delay, damage source, area, and no-knockback behavior match");
        storm(evidence, "transformation_focus", "Single-target thunderclap capstone",
                "After primary thunderclap", "stormbreak", "100% thunderclap damage",
                "Strongest surviving hit-target selection", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Selects maximum health, then current health, then nearest surviving target");
        storm(evidence, "transformation_release", "Persistent thunderclap capstone",
                "Every 20t for 80t", "stormbreak", "20% thunderclap damage; inward pull 0.8",
                "ResidualStorm area pulse capped at 16 targets", "Execution finishes at 80t or cancels with actor",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Produces four bounded pulses and cleans up its execution");
        return evidence;
    }

    private static void storm(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                              String baseMechanic, String trigger, String definitions, String tuning,
                              String consumer, String cleanup, MasteryNodeAuditReport.Verdict verdict,
                              String finding) {
        evidence.put("storms_edge/storms_edge_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, definitions, tuning, consumer, cleanup,
                "Storm's Edge route and focused regression suite", verdict, finding));
    }
}
