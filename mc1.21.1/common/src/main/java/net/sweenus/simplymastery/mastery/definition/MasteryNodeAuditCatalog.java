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
        brimstone(evidence, "signature_opening", "Brimstone eruption proc",
                "Melee hit", "brimstone_eruption", "+5 proc chance",
                "BrimstoneClaymoreItem eruption roll", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "The tuned chance is read from the built execution before the roll");
        brimstone(evidence, "signature_cadence", "Eruption ignite and burning bonus",
                "Eruption hit", "brimstone_eruption", "Fire 100t; burning damage x1.15",
                "BrimstoneEruptionManager ignite and burning multiplier", "Status duration",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "The burning check is sampled before the eruption ignites its targets");
        brimstone(evidence, "signature_pressure", "Eruption area and damage",
                "Eruption", "brimstone_eruption", "+1 radius; damage x1.15",
                "BrimstoneEruptionManager radius query and damage multiplier", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Both modifiers are consumed by the area query and damage");
        brimstone(evidence, "signature_reversal", "Eruption proc chance",
                "Eruption roll failure", "brimstone_eruption", "+5 chance per stack; 3 stacks; 80t window",
                "KINDLING runtime read during eruption tuning", "Stack TTL; cleared at eruption start",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Stacks build on the cancelled execution and are read before the next roll; killing blows now erupt, "
                        + "so a cleared stack always buys a real eruption");
        brimstone(evidence, "signature_reserve", "Eruption proc chance",
                "Eruption preparation at maximum Kindling", "brimstone_eruption", "Proc chance 100",
                "BrimstoneClaymoreItem eruption roll", "Consumed with the Kindling stacks",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Reachable because Kindling caps at the required stack count");
        brimstone(evidence, "signature_threshold", "Eruption follow-up damage",
                "Eruption", "brimstone_eruption", "3 cinders; 6 blocks; 20% damage; 40t fire",
                "BrimstoneEruptionManager scatterCinders position query", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Cinders are selected from the eruption origin and exclude targets already hit");
        brimstone(evidence, "signature_convergence", "Eruption force and control",
                "Eruption hit", "brimstone_eruption", "Backdraft mode; pull 0.8; Slowness I 40t",
                "BrimstoneEruptionManager applyBackdraftPull after the damage call", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the pull is applied after the damage so vanilla knockback cannot halve or reverse it");
        brimstone(evidence, "signature_focus", "Eruption chain capstone",
                "Enemy defeated by the eruption", "brimstone_eruption", "50% damage; 3 blocks; 8 detonations",
                "BrimstoneEruptionManager chainReactions defeated-target loop", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Detonations originate from each defeated enemy and never damage the same target twice");
        brimstone(evidence, "signature_release", "Eruption focus capstone",
                "Eruption", "brimstone_eruption", "Radius x0.6; primary damage x2",
                "BrimstoneEruptionManager radius and primary multipliers", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "The crucible mode identifier is informational; the radius and primary multipliers carry the behaviour");
        brimstone(evidence, "combat_opening", "Brimstone Rite target jump",
                "Tracked target lost", "brimstone_rite", "+4 blocks jump range",
                "BrimstoneClaymoreAbilityManager findJumpTarget range", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Idles only while Walking Furnace replaces target tracking");
        brimstone(evidence, "combat_cadence", "Brimstone Rite pulse cadence",
                "Ability preparation", "brimstone_rite", "Pulse interval 16t",
                "BrimstoneClaymoreAbilityManager pulse scheduling", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The tuned interval sets the first pulse and every reschedule");
        brimstone(evidence, "combat_pressure", "Brimstone Rite radius growth",
                "Pulse that hits", "brimstone_rite", "+0.15 growth per hit; +1 maximum radius",
                "BrimstoneClaymoreAbilityManager radius growth clamp", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Growth and the raised ceiling are both consumed");
        brimstone(evidence, "combat_reversal", "Brimstone Rite pulse control",
                "Pulse hit", "brimstone_rite", "Pull 0.4; Slowness I 40t",
                "BrimstoneClaymoreAbilityManager applyPulseControl after damage", "Status duration",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Control runs after the damage, so the pull survives knockback");
        brimstone(evidence, "combat_reserve", "Brimstone Rite damage scaling",
                "Pulse that hits", "brimstone_rite", "+10% per pulse; 50% cap",
                "BrimstoneClaymoreAbilityManager pulse and plunge multipliers", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The cap is reached in five hitting pulses and bounds both uses");
        brimstone(evidence, "combat_threshold", "Brimstone Rite target jump",
                "Target jump", "brimstone_rite", "35% pulse damage; 3 blocks",
                "BrimstoneClaymoreAbilityManager updateTarget snapback burst", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Idles only while Walking Furnace replaces target tracking");
        brimstone(evidence, "combat_convergence", "Brimstone Rite field movement",
                "Field travel", "brimstone_rite", "40t duration; 20t interval; 20% damage; 1.5 block spacing",
                "BrimstoneClaymoreAbilityManager createWake spacing check and tickWakes",
                "Wake TTL; 16 wake cap; execution finishes after the last wake",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: spacing is measured from the last wake instead of last tick's position, so a moving field "
                        + "leaves a trail");
        brimstone(evidence, "combat_focus", "Brimstone Rite duration capstone",
                "Ability preparation", "brimstone_rite", "Duration 60t; plunge damage x2.25",
                "BrimstoneClaymoreAbilityManager expiry and impact multiplier", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "The executioner duration mode identifier is informational; duration and final damage carry the behaviour");
        brimstone(evidence, "combat_release", "Brimstone Rite duration capstone",
                "Pulse that hits", "brimstone_rite", "Duration 200t; 75% to 150% at +10% per pulse; plunge 50%",
                "BrimstoneClaymoreAbilityManager perpetual pulse multiplier growth", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: +10% per pulse makes the advertised cap reachable within the cast at both pulse intervals");
        brimstone(evidence, "transformation_opening", "Brimstone Rite activation",
                "Rite start", "brimstone_rite", "Absorption I 80t",
                "START observer", "Status duration",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The start event fires after the field instance is created");
        brimstone(evidence, "transformation_cadence", "Brimstone Rite duration",
                "Rite start", "brimstone_rite", "Fire Resistance for the duration plus 18t",
                "START observer", "Status duration",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The padding covers the plunge window");
        brimstone(evidence, "transformation_pressure", "Damage taken during the rite",
                "Damage received, then a pulse that hits", "brimstone_rite", "2 stacks; 80t window; Absorption 80t",
                "SkillRuntime damage-received dispatch and PULSE_FINISH observer",
                "Stack TTL; cleared on rite finish or cancel",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The amplifier follows Minecraft's zero-based stack count");
        brimstone(evidence, "transformation_reversal", "Damage taken during the rite",
                "Melee attacker during the rite", "brimstone_rite", "20% pulse damage; 20t lockout; 40t fire",
                "SkillRuntime damage-received dispatch", "Lockout TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Secondary damage is origin-guarded and cannot re-enter the mastery dispatch");
        brimstone(evidence, "transformation_reserve", "Brimstone Rite pulse",
                "Pulse that hits below 40% health", "brimstone_rite", "Resistance I 60t; Absorption II 80t",
                "PULSE_FINISH observer", "Flag expires with the rite",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Once per cast; the flag deadline is taken from the live rite deadline");
        brimstone(evidence, "transformation_threshold", "Damage taken during the rite",
                "Damage received", "brimstone_rite", "Speed I 60t; 100t cooldown",
                "SkillRuntime damage-received dispatch", "Cooldown TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The node cooldown matches the advertised five seconds");
        brimstone(evidence, "transformation_convergence", "Brimstone Rite pulse",
                "Pulse hitting three or more enemies", "brimstone_rite", "Absorption I 60t",
                "PULSE_FINISH affected-target count", "Status duration",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The affected-target threshold maps to the advertised shield");
        brimstone(evidence, "transformation_focus", "Brimstone Rite guard capstone",
                "Rite start", "brimstone_rite", "Walking mode; radius 85%; pull 0.5; Resistance I",
                "BrimstoneClaymoreAbilityManager owner-follow branch and applyPulseControl", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Replaces target tracking, so the target-jump nodes idle while it is selected");
        brimstone(evidence, "transformation_release", "Brimstone Rite guard capstone",
                "Health crossing 35% during the rite", "brimstone_rite",
                "Last reprisal mode; plunge x1.75; Absorption II and Resistance II 60t",
                "BrimstoneClaymoreAbilityManager triggerEmergencyPlunge and EMERGENCY_PLUNGE observer",
                "Execution finishes after the early plunge",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "A downward crossing is required and the health ratio is sampled every tick");
        watcher(evidence, "signature_opening", "Dread mark duration",
                "Melee hit", "watcher/dread", "Stack duration 260t",
                "WatcherAbilityManager mark expiry", "Mark TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The tuned duration is written on every refresh");
        watcher(evidence, "signature_cadence", "Dread stack cap",
                "Melee hit", "watcher/dread + watcher/final_omen", "Stack cap 6",
                "WatcherAbilityManager dread clamp and omen dread progress", "Mark TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The cap governs both stacking and the omen dread ramp");
        watcher(evidence, "signature_pressure", "Watched target cap",
                "New mark", "watcher/dread", "Marked target cap 7",
                "WatcherAbilityManager evictOldestMarkIfNeeded", "Oldest mark released with its bats",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Eviction is ordered by mark creation tick");
        watcher(evidence, "signature_reversal", "Melee damage against Watched enemies",
                "Melee hit", "watcher/dread", "+3% per Dread; 18% cap",
                "WatcherAbilityManager pre-increment bonus strike", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The bonus reads the dread present before this hit adds to it");
        watcher(evidence, "signature_reserve", "Dread spread",
                "Melee hit at 4 Dread", "watcher/dread", "4 blocks; 40t lockout",
                "WatcherAbilityManager shared terror query", "Spread mark TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the radius moved to its own setting, so later nodes no longer widen or disable the spread");
        watcher(evidence, "signature_threshold", "Dread gain per hit",
                "Repeat hit within 30t", "watcher/dread", "2 Dread; 30t window; 80t lockout",
                "WatcherAbilityManager unblinking gate", "Per-mark lockout",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the window and lockout moved to dedicated settings and no longer collide with the dread gates");
        watcher(evidence, "signature_convergence", "Final Omen cooldown",
                "Omen resolution at maximum Dread", "watcher/final_omen", "30t refund",
                "WatcherAbilityManager resolve cooldown branch", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Applies when the target survives; a kill already clears the cooldown");
        watcher(evidence, "signature_focus", "Final Omen target selection capstone",
                "Ability preparation", "watcher/dread + watcher/final_omen",
                "Stack cap 3; marked cap 12; 3 targets within 12 blocks; damage x0.7; cooldown x1.25",
                "WatcherAbilityManager additionalOmenTargets and manyEyedScale", "Each consumed mark is removed",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the capstone now starts an omen on each consumed mark and applies its damage penalty");
        watcher(evidence, "signature_release", "Dread concentration capstone",
                "New mark", "watcher/dread + watcher/final_omen", "Single mark; stack cap 8; +20% per stack",
                "WatcherAbilityManager mark clearing and final per-stack multiplier", "Previous marks released",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Clearing and the per-stack multiplier are both consumed");
        watcher(evidence, "combat_opening", "Final Omen duration",
                "Ability preparation", "watcher/final_omen", "Duration 54t",
                "WatcherAbilityManager omen impact tick", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Swoop scheduling rescales with the duration, so no swoop is lost");
        watcher(evidence, "combat_cadence", "Final Omen swoop count",
                "Ability preparation", "watcher/final_omen", "+1 swoop per Dread; 17 total",
                "WatcherAbilityManager totalSwoops", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the total is clamped to the advertised maximum instead of exceeding it");
        watcher(evidence, "combat_pressure", "Final Omen swoop damage",
                "Ability preparation", "watcher/final_omen", "Swoop damage x1.12",
                "WatcherAbilityManager swoop damage", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Multiplies the configured swoop scaling");
        watcher(evidence, "combat_reversal", "Final Omen control",
                "Ability start", "watcher/final_omen", "Slowness IV 90t",
                "WatcherAbilityManager omen slowness", "Status duration",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the guard nodes no longer write the omen slowness duration");
        watcher(evidence, "combat_reserve", "Final Omen damage ramp",
                "Bat swoop that hits", "watcher/final_omen", "+3% per swoop; 36% cap",
                "WatcherAbilityManager gathered bonus at resolve", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: Claimed Vitality now uses its own bonus settings and cannot rewrite this ramp");
        watcher(evidence, "combat_threshold", "Final Omen tether",
                "Target beyond 12 blocks", "watcher/final_omen", "Pull 2; once per cast",
                "WatcherAbilityManager omen tick pull", "Execution-scoped flag",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Fires at most once per omen and only past the tuned range");
        watcher(evidence, "combat_convergence", "Final Omen damage",
                "Omen resolution", "watcher/final_omen", "Final damage x1.15; missing-health cap 0.9",
                "WatcherAbilityManager resolve damage", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Both multipliers reach the resolve calculation");
        watcher(evidence, "combat_focus", "Final Omen duration capstone",
                "Ability preparation", "watcher/final_omen",
                "Duration 100t; 20 swoops; swoop damage 0.65; final damage x0.65; cooldown +60t",
                "WatcherAbilityManager totalSwoops swoop-count override", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the advertised swoop count is now read from the capstone instead of the dread ramp");
        watcher(evidence, "combat_release", "Final Omen duration capstone",
                "Ability preparation", "watcher/final_omen",
                "Duration 20t; no swoops; final damage x1.9; no slowness; missing-health cap 0.35",
                "WatcherAbilityManager totalSwoops swoop-count override", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the capstone now actually suppresses its bats, and no later node restores the slowness");
        watcher(evidence, "transformation_opening", "Final Omen execution",
                "Omen resolution at maximum Dread", "watcher/final_omen", "Execute threshold 28%",
                "WatcherAbilityManager execute branch", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: entities in the execution-immune tag are excluded, matching the description");
        watcher(evidence, "transformation_cadence", "Execution absorption",
                "Execution", "watcher/final_omen", "Absorption x1.15",
                "WatcherAbilityManager grantOmenAbsorption", "Absorption cap",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Multiplies the vitality converted into absorption");
        watcher(evidence, "transformation_pressure", "Non-lethal Final Omen",
                "Omen resolution without a kill", "watcher/final_omen", "1 Absorption per 10% health removed; cap 4",
                "WatcherAbilityManager reserve branch", "Absorption cap",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the unread duration write is gone, so the omen slowness keeps its own value");
        watcher(evidence, "transformation_reversal", "Final Omen kills",
                "Bat swoop or omen kill", "watcher/final_omen", "Cooldown reset; Resistance I 60t",
                "WatcherAbilityManager swoop and resolve kill branches", "Status duration",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the unread duration write is gone; both kill paths grant the buff");
        watcher(evidence, "transformation_reserve", "Incoming melee damage",
                "Melee hit from a Watched enemy with 4 Dread", "watcher/dread", "Damage x0.85",
                "WatcherAbilityManager modifyIncomingDamage", "Owner state TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the node now has a consumer through the shared incoming-damage hook");
        watcher(evidence, "transformation_threshold", "Low health response",
                "Health crossing 30%", "watcher/dread", "2 Dread within 12 blocks; 200t lockout",
                "WatcherAbilityManager modifyIncomingDamage witness branch", "Owner state TTL and lockout",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the crossing is evaluated against the pending damage and applies dread to the nearest mark");
        watcher(evidence, "transformation_convergence", "Absorption from Final Omen",
                "Omen absorption then the next melee hit", "watcher/final_omen + watcher/dread",
                "+5% per point; 40% cap; 120t window",
                "WatcherAbilityManager recordClaim and applyClaimedVitality", "Claim deadline; consumed on use",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the buff has its own settings and no longer rewrites the Final Omen duration");
        watcher(evidence, "transformation_focus", "Execution replacement capstone",
                "Omen resolution at maximum Dread", "watcher/final_omen",
                "Threshold 28%; 1 health; rooted 60t; absorption x1.5; no cooldown reset",
                "WatcherAbilityManager mercy branch", "Status duration",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: a positive threshold makes the branch reachable, absorption is granted, and swoop kills no longer reset the cooldown");
        watcher(evidence, "transformation_release", "Execution capstone",
                "Omen resolution at maximum Dread", "watcher/final_omen",
                "Threshold 35%; no absorption; failed execution x0.7 and +100t",
                "WatcherAbilityManager absoluteReady and failure branch", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the dread gate is clamped to the reachable maximum and the cooldown penalty applies only on failure");
        devourer(evidence, "signature_opening", "Devouring Mass area",
                "Ability preparation", "devourer/mass", "Radius 4.3; scan radius 8",
                "DevourerAbilityManager mass radius and acquireTargets", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Both radii are consumed at cast and during acquisition");
        devourer(evidence, "signature_cadence", "Devouring Mass pulse damage",
                "Ability preparation", "devourer/mass", "Damage x1.1",
                "DevourerAbilityManager cast damage snapshot", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Folded into the mass damage before any pulse");
        devourer(evidence, "signature_pressure", "Devouring Mass pull",
                "Held target tick", "devourer/mass", "Pull 0.34",
                "DevourerAbilityManager pullTowardSlot", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the reprisal capstone no longer overwrites the mass pull strength");
        devourer(evidence, "signature_reversal", "Devouring Mass duration",
                "Ability preparation", "devourer/mass", "Duration 900t",
                "DevourerAbilityManager active window", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Drives the visual, active window, and collapse schedule");
        devourer(evidence, "signature_reserve", "Devouring Mass capacity",
                "Target acquisition", "devourer/mass", "Target cap 8",
                "DevourerAbilityManager acquireTargets", "Tendrils released with the mass",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The cap bounds both acquisition and held targets");
        devourer(evidence, "signature_threshold", "Devouring Mass pulse cadence",
                "Target held for 100t", "devourer/mass", "Accelerated interval 16t after 100t",
                "DevourerAbilityManager pulseInterval", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the accelerated interval has its own setting, so Event Horizon cannot slow it");
        devourer(evidence, "signature_convergence", "Devouring Mass collapse",
                "Collapse", "devourer/mass", "125% damage; 4.5 blocks; 12 targets",
                "DevourerAbilityManager collapseDamage", "Execution finishes after the collapse",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Value-gated and bounded by its own target cap");
        devourer(evidence, "signature_focus", "Devouring Mass movement capstone",
                "Active tick", "devourer/mass", "Follow range 10; speed 0.25; duration 600t; radius 3; damage x0.8",
                "DevourerAbilityManager tickFollow", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the mass now tracks the nearest valid enemy and carries its gloam field with it");
        devourer(evidence, "signature_release", "Devouring Mass anchor capstone",
                "Ability preparation", "devourer/mass",
                "Radius 6; target cap 12; pull 0.42; pulse interval 30t; cooldown x1.25",
                "DevourerAbilityManager pulseInterval base interval", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the tuned interval is now the base pulse cadence rather than only the accelerated one");
        devourer(evidence, "combat_opening", "Devouring Mass loose collection",
                "Loose acquisition", "devourer/mass", "96 loose targets; launch speed 0.42",
                "DevourerAbilityManager acquireLooseTarget", "Loose targets released at collapse",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Both values reach the loose-target path");
        devourer(evidence, "combat_cadence", "Devouring Mass tendrils",
                "Loose acquisition", "devourer/mass", "Tendril cap 5",
                "DevourerAbilityManager tendril clamp", "Tendrils discarded with the mass",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The manager clamp admits the raised cap");
        devourer(evidence, "combat_pressure", "Gloam field",
                "Ability preparation", "devourer/mass", "16 carriers; trail 1.6",
                "DevourerStainManager field", "Field ends with the mass",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Carrier cap and trail width are passed at field creation");
        devourer(evidence, "combat_reversal", "Gloam field",
                "Gloam contact", "devourer/mass", "Carrier duration 80t; Slowness II",
                "DevourerStainManager and GloamMechanicsManager", "Carrier TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired description: the slow tier is tuned while its duration is contact-refreshed");
        devourer(evidence, "combat_reserve", "Devouring Mass pulse damage",
                "Pulse with multiple held enemies", "devourer/mass", "+3% per extra enemy; 24% cap",
                "DevourerAbilityManager compression", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: Routed Prey now uses its own bonus setting and cannot rewrite the compression ramp");
        devourer(evidence, "combat_threshold", "Kills inside Devouring Mass",
                "Pulse kill", "devourer/mass", "+20t per kill; 100t cap",
                "DevourerAbilityManager onMassKill and extendedEnd", "Extension bounded by the cast cap",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: pulse kills are now detected, emit the kill event, and extend the active window");
        devourer(evidence, "combat_convergence", "Long-held target",
                "Target held for 160t", "devourer/mass", "75% pulse damage; once per enemy",
                "DevourerAbilityManager rupture branch", "Per-target flag for the cast",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: Pack Retaliation moved off the shared secondary multiplier, so the rupture keeps its value");
        devourer(evidence, "combat_focus", "Devouring Mass loose capstone",
                "Ability preparation", "devourer/mass", "160 loose targets; launch 0.84; target cap 3; damage x0.7",
                "DevourerAbilityManager loose and target caps", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "All four values are consumed by their existing paths");
        devourer(evidence, "combat_release", "Devouring Mass focus capstone",
                "Pulse kill", "devourer/mass", "No gloam or loose collection; damage x1.75; 20t refund per kill; 160t cap",
                "DevourerAbilityManager mode gate and onMassKill refund", "Refund bounded per cast",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: kills now refund cooldown against the elapsed cast time, capped as advertised");
        devourer(evidence, "transformation_opening", "Ravenous Reprisal",
                "Reprisal trigger", "devourer/reprisal", "Slowness I 40t on the attacker",
                "DevourerReprisalManager applyReprisalStatus", "Status duration",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the reprisal now applies the tuned status to the triggering attacker");
        devourer(evidence, "transformation_cadence", "Ravenous Reprisal area",
                "Reprisal trigger", "devourer/reprisal", "Radius 4",
                "DevourerReprisalManager collectTargets", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "The tuned radius bounds the maw's target search");
        devourer(evidence, "transformation_pressure", "Ravenous Reprisal drag",
                "Drag tick", "devourer/reprisal", "Pull 0.38",
                "DevourerReprisalManager tickPull", "Drag window",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the drag reads the tuned pull instead of the configuration value");
        devourer(evidence, "transformation_reversal", "Ravenous Reprisal damage",
                "Reprisal trigger", "devourer/reprisal", "Damage x1.12",
                "DevourerReprisalManager damage", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED, "Multiplies the configured reprisal scaling");
        devourer(evidence, "transformation_reserve", "Reprisal into Devouring Mass",
                "Attacker redirected into the mass", "devourer/reprisal + devourer/mass", "+25% on the next pulse",
                "DevourerAbilityManager markRouted and routedBonus", "Consumed on the next pulse; window TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the redirect marks the target and the mass spends the bonus exactly once");
        devourer(evidence, "transformation_threshold", "Ravenous Reprisal trigger",
                "Reprisal trigger", "devourer/reprisal", "3 Absorption",
                "DevourerReprisalManager grantReprisalAbsorption", "Absorption cap",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: absorption is granted and the description no longer claims a timer");
        devourer(evidence, "transformation_convergence", "Ravenous Reprisal targets",
                "Reprisal trigger", "devourer/reprisal", "9 targets; 70% damage to others",
                "DevourerReprisalManager targetDamage", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: non-attacker targets are scaled by their own multiplier");
        devourer(evidence, "transformation_focus", "Ravenous Reprisal guard capstone",
                "Reprisal trigger", "devourer/reprisal", "Damage x0.5; push 2; Resistance I 60t; 80t lockout",
                "DevourerReprisalManager push branch and applyGuardianBoon", "Lockout TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Redesigned: the impossible damage reduction became a push plus a guard buff, since a deflect already negates the hit");
        devourer(evidence, "transformation_release", "Ravenous Reprisal focus capstone",
                "Reprisal trigger", "devourer/reprisal + devourer/mass",
                "Damage x2.25; single target; 30 block drag; 40t lockout",
                "DevourerReprisalManager drag range and lockout", "Lockout TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the drag range and lockout are now consumed by the redirect");
        wickpiercer(evidence, "signature_opening", "Wickpiercer throw damage",
                "Ability preparation", "wickpiercer/throw", "Projectile damage x1.1",
                "WickpiercerSwordItem primaryBaseDamage", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: applied multiplicatively so a capstone no longer discards it");
        wickpiercer(evidence, "signature_cadence", "Wickpiercer throw velocity",
                "Ability preparation", "wickpiercer/throw", "Projectile speed 1.8",
                "WickpiercerSwordItem setVelocity", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired description: 1.5 to 1.8 is a 20% increase");
        wickpiercer(evidence, "signature_pressure", "Wickpiercer return path",
                "Loyalty return tick", "wickpiercer/throw", "Trail radius 2.5; trail damage x0.5; lifetime 100t",
                "WickpiercerEntity damageOnReturn", "Per-target set released with the projectile",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Redesigned: the old lifetime value never reached a player throw; the node now burns enemies on the return path");
        wickpiercer(evidence, "signature_reversal", "Wickpiercer direct hit burn",
                "Projectile hit", "wickpiercer/throw", "Projectile fire 60t",
                "WickpiercerEntity onSuccessfulHit", "Vanilla fire ticks",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: moved off the shared FIRE_TICKS setting used by the melee and revive nodes");
        wickpiercer(evidence, "signature_reserve", "Wickpiercer return speed",
                "Ability preparation", "wickpiercer/throw", "Loyalty 5; cooldown 26t",
                "ThrownSpearEntity loyalty return; delegated activation cooldown", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Redesigned: the player throw path applies no cooldown, so the node now speeds up the return instead");
        wickpiercer(evidence, "signature_threshold", "Wickpiercer piercing",
                "Projectile hit", "wickpiercer/throw", "Pierce 1; pierce damage x0.7",
                "WickpiercerEntity getAdditionalPierces and getPierceDamageMultiplier", "Pierced set released with the projectile",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: pierce damage no longer shares a setting with the orbit capstone; the unimplementable boss clause was removed");
        wickpiercer(evidence, "signature_convergence", "Wickpiercer impact burst",
                "Projectile hit", "wickpiercer/throw", "Burst x0.35; radius 2.5; cap 5",
                "WickpiercerEntity burst", "Once per projectile",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the burst settings are no longer shared with Wildfire Frenzy or Funeral Pyre");
        wickpiercer(evidence, "signature_focus", "Comet Wick focus capstone",
                "Ability preparation", "wickpiercer/throw", "Mode 1; speed 2.2; damage x1.8; loyalty 0; return delay 60t",
                "WickpiercerEntity tickDelayedReturn", "Loyalty restored after the delay",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the delayed return is implemented, so the thrown weapon comes back instead of being left behind");
        wickpiercer(evidence, "signature_release", "Orbiting Taper release capstone",
                "Projectile hit", "wickpiercer/throw", "Mode 2; orbit 60t; interval 20t; strike x0.45; damage x0.6; cooldown 100t",
                "WickpiercerEntity startOrbit and tickOrbit", "Orbit released on expiry or target death",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the orbit is implemented; previously only the damage penalty applied");
        wickpiercer(evidence, "combat_opening", "Frenzy duration",
                "Wickpiercer throw", "wickpiercer/throw", "Stack duration 110t",
                "WickpiercerSwordItem frenzy application", "Status effect TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the dual-wield doubling now applies to the tuned duration");
        wickpiercer(evidence, "combat_cadence", "Frenzy stack cap",
                "Wickpiercer throw", "wickpiercer/throw", "Stack cap 5",
                "WickpiercerSwordItem frenzy application", "Status effect TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the duplicate hard-capped frenzy application was removed, so the tuned cap is reachable");
        wickpiercer(evidence, "combat_pressure", "Frenzy attack damage",
                "Frenzy melee hit", "wickpiercer/throw", "Melee bonus x1.1",
                "Phase2CombatStateManager applyWickFrenzyHit", "Wick state TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: composes with the release capstone instead of being discarded by a hard-coded multiplier");
        wickpiercer(evidence, "combat_reversal", "Frenzy conservation",
                "Frenzy killing blow", "wickpiercer/throw", "Mode 4; lockout 20t",
                "Phase2CombatStateManager conserve branch", "Lockout carried across throws",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the lockout no longer shares a setting with three other nodes");
        wickpiercer(evidence, "combat_reserve", "Frenzy ignition",
                "Frenzy melee hit", "wickpiercer/throw", "Mode 8; fire 40t; burn bonus 0.2",
                "Phase2CombatStateManager applyWickFrenzyHit", "Vanilla fire ticks",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the burning bonus moved off the chain node's shared setting");
        wickpiercer(evidence, "combat_threshold", "Frenzy chain",
                "Frenzy melee hit", "wickpiercer/throw", "Mode 16; window 40t; bonus 0.05; cap 0.2",
                "Phase2CombatStateManager chain branch", "Wick state TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the chain window is no longer stretched by the throw and revive nodes");
        wickpiercer(evidence, "combat_convergence", "Dual-wield frenzy grant",
                "Wickpiercer throw", "wickpiercer/throw", "Mode 32; grant 2; lockout 40t",
                "Phase2CombatStateManager frenzyGrant and consumption", "Lockout carried across throws",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: mode bit 32 had no consumer, so the node did nothing");
        wickpiercer(evidence, "combat_focus", "Wildfire Frenzy focus capstone",
                "Frenzy melee hit", "wickpiercer/throw", "Mode 64; burst x0.45; radius 3; cap 6",
                "Phase2CombatStateManager burst and consumption", "Wick state TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: its own burst settings, and kills are no longer conserved as the description states");
        wickpiercer(evidence, "combat_release", "White-Hot Focus release capstone",
                "Frenzy melee hit", "wickpiercer/throw", "Mode 128; stack cap 3; melee bonus x1.75; armor ignore 4",
                "Phase2CombatStateManager armorIgnoreMultiplier", "Wick state TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the armor ignore is implemented and the stack cap is reachable");
        wickpiercer(evidence, "transformation_opening", "Frenzy damage reduction",
                "Incoming damage", "wickpiercer/throw", "Reduction 0.1",
                "Phase2CombatStateManager modifyIncomingDamage", "Wick state TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: no Wickpiercer incoming-damage hook existed, so the node was inert");
        wickpiercer(evidence, "transformation_cadence", "Revival absorption",
                "Revival", "wickpiercer/revive", "Absorption 4",
                "WickpiercerSwordItem postRevive", "Absorption consumed by damage",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the redundant full-health and phantom duration clauses were removed");
        wickpiercer(evidence, "transformation_pressure", "Revival resistance duration",
                "Revival", "wickpiercer/revive", "Status duration 140t",
                "WickpiercerSwordItem postRevive", "Status effect TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the ally regeneration and low-health guard now use their own durations");
        wickpiercer(evidence, "transformation_reversal", "Low-health guard",
                "Incoming damage", "wickpiercer/throw", "Mode 256; threshold 30%; status 60t; lockout 300t",
                "Phase2CombatStateManager modifyIncomingDamage", "Lockout carried across throws",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: mode bit 256 had no consumer, so the node was inert");
        wickpiercer(evidence, "transformation_reserve", "Ally regeneration",
                "Revival", "wickpiercer/revive", "Mode 512; radius 6; cap 8; ally status 80t",
                "WickpiercerSwordItem regenerateAllies", "Status effect TTL",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: the regeneration duration no longer follows the revival resistance duration");
        wickpiercer(evidence, "transformation_threshold", "Post-revival strike",
                "First attack after reviving", "wickpiercer/revive", "Mode 1024; window 100t; damage x0.6; fire 100t",
                "Phase2CombatStateManager applyPhoenixBlow", "Consumed on first attack or window expiry",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: nothing tracked a post-revival window, so the node was inert");
        wickpiercer(evidence, "transformation_convergence", "Revival cooldown reduction",
                "Revival", "wickpiercer/revive", "Cooldown x0.85; floor 600t",
                "WickpiercerSwordItem postRevive", "Weapon cooldown",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: compounds with the focus capstone and no longer leaks a cooldown onto the throw ability");
        wickpiercer(evidence, "transformation_focus", "Everlasting Wick focus capstone",
                "Revival", "wickpiercer/revive", "Mode 2048; health x0.5; cooldown x0.6; Resistance II 80t",
                "WickpiercerSwordItem getReviveHealth and postRevive", "Weapon cooldown",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Verified: health, cooldown, and resistance all route correctly");
        wickpiercer(evidence, "transformation_release", "Funeral Pyre release capstone",
                "Revival", "wickpiercer/revive", "Mode 4096; health x0; pyre radius 5; pyre damage x2; cap 12",
                "WickpiercerSwordItem funeralPyre", "Execution-scoped",
                MasteryNodeAuditReport.Verdict.VERIFIED,
                "Repaired: its own blast settings, and the description no longer claims you stop reviving");
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

    private static void brimstone(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                  String baseMechanic, String trigger, String definitions, String tuning,
                                  String consumer, String cleanup, MasteryNodeAuditReport.Verdict verdict,
                                  String finding) {
        evidence.put("brimstone_claymore/brimstone_claymore_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, definitions, tuning, consumer, cleanup,
                "Brimstone Claymore route and focused regression suite", verdict, finding));
    }

    private static void watcher(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                String baseMechanic, String trigger, String definitions, String tuning,
                                String consumer, String cleanup, MasteryNodeAuditReport.Verdict verdict,
                                String finding) {
        evidence.put("watcher_claymore/watcher_claymore_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, definitions, tuning, consumer, cleanup,
                "The Watcher route and focused regression suite", verdict, finding));
    }

    private static void devourer(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                 String baseMechanic, String trigger, String definitions, String tuning,
                                 String consumer, String cleanup, MasteryNodeAuditReport.Verdict verdict,
                                 String finding) {
        evidence.put("the_devourer/the_devourer_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, definitions, tuning, consumer, cleanup,
                "The Devourer route and focused regression suite", verdict, finding));
    }

    private static void wickpiercer(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                    String baseMechanic, String trigger, String definitions, String tuning,
                                    String consumer, String cleanup, MasteryNodeAuditReport.Verdict verdict,
                                    String finding) {
        evidence.put("wickpiercer/wickpiercer_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, definitions, tuning, consumer, cleanup,
                "Wickpiercer route and focused regression suite", verdict, finding));
    }
}
