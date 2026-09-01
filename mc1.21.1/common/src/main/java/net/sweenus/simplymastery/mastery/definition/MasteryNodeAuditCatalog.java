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
        gloampiercer(evidence, "signature_opening", "Phantom Ambush cooldown", "Valid weapon swing",
                "gloampiercer/ambush", "Cooldown 10t", "GloampiercerAbilityManager passive gate",
                "Owner cooldown TTL", "The tuned half-second cooldown is consumed");
        gloampiercer(evidence, "signature_cadence", "Phantom Ambush clone throw", "Valid passive activation",
                "gloampiercer/ambush", "Fire delay 7t", "PendingPassiveStrike schedule", "Execution-scoped",
                "Repaired: Ambush delay is isolated from Barrage and chain delay");
        gloampiercer(evidence, "signature_pressure", "Phantom Ambush targeting", "Weapon swing target scan",
                "gloampiercer/ambush", "Cone 125 degrees; range 14", "findPassiveTargets", "Execution-scoped",
                "Repaired: passive range is no longer overwritten by Stained Ground nodes");
        gloampiercer(evidence, "signature_reversal", "Phantom Ambush damage", "Passive spear creation",
                "gloampiercer/ambush", "Projectile damage x1.1", "PendingPassiveStrike damage snapshot",
                "Projectile lifetime", "Repaired: the multiplier no longer modifies Barrage");
        gloampiercer(evidence, "signature_reserve", "Phantom Ambush cadence", "Every third committed activation",
                "gloampiercer/ambush", "Mode 1; two clones; secondary x0.7", "passiveCloneCount and target list",
                "Proc state inactivity cleanup", "Repaired: misses do not advance the proc and Barrage clone count cannot overwrite it");
        gloampiercer(evidence, "signature_threshold", "Stored Phantom Ambush", "Eligible swing without a target",
                "gloampiercer/ambush", "Mode 2; storage 80t", "StoredPassive release on the next valid swing",
                "Expiry, stack swap, death, world unload", "Repaired: a stored full-damage clone is released in addition to the next activation");
        gloampiercer(evidence, "signature_convergence", "Phantom Ambush homing", "Passive spear flight",
                "gloampiercer/ambush", "Turn 8 degrees; lifetime 100t", "GloampiercerTuningSnapshot flight math",
                "Projectile TTL", "Repaired: both values are consumed and the fixed 48-block cap no longer defeats the tuned lifetime");
        gloampiercer(evidence, "signature_focus", "Hall of Mirrors capstone", "Passive activation",
                "gloampiercer/ambush", "Three clones; damage x0.55; cooldown 24t", "Ambush-only scheduling",
                "Execution-scoped", "Repaired: its clone and damage settings no longer alter Barrage");
        gloampiercer(evidence, "signature_release", "Perfect Reflection capstone", "Passive activation",
                "gloampiercer/ambush", "One clone; damage x2.2; turn 10; cone 70; range 10",
                "Nearest-first passive targeting and spear snapshot", "Execution-scoped",
                "Repaired: the clone selects the nearest eligible enemy and consumes sharper homing");
        gloampiercer(evidence, "combat_opening", "Phantom Phalanx spear count", "Barrage preparation",
                "gloampiercer/barrage", "Spear count 20", "fireScheduledSpears", "Execution-scoped",
                "The tuned count is consumed by scheduling");
        gloampiercer(evidence, "combat_cadence", "Phantom Phalanx clone count", "Barrage preparation",
                "gloampiercer/barrage", "Clone count 6", "spawnActiveClones", "Channel TTL",
                "Repaired: Barrage clone count no longer modifies Phantom Ambush");
        gloampiercer(evidence, "combat_pressure", "Phantom Phalanx firing window", "Barrage channel",
                "gloampiercer/barrage", "Start 10t; end margin 10t", "fireScheduledSpears", "Channel TTL",
                "Repaired: the firing window is isolated from passive and chain delays");
        gloampiercer(evidence, "combat_reversal", "Phantom Phalanx spear flight", "Barrage spear creation",
                "gloampiercer/barrage", "Speed 1.7; damage x1.08", "Barrage-only channel and projectile snapshots",
                "Projectile TTL", "Repaired: neither modifier leaks onto Phantom Ambush");
        gloampiercer(evidence, "combat_reserve", "Phantom Phalanx explosion", "Barrage spear explosion",
                "gloampiercer/barrage", "Radius 3; target cap 8", "GloampiercerSpearEntity explosion query",
                "Projectile-scoped", "Repaired: chain tuning no longer overwrites the explosion cap");
        gloampiercer(evidence, "combat_threshold", "Phantom Phalanx ground cadence", "Barrage scheduling",
                "gloampiercer/barrage", "Ground interval 2", "fireSpear ground selection", "Execution-scoped",
                "Every second spear uses a bounded ground destination");
        gloampiercer(evidence, "combat_convergence", "Phantom Phalanx channel", "Barrage preparation",
                "gloampiercer/barrage", "Duration 52t; cooldown 420t", "ActiveChannel and ability cooldown key",
                "Channel and cooldown TTL", "Both values are consumed and Royal Piercer adds its cooldown afterward");
        gloampiercer(evidence, "combat_focus", "Gloam Rain capstone", "Barrage preparation and channel",
                "gloampiercer/barrage", "30 ground spears; radius 10; damage x0.65; duration 70t; retention 0",
                "fireSpear and guideOwner", "Channel TTL", "Repaired: its damage penalty is confined to Barrage");
        gloampiercer(evidence, "combat_release", "Royal Piercer capstone", "Barrage preparation",
                "gloampiercer/barrage", "Six spears; damage x2.4; radius 1.5; turn 10; cooldown +90t",
                "ActiveChannel royal target lock and projectile snapshot", "Locked target UUID and last position",
                "Repaired: all spears retain one target and consume the tuned homing angle");
        gloampiercer(evidence, "transformation_opening", "Gloam patch radius", "Spear landing",
                "gloampiercer/ambush + barrage", "Stain radius 1.8", "GloamStainManager patch bounds",
                "Patch TTL", "Every created patch consumes the tuned radius");
        gloampiercer(evidence, "transformation_cadence", "Gloam patch duration", "Spear landing",
                "gloampiercer/ambush + barrage", "Stain duration 300t", "GloamStainManager expiry",
                "Patch TTL", "Every created patch consumes the tuned duration");
        gloampiercer(evidence, "transformation_pressure", "Gloam Slowness", "Owner-patch contact",
                "gloampiercer/ambush + barrage", "Slowness II for 60t", "GloamMechanicsManager contact state",
                "Status TTL", "Repaired: the tuned lingering duration is now applied");
        gloampiercer(evidence, "transformation_reversal", "Gloam explosion vulnerability", "Explosion target in owner Gloam",
                "gloampiercer/ambush + barrage", "Explosion damage +15%", "Owner-aware patch query and damage multiplier",
                "Projectile-scoped", "Repaired: only the caster's Gloam qualifies");
        gloampiercer(evidence, "transformation_reserve", "Embedded Gloam spear", "Block impact and proximity",
                "gloampiercer/ambush + barrage", "Trigger 1.5; duration 140t", "Persistent projectile tuning snapshot",
                "NBT round trip and projectile TTL", "Repaired: reloads preserve trigger, explosion, and stain tuning");
        gloampiercer(evidence, "transformation_threshold", "Embedded spear chain", "Proximity-triggered detonation",
                "gloampiercer/ambush + barrage", "Range 5; delay 4t", "Same-owner nearest embedded spear schedule",
                "One bounded follow-up; projectile TTL", "Repaired: chain keys are isolated and a chained detonation cannot recurse");
        gloampiercer(evidence, "transformation_convergence", "Gloam explosion pull", "Explosion targets in owner Gloam",
                "gloampiercer/ambush + barrage", "Six targets; one-block pull", "Collision-checked pre-damage displacement",
                "Explosion-scoped", "Repaired: owner filtering, cap, ordering, and displacement are consumed");
        gloampiercer(evidence, "transformation_focus", "Creeping Gloam capstone", "Patch tick and explosion",
                "gloampiercer/ambush + barrage", "Range 6; speed 0.15; duration 200t; explosion x0.75",
                "GloamStainManager movement and projectile explosion multiplier", "Patch TTL and owner cleanup",
                "Repaired: patches pursue the nearest eligible enemy and the damage penalty applies");
        gloampiercer(evidence, "transformation_release", "Black Bloom capstone", "Gloam expiry",
                "gloampiercer/ambush + barrage", "No slow; duration 100t; damage x0.8; radius 2.5; cap 6",
                "PatchBehavior expiry damage", "One expiry pulse then patch removal",
                "Repaired: Slowness is suppressed and the bounded owner-attributed expiry burst fires once");
        wraithfang(evidence, "signature_opening", "Thrown Wraithfang damage", "Throw preparation",
                "Damage x1.1", "Projectile damage snapshot", "Projectile NBT",
                "Repaired: multiplies with Death's Fang instead of being overwritten");
        wraithfang(evidence, "signature_cadence", "Thrown Wraithfang velocity", "Throw preparation",
                "Speed x1.233", "Player and delegated launch velocity", "Projectile NBT",
                "Repaired: composes with Banshee Cast and Possessing Lunge");
        wraithfang(evidence, "signature_pressure", "Loyalty return", "Throw preparation",
                "Loyalty II", "Persisted desired loyalty", "Return and projectile NBT",
                "Repaired: Loyalty II survives reload and drives return speed");
        wraithfang(evidence, "signature_reversal", "Direct-hit Weakness", "Successful projectile hit",
                "Weakness I 60t", "WraithfangEntity onSuccessfulHit", "Status TTL",
                "Repaired: dash Slowness has an independent duration");
        wraithfang(evidence, "signature_reserve", "Flight damage growth", "Projectile hit after flight",
                "+0.65 per tick; cap 40t", "WraithfangEntity doExtraDamage", "Projectile NBT",
                "Repaired: Soul Cadence no longer overwrites flight growth");
        wraithfang(evidence, "signature_threshold", "Projectile piercing", "Successful projectile hit",
                "One pierce; secondary x0.65", "Pierce count, damage, and pierce-immune entity tag", "Projectile NBT",
                "Repaired: bosses stop the projectile and pursuit contact damage is independent");
        wraithfang(evidence, "signature_convergence", "Direct-hit soul burst", "First direct hit outside lockout",
                "Damage x0.35; radius 2.5; cap 5; lockout 20t", "Owner lockout and bounded nearest query",
                "Owner TTL and projectile one-shot flag", "Repaired: arrival damage cannot overwrite the burst");
        wraithfang(evidence, "signature_focus", "Banshee throw capstone", "Throw and forced return",
                "Speed x1.533; four pierces x0.75; return 80t; cooldown +40t",
                "Composed tuning and WraithfangEntity beginReturn", "Projectile NBT and forced return",
                "Repaired: the player throw now returns and consumes its cooldown drawback");
        wraithfang(evidence, "signature_release", "Death's Fang capstone", "Throw, hit, and forced return",
                "Damage x2.25; no pierce; Weakness II 80t; return 30t",
                "Composed damage and persisted return delay", "Projectile NBT and forced return",
                "Repaired: Keen Fang remains effective and the delay is consumed");
        wraithfang(evidence, "combat_opening", "Spectral Leap duration", "Projectile pursuit",
                "Dash duration 14t", "WraithfangEntity bounded pursuit", "Projectile NBT",
                "Repaired: the hardcoded 100-tick pull was replaced");
        wraithfang(evidence, "combat_cadence", "Spectral Leap speed", "Projectile pursuit",
                "Dash speed 1.55", "WraithfangEntity pursuit velocity", "Projectile NBT",
                "Repaired: player and delegated pursuit share the tuned speed");
        wraithfang(evidence, "combat_pressure", "Pursuit target acquisition", "Player aim and pursuit validation",
                "Range x1.25 to 18", "Aim target selection and squared range gate", "Projectile target UUID",
                "Repaired: the dedicated range is consumed before pursuit effects");
        wraithfang(evidence, "combat_reversal", "Spectral Leap contact damage", "Swept owner movement",
                "Damage x0.25; cap 6", "Per-target swept path query", "Contact UUID set in projectile NBT",
                "Repaired: contact damage no longer modifies projectile piercing");
        wraithfang(evidence, "combat_reserve", "Spectral Leap steering", "First 10 pursuit ticks",
                "Turn 8 degrees per tick", "Bounded vector turn toward target", "Projectile TTL",
                "Repaired: the previously unused homing values steer pursuit");
        wraithfang(evidence, "combat_threshold", "Spectral Leap arrival strike", "Owner within two blocks",
                "Damage x0.7; range 2", "Single arrival strike and pursuit stop", "One-shot flag in projectile NBT",
                "Repaired: arrival uses independent damage and range settings");
        wraithfang(evidence, "combat_convergence", "Throw cooldown", "Ability preparation",
                "Cooldown -6t", "Composed execution cooldown consumed before stack removal", "Item cooldown",
                "Repaired: applies to player and delegated activation without erasing drawbacks");
        wraithfang(evidence, "combat_focus", "Spectral Stampede capstone", "Swept owner movement",
                "Ten contacts x0.5; Slowness I 20t; cooldown +20t", "Contact query with arrival suppressed",
                "Contact UUID set and status TTL", "Repaired: Weakness, piercing, and burst settings remain independent");
        wraithfang(evidence, "combat_release", "Possessing Lunge capstone", "Valid target within 12 blocks",
                "Teleport; strike x1.6; speed x0.75; cooldown +40t", "Behind-target teleport and one arrival strike",
                "One-shot flag and target UUID", "Repaired: all advertised effects are reachable and bounded");
        wraithfang(evidence, "transformation_opening", "Return Haste", "Catch or delegated pursuit completion",
                "Haste II 110t", "WraithfangAbilityManager Haste grant", "Status TTL",
                "Repaired: player pickup consumes the mastery snapshot instead of config alone");
        wraithfang(evidence, "transformation_cadence", "Haste melee damage", "Wraithfang melee while Haste is active",
                "Melee damage +8%", "Outgoing player-attack modifier", "Owner state TTL",
                "Repaired: the dedicated melee hook consumes the bonus");
        wraithfang(evidence, "transformation_pressure", "Extended return rhythm", "Returning Wraithfang catch",
                "Return Haste +60t", "Composed Haste duration", "Status TTL",
                "Redesigned: extends the stronger base Haste instead of redundantly applying weaker Haste");
        wraithfang(evidence, "transformation_reversal", "Projectile guard", "Throw then incoming projectile damage",
                "Reduction 20% for 40t", "Damage-type-gated incoming modifier", "Owner guard TTL",
                "Repaired: the guard is bounded and ignores non-projectile damage");
        wraithfang(evidence, "transformation_reserve", "Thrown-kill refrain", "Lethal direct projectile hit",
                "Haste III 100t; cooldown refund 20t", "KILL emission and WraithfangAbilityManager",
                "Status and owner TTL", "Repaired: rewards kills instead of every delegated cast");
        wraithfang(evidence, "transformation_threshold", "Throw/melee alternation", "Alternating successful actions within 60t",
                "+5% per alternation; cap 20%", "Preview then commit alternation state", "Reset on repeat, expiry, or actor removal",
                "Repaired: no longer overwrites projectile flight growth");
        wraithfang(evidence, "transformation_convergence", "Haste melee-kill refund", "Wraithfang melee kill while Hasted",
                "Refund 10t; cap 40t per Haste grant", "Death hook and bounded refund budget", "Reset on Haste grant and owner TTL",
                "Repaired: cooldown refund and per-Haste cap are both consumed");
        wraithfang(evidence, "transformation_focus", "Frenzied Wraith capstone", "Throw, Haste, and Wraithfang damage",
                "Haste IV 80t; cooldown x0.5; damage x0.8 while Hasted",
                "Composed cooldown and hit-time Haste gates", "Status and owner TTL",
                "Repaired: player and delegated damage use the same conditional penalty");
        wraithfang(evidence, "transformation_release", "Silent Wraith capstone", "First melee after return within 80t",
                "No Haste; soul strike x1; cooldown +20t", "Return-armed one-shot soul damage", "Consumed or owner TTL",
                "Repaired: return arms one bounded hit and pickup does not grant Haste");
        wraithmaw(evidence, "signature_opening", "Spectral Downpour cutlass count", "Ability preparation",
                "Cutlasses 18", "WraithmawAbilityManager spawn loop clamped 1-32", "Execution-scoped",
                "Verified: the tuned count reaches the spawn loop and both capstones override it deliberately");
        wraithmaw(evidence, "signature_cadence", "Cutlass muster hold", "Muster state",
                "Materialize 10t", "WraithmawCutlassEntity tickMuster via snapshot", "State transition",
                "Verified: the tuned hold is snapshotted and survives reload");
        wraithmaw(evidence, "signature_pressure", "Falling cutlass damage", "Ability preparation",
                "Damage x1.1", "WraithmawAbilityManager damage calculation", "Execution-scoped",
                "Repaired: all three capstones now multiply the damage multiplier instead of discarding it");
        wraithmaw(evidence, "signature_reversal", "Cutlass descent speed", "Positioning and falling",
                "Fall speed 1.45", "WraithmawCutlassEntity fall velocity via snapshot", "State transition",
                "Verified: consumed on both the launch impulse and the per-tick fall vector");
        wraithmaw(evidence, "signature_reserve", "Downpour radius", "Ability preparation",
                "Radius 7", "WraithmawAbilityManager landing disc", "Execution-scoped",
                "Verified: the tuned radius drives the golden-angle landing spread");
        wraithmaw(evidence, "signature_threshold", "Cutlass fall stagger", "Positioning state",
                "Sequence delay 1t", "WraithmawCutlassEntity tickPositioning via snapshot", "State transition",
                "Repaired: Haunted Steel moved to HAUNT_INTERVAL_TICKS, so the fall cadence is no longer overwritten");
        wraithmaw(evidence, "signature_convergence", "Falling impact splash", "Cutlass impact",
                "Damage x0.3; radius 2; cap 4", "WraithmawCutlassEntity burst through iframes",
                "One-shot flag persisted in cutlass NBT",
                "Verified: one bounded burst per cutlass on either an entity hit or a landing");
        wraithmaw(evidence, "signature_focus", "Blade Tempest capstone", "Ability preparation and impact",
                "28 cutlasses; radius 9; damage x0.65; cooldown x1.2",
                "Composed damage and cooldown; mode 1 skips embedding after the Gloam patch",
                "Execution-scoped; cutlass dissipates on impact",
                "Repaired: composes with Heavy Fall, the cooldown drawback matches its 20% text, and Gloam Graveyard is no longer nullified");
        wraithmaw(evidence, "signature_release", "Wraith Guillotine capstone", "Ability preparation",
                "6 cutlasses; radius 2; damage x2.5; no splash; cooldown +100t",
                "Mode 2 collapses the landing spread onto the resolved target", "Execution-scoped",
                "Repaired: the single-target mode bit now has a consumer and Heavy Fall survives");
        wraithmaw(evidence, "combat_opening", "Orbit capacity", "Embedded cutlass recovery",
                "Orbit cap 7", "WraithmawAbilityManager tryRecover slot allocation", "Cutlass NBT snapshot",
                "Verified: the cap bounds concurrent orbiting cutlasses and survives reload");
        wraithmaw(evidence, "combat_cadence", "Recovery radius", "Owner near an embedded cutlass",
                "Recovery radius 2", "WraithmawCutlassEntity tickEmbedded proximity gate", "Cutlass NBT snapshot",
                "Verified: recovery distance is read from the persisted snapshot");
        wraithmaw(evidence, "combat_pressure", "Orbit duration", "Successful recovery",
                "Orbit 1100t", "WraithmawAbilityManager recover deadline", "Cutlass expiry",
                "Verified: the deadline is stamped at recovery and both capstones override it deliberately");
        wraithmaw(evidence, "combat_reversal", "Launch velocity", "Swing with an orbiting cutlass",
                "Launch speed 1.55", "WraithmawAbilityManager onSwing and homing re-aim", "Cutlass NBT snapshot",
                "Verified: launch and homing share the persisted speed");
        wraithmaw(evidence, "combat_reserve", "Launched cutlass reach", "Launched state",
                "Range 28; lifetime 100t", "WraithmawCutlassEntity travel and lifetime bounds", "Cutlass NBT snapshot",
                "Verified: both bounds terminate the projectile and survive reload");
        wraithmaw(evidence, "combat_threshold", "Launched cutlass homing", "Launched state",
                "Corridor 5; turn 9 degrees", "WraithmawCutlassEntity findHomingTarget and turnToward", "Cutlass NBT snapshot",
                "Verified: the corridor filter and clamped turn rate are both consumed");
        wraithmaw(evidence, "combat_convergence", "Stored Malice recovery bonus", "Successful recovery",
                "+4% per orbiting cutlass; cap 24%", "WraithmawAbilityManager tryRecover multiplyDamage",
                "Applied once per cutlass; damage persisted",
                "Repaired: Grave Wound moved to GLOAM_VULNERABILITY_BONUS, so the per-cutlass bonus is no longer overwritten");
        wraithmaw(evidence, "combat_focus", "Crown of Blades capstone", "Swing with orbiting cutlasses",
                "Orbit cap 12; launch 2; damage x0.65; orbit 600t",
                "LAUNCH_COUNT consumed by onSwing; twelve distinct orbit slots", "Cutlass NBT snapshot",
                "Repaired: the launch count is tuned rather than hardcoded and no two cutlasses share an orbit slot");
        wraithmaw(evidence, "combat_release", "Lone Executioner capstone", "Successful recovery",
                "Orbit cap 1; damage x3; turn 12 degrees; lockout 80t",
                "Per-owner recovery lockout in WraithmawAbilityManager",
                "Lockout map cleared on expiry, world unload, and server stop",
                "Repaired: the advertised four-second recovery lockout now has a consumer");
        wraithmaw(evidence, "transformation_opening", "Gloam patch radius", "Cutlass embed",
                "Stain radius 1.6", "WraithmawCutlassEntity createGloam", "Patch expiry",
                "Verified: the tuned radius reaches GloamStainManager");
        wraithmaw(evidence, "transformation_cadence", "Gloam and embed duration", "Cutlass embed",
                "Gloam 320t; embedded 600t", "createGloam duration and embed deadline", "Patch and cutlass expiry",
                "Repaired: the cutlass now passes its tuned Gloam duration instead of falling back to config");
        wraithmaw(evidence, "transformation_pressure", "Gloam Slowness", "Contact with owner Gloam",
                "Slowness II 60t", "PatchBehavior amplifier and slow duration through GloamMechanicsManager",
                "Status TTL and patch expiry",
                "Repaired: the cutlass supplies a PatchBehavior, so amplifier and duration are no longer config-only");
        wraithmaw(evidence, "transformation_reversal", "Gloam vulnerability", "Falling or launched cutlass hit",
                "+15% damage on owner Gloam", "WraithmawCutlassEntity gloamAdjustedDamage via isOnOwnerGloam",
                "Evaluated per hit", "Repaired: a dedicated setting with a real consumer replaces the inert Stored Malice collision");
        wraithmaw(evidence, "transformation_reserve", "Haunted Steel strikes", "Embedded cutlass every 40t",
                "Range 3; damage x0.25; candidate cap 8", "WraithmawCutlassEntity tickHaunt nearest Gloam target",
                "Bounded by the embedded deadline",
                "Repaired: implemented on dedicated settings; the fall cadence collision is gone");
        wraithmaw(evidence, "transformation_threshold", "Shared Burial extension", "Cutlass kill",
                "Range 5; +40t; cap 80t", "GloamStainManager extendPatches driven by the KILL emission",
                "Per-patch extension budget and patch expiry",
                "Repaired: an additive owner-scoped patch extension replaces the previously unconsumed settings");
        wraithmaw(evidence, "transformation_convergence", "Grave Recall pull", "Successful recovery",
                "Range 4; cap 5; pull 1", "WraithmawCutlassEntity onRecovered bounded Gloam pull",
                "One pull per recovery", "Repaired: recovery now emits RECOVER and performs the advertised pull");
        wraithmaw(evidence, "transformation_focus", "Living Graveyard capstone", "Embedded cutlass",
                "Range 6; speed 0.18; damage x0.6", "WraithmawCutlassEntity tickGravewalk and one-shot strike",
                "One strike then dissipation; recovery suppressed",
                "Repaired: the walk, the strike, and the no-recovery drawback are all implemented");
        wraithmaw(evidence, "transformation_release", "Mausoleum Burst capstone", "Reactivation within 200t",
                "Window 200t; range 12; cap 8; damage x0.9; cooldown +120t",
                "UniqueWeaponSecondaryAction into WraithmawAbilityManager tryDetonate",
                "Last-cast map cleared on use, expiry, world unload, and server stop",
                "Repaired: reactivation is reachable while the cast is on cooldown and no longer clobbers cast damage");
        stormscale(evidence, "signature_opening", "Lightning Rod anchor selection", "Ability preparation",
                "Targeting range 22", "StormscaleLightningRodManager resolveAnchor", "Execution-scoped",
                "Verified: the tuned range gates both target selection and the collider raycast");
        stormscale(evidence, "signature_cadence", "Pulse radius", "Ability preparation",
                "Radius 4", "ActiveRod baseRadius consumed by every pulse", "Execution-scoped",
                "Repaired: the undocumented target-cap write was removed; the pulse cap is now a config default");
        stormscale(evidence, "signature_pressure", "Rod lifetime", "Ability preparation",
                "Duration 900t", "ActiveRod expiresAt", "Rod expiry and tether break",
                "Verified: the tuned duration reaches the rod deadline");
        stormscale(evidence, "signature_reversal", "Tether break distance", "Rod tick",
                "Tether 38", "tickRod squared distance gate", "Cancel on break",
                "Verified: the tuned distance is checked every tick");
        stormscale(evidence, "signature_reserve", "Charge travel time", "Melee hit",
                "Travel 12t", "PendingPulse arrival deadline", "Pulse consumed on arrival",
                "Verified: the tuned travel time drives both the visual and the arrival");
        stormscale(evidence, "signature_threshold", "Plant pulse", "Ability preparation",
                "Mode 1; plant damage x0.7", "start() immediate pulse", "Execution-scoped",
                "Repaired: the plant multiplier is tuned rather than hardcoded, and it still adds no growth");
        stormscale(evidence, "signature_convergence", "Rod repositioning", "Reactivation within the cast",
                "Mode 2; range 14; duration cost 80t",
                "UniqueWeaponSecondaryAction into tryReactivate", "One reposition per cast",
                "Repaired: reactivation is reachable because the secondary action runs before the cooldown gate");
        stormscale(evidence, "signature_focus", "Mobile Conductor capstone", "Rod tick",
                "Mode 4; follow 0.3/t; radius 3; damage x0.75; growth limit 0.4",
                "Anchor lerp toward the actor and a min-applied growth limit", "Execution-scoped",
                "Repaired: its radius and growth drawbacks survive the Gathering Charge branch");
        stormscale(evidence, "signature_release", "Storm Spire capstone", "Ability preparation and reactivation",
                "Mode 8; radius 6; damage x1.4; tether 20; duration 600t; cooldown x1.2",
                "Composed cooldown plus a reposition refusal on mode 8", "Execution-scoped",
                "Repaired: mode 8 now has a consumer, so the rod genuinely cannot be moved");
        stormscale(evidence, "combat_opening", "Pulse damage", "Ability preparation",
                "Damage x1.1", "ActiveRod baseDamage", "Execution-scoped",
                "Verified: multiplies onto whichever signature capstone is selected");
        stormscale(evidence, "combat_cadence", "Growth per arriving charge", "Charge arrival",
                "Growth 1.5% per hit", "tickRod growth accumulation", "Execution-scoped",
                "Repaired: the redundant growth-cap write that erased Mobile Conductor was removed");
        stormscale(evidence, "combat_pressure", "Growth ceiling", "Charge arrival",
                "Growth cap 100%", "ActiveRod maximumGrowth", "Execution-scoped",
                "Verified: raises the ceiling without erasing a capstone limit");
        stormscale(evidence, "combat_reversal", "Double Charge", "Critical melee hit",
                "Mode 16; second charge x0.5; lockout 10t", "onMeleeHit critical predicate and lockout",
                "Lockout TTL", "Repaired: uses vanilla's full critical conditions instead of an airborne approximation");
        stormscale(evidence, "combat_reserve", "Conductive marking", "Pulse hit then melee hit",
                "Mode 32; conductive 60t; cap 12; arrival -4t", "Conductive map and travel reduction",
                "Conductive entries expire each tick",
                "Repaired: the conductive cap moved off TARGET_CAP, so it no longer halves the pulse cap");
        stormscale(evidence, "combat_threshold", "Overflow", "Charge arrival at maximum growth",
                "Mode 64; +5% per charge; cap 20%", "Accumulated overflow bonus on the next pulse",
                "Cleared when growth is no longer capped",
                "Repaired: overflow accumulates across arrivals, so the advertised 20% is reachable");
        stormscale(evidence, "combat_convergence", "Fifth Surge", "Every fifth arrival",
                "Mode 128; interval 5; extra pulse x0.6", "Arrival counter and extra pulse", "Execution-scoped",
                "Repaired: interval and damage are tuned; the extra pulse still adds no growth");
        stormscale(evidence, "combat_focus", "Rapid Dynamo capstone", "Melee hit",
                "Mode 256; instant pulse x0.55; growth x2; radius cap 4.5",
                "Immediate pulse path and a min-applied radius cap", "Execution-scoped",
                "Repaired: 4.5 is a cap rather than a base radius, and the doubling composes with Swelling Charge");
        stormscale(evidence, "combat_release", "Patient Supercell capstone", "Charge arrival then reactivation",
                "Mode 512; 10 charges; 120t window; release x0.35 within 6; cooldown +80t",
                "Stored charges released through tryReactivate", "Charges expire with the window",
                "Repaired: the release is reachable through the secondary action");
        stormscale(evidence, "transformation_opening", "Pulse pull", "Pulse hit",
                "Pull 0.38", "pullTowardRod strength", "Applied per hit",
                "Verified: the tuned strength reaches the knockback-resistance-aware pull");
        stormscale(evidence, "transformation_cadence", "Static Drag", "Pulse hit",
                "Slowness I 40t", "Pulse status application", "Status TTL",
                "Repaired: the amplifier is now read from tuning instead of a hardcoded zero");
        stormscale(evidence, "transformation_pressure", "Charged Armor", "Projectile damage inside the radius",
                "Projectile damage -15%", "StormscaleLightningRodManager modifyIncomingDamage",
                "Bounded by the rod lifetime and current radius",
                "Repaired: implemented through the shared incoming-damage hook; it previously had no consumer at all");
        stormscale(evidence, "transformation_reversal", "Arc Jump", "Pulse that damaged a target",
                "Mode 1024; chain x0.25; range 3; cap 3", "chainBeyondPulse bounded query", "Per pulse",
                "Repaired: the chain damage multiplier is tuned rather than hardcoded");
        stormscale(evidence, "transformation_reserve", "Center Shock", "Pulse hit near the anchor",
                "Mode 2048; centre 1.5; +20%; lift 0.2", "Distance-gated bonus and lift", "Applied per hit",
                "Repaired: both the bonus and the lift are consumed from tuning");
        stormscale(evidence, "transformation_threshold", "Repelling Charge", "Sneak reactivation",
                "Mode 4096; reverse 1.5; Weakness 60t; lockout 60t",
                "tryReactivate arms one reversed pulse", "One-shot flag and lockout TTL",
                "Repaired: reachable through the secondary action; duration and lockout are tuned");
        stormscale(evidence, "transformation_convergence", "Storm Ward", "Pulse hitting six targets",
                "Mode 8192; threshold 6; absorption 3 for 80t; refund 20t; lockout 100t",
                "Timed absorption plus SimplySwordsAPI reduceWeaponCooldown", "Status TTL and lockout",
                "Repaired: the absorption now expires and the refund shortens the remaining cooldown instead of lengthening it");
        stormscale(evidence, "transformation_focus", "Storm Cage capstone", "Pulse hit near the anchor",
                "Mode 16384; root 2.5; 20t; cap 8; damage x0.8", "Velocity zero plus bounded root count",
                "Status TTL", "Repaired: the root duration is tuned and the damage penalty composes");
        stormscale(evidence, "transformation_release", "Thunderhead capstone", "Pulse hit",
                "Mode 32768; push 2.5; edge +35%; centre -30%", "Reversed pull and distance-banded damage",
                "Applied per hit", "Verified: edge bonus and reverse strength are both consumed");
        ionbound(evidence, "signature_opening", "Ion Cube recharge", "Held tick",
                "Reserve interval 145t", "RechargeState interval via RESERVE_INTERVAL_TICKS", "Per-stack recharge state",
                "Repaired: the reserve interval no longer lands on the beam, where it reduced the beam to zero damage");
        ionbound(evidence, "signature_cadence", "Ion shield trigger gate", "Incoming damage",
                "Shield threshold 25%", "handleIncomingDamage SHIELD_THRESHOLD", "Shield expiry",
                "Repaired: a reserve-scoped threshold that cannot reach the crusher or beam");
        ionbound(evidence, "signature_pressure", "Ion shield duration", "Shield trigger",
                "Shield 50t", "ActiveShield expiry", "Shield expiry and visual discard",
                "Verified: the tuned duration drives both the shield window and its visual");
        ionbound(evidence, "signature_reversal", "Ion Rebound push", "Shield trigger",
                "Mode 1; push 2", "Knockback-resistance-aware attacker shove", "One shove per trigger",
                "Repaired: the mode bit now has a consumer, and its push no longer disables the corridor pull");
        ionbound(evidence, "signature_reserve", "Stored Charge", "Cube generated at maximum",
                "Mode 2; cap 3; 200t; +10% each", "ReserveState charges consumed by the next slam",
                "Charges expire and are cleared on use",
                "Repaired: implemented on reserve-scoped stack settings that had no consumer at all");
        ionbound(evidence, "signature_threshold", "Emergency Cell", "Low health with no cubes",
                "Mode 4; below 25%; lockout 1200t", "tickEmergencyCell per-wielder lockout", "Lockout TTL",
                "Repaired: implemented, and its lockout no longer stretched the beam follow-up window to 60 seconds");
        ionbound(evidence, "signature_convergence", "Recycled Current", "Ion beam completion",
                "Mode 8; refund 40t", "ReserveState recharge refund consumed once", "Cleared on the next cube",
                "Repaired: the beam finish now grants the advertised refund");
        ionbound(evidence, "signature_focus", "Aegis Reactor capstone", "Incoming damage",
                "Mode 16; threshold 15%; shield 80t; 2 cubes; ability damage -25%",
                "Multi-cube consumption and a shared ability-damage penalty", "Shield expiry",
                "Repaired: both drawbacks exist; it was previously a pure buff");
        ionbound(evidence, "signature_release", "Overcharged Core capstone", "Held tick and ability damage",
                "Mode 32; recharge 100t; +15% per cube to 45%",
                "Shield suppression plus a per-cube ability-damage factor", "Evaluated per cast",
                "Repaired: the suppression and the damage bonus are implemented, and the recharge interval no longer disables the beam");
        ionbound(evidence, "combat_opening", "Corridor dimensions", "Ability preparation",
                "Length 14; width 7; height 5", "ActiveCorridor geometry and target query", "Execution-scoped",
                "Repaired: the undocumented target-cap write was removed in favour of a config default");
        ionbound(evidence, "combat_cadence", "Corridor timing", "Ability preparation",
                "Materialize 5t; hold 7t; close 7t", "Corridor phase deadlines", "Execution-scoped",
                "Verified: three ticks saved overall, matching the described 0.15 seconds");
        ionbound(evidence, "combat_pressure", "Corridor pull", "Corridor closing",
                "Pull 0.4", "pullTargetsToCentre strength", "Applied per closing tick",
                "Verified: the tuned strength is consumed and is no longer zeroed by Ion Rebound");
        ionbound(evidence, "combat_reversal", "Slam damage", "Corridor slam",
                "Damage x1.12", "ActiveCorridor slam damage", "Execution-scoped",
                "Repaired: both capstones multiply instead of discarding it");
        ionbound(evidence, "combat_reserve", "Paralytic Walls", "Corridor slam",
                "Mode 2048; status 60t", "Slam applies Ion Paralysis in place of Slowness", "Status TTL",
                "Repaired: the headline effect existed only in the description; the slam always applied Slowness");
        ionbound(evidence, "combat_threshold", "Crushing Focus", "Corridor slam",
                "Mode 64; lane 2; +25%", "Lane-offset damage bonus", "Applied per slam target",
                "Repaired: the lane width no longer overwrites the corridor width, which shrank it to 2 blocks");
        ionbound(evidence, "combat_convergence", "Held Breath", "Corridor slam",
                "Follow-up 30t", "ActiveCorridor followupEnds", "Window expiry",
                "Verified: the tuned window gates both canActivate and startBeam");
        ionbound(evidence, "combat_focus", "Ion Coffin capstone", "Corridor slam",
                "Mode 128; 8x4x4; cap 8; trap 90%; damage x2.2",
                "Corridor geometry, a trap Slowness amplifier, and a real beam block", "Status TTL",
                "Repaired: the trap slow and the no-beam drawback are implemented and Heavy Slam survives");
        ionbound(evidence, "combat_release", "Repulsor Gate capstone", "Corridor slam",
                "Mode 256; burst 3; damage x1.5; cap 24; beam width x0.75",
                "Dedicated outward burst plus a reserve-carried beam drawback", "Applied per slam",
                "Repaired: the outward burst was clamped to zero by the pull key, and the beam drawback could never reach the beam");
        ionbound(evidence, "transformation_opening", "Beam width", "Beam pulse",
                "Width 1.3", "pulseBeam half-width", "Execution-scoped",
                "Repaired: the undocumented target-cap write was removed in favour of a config default");
        ionbound(evidence, "transformation_cadence", "Beam length", "Beam pulse",
                "Range 14", "pulseBeam end point", "Execution-scoped",
                "Verified: the tuned range drives both the damage query and the visual");
        ionbound(evidence, "transformation_pressure", "Beam cadence", "Beam tick",
                "Interval 4t", "tickBeams pulse gate and per-pulse division", "Execution-scoped",
                "Verified: total damage is preserved because the pulse count is derived from the same interval");
        ionbound(evidence, "transformation_reversal", "Beam damage", "Beam pulse",
                "Damage x1.1", "pulseBeam damage", "Execution-scoped",
                "Repaired: Sustained Current and both capstones multiply instead of discarding it");
        ionbound(evidence, "transformation_reserve", "Deep Paralysis", "Beam pulse hit",
                "Ion Paralysis 130t", "pulseBeam status duration", "Status TTL",
                "Verified: the tuned duration reaches the applied effect");
        ionbound(evidence, "transformation_threshold", "Mobile Channel", "Beam channel",
                "Movement 30%", "applyBeamMovementSlow attribute modifier", "Modifier removed on beam end",
                "Verified: the tuned speed replaces the configured reduction");
        ionbound(evidence, "transformation_convergence", "Sustained Current", "Beam start and pulse",
                "Duration 72t; damage x1.2", "Beam deadline and composed damage", "Execution-scoped",
                "Repaired: composes with Conductive Burn and no longer writes a meaningless cube count");
        ionbound(evidence, "transformation_focus", "Sweeping Ray capstone", "Beam pulse",
                "Mode 512; width 2; cap 24; damage x0.7; paralysis 60t",
                "Beam width, target cap, and composed damage", "Execution-scoped",
                "Repaired: composes with Conductive Burn; the description no longer claims steering, which the base beam already does every pulse");
        ionbound(evidence, "transformation_release", "Disintegration Lance capstone", "Beam pulse",
                "Mode 1024; width 0.7; 40t; damage x2.25; cap 3; armour 6; no movement",
                "Composed damage, armour-ignore compensation, and a full movement lock", "Execution-scoped",
                "Repaired: the armour ignore now has a consumer and Conductive Burn survives");
        soulrender(evidence, "signature_opening", "Rendmark hit roll", "Melee hit",
                "Chance bonus +5", "SoulrenderSwordItem adds the bonus to the configured chance", "Per hit",
                "Repaired: the bonus no longer replaces a changed config value, and the roll keeps its original comparison so +5 is a true +5 points");
        soulrender(evidence, "signature_cadence", "Rendmark duration", "Rendmark application",
                "Mark duration 600t", "SoulrenderSwordItem Weakness and Slowness duration", "Status TTL",
                "Repaired: both halves of the mark are now refreshed on every application, so the advertised 30 seconds is reachable");
        soulrender(evidence, "signature_pressure", "Rendmark stack ceiling", "Rendmark application",
                "Stack cap 10", "SoulrenderSwordItem Slowness amplifier ceiling", "Status TTL",
                "Repaired description: the node reaches Slowness XI and Weakness II, not the base IX and I it used to claim");
        soulrender(evidence, "signature_reversal", "Rendmark melee rider", "Melee hit on a slowed target",
                "+3% per stack; 18% cap", "SoulrenderSwordItem melee bonus damage", "Per hit",
                "Verified: both keys are consumed and the cap is reached at six stacks");
        soulrender(evidence, "signature_reserve", "Fresh Ink opening stacks", "Rendmark on an unafflicted enemy",
                "2 stacks; 80t per-target lockout", "SoulrenderAbilityManager.openingStacks", "Per-target lockout TTL; world unload",
                "Repaired: implemented on dedicated keys that had no consumer at all");
        soulrender(evidence, "signature_threshold", "Echoed Curse mark copy", "Rendmark application",
                "Echo chance 25; range 4; cap 1; 300t; 20t lockout", "SoulrenderAbilityManager.echoMark", "Owner lockout TTL; world unload",
                "Repaired: implemented, and its own chance no longer replaced the mark roll or capped the harvest to a single target");
        soulrender(evidence, "signature_convergence", "Condemnation harvest bonus", "Soulrend consumption",
                "+6% per stack; 36% cap", "SoulrenderSwordItem reap damage multiplier", "Execution-scoped",
                "Repaired: reaches the harvest through reserve-scoped keys instead of the branch-wide leak that carried Echoed Curse with it");
        soulrender(evidence, "signature_focus", "Pallbearer's Ledger capstone", "Melee hit",
                "Mode 4; repeat penalty 20", "SoulrenderSwordItem guarantee and repeat penalty", "Per hit",
                "Repaired: the advertised drawback exists; the node was previously a flat 100% chance with no cost");
        soulrender(evidence, "signature_release", "Withering Palimpsest capstone", "Rendmark and consumption",
                "Stack cap 5; +15% per stack; 75% cap", "Mark ceiling plus reap stack bonus", "Status TTL; execution-scoped",
                "Redesigned: removing Slowness would have made the harvest unreachable, so the node now caps stacks and pays for it in consumption damage");
        soulrender(evidence, "combat_opening", "Harvest radius", "Ability activation",
                "Radius 12", "SoulrenderSwordItem target scan", "Execution-scoped",
                "Repaired: the undocumented target-cap write was dropped for a config default, and the activation gate no longer measures the untuned radius");
        soulrender(evidence, "combat_cadence", "Harvest damage", "Soulrend consumption",
                "Damage x1.1", "SoulrenderSwordItem reap damage multiplier", "Execution-scoped",
                "Repaired: all three capstones multiply instead of discarding it");
        soulrender(evidence, "combat_pressure", "Swift Reaping", "Harvest completion",
                "Threshold 3; Speed I 60t", "SoulrenderAbilityManager.recordReap", "Status TTL",
                "Repaired: implemented on dedicated keys; the shared threshold had no consumer");
        soulrender(evidence, "combat_reversal", "Soul Dividend healing", "Soulrend consumption",
                "Heal bonus +0.5; cap 9", "SoulrenderSwordItem heal pipeline", "Execution-scoped",
                "Repaired: composes with the configured ratio instead of hardcoding it, and the capstones now scale it rather than replacing it");
        soulrender(evidence, "combat_reserve", "Reaper's Reach pull", "Ability activation",
                "Bonus range 4; pull 1.5; cap 8", "SoulrenderAbilityManager.reachPull", "Execution-scoped",
                "Repaired: the pull existed only in the description; the node's only live write was a silent harvest cap");
        soulrender(evidence, "combat_threshold", "Shared Ending split", "Lethal Soulrend consumption",
                "35% of the hit; range 5; cap 6", "SoulrenderAbilityManager.sharedEnding", "Execution-scoped",
                "Repaired: implemented from the victim's position, and its radius write no longer shrinks the harvest to five blocks");
        soulrender(evidence, "combat_convergence", "Full Ledger", "Harvest completion",
                "Threshold 6; Haste II 100t", "SoulrenderAbilityManager.recordReap", "Status TTL",
                "Repaired: implemented on dedicated keys; nothing read the shared threshold or status duration");
        soulrender(evidence, "combat_focus", "Harvest Moon capstone", "Ability activation",
                "Radius 16; cap 32; damage x0.7; healing x0.5", "Composed reap radius, cap, damage, and heal multiplier", "Execution-scoped",
                "Repaired: the healing drawback is real rather than a rewrite to the base ratio, and Keen Harvest survives");
        soulrender(evidence, "combat_release", "Headsman's Tithe capstone", "Ability activation",
                "Radius 10; cap 5; damage x1.5; healing x1.25", "Distance-ordered target selection", "Execution-scoped",
                "Repaired: the harvest is now sorted by distance, so \"your 5 nearest\" is real rather than an arbitrary five");
        soulrender(evidence, "transformation_opening", "Soul Sheath", "Harvest completion",
                "Absorption 2; 80t", "SoulrenderAbilityManager.recordReap", "Absorption expiry",
                "Repaired: implemented; neither key had a consumer");
        soulrender(evidence, "transformation_cadence", "Deathly Patience", "Held tick near a mark",
                "Range 6; knockback resistance +0.1", "SoulrenderAbilityManager managed attribute modifier", "Removed when no mark is near; world unload",
                "Repaired: implemented as the knockback resistance it advertises, and its radius write no longer shrank the harvest");
        soulrender(evidence, "transformation_pressure", "Borrowed Time", "Kill on a marked enemy",
                "Resistance I 40t; 40t lockout", "SoulrenderAbilityManager.onTargetDeath", "Lockout TTL; world unload",
                "Repaired: implemented on the death hook; the node had no trigger at all");
        soulrender(evidence, "transformation_reversal", "Cold Grip", "Damage from a marked attacker",
                "Slowness I 40t; 40t lockout; cap 8", "SoulrenderAbilityManager.modifyIncomingDamage", "Lockout TTL; world unload",
                "Repaired: implemented on the incoming-damage hook; its only live write was a silent harvest cap");
        soulrender(evidence, "transformation_reserve", "Grave Reserve", "Harvest damage then low health",
                "Store 10%; cap 6; below 35%; 100t", "recordReap stores; tickHolder converts", "Spent on conversion; world unload",
                "Repaired: implemented; the node previously landed on the harvest damage bonus and roughly tripled Condemnation's ceiling");
        soulrender(evidence, "transformation_threshold", "Quietus", "Soulrend consumed below the health gate",
                "Below 20%; 1 absorption per mark; cap 6", "SoulrenderSwordItem per-mark tally into recordReap", "Absorption expiry",
                "Repaired: implemented; its bonus cap was inflating harvest damage instead");
        soulrender(evidence, "transformation_convergence", "Unbroken Reaper capstone", "Lethal damage",
                "5 marks; range 10; Resistance II 40t; 1200t lockout", "SoulrenderAbilityManager.tryUnbrokenReaper on tryUseTotem", "Lockout TTL; world unload",
                "Repaired: the cheat-death is implemented on vanilla's lethal-damage hook; only its target cap used to reach anything");
        soulrender(evidence, "transformation_focus", "Soul Shelter capstone", "Soulrend consumption",
                "Damage x0.75; convert 40%; cap 8; 120t", "Composed reap damage and absorption conversion", "Absorption expiry",
                "Repaired: the conversion exists; the node used to apply both drawbacks and no benefit");
        soulrender(evidence, "transformation_release", "Death's Due capstone", "Harvest kills then next melee hit",
                "No healing; +10% per kill; 50% cap; 100t", "recordReap stores; postHit spends through takeTitheBonus", "Window TTL; consumed on the next hit",
                "Repaired: the kill bonus now rides the next attack as described instead of inflating the harvest that produced it");
        soulstalker(evidence, "signature_opening", "Tendril proc roll", "Held passive check",
                "Chance 30", "SoulstalkerAbilityManager.tickHeldPassive roll", "Per check",
                "Repaired: the redundant interval write that duplicated the config default was removed");
        soulstalker(evidence, "signature_cadence", "Tendril search range", "Held passive check",
                "Tendril range 10", "findPassiveTargets range and impact re-validation", "Per strike",
                "Repaired: moved to a dedicated range key, and the description no longer claims nearest-target selection the base already performs");
        soulstalker(evidence, "signature_pressure", "Tendril lockout", "Successful tendril launch",
                "Tendril lockout 50t", "Per-owner passive lockout", "Lockout TTL; world unload",
                "Repaired: moved to a dedicated lockout key that Tangled Prey no longer overwrites");
        soulstalker(evidence, "signature_reversal", "Tendril strike", "Tendril impact",
                "Damage x1.12; Slowness I 40t", "Pending strike damage and tendril slow", "Status TTL",
                "Repaired: both capstones multiply instead of discarding it, and the slow has its own key");
        soulstalker(evidence, "signature_reserve", "Gloam Ambush", "Tendril impact on owner Gloam",
                "Gloam bonus 25%", "GloamStainManager.isOnOwnerGloam damage rider", "Per strike",
                "Repaired: implemented; no Gloam check existed anywhere in the strike path");
        soulstalker(evidence, "signature_threshold", "Seeking Root", "Tendril impact on a dead target",
                "Redirect range 4", "redirectStrike nearest-enemy selection", "Per strike",
                "Repaired: implemented; a dead target used to cancel the strike, and its only live write shrank tendril range to four blocks");
        soulstalker(evidence, "signature_convergence", "Tangled Prey", "Tendril impact",
                "Slowness IV 20t; 80t per-enemy lockout", "applySnare per-target lockout", "Lockout TTL; world unload",
                "Repaired: implemented on dedicated keys; it previously reverted Unresting Coils and halved Barbed Emergence's slow");
        soulstalker(evidence, "signature_focus", "Eldritch Thicket capstone", "Held passive check",
                "3 tendrils; damage x0.65; lockout 90t", "findPassiveTargets count and per-target strikes", "Per strike",
                "Repaired: the multi-strike exists, it composes with Barbed Emergence, and it no longer reverts Grasping Reach");
        soulstalker(evidence, "signature_release", "Hungering Tendril capstone", "Held passive check",
                "Mode 16; damage x1.75; refund 20t; interval 30t", "Lowest-health selection and lockout refund", "Lockout TTL",
                "Repaired: the targeting, the kill refund and the advertised interval drawback all exist; the node used to shorten its own lockout instead");
        soulstalker(evidence, "combat_opening", "Stride duration", "Ability activation",
                "Stride duration 900t", "ActiveStride expiry", "Execution-scoped",
                "Verified: consumed at activation and composed by both capstones");
        soulstalker(evidence, "combat_cadence", "Stride speed", "Ability activation and climbing",
                "Movement x1.1; climb x1.1", "Stride movement attribute and climb speed", "Execution-scoped",
                "Repaired: the climb half now has a consumer; the stride entity read the config value directly");
        soulstalker(evidence, "combat_pressure", "Heavy Footfall", "Footfall impact",
                "Footfall radius 1; damage x1.1", "SoulstalkerStrideEntity.resolveFootfall", "Execution-scoped",
                "Repaired: the node now reaches the footfall it names; its damage bonus used to land on Shadow cleave");
        soulstalker(evidence, "combat_reversal", "Lingering Stain", "Gloam trail",
                "Trail duration 320t", "createTunedPatch duration", "Patch expiry",
                "Repaired: implemented; the trail was created entirely from config");
        soulstalker(evidence, "combat_reserve", "Mired Wake", "Gloam trail contact",
                "Slowness II; 60t", "Patch slow amplifier and PatchBehavior duration", "Patch expiry",
                "Repaired: implemented through the stain behaviour; none of its four writes had a consumer");
        soulstalker(evidence, "combat_threshold", "Predatory Momentum", "Continuous riding",
                "Distance 8; bonus 15%", "trackMomentum feeding cleave and footfall damage", "Reset when the stride stops",
                "Repaired: implemented; no distance tracking existed");
        soulstalker(evidence, "combat_convergence", "Swift Summons", "Ability preparation",
                "Cooldown 1020t", "Phase 3 cooldown pipeline", "Execution-scoped",
                "Verified: reaches the weapon cooldown, and both capstone multipliers now compose on top of it");
        soulstalker(evidence, "combat_focus", "Endless Procession capstone", "Ability activation",
                "Duration +400t; trail width x1.5; cooldown x1.2; footfall damage x0.75",
                "Composed duration, trail width, cooldown and footfall damage", "Execution-scoped",
                "Repaired: the wider Gloam exists, its cooldown drawback survives Horizon Eater, and its penalty lands on the footfall it names");
        soulstalker(evidence, "combat_release", "Rift Stride capstone", "Fully charged leap",
                "Rift range 12; stain 2; duration x0.75; 60t lockout", "tickRiftStride teleport and paired stains", "Lockout TTL; world unload",
                "Repaired: the teleport is implemented; the node previously delivered only its duration cut and shrank Shadow cleave");
        soulstalker(evidence, "transformation_opening", "Cleave range", "Weapon swing while riding",
                "Cleave range 18", "SoulstalkerCleaveEntity travel distance", "Entity lifetime",
                "Repaired: moved to a dedicated cleave key that Rift Stride can no longer shrink");
        soulstalker(evidence, "transformation_cadence", "Cleave width", "Weapon swing while riding",
                "Final width 3.8", "SoulstalkerCleaveEntity width growth", "Entity lifetime",
                "Verified: consumed, on a dedicated key");
        soulstalker(evidence, "transformation_pressure", "Cleave damage", "Weapon swing while riding",
                "Cleave damage x1.12", "SoulstalkerCleaveEntity weapon damage", "Entity lifetime",
                "Repaired: both capstones multiply instead of discarding it");
        soulstalker(evidence, "transformation_reversal", "Rapid Rending", "Weapon swing while riding",
                "Minimum swing 1t", "getCleaveCooldownTicks tuned minimum", "Per swing",
                "Repaired: the swing gate now reads tuning; it was hardcoded to the config minimum");
        soulstalker(evidence, "transformation_reserve", "Crushing Descent", "Leap impact",
                "Impact damage x1.2; radius 3", "SoulstalkerStrideEntity.resolveLeapImpact", "Execution-scoped",
                "Repaired: the leap impact now reads tuning, and the radius matches the described 3 blocks");
        soulstalker(evidence, "transformation_threshold", "Repulsive Landing", "Leap impact",
                "Knockback x1.25; lift 0.28", "Leap impact knockback and lift", "Execution-scoped",
                "Repaired: implemented; both keys were unread");
        soulstalker(evidence, "transformation_convergence", "Gathering Hunger", "Cleave hit then leap impact",
                "+4% per hit; 5 stacks; 20% cap", "Stride cleave-hit accumulator spent on landing", "Cleared on landing",
                "Repaired: implemented; no cleave-hit counter existed");
        soulstalker(evidence, "transformation_focus", "Horizon Eater capstone", "Weapon swing while riding",
                "Range 24; width 5; damage x0.8; cap 16; cooldown x2",
                "Composed cleave geometry, damage, target cap and cooldown", "Entity lifetime",
                "Repaired: the target cap is real against a previously unbounded cleave, and it no longer discards Severing Arc or Endless Procession's cooldown");
        soulstalker(evidence, "transformation_release", "Abyssal Meteor capstone", "Fully charged leap impact",
                "Charged damage x2; radius 4; stain 2 for 240t; cleave damage x0.7",
                "Charged leap impact branch and Gloam patch", "Execution-scoped",
                "Repaired: every benefit is implemented; the node used to deliver only its cleave-damage drawback");
        whisperwind(evidence, "signature_opening", "Dash velocity", "Fatal Flicker dash tick",
                "Dash speed x1.1", "FatalFlickerEffect.performDash tuned velocity", "Effect TTL",
                "Repaired: the dash effect now reads tuning; it used the config velocity, so the node did nothing for a player");
        whisperwind(evidence, "signature_cadence", "Dash cooldown", "Ability preparation",
                "Cooldown 155t", "Phase 3 cooldown pipeline", "Execution-scoped",
                "Verified: consumed on the player path and composed by both cooldown capstones");
        whisperwind(evidence, "signature_pressure", "Dash and guard duration", "Ability activation",
                "Dash 18t; Absorption 120t", "startPlayerAbility tuned effect durations", "Status TTL",
                "Repaired: the player path hardcoded 12 and 100 ticks, so both values were mob-only");
        whisperwind(evidence, "signature_reversal", "Wind Wake", "Dash completion",
                "Speed II 30t; knockback resistance 0.2", "WhisperwindRhythmManager.startWake", "Wake TTL; world unload",
                "Repaired: implemented; none of its three writes had a consumer");
        whisperwind(evidence, "signature_reserve", "Passing Cut", "Dash contact",
                "20% immediate; cap 8", "recordDashTick immediate damage", "Execution-scoped",
                "Repaired: implemented, and its target cap no longer silently limits the Delayed strike");
        whisperwind(evidence, "signature_threshold", "Wind's Return", "Delayed strike completion",
                "Threshold 3; refund 25t", "refundOnWideStrike additive cooldown refund", "Per strike",
                "Repaired: implemented through reduceWeaponCooldown; no refund path existed");
        whisperwind(evidence, "signature_convergence", "Crosswind", "Dash completion",
                "+1t per enemy; 4t cap", "One-shot dash extension in FatalFlickerEffect", "Consumed once per dash",
                "Repaired: implemented as a real extension; its only live write used to cap the Delayed strike to four enemies");
        whisperwind(evidence, "signature_focus", "Gale Passage capstone", "Dash and Delayed strike",
                "Range x1.5; cap 16; damage x0.75; cooldown x1.2", "Dash range multiplier and composed strike values", "Execution-scoped",
                "Repaired: the longer dash exists, and the cooldown drawback now multiplies instead of being overwritten");
        whisperwind(evidence, "signature_release", "Flashing Petal capstone", "Delayed strike",
                "Nearest only; 2 hits; damage x0.8", "Distance-ordered strike and repeat count", "Execution-scoped",
                "Redesigned: stopping the dash at the first enemy was unimplementable, so the node now strikes the nearest enemy twice; it previously delivered only its drawbacks");
        whisperwind(evidence, "combat_opening", "Delayed strike damage", "Delayed strike",
                "Strike damage x1.1", "applyStrike multiplier", "Execution-scoped",
                "Repaired: all three capstones multiply instead of discarding it");
        whisperwind(evidence, "combat_cadence", "Delayed strike timing", "Dash completion",
                "Delay 28t; damage x1.12", "Pending strike trigger tick and multiplier", "Execution-scoped",
                "Verified: both values are consumed, and the capstones state their own delay");
        whisperwind(evidence, "combat_pressure", "Gathered Bouquet", "Delayed strike",
                "+20% per-target scaling; cap 8", "applyStrike per-target damage scaling", "Execution-scoped",
                "Repaired: implemented; neither key had a consumer and the base scaled from config alone");
        whisperwind(evidence, "combat_reversal", "Bloodless Cut", "Delayed strike hit",
                "Weakness I 60t", "applyStrike status application", "Status TTL",
                "Verified: consumed on every damaged target");
        whisperwind(evidence, "combat_reserve", "Second Flowering", "Lethal Delayed strike",
                "35%; radius 3; cap 3", "secondFlowering splash from the victim", "Execution-scoped",
                "Repaired: implemented; the splash did not exist and its target cap limited the strike instead");
        whisperwind(evidence, "combat_threshold", "Wind Shear", "Delayed strike hit",
                "Ignore 15%; cap 4 armour", "applyArmorIgnore compensation", "Per hit",
                "Repaired: implemented; no armour-ignore path existed anywhere in the strike");
        whisperwind(evidence, "combat_convergence", "Perfect Arrangement", "Delayed strike",
                "Solo +30%; crowd 5 for +15%", "arrangementBonus target-count branch", "Execution-scoped",
                "Repaired: implemented; all four of its writes were unread");
        whisperwind(evidence, "combat_focus", "Thousand Petals capstone", "Delayed strike",
                "3 hits; damage x0.45; delay 36t; cap 8", "Repeat count and composed strike values", "Execution-scoped",
                "Repaired: the triple hit exists; the node used to apply only its delay, cap and damage cut");
        whisperwind(evidence, "combat_release", "Single Falling Leaf capstone", "Delayed strike",
                "Nearest only; damage x2.25; delay 16t; cooldown +30t", "Distance-ordered single-target strike", "Execution-scoped",
                "Repaired: targets are now distance-ordered, so \"nearest\" is real, and an invalid first entry no longer swallows the strike");
        whisperwind(evidence, "transformation_opening", "Refresh chance", "Melee hit",
                "Chance 20", "WhisperwindSwordItem postHit roll", "Per hit",
                "Verified: consumed by the refresh roll");
        whisperwind(evidence, "transformation_cadence", "Gentle Guard", "Ability activation",
                "Absorption 4", "startPlayerAbility absorption amplifier", "Status TTL",
                "Repaired: reaches the player path, and its duration write no longer shortens Lasting Flicker");
        whisperwind(evidence, "transformation_pressure", "Rhythmic Cuts", "Failed refresh roll",
                "+3 per failure; 15 cap; 100t window", "WhisperwindRhythmManager.resolveChance", "Window TTL; world unload",
                "Repaired: implemented; no failure counter existed");
        whisperwind(evidence, "transformation_reversal", "Light on Foot", "Held tick while ready",
                "Speed I; fall damage -10%", "Rhythm held tick and incoming-damage hook", "Recomputed each second; world unload",
                "Repaired: implemented; both writes were unread");
        whisperwind(evidence, "transformation_reserve", "Windbreak", "Ability activation",
                "20t; projectile damage -30%", "startWindbreak plus modifyIncomingDamage", "Window TTL; world unload",
                "Repaired: implemented; neither the Slowness removal nor the projectile reduction existed");
        whisperwind(evidence, "transformation_threshold", "Reprise", "Delayed strike kill then melee hit",
                "Window 80t", "recordStrikeKill arming resolveChance", "Window TTL; consumed on use",
                "Repaired: the kill condition exists; its chance write used to force a guaranteed refresh on every hit");
        whisperwind(evidence, "transformation_convergence", "Perfect Tempo", "Dash soon after a refresh",
                "Window 40t; +20%; Haste 60t", "tempoBonus applied to the Delayed strike", "Window TTL",
                "Repaired: implemented; all four writes were unread");
        whisperwind(evidence, "transformation_focus", "Dancing Gale capstone", "Refresh proc",
                "Refund 60t; interval 20t; Speed II 40t", "tryPartialRefresh additive cooldown reduction", "Interval TTL; world unload",
                "Repaired: refreshes now shorten the cooldown as described instead of clearing it outright");
        whisperwind(evidence, "transformation_release", "Still Wind capstone", "Every third attack then Delayed strike",
                "Threshold 3; window 80t; damage x1.75", "Rhythm attack counter priming a single-target strike", "Consumed on the next strike",
                "Repaired: the priming exists; the node used to make every strike permanently single-target at 175% and discarded the whole Bloom Cut chain");
        dreadwhisper(evidence, "signature_opening", "Rend front width", "Ability activation",
                "Front width 5", "Front visual, trail width and sweep box", "Execution-scoped",
                "Repaired: moved to a dedicated key, and the undocumented target-cap write was dropped");
        dreadwhisper(evidence, "signature_cadence", "Rend damage", "Dash contact",
                "Rend damage x1.1", "damageDashTargets weapon damage", "Execution-scoped",
                "Repaired: all four capstones multiply instead of discarding it");
        dreadwhisper(evidence, "signature_pressure", "Rend speed", "Dash tick",
                "Rend speed 1.76", "applyDashVelocity", "Execution-scoped",
                "Verified: consumed every tick, on a dedicated key");
        dreadwhisper(evidence, "signature_reversal", "Rend distance", "Dash tick",
                "Rend range 23", "tickDash distance gate and tick ceiling", "Execution-scoped",
                "Repaired: moved to a dedicated key that three wound and Gloam nodes could previously shrink to three blocks");
        dreadwhisper(evidence, "signature_reserve", "Deep Leech", "Dash contact",
                "Leech 40%; cap 18", "Per-dash leech accumulator", "Execution-scoped",
                "Repaired: the ceiling is enforced; the leech was uncapped and the tuning disagreed with the text");
        dreadwhisper(evidence, "signature_threshold", "Collision Feast", "Dash ending on a surface",
                "40%; radius 3; cap 8", "resolveDashFinish collision burst", "Execution-scoped",
                "Repaired: implemented on a new dash finish hook; the dash had no finish path at all");
        dreadwhisper(evidence, "signature_convergence", "Reaped Momentum", "Dash contact",
                "+4% speed and +3% damage per enemy; 20%/15% caps", "momentumBonus on velocity and damage", "Execution-scoped",
                "Repaired: implemented; all five writes were unread");
        dreadwhisper(evidence, "signature_focus", "Mass Reaving capstone", "Ability activation",
                "Width x2; cap 32; damage x0.7; leech 20%; cooldown x1.25",
                "Composed dash geometry, damage, leech and cooldown", "Execution-scoped",
                "Repaired: composes with Hungering Edge instead of discarding it");
        dreadwhisper(evidence, "signature_release", "Assassin's Rend capstone", "Dash contact",
                "Width 2; stop on hit; damage x2.2; leech 60%", "Dash termination on first contact", "Execution-scoped",
                "Repaired: the dash genuinely stops; it previously kept travelling under a silent target cap");
        dreadwhisper(evidence, "combat_opening", "Wound duration", "Dash contact",
                "Wound 260t", "applyWound status duration", "Status TTL",
                "Repaired: Reopen and Plague of Whispers no longer shorten it");
        dreadwhisper(evidence, "combat_cadence", "Wound consumption", "Direct melee on a wounded enemy",
                "Wound damage x1.12", "modifyIncomingDamage multiplier", "Consumed with the wound",
                "Repaired: both capstones multiply instead of discarding it");
        dreadwhisper(evidence, "combat_pressure", "Festering Cut", "Wound application",
                "Weakness I 260t", "applyWound status rider", "Status TTL",
                "Repaired: implemented; the key had no consumer");
        dreadwhisper(evidence, "combat_reversal", "Dark Patience", "Wound consumption",
                "+3% per second; 30% cap", "woundAgeBonus from remaining duration", "Consumed with the wound",
                "Repaired: implemented; no age tracking existed");
        dreadwhisper(evidence, "combat_reserve", "Splintered Pain", "Wound consumption",
                "35% of the bonus; range 3", "splinterPain nearest-neighbour hit", "Per consumption",
                "Repaired: implemented, and its range write no longer cuts Shadow Rend to three blocks");
        dreadwhisper(evidence, "combat_threshold", "Reopen", "Wound consumption",
                "25%; 80t wound; 120t per-enemy lockout", "reopenWound roll and lockout", "Lockout TTL; world unload",
                "Repaired: implemented; its only live write used to shorten Persistent Corruption");
        dreadwhisper(evidence, "combat_convergence", "Mortal Tell", "Wound consumption",
                "Below 25% for +30%; execution-immune +15%", "mortalBonus health and tag branch", "Consumed with the wound",
                "Repaired: implemented, reusing the existing execution-immune tag rather than a parallel boss check");
        dreadwhisper(evidence, "combat_focus", "Plague of Whispers capstone", "Dash contact",
                "Spread 2; range 4; 120t; consumption x0.7", "spreadWound seeding neighbours", "Status TTL",
                "Repaired: the spread exists; every one of the node's writes used to be a drawback, including a dash cut to four blocks");
        dreadwhisper(evidence, "combat_release", "Final Word capstone", "Wound consumption",
                "Wound 80t; consumption x2.5", "Composed wound duration and multiplier", "Status TTL",
                "Repaired: composes with Cruel Opening; the unimplementable expiry penalty was dropped from the description");
        dreadwhisper(evidence, "transformation_opening", "Trail duration", "Ability activation",
                "Trail 300t", "GloamStainManager trail duration", "Patch expiry",
                "Verified: consumed at trail creation");
        dreadwhisper(evidence, "transformation_cadence", "Clutching Shadow", "Trail contact",
                "Slowness II; 60t", "Trail slow amplifier and an additive PatchBehavior", "Patch expiry",
                "Repaired: implemented through a new additive beginTrail overload; both writes were unread");
        dreadwhisper(evidence, "transformation_pressure", "Veiled Passage", "Dash completion",
                "Veil 8t", "DreadwhisperTrailManager veil consulted by blocksIncomingDamage", "Veil TTL; world unload",
                "Repaired: implemented; immunity used to end with the dash");
        dreadwhisper(evidence, "transformation_reversal", "Gloam Hunger", "Dash and wound damage",
                "+15% on owner Gloam", "gloamRider on wound consumption", "Per hit",
                "Repaired: implemented; no Gloam check existed in either damage path");
        dreadwhisper(evidence, "transformation_reserve", "Shadow Recall", "Dash completion",
                "Range 6; pull 1.5; cap 8", "resolveDashFinish Gloam-scoped pull", "Execution-scoped",
                "Repaired: implemented on the new finish hook; its range write used to cut the dash to six blocks");
        dreadwhisper(evidence, "transformation_threshold", "Umbral Shelter", "Dash contact",
                "1 per enemy; limit 6; 80t", "Per-dash absorption accumulator", "Absorption expiry",
                "Repaired: implemented; all three writes were unread");
        dreadwhisper(evidence, "transformation_convergence", "Fading Footprint", "After the dash, standing in Gloam",
                "60t; projectile damage -25%", "Trail manager tick and incoming-damage hook", "Window TTL; world unload",
                "Repaired: implemented; both writes were unread");
        dreadwhisper(evidence, "transformation_focus", "Living Shadow capstone", "Dash completion",
                "100t; 20t pulses; 20%; cap 12", "DreadwhisperTrailManager following trail", "Trail TTL; world unload",
                "Repaired: the following trail exists; the node used to gut Shadow Rend to 20% damage and deliver nothing");
        dreadwhisper(evidence, "transformation_release", "Void Crossing capstone", "Dash completion",
                "No dash damage or leech; range 30; collapse 160% within 12", "resolveDashFinish Gloam-scoped collapse", "Execution-scoped",
                "Repaired: the collapse exists, so the zeroed dash damage is a real trade; the node previously reduced Shadow Rend to nothing at all");
        lichblade(evidence, "signature_opening", "Soul Anguish passive aura", "Held tick",
                "AURA_INTERVAL_TICKS 30", "Phase4LichbladeManager.tickPassive interval gate", "Execution-scoped; aura cache cleared on unload",
                "Repaired: moved off the shared INTERVAL_TICKS key, and the manager aura now emits the vanilla particle field and no longer siphons");
        lichblade(evidence, "signature_cadence", "Soul Anguish pulse radius", "Pulse",
                "RADIUS 3.5; TARGET_CAP 24", "Phase4LichbladeManager.pulse box and nearest() cap", "Execution-scoped",
                "Repaired: Resonant Souls no longer overwrites this cap with 8");
        lichblade(evidence, "signature_pressure", "Soul Anguish pulse damage", "Pulse",
                "DAMAGE_MULTIPLIER x1.1", "Phase4LichbladeManager.pulse multiplier", "Execution-scoped",
                "Composes multiplicatively and survives both signature capstones");
        lichblade(evidence, "signature_reversal", "Repeated Soul Anguish pulses", "Second pulse on the same target",
                "Mode 1; SLOW_DURATION_TICKS 60; REPEAT_WINDOW_TICKS 40", "Phase4LichbladeManager.recordPulse per-world history", "History cleared per world on unload",
                "Repaired: dedicated window and slow keys, and the history is keyed per world so a dimension change cannot misfire it");
        lichblade(evidence, "signature_reserve", "Soul Anguish pulse damage per target", "Pulse",
                "Mode 2; PER_TARGET_BONUS .03; BONUS_CAP .24", "Phase4LichbladeManager.pulse target-count bonus", "Execution-scoped",
                "Repaired: dropped the stray TARGET_CAP write and moved Grave Interest off the shared bonus keys");
        lichblade(evidence, "signature_threshold", "Soul Anguish kill", "Pulse kill",
                "Mode 4; DEATH_BURST_DAMAGE_MULTIPLIER .35; DEATH_BURST_RADIUS 3; DEATH_BURST_TARGET_CAP 4", "Phase4LichbladeManager.deathBurst from the victim position", "Execution-scoped",
                "Repaired: dropped the stray LOCKOUT_TICKS write that halved Clinging Misery's window");
        lichblade(evidence, "signature_convergence", "Soul Anguish pulse cadence", "Channel age past the window",
                "Mode 8; FAST_PULSE_INTERVAL_TICKS 4; FAST_PULSE_AFTER_TICKS 60", "Phase4LichbladeManager.pulseInterval", "Execution-scoped",
                "Repaired: real consumers for both values, and the mode bit no longer suppresses Swift Haunting");
        lichblade(evidence, "signature_focus", "Soul Anguish area capstone", "Channel start and pulse",
                "Mode 16; RADIUS 6; TARGET_CAP 32; DAMAGE_MULTIPLIER x.65; MOVEMENT_MULTIPLIER .6; COOLDOWN_MULTIPLIER x1.2", "Phase4LichbladeManager.pulse plus a managed movement-speed modifier and the cooldown pipeline", "Modifier removed on every finish path, world unload and server stop",
                "Repaired: the movement penalty is a real attribute modifier instead of an unread key and a player-ignored setVelocity, and the cooldown multiplies the configured base");
        lichblade(evidence, "signature_release", "Soul Anguish single-target capstone", "Pulse",
                "Mode 32; RADIUS 1.5; TARGET_CAP 1; DAMAGE_MULTIPLIER x2", "Phase4LichbladeManager.pulse primary-only branch", "Execution-scoped; dispersal resolves Absorption",
                "Repaired: dispersal no longer forfeits earned Absorption, and the description states the lock follows at any range");
        lichblade(evidence, "combat_opening", "Soul Anguish siphon", "Pulse hit",
                "CHANCE 12", "Phase4LichbladeManager.heal exact roll", "Execution-scoped",
                "Rolls once per damaged enemy, matching the description");
        lichblade(evidence, "combat_cadence", "Soul Anguish siphon amount", "Successful siphon",
                "HEAL_AMOUNT .75", "Phase4LichbladeManager.heal", "Execution-scoped",
                "Replaces the configured 0.5 heal");
        lichblade(evidence, "combat_pressure", "Soul Anguish return Absorption", "Channel end",
                "ABSORPTION_CAP 10", "Phase4LichbladeManager.resolveAbsorption cap", "Execution-scoped",
                "Reachable under the global ability absorption cap");
        lichblade(evidence, "combat_reversal", "Soul Anguish charge", "Pulse hit",
                "Mode 64; CHARGE_LOCKOUT_TICKS 10; CHARGE_PER_HIT 2", "Phase4LichbladeManager.addCharge per-target lockout", "Execution-scoped; expired locks pruned",
                "Repaired: dedicated CHARGE_PER_HIT key ends the four-way COUNT collision that made each hit grant ten charge");
        lichblade(evidence, "combat_reserve", "Soul Anguish charge overflow", "Channel end at a capped Absorption grant",
                "Mode 128; RESISTANCE_CHARGE_STEP 4; RESISTANCE_STEP_TICKS 40; RESISTANCE_DURATION_CAP_TICKS 120", "Phase4LichbladeManager.applyOverflowResistance", "Status-effect duration",
                "Repaired: the maximum-Absorption gate and the additional-charge accounting are now implemented instead of firing on raw charge");
        lichblade(evidence, "combat_threshold", "Soul Anguish overheal", "Siphon above maximum health",
                "Mode 256; HEAL_MULTIPLIER .5; TEMP_ABSORPTION_CAP 4; OVERHEAL_ABSORPTION_TICKS 80", "Phase4LichbladeManager.heal overheal conversion and the temporary absorption tracker", "Expires on the owner tick; cleared on unload and server stop",
                "Repaired: no longer truncates the channel through DURATION_TICKS, no longer strips existing Absorption down to four, and the four-second expiry is real");
        lichblade(evidence, "combat_convergence", "Soul Anguish return Absorption", "Channel end",
                "Mode 512; INTEREST_CHARGE_STEP 10; INTEREST_PER_STEP .1; INTEREST_CAP .4", "Phase4LichbladeManager.resolveAbsorption interest", "Execution-scoped",
                "Repaired: dedicated interest keys no longer overwrite Resonant Souls");
        lichblade(evidence, "combat_focus", "Soul Anguish defensive capstone", "Channel end",
                "Mode 1024; DAMAGE_MULTIPLIER x.7; CHANCE 0; BASTION_CHARGE_PER_ABSORPTION 2; ABSORPTION_CAP 16; BASTION_ABSORPTION_TICKS 160", "Phase4LichbladeManager.resolveAbsorption bastion branch and the temporary absorption tracker", "Expires on the owner tick; cleared on unload and server stop",
                "Repaired: no longer truncates the channel to eight seconds, and the advertised eight-second Absorption really expires");
        lichblade(evidence, "combat_release", "Soul Anguish siphon capstone", "Successful siphon then channel end",
                "Mode 2048; CHANCE 25; HEAL_AMOUNT 1; COOLDOWN_PER_SIPHON_TICKS 10; COOLDOWN_PENALTY_CAP_TICKS 200", "Phase4LichbladeManager.cooldownTicks siphon penalty", "Execution-scoped",
                "Repaired: the penalty reads its own keys, and the stray LOCKOUT_TICKS and ABSORPTION_CAP writes are gone");
        lichblade(evidence, "transformation_opening", "Soul Anguish target acquisition", "Ability preparation",
                "ACQUISITION_RANGE 26", "Phase4LichbladeManager.beginChannel lenient target search", "Execution-scoped",
                "Consumed before the channel starts, over the configured 22 block range");
        lichblade(evidence, "transformation_cadence", "Soul Anguish cloud advance", "Channel tick",
                "CLOUD_MOVE_INTERVAL_TICKS 4", "Phase4LichbladeManager.tickChannel move interval", "Execution-scoped",
                "Repaired: a dedicated key, so Unceasing Cry no longer suppresses this node entirely");
        lichblade(evidence, "transformation_pressure", "Soul Anguish channel length", "Channel tick and maximum use time",
                "DURATION_TICKS 240", "Phase4LichbladeManager.tickChannel duration and maxUseTime", "Execution-scoped",
                "Now the only node writing the channel duration");
        lichblade(evidence, "transformation_reversal", "Soul Anguish channel under damage", "First incoming damage during the channel",
                "Mode 4096; INTERRUPT_RESISTANCE_TICKS 20", "Phase4LichbladeManager.onOwnerDamaged once-per-cast guard", "Execution-scoped",
                "Repaired: the description now matches the mechanic, because no base weapon path interrupts the channel on damage; the stray STATUS_DURATION_TICKS write is gone");
        lichblade(evidence, "transformation_reserve", "Soul Anguish return", "Return after losing every target",
                "Mode 8192; SPEED 1.5", "Phase4LichbladeManager.returnSpeed in moveCloud", "Execution-scoped",
                "Repaired: gated on a death-caused return and the redundant no-damage clause is gone, since the base return leg never pulses");
        lichblade(evidence, "transformation_threshold", "Soul Anguish retargeting", "Target death inside the window",
                "Mode 16384; RETARGET_RANGE 8; RETARGET_CAP 1; RETARGET_WINDOW_TICKS 80", "Phase4LichbladeManager.retargetMaximum and retargetOrReturn", "Execution-scoped",
                "Repaired: a real window key replaces the unread THRESHOLD, and this node no longer silently pays Wandering Phylactery's damage penalty");
        lichblade(evidence, "transformation_convergence", "Soul Anguish cooldown", "Channel end",
                "COOLDOWN_TICKS 600", "Phase4LichbladeManager.cooldownTicks over the execution cooldown key", "Execution-scoped",
                "Repaired: composes with Choir of the Damned through COOLDOWN_MULTIPLIER instead of hand-coding a 720 tick branch");
        lichblade(evidence, "transformation_focus", "Soul Anguish chaining capstone", "Each target death",
                "Mode 32768; RETARGET_RANGE 8; RETARGET_CAP 4; RETARGET_DAMAGE_PENALTY .15; RETARGET_DAMAGE_FLOOR .4; ABSORPTION_CAP 0", "Phase4LichbladeManager.retargetPenalty and retargetOrReturn", "Execution-scoped",
                "Repaired: the penalty reads its own keys, applies only to this capstone, and no longer overwrites Resonant Souls or Grave Interest");
        lichblade(evidence, "transformation_release", "Soul Anguish recall capstone", "Early channel release",
                "Mode 65536; RECALL_DAMAGE_MULTIPLIER 1.25; RECALL_RADIUS 4; RECALL_COOLDOWN_TICKS 100", "Phase4LichbladeManager.releaseChannel and recallBurst", "Execution-scoped; components cleared on finish",
                "Redesigned: reactivation could never fire on a hold channel, so an early release now triggers the recall; the burst also composes the pulse damage multiplier");
        sunfire(evidence, "signature_opening", "Righteous Standard aura damage", "Aura pulse",
                "DAMAGE_MULTIPLIER x1.1", "Phase4StandardManager.sunfireHostilePulse multiplier", "Execution-scoped",
                "Repaired: Solar Pillar now multiplies instead of overwriting, so this bonus survives its capstone");
        sunfire(evidence, "signature_cadence", "Righteous Standard hostile radius", "Aura pulse",
                "RADIUS 7", "Phase4StandardManager.sunfireHostilePulse target box", "Execution-scoped",
                "Repaired: dropped the TARGET_CAP write that only restated the default");
        sunfire(evidence, "signature_pressure", "Righteous Standard burn", "Aura pulse and landing",
                "FIRE_TICKS 40", "setOnFireFor in sunfireHostilePulse and sunfireLanding", "Vanilla fire duration",
                "Two seconds on both the pulse and the landing");
        sunfire(evidence, "signature_reversal", "Repeated aura pulses", "Third pulse inside the window",
                "Mode 1; WEAKNESS_PULSE_COUNT 3; WEAKNESS_WINDOW_TICKS 40; WEAKNESS_DURATION_TICKS 60", "Phase4StandardManager.nextPulseCount and the Weakness application", "Window entries pruned per pulse",
                "Repaired: the two-second window is real. The count previously accumulated for the standard's whole life and never decayed, so Weakness applied on every pulse forever, and its duration was overwritten by Enduring Valor");
        sunfire(evidence, "signature_reserve", "Righteous Standard landing", "Standard touching ground",
                "LANDING_DAMAGE_MULTIPLIER 3.5; LANDING_RADIUS 2; LANDING_TARGET_CAP 12", "Phase4StandardManager.sunfireLanding", "One-shot per standard",
                "Repaired: the landing restores the vanilla launch and particles the manager path had dropped");
        sunfire(evidence, "signature_threshold", "Righteous Standard aura cycle", "Every fourth pulse",
                "Mode 2; CYCLE_PULSE_COUNT 4; CYCLE_DAMAGE_MULTIPLIER 1.4; GLOWING_DURATION_TICKS 60", "Phase4StandardManager.sunfireHostilePulse cycle branch", "Execution-scoped",
                "Repaired: a dedicated cycle count, so Rallying Standard no longer turns every fourth pulse into every third");
        sunfire(evidence, "signature_convergence", "Righteous Standard early aura", "First five seconds",
                "Mode 4; EARLY_WINDOW_TICKS 100; EARLY_DAMAGE_MULTIPLIER 1.2", "Phase4StandardManager.sunfireHostilePulse early branch", "Execution-scoped",
                "Repaired: the window is a real consumer rather than a hardcoded age check with a dead write");
        sunfire(evidence, "signature_focus", "Righteous Standard following capstone", "Standard tick",
                "Mode 8; MOVEMENT_SPEED .35; RADIUS 5; DAMAGE_MULTIPLIER x.75", "Phase4StandardManager.moveSunfire and the support suppression", "Execution-scoped",
                "Movement, radius, damage penalty and the suppressed ally support are all consumed");
        sunfire(evidence, "signature_release", "Righteous Standard stationary capstone", "Standard tick and aura pulse",
                "Mode 16; RADIUS 9; INTERVAL_TICKS 15; DAMAGE_MULTIPLIER x1.6; LIFETIME_MULTIPLIER .65", "Phase4StandardManager.standardLifetime, pulse interval and the suppressed Slowness", "Execution-scoped",
                "Repaired: multiplies instead of overwriting so Hotter Aura survives, and the description no longer claims immobility the base standard already has");
        sunfire(evidence, "combat_opening", "Righteous Standard ally healing", "Support pulse",
                "HEAL_MULTIPLIER x1.12", "Phase4StandardManager.sunfireSupportPulse heal", "Execution-scoped",
                "Repaired: Sanctuary of Noon now multiplies, so this bonus survives its capstone");
        sunfire(evidence, "combat_cadence", "Righteous Standard support radius", "Support pulse",
                "SUPPORT_RADIUS 7", "Phase4StandardManager.sunfireSupportPulse ally query", "Execution-scoped",
                "Repaired: dropped the SUPPORT_TARGET_CAP write that only restated the default");
        sunfire(evidence, "combat_pressure", "Righteous Standard Strength", "Support pulse",
                "STRENGTH_DURATION_TICKS 120", "Phase4StandardManager.sunfireSupportPulse Strength application", "Status-effect duration",
                "Repaired: a dedicated key, so this no longer doubles Scorching Ground's Weakness");
        sunfire(evidence, "combat_reversal", "Righteous Standard ally cleanse", "Support pulse",
                "Mode 32; CLEANSE_LOCKOUT_TICKS 160", "Phase4StandardManager.removeHarmful and the per-ally lockout", "Expired locks pruned per pulse",
                "Repaired: the eight-second lockout is a real consumer instead of a hardcoded constant");
        sunfire(evidence, "combat_reserve", "Righteous Standard ally Regeneration", "Support pulse",
                "Mode 64; ALLY_REGEN_TICKS 60", "Phase4StandardManager.sunfireSupportPulse Regeneration", "Status-effect duration",
                "Repaired: moved off the shared DURATION_TICKS key");
        sunfire(evidence, "combat_threshold", "Righteous Standard ally Absorption", "Support pulse on a wounded ally",
                "Mode 128; GUARDIAN_THRESHOLD .35; GUARDIAN_ABSORPTION 4; GUARDIAN_ABSORPTION_TICKS 100; GUARDIAN_LOCKOUT_TICKS 200", "Phase4AbsorptionTracker grant driven by sunfireSupportPulse", "Expires on the owner tick and on the standard sweep; cleared on unload and server stop",
                "Repaired: all four values were inert against hardcoded constants, and the advertised five-second Absorption was permanent");
        sunfire(evidence, "combat_convergence", "Righteous Standard cooldown", "Support pulse reaching the ally count",
                "Mode 256; RALLY_ALLY_COUNT 3; RALLY_REFUND_TICKS 40", "Phase4StandardManager.rallyTriggers and reduceCooldown", "One-shot per standard; refund capped at 100 ticks",
                "Repaired: the refund now shortens the remaining cooldown through reduceWeaponCooldown. The absolute set it used could lengthen a part-elapsed cooldown, and it resolved the Harbinger standard first");
        sunfire(evidence, "combat_focus", "Righteous Standard support capstone", "Support pulse",
                "Mode 512; SUPPORT_INTERVAL_TICKS 40; HEAL_MULTIPLIER x1.5; SANCTUARY_RESISTANCE_TICKS 60", "Phase4StandardManager.sunfireSupportPulse and the suppressed hostile pulse and landing", "Execution-scoped",
                "Repaired: the landing burst is now suppressed too, so 'deals no damage' is true, and the heal bonus composes with Generous Light");
        sunfire(evidence, "combat_release", "Righteous Standard offensive support capstone", "Support pulse then the ally's next attack",
                "Mode 1024; SUPPORT_RADIUS 5; STRENGTH_DURATION_TICKS 100; ALLY_CHARGE_TICKS 60; FIRE_TICKS 40", "supportSunfireAlly through modifyOutgoingDamage and onDamageApplied", "Charge expires or is consumed on the next attack",
                "Repaired: its charge window moved off the shared DURATION_TICKS key");
        sunfire(evidence, "transformation_opening", "Sunfire melee Regeneration chance", "Melee hit",
                "CHANCE 20", "Phase4PassiveManager.sunfireMelee exact roll", "Execution-scoped",
                "Twenty percent through the exact-chance path");
        sunfire(evidence, "transformation_cadence", "Sunfire melee Regeneration duration", "Regeneration proc",
                "STATUS_DURATION_TICKS 60", "Phase4PassiveManager.sunfireMelee Regeneration and regenUntil", "Status-effect duration",
                "The only writer of this key in regeneration scope");
        sunfire(evidence, "transformation_pressure", "Sunfire melee Fire Resistance", "Regeneration proc",
                "Mode 2048; FIRE_RESISTANCE_TICKS 80", "Phase4PassiveManager.sunfireMelee Fire Resistance", "Status-effect duration",
                "Repaired: a real consumer replaces the hardcoded duration and its dead write");
        sunfire(evidence, "transformation_reversal", "Sunfire stored healing", "Regeneration proc at full health, spent below the threshold",
                "Mode 4096; RESERVE_CAP 4; RESERVE_THRESHOLD .5; RESERVE_ABSORPTION_TICKS 100", "Phase4PassiveManager.sunfireMelee accumulation and tickHeld payout through Phase4AbsorptionTracker", "Expires on the owner tick; cleared on unload and server stop",
                "Repaired: mode bit 4096 had no consumer, so the whole mechanic ran for every Sunfire wielder whether or not they owned the node. Its cap, threshold and five-second duration were all inert");
        sunfire(evidence, "transformation_reserve", "Sunfire retaliation burn", "Melee hit taken while Regeneration is active",
                "Mode 8192; REPRISAL_FIRE_TICKS 40; REPRISAL_LOCKOUT_TICKS 40", "Phase4PassiveManager.modifyIncomingDamage per-attacker lockout", "Expired locks pruned on use",
                "Repaired: dedicated lockout and fire keys, so Rekindling no longer stretches the two-second lockout to thirty");
        sunfire(evidence, "transformation_threshold", "Righteous Standard proximity guard", "Incoming damage near the standard",
                "Mode 16384; GUARD_RANGE 7; DAMAGE_REDUCTION .15; KNOCKBACK_RESISTANCE .2", "Phase4StandardManager.modifyIncomingDamage and a managed knockback-resistance modifier", "Modifier removed out of range, on the Phoenix trigger, on unload and on server stop",
                "Repaired: the advertised knockback resistance did not exist anywhere, and the range was a hardcoded squared constant");
        sunfire(evidence, "transformation_convergence", "Sunfire low-health recovery", "Held tick below the threshold",
                "Mode 32768; REKINDLE_THRESHOLD .3; REKINDLE_DURATION_TICKS 100; REKINDLE_ABSORPTION 4; REKINDLE_LOCKOUT_TICKS 600", "Phase4PassiveManager.tickHeld and Phase4AbsorptionTracker", "Expires on the owner tick; cleared on unload and server stop",
                "Repaired: three inert values given real consumers and its lockout separated from Radiant Reprisal's");
        sunfire(evidence, "transformation_focus", "Righteous Standard lethal-damage capstone", "Lethal incoming damage",
                "Mode 65536; PHOENIX_DURATION_TICKS 80; PHOENIX_COOLDOWN_TICKS 300; DAMAGE_MULTIPLIER x.8", "Phase4StandardManager.modifyIncomingDamage lethal branch", "Standard discarded, guard removed and execution finished",
                "Repaired: duration and cooldown surcharge are tuned rather than hardcoded, and the proximity guard is removed with the standard");
        sunfire(evidence, "transformation_release", "Sunfire combo flare capstone", "Third melee hit inside the window",
                "Mode 131072; CHANCE 0; COMBO_COUNT 3; COMBO_WINDOW_TICKS 80; FLARE_LOCKOUT_TICKS 40; RADIUS 3; FLARE_DAMAGE_MULTIPLIER .7; TARGET_CAP 8; HEAL_AMOUNT 1", "Phase4PassiveManager.flare driven by the combo counter", "Per-owner lockout; state expires after 2400 idle ticks",
                "Repaired: the flare now scales off aura damage as described rather than weapon attack damage, its combo count and window are tuned, and the description states that it also stops Ember Reserve");
        harbinger(evidence, "signature_opening", "Abyssal Standard aura damage", "Aura pulse",
                "DAMAGE_MULTIPLIER x1.1", "Phase4StandardManager.harbingerHostilePulse multiplier", "Execution-scoped",
                "Repaired: Black Monolith and Solitary Harbinger both multiply now. The latter sits in the combat branch and its absolute write discarded the entire signature branch's damage tuning");
        harbinger(evidence, "signature_cadence", "Abyssal Standard hostile radius", "Aura pulse",
                "RADIUS 7", "Phase4StandardManager.harbingerHostilePulse target box", "Execution-scoped",
                "Repaired: Doom Cycle's and Executioner's Portent's stray TARGET_CAP writes are gone, so the cap is no longer cut to 24 or to 1");
        harbinger(evidence, "signature_pressure", "Abyssal Standard inward pull", "Aura pulse beyond the edge",
                "PULL_STRENGTH x1.25", "Phase4StandardManager.applyHarbingerPull base branch", "Execution-scoped",
                "Repaired: the base pull is distance-proportional again, matching the vanilla aura it replaced, and the two capstones no longer overwrite this multiplier with an absolute value the consumer ignored");
        harbinger(evidence, "signature_reversal", "Repeated aura pulses", "Third pulse inside the window",
                "Mode 1; WEAKNESS_PULSE_COUNT 3; WEAKNESS_WINDOW_TICKS 40; WEAKNESS_DURATION_TICKS 80", "Phase4StandardManager.nextPulseCount and the Weakness application", "Window entries pruned per pulse",
                "Repaired: the two-second window is real. The count accumulated for the standard's whole life, and the duration was overwritten by Lingering Haste, Solitary Harbinger or Plague Standard");
        harbinger(evidence, "signature_reserve", "Abyssal Standard landing", "Standard touching ground",
                "LANDING_DAMAGE_MULTIPLIER 3.5; LANDING_RADIUS 2; LANDING_TARGET_CAP 12", "Phase4StandardManager.harbingerLanding", "One-shot per standard",
                "Repaired: the landing restores the vanilla launch and particles the manager path had dropped");
        harbinger(evidence, "signature_threshold", "Abyssal Standard near-target damage", "Aura pulse inside the core",
                "Mode 2; CORE_RANGE 2; NEAR_DAMAGE_MULTIPLIER 1.2", "Phase4StandardManager.harbingerHostilePulse core branch", "Execution-scoped",
                "Repaired: a dedicated range key, so this no longer lands on the pursuit and owner-aura ranges");
        harbinger(evidence, "signature_convergence", "Abyssal Standard aura cycle", "Every fifth pulse",
                "Mode 4; CYCLE_PULSE_COUNT 5; CYCLE_PULL_STRENGTH 1.5; CYCLE_DAMAGE_MULTIPLIER 1.4", "Phase4StandardManager.harbingerHostilePulse cycle branch and applyHarbingerPull", "Execution-scoped",
                "Repaired: the cycle pull was ungated, so every standard pulled everything every fifth pulse without this node; its count and pull strength were inert and its stray TARGET_CAP shrank the aura");
        harbinger(evidence, "signature_focus", "Abyssal Standard pursuing capstone", "Standard tick",
                "Mode 8; PURSUIT_RANGE 12; MOVEMENT_SPEED .3; RADIUS 4; DAMAGE_MULTIPLIER x.8", "Phase4StandardManager.moveHarbinger and the suppressed ally support", "Execution-scoped",
                "Repaired: a dedicated pursuit range, so Commanding Presence no longer shrinks the chase to seven blocks");
        harbinger(evidence, "signature_release", "Abyssal Standard stationary capstone", "Aura pulse",
                "Mode 16; RADIUS 9; INTERVAL_TICKS 15; CONSTANT_PULL_STRENGTH 1.5; DAMAGE_MULTIPLIER x1.6; LIFETIME_MULTIPLIER .65", "Phase4StandardManager.applyHarbingerPull constant branch and standardLifetime", "Execution-scoped",
                "Repaired: multiplies instead of overwriting, its pull strength is a real consumer, and the description no longer claims immobility the base standard already has");
        harbinger(evidence, "combat_opening", "Abyssal Standard ally Haste", "Support pulse",
                "HASTE_DURATION_TICKS 120", "Phase4StandardManager.harbingerSupportPulse Haste application", "Status-effect duration",
                "Repaired: a dedicated key, so this no longer lengthens Crushing Gloom's Weakness");
        harbinger(evidence, "combat_cadence", "Abyssal Standard support radius", "Support pulse",
                "SUPPORT_RADIUS 7", "Phase4StandardManager.harbingerSupportPulse ally query", "Execution-scoped",
                "Repaired: dropped the SUPPORT_TARGET_CAP write that only restated the default");
        harbinger(evidence, "combat_pressure", "Abyssal Standard owner aura", "Owner within range of the standard",
                "Mode 32; OWNER_AURA_RANGE 7", "Phase4StandardManager.harbingerOwnerAura", "Status-effect duration",
                "Repaired: mode bit 32 had no consumer, so the range came from a shared key that Crushing Core and Marching Omen also wrote and that applied to wielders who did not own this node");
        harbinger(evidence, "combat_reversal", "Abyssal Standard ally Speed", "Support pulse",
                "Mode 64; ALLY_SPEED_TICKS 80", "Phase4StandardManager.harbingerSupportPulse Speed application", "Status-effect duration",
                "Repaired: moved off the shared DURATION_TICKS key");
        harbinger(evidence, "combat_reserve", "Abyssal Standard ally malice", "Support pulse then the ally's next melee hit",
                "Mode 128; ALLY_CHARGE_TICKS 80; ALLY_WEAKNESS_TICKS 60", "supportHarbingerAlly through modifyOutgoingDamage and onDamageApplied", "Charge expires or is consumed on the next hit",
                "Repaired: both the charge window and the applied Weakness read their own keys instead of literals");
        harbinger(evidence, "combat_threshold", "Abyssal Standard cooldown", "Support pulse reaching the ally count",
                "Mode 256; RALLY_ALLY_COUNT 3; RALLY_REFUND_TICKS 40", "Phase4StandardManager.rallyTriggers and reduceCooldown", "One-shot per standard; refund capped",
                "Repaired: both values are real consumers, and the refund direction was corrected during the Sunfire repair");
        harbinger(evidence, "combat_convergence", "Abyssal Standard ally formation", "Support pulse",
                "Mode 512; ALLY_CHARGE_TICKS 80; SUPPORT_DAMAGE_BONUS .1; KNOCKBACK_RESISTANCE .15", "supportHarbingerAlly damage bonus and a managed ally knockback-resistance modifier", "Modifier released when a pulse no longer reaches the ally, on standard end, unload and server stop",
                "Repaired: the advertised knockback resistance did not exist anywhere");
        harbinger(evidence, "combat_focus", "Abyssal Standard support capstone", "Support pulse",
                "Mode 1024; SUPPORT_INTERVAL_TICKS 40; STATUS_AMPLIFIER 3; OWNER_HASTE_AMPLIFIER 1; ALLY_SPEED_TICKS 60; HASTE_DURATION_TICKS 60; ALLY_CHARGE_TICKS 60; SUPPORT_DAMAGE_BONUS .15", "Phase4StandardManager.harbingerSupportPulse and the suppressed hostile pulse and landing", "Execution-scoped",
                "Repaired: the landing burst is now suppressed too, so 'deals no damage' is true, and the Haste amplifiers are tuned rather than hardcoded");
        harbinger(evidence, "combat_release", "Abyssal Standard solitary capstone", "Owner within range",
                "Mode 2048; RADIUS 5; DAMAGE_MULTIPLIER x1.25; OWNER_HASTE_AMPLIFIER 2", "Phase4StandardManager.harbingerOwnerAura and setHarbingerOwnerBonus", "Owner bonus expires on its deadline",
                "Repaired: multiplies instead of overwriting, and its stray STATUS_DURATION_TICKS write no longer cuts Crushing Gloom to three seconds");
        harbinger(evidence, "transformation_opening", "Harbinger melee omen chance", "Melee hit",
                "CHANCE 20", "Phase4PassiveManager.harbingerMelee exact roll", "Execution-scoped",
                "Twenty percent through the exact-chance path");
        harbinger(evidence, "transformation_cadence", "Harbinger melee omen duration", "Weakness application",
                "STATUS_DURATION_TICKS 220", "Phase4PassiveManager.harbingerMelee Weakness", "Status-effect duration",
                "Eleven seconds; the description now states that Plague Standard sets its own duration instead");
        harbinger(evidence, "transformation_pressure", "Repeated omens on one enemy", "Third application inside the window",
                "Mode 4096; OMEN_UPGRADE_COUNT 3; OMEN_UPGRADE_TICKS 80; OMEN_WINDOW_TICKS 200", "Phase4PassiveManager.harbingerMelee omen counter", "Counter resets on a new target or a lapsed window",
                "Repaired: all three values were inert against literals, and the window write broke Drawn to Doom's lockout");
        harbinger(evidence, "transformation_reversal", "Harbinger melee against weakened enemies", "Melee hit on a weakened target",
                "MELEE_DAMAGE_MULTIPLIER 1.1", "Phase4PassiveManager.modifyOutgoingDamage", "Execution-scoped",
                "Correctly gated on Weakness and on the Harbinger weapon stack");
        harbinger(evidence, "transformation_reserve", "Harbinger melee pull", "Melee hit on a weakened enemy near the standard",
                "Mode 8192; DOOM_PULL_RANGE 10; PULL_STRENGTH .75; DOOM_PULL_LOCKOUT_TICKS 20", "Phase4PassiveManager.harbingerMelee pull with a per-enemy lockout", "Expired locks pruned on use",
                "Repaired: it pulled on any melee hit rather than only weakened targets, its range was a hardcoded squared constant, and its lockout was one per-owner timer that Deep Foretelling and Fulfilled Prophecy also overwrote");
        harbinger(evidence, "transformation_threshold", "Harbinger weakened kill", "Killing a weakened enemy",
                "Mode 16384; REFUND_TICKS 20; PROPHECY_REFUND_CAP_TICKS 100", "Phase4PassiveManager.harbingerMelee kill branch and reduceCooldown", "Refund capped per standard",
                "Repaired: the five-second cap is a real consumer, and its LOCKOUT_TICKS write no longer breaks Drawn to Doom");
        harbinger(evidence, "transformation_convergence", "Abyssal Standard against weakened prey", "Aura pulse on a weakened target",
                "Mode 32768; LOW_HEALTH_THRESHOLD .25; LOW_HEALTH_DAMAGE_MULTIPLIER 1.2; BOSS_DAMAGE_MULTIPLIER 1.1", "Phase4StandardManager.finalOmenMultiplier using the execution-immune tag", "Execution-scoped",
                "Repaired: the Weakness requirement was never checked and the boss clause was entirely unimplemented. The boss test reuses WatcherAbilityManager.isExecutionImmune, matching Dreadwhisper's Mortal Tell");
        harbinger(evidence, "transformation_focus", "Harbinger plague capstone", "Aura pulse and melee hit",
                "Mode 65536; CHANCE 100; PLAGUE_DAMAGE_MULTIPLIER .8; PLAGUE_WEAKNESS_TICKS 120", "harbingerHostilePulse and modifyOutgoingDamage plague branches", "Status-effect duration",
                "Repaired: its damage multiplier has its own key, since it shared one with Executioner's Portent that carried the opposite default");
        harbinger(evidence, "transformation_release", "Harbinger single-omen capstone", "Aura pulse and melee hit on the marked enemy",
                "Mode 131072; EXECUTION_DAMAGE_MULTIPLIER 1.4; EXECUTION_COOLDOWN_TICKS 80; COOLDOWN_TICKS +80", "harbingerMelee omen tracking, harbingerHostilePulse and modifyOutgoingDamage", "Previous omen cleared when a new target is marked",
                "Repaired: its stray TARGET_CAP 1 reached the standard definition and cut the aura from thirty-two targets to one, and its damage multiplier now has its own key");
        phase5Fire(evidence, "hearthflame", "signature_opening", "Furnace-chain echo damage", "Echo pulse",
                "HEARTH_ECHO_DAMAGE_MULTIPLIER x1.1", "HearthflameAbilityManager.echoHit", "Execution-scoped",
                "Verified after moving Hearthflame onto its dedicated runtime manager");
        phase5Fire(evidence, "hearthflame", "signature_cadence", "Furnace-chain acquisition", "Ability activation",
                "HEARTH_BIND_RANGE +2", "HearthflameAbilityManager.findTargets", "Execution-scoped",
                "The range now reaches the live target query while preserving the six-target base cap");
        phase5Fire(evidence, "hearthflame", "signature_pressure", "Chain contraction and pull", "Active-chain tick",
                "PULL_STRENGTH x1.2; HEARTH_MIN_LENGTH -0.5", "HearthflameAbilityManager.tickAbilities and pullTarget", "Execution-scoped",
                "Both advertised changes are consumed by the contraction loop");
        phase5Fire(evidence, "hearthflame", "signature_reversal", "Chain echo ignition", "Echo pulse hit",
                "HEARTH_ECHO_FIRE_TICKS 40", "HearthflameAbilityManager.echoHit", "Vanilla fire duration",
                "Two seconds of fire refreshes on each echo");
        phase5Fire(evidence, "hearthflame", "signature_reserve", "Furnace-chain echo damage", "Echo pulse",
                "HEARTH_ECHO_DAMAGE_MULTIPLIER x1.15", "HearthflameAbilityManager.echoHit", "Execution-scoped",
                "Composes multiplicatively with Tempered Links instead of overwriting it");
        phase5Fire(evidence, "hearthflame", "signature_threshold", "Pressure from snapped chains", "Chain snap",
                "HEARTH_SNAP_PRESSURE_MULTIPLIER x1.2; HEARTH_SNAP_RADIUS +0.5", "HearthflameAbilityManager.addSnapPressure and snapChain", "Execution-scoped",
                "Redesigned from an inert pressure threshold into a snap-pressure and radius bonus");
        phase5Fire(evidence, "hearthflame", "signature_convergence", "Six-chain finale", "Final shatter after six initial bindings",
                "HEARTH_FINAL_DAMAGE_MULTIPLIER x1.25", "HearthflameAbilityManager.gatedFinalDamage", "Execution-scoped",
                "The multiplier is gated on all six enemies having been bound");
        phase5Fire(evidence, "hearthflame", "signature_focus", "Wide furnace network", "Ability activation and echo pulse",
                "TARGET_CAP 8; HEARTH_ECHO_DAMAGE_MULTIPLIER x1.2; PULL_STRENGTH x0.7", "HearthflameAbilityManager.findTargets, echoHit and pullTarget", "Execution-scoped",
                "The extra targets, echo bonus and pull penalty all compose in one execution");
        phase5Fire(evidence, "hearthflame", "signature_release", "Single-target furnace sentence", "Ability activation and finale",
                "TARGET_CAP 1; HEARTH_CHAIN_DURATION_TICKS x0.6; HEARTH_FINAL_DAMAGE_MULTIPLIER x2", "HearthflameAbilityManager activation, tickAbilities and finishAbility", "Execution-scoped",
                "The target cap, faster contraction window and doubled finale are all consumed");
        phase5Fire(evidence, "hearthflame", "combat_opening", "Cast protection", "Ability activation",
                "HEARTH_CAST_ABSORPTION 4; HEARTH_CAST_ABSORPTION_DURATION_TICKS 80", "HearthflameAbilityManager.grantTimedAbsorption", "Tracked absorption expires after four seconds and clears on lifecycle teardown",
                "Repaired by giving the advertised temporary absorption a bounded tracker");
        phase5Fire(evidence, "hearthflame", "combat_cadence", "Fire protection while chained", "Incoming fire damage",
                "HEARTH_FIRE_DAMAGE_REDUCTION 0.3", "HearthflameAbilityManager.modifyIncomingDamage", "Ends with the active chain execution",
                "The damage hook now reaches Hearthflame and only applies while chains persist");
        phase5Fire(evidence, "hearthflame", "combat_pressure", "Snap resistance", "Each chain snap",
                "HEARTH_SNAP_RESISTANCE_DURATION_TICKS 30; HEARTH_SNAP_RESISTANCE_MAX_TICKS 90", "HearthflameAbilityManager.grantSnapResistance", "Vanilla status duration capped at 4.5 seconds",
                "Refresh and extension are deterministic and bounded");
        phase5Fire(evidence, "hearthflame", "combat_reversal", "Protection from nearby bound enemies", "Incoming damage from a bound attacker",
                "HEARTH_BOUND_DAMAGE_REDUCTION 0.15; HEARTH_BOUND_DAMAGE_REDUCTION_RANGE 6", "HearthflameAbilityManager.modifyIncomingDamage", "Ends when the chain snaps or the execution clears",
                "Both the bound-attacker identity and six-block range are checked");
        phase5Fire(evidence, "hearthflame", "combat_reserve", "Brand-hit absorption", "Melee hit on a Branded enemy",
                "HEARTH_BRAND_ABSORPTION 2; HEARTH_BRAND_ABSORPTION_LOCKOUT_TICKS 40", "HearthflameAbilityManager.onMeleeHit", "Per-owner/target lockout expires; absorption remains until spent",
                "The two-second lockout is isolated from reactive branding");
        phase5Fire(evidence, "hearthflame", "combat_threshold", "Chain preservation", "First would-be snap",
                "HEARTH_CHAIN_PRESERVE_TICKS 20", "HearthflameAbilityManager.onMeleeHit and tickAbilities expiry", "One use per chain; execution-scoped",
                "The first snap is postponed exactly one second and cannot retrigger");
        phase5Fire(evidence, "hearthflame", "combat_convergence", "Safe finale protection", "Final shatter with three completed chains",
                "HEARTH_COMPLETION_ABSORPTION 4; HEARTH_COMPLETION_ABSORPTION_DURATION_TICKS 60; HEARTH_COMPLETION_MIN_CHAINS 3", "HearthflameAbilityManager.grantCompletionAbsorption", "Tracked absorption expires after three seconds and clears on lifecycle teardown",
                "Completion count, value and duration all have live consumers");
        phase5Fire(evidence, "hearthflame", "combat_focus", "Fixed Bastion anchor", "Ability activation and active-chain tick",
                "HEARTH_ANCHOR_SPEED_MULTIPLIER 0.75", "HearthflameAbilityManager.anchor, applyBastionMovementPenalty and anchored visuals", "Movement modifier, managed status and visuals clear when the execution ends, unloads or stops",
                "Repaired so mechanics and chain visuals remain at the cast position while Resistance II persists");
        phase5Fire(evidence, "hearthflame", "combat_release", "Mobile Hearth stance", "Ability activation and active-chain tick",
                "HEARTH_BREAK_RANGE +6; HEARTH_BIND_RANGE x0.8; HEARTH_FINAL_DAMAGE_MULTIPLIER x0.8", "HearthflameAbilityManager activation, findTargets, tickAbilities and finishAbility", "Managed Speed duration and execution-scoped tuning",
                "Redesigned to provide a real break-range benefit with explicit radius and finale penalties");
        phase5Fire(evidence, "hearthflame", "transformation_opening", "Furnace Brand chance", "Melee hit",
                "CHANCE +10 percentage points", "HearthflameAbilityManager.rollBrand", "Execution-scoped roll",
                "The additive chance reaches the exact brand roll");
        phase5Fire(evidence, "hearthflame", "transformation_cadence", "Furnace Brand duration", "Successful brand application",
                "HEARTH_BRAND_DURATION_TICKS +80", "HearthflameAbilityManager.applyBrand", "Brand expires by deadline and clears on unload or stop",
                "Adds four seconds to the live brand deadline");
        phase5Fire(evidence, "hearthflame", "transformation_pressure", "Attacks against Branded enemies", "Melee hit on own Brand",
                "HEARTH_BRAND_HIT_DAMAGE_MULTIPLIER 1.12; HEARTH_BRAND_HIT_FIRE_TICKS 20", "HearthflameAbilityManager.onMeleeHit", "Fire refreshes; Brand retains its own deadline",
                "Bonus damage and one-second burning both require the attacker's Brand");
        phase5Fire(evidence, "hearthflame", "transformation_reversal", "Reactive Furnace Brand", "Melee damage taken",
                "HEARTH_REACTIVE_BRAND_DURATION_TICKS 120; HEARTH_REACTIVE_BRAND_LOCKOUT_TICKS 60", "HearthflameAbilityManager.onDamageApplied", "Per-owner lockout and Brand deadlines are pruned and lifecycle-cleared",
                "The post-damage hook now applies a six-second Brand once every three seconds");
        phase5Fire(evidence, "hearthflame", "transformation_reserve", "Violent chain snap", "Chain snap",
                "HEARTH_SNAP_DAMAGE_MULTIPLIER x1.2; HEARTH_SNAP_RADIUS +0.5", "HearthflameAbilityManager.snapChain", "Execution-scoped",
                "Damage and blast radius are separate live values");
        phase5Fire(evidence, "hearthflame", "transformation_threshold", "Brand conduction", "A Branded target's chain snap",
                "HEARTH_BRAND_SPREAD_COUNT 2; HEARTH_BRAND_SPREAD_RANGE 4; HEARTH_BRAND_SPREAD_DURATION_TICKS 100", "HearthflameAbilityManager.spreadBrand", "Spread Brands expire and clear with world lifecycle",
                "Count, range and five-second duration are all enforced");
        phase5Fire(evidence, "hearthflame", "transformation_convergence", "Final Rebuke", "Final shatter",
                "HEARTH_FINAL_DAMAGE_MULTIPLIER x1.3; HEARTH_FINAL_KNOCKBACK_MULTIPLIER 1.25", "HearthflameAbilityManager.finishAbility", "Execution-scoped",
                "Final damage and knockback scale independently");
        phase5Fire(evidence, "hearthflame", "transformation_focus", "Timed Judgment Pyre snaps", "Three seconds after activation",
                "HEARTH_FORCED_SNAP_TICKS 60; HEARTH_SNAP_DAMAGE_MULTIPLIER 1.6; PULL_STRENGTH 0", "HearthflameAbilityManager.forcedSnapAt, tickAbilities and snapChain", "Execution-scoped",
                "Every chain uses the forced deadline while pull is disabled");
        phase5Fire(evidence, "hearthflame", "transformation_release", "Endless Furnace rebinding", "Chain snap near a Branded enemy",
                "HEARTH_REBIND_COUNT 3; HEARTH_REBIND_RANGE 5; HEARTH_REBIND_DAMAGE_MULTIPLIER 0.75", "HearthflameAbilityManager.createReboundChain and rebindDamageMultiplier", "Rebound generation is execution-scoped and capped",
                "Up to three rebounds use cumulative 25% generation penalties");
        phase5Fire(evidence, "emberblade", "signature_opening", "Minimum-charge shrapnel", "Channel release",
                "EMBERBLADE_MIN_DAMAGE_MULTIPLIER x1.12", "EmberbladeAbilityManager.release", "Execution-scoped",
                "Minimum output is isolated from maximum-charge tuning");
        phase5Fire(evidence, "emberblade", "signature_cadence", "Maximum-charge shrapnel", "Full channel release",
                "EMBERBLADE_MAX_DAMAGE_MULTIPLIER x1.15", "EmberbladeAbilityManager.release", "Execution-scoped",
                "Raises full-charge output without changing channel time");
        phase5Fire(evidence, "emberblade", "signature_pressure", "Shrapnel piercing", "Primary shrapnel hit",
                "EMBERBLADE_PIERCE_COUNT 1; EMBERBLADE_PIERCE_DAMAGE_MULTIPLIER 0.7; EMBERBLADE_PIERCE_RANGE 12", "EmberbladeAbilityManager.release", "Execution-scoped and target-capped",
                "The second target is selected in range and receives the advertised scaled hit");
        phase5Fire(evidence, "emberblade", "signature_reversal", "Precision shrapnel", "Release with target inside two degrees",
                "EMBERBLADE_AIM_DAMAGE_MULTIPLIER 1.2", "EmberbladeAbilityManager.aimAngle and release", "Execution-scoped",
                "Crosshair angle now gates the 20% multiplier");
        phase5Fire(evidence, "emberblade", "signature_reserve", "Interrupted-channel bank", "Interruption after two seconds",
                "EMBERBLADE_BANK_DURATION_TICKS 60; EMBERBLADE_BANK_MULTIPLIER 0.5", "EmberbladeAbilityManager.interruptChannel and bankInterrupted", "Bank expires after three seconds, clears on damage, disconnect and world lifecycle",
                "Repaired as real per-owner channel state instead of inert tuning");
        phase5Fire(evidence, "emberblade", "signature_threshold", "Molten shrapnel splash", "Shrapnel impact",
                "FIRE_TICKS 60; EMBERBLADE_SPLASH_DAMAGE_MULTIPLIER 0.25; EMBERBLADE_SPLASH_RADIUS 2; EMBERBLADE_SPLASH_TARGET_CAP 6", "EmberbladeAbilityManager.splash and ignite", "Execution-scoped and target-capped",
                "Fire, radius, cap and splash damage are all consumed");
        phase5Fire(evidence, "emberblade", "signature_convergence", "White Heat release window", "Release during the final ten ticks",
                "EMBERBLADE_FULL_CHARGE_WINDOW_TICKS 10; EMBERBLADE_MAX_DAMAGE_MULTIPLIER x1.25; EMBERBLADE_FULL_CHARGE_FIRE_TICKS 80", "EmberbladeAbilityManager.inFullChargeWindow and release", "Execution-scoped",
                "The half-second timing window controls both damage and four-second fire");
        phase5Fire(evidence, "emberblade", "signature_focus", "Siege Crucible channel", "Channel tick and full release",
                "EMBERBLADE_CHANNEL_TICKS +20; EMBERBLADE_MAX_DAMAGE_MULTIPLIER x1.6", "EmberbladeAbilityManager.channelTicks, tickHolder and release", "Channel modifier removed on cancel/release/disconnect and lifecycle clear",
                "The extra second, Slowness II and full-charge multiplier share the same execution");
        phase5Fire(evidence, "emberblade", "signature_release", "Hair Trigger channel", "Channel tick and release",
                "EMBERBLADE_CHANNEL_TICKS x0.5; COOLDOWN_TICKS -20; EMBERBLADE_MAX_DAMAGE_MULTIPLIER x0.65", "EmberbladeAbilityManager.channelTicks and releaseDelegated", "Execution-scoped",
                "Half channel time, one-second refund and damage penalty all reach runtime");
        phase5Fire(evidence, "emberblade", "combat_opening", "Quickdraw Speed", "Release after at least one second",
                "EMBERBLADE_QUICKDRAW_DURATION_TICKS 60", "EmberbladeAbilityManager.applyRewards", "Vanilla status duration",
                "The minimum channel threshold is enforced before granting Speed I");
        phase5Fire(evidence, "emberblade", "combat_cadence", "Forged Rhythm Haste", "Shrapnel hit",
                "EMBERBLADE_HASTE_DURATION_TICKS 80", "EmberbladeAbilityManager.applyRewards", "Vanilla status refresh",
                "Each hit refreshes Haste I for four seconds");
        phase5Fire(evidence, "emberblade", "combat_pressure", "Recoil Step", "Channel release",
                "EMBERBLADE_RECOIL_DISTANCE 1.5; EMBERBLADE_FALL_PROTECTION_TICKS 20", "EmberbladeAbilityManager.applyReleaseMovement", "Fall protection expires after one second",
                "Backward displacement and one-second fall protection are both managed");
        phase5Fire(evidence, "emberblade", "combat_reversal", "Scorching Footwork reward", "Move six blocks while Quickdraw is active",
                "EMBERBLADE_MOVE_DISTANCE 6; EMBERBLADE_NEXT_HIT_MULTIPLIER 1.15; EMBERBLADE_NEXT_HIT_DURATION_TICKS 80", "EmberbladeAbilityManager.tickHolder and modifyOutgoingDamage", "Charge expires after four seconds or is consumed by the next attack",
                "Movement is measured from release and awards one bounded attack charge");
        phase5Fire(evidence, "emberblade", "combat_reserve", "Late-channel guard", "Incoming damage during the final eight ticks",
                "EMBERBLADE_LATE_DAMAGE_REDUCTION 0.25; EMBERBLADE_LATE_GUARD_TICKS 8", "EmberbladeAbilityManager.modifyIncomingDamage", "Ends on release or channel cancellation",
                "The live incoming-damage hook now recognizes the exact late-channel window");
        phase5Fire(evidence, "emberblade", "combat_threshold", "Full-charge Ire Rush", "Full-charge shrapnel hit",
                "EMBERBLADE_IRE_RUSH_DURATION_TICKS 60; EMBERBLADE_FULL_BUFF_LOCKOUT_TICKS 100", "EmberbladeAbilityManager.applyRewards", "Per-owner lockout expires and clears with owner/world state",
                "Strength I is bounded by the five-second lockout");
        phase5Fire(evidence, "emberblade", "combat_convergence", "Ember Pursuit reset", "Shrapnel kill",
                "EMBERBLADE_PURSUIT_SPEED_TICKS 40", "EmberbladeAbilityManager.dealAndRecord and applyRewards", "Vanilla Speed duration; fall-protection state reset",
                "A lethal shrapnel event resets Recoil Step and grants Speed II");
        phase5Fire(evidence, "emberblade", "combat_focus", "Duelist teleport", "First shrapnel hit",
                "Mode 65536", "EmberbladeAbilityManager.applyReleaseMovement, fragments and splash", "Execution-scoped",
                "Teleports beside the primary target while piercing and splash paths are suppressed");
        phase5Fire(evidence, "emberblade", "combat_release", "Blastback release", "Channel release",
                "EMBERBLADE_RECOIL_DISTANCE 4; EMBERBLADE_BLASTBACK_RESISTANCE_TICKS 40; EMBERBLADE_MIN_DAMAGE_MULTIPLIER x0.8; EMBERBLADE_MAX_DAMAGE_MULTIPLIER x0.8", "EmberbladeAbilityManager.applyReleaseMovement and release", "Vanilla Resistance duration and bounded fall protection",
                "Launch, Resistance and the across-curve damage penalty all apply");
        phase5Fire(evidence, "emberblade", "transformation_opening", "Ember Ire chance", "Shrapnel hit",
                "CHANCE 10 percentage points", "EmberbladeAbilityManager.ireChance and applyRewards", "Execution-scoped roll",
                "The chance is additive and bounded by the exact roll helper");
        phase5Fire(evidence, "emberblade", "transformation_cadence", "Ember Ire status duration", "Speed, Haste or Strength reward",
                "EMBERBLADE_IRE_DURATION_BONUS_TICKS 40", "EmberbladeAbilityManager.ireDuration and applyRewards", "Vanilla status duration",
                "Adds two seconds to each qualifying Ember Ire buff");
        phase5Fire(evidence, "emberblade", "transformation_pressure", "Shrapnel Bloom", "Full-charge release",
                "EMBERBLADE_FRAGMENT_COUNT 2; EMBERBLADE_FRAGMENT_DAMAGE_MULTIPLIER 0.35; EMBERBLADE_FRAGMENT_RANGE 5", "EmberbladeAbilityManager.fragments", "Execution-scoped and count-capped",
                "Two distinct side targets receive 35% fragment hits");
        phase5Fire(evidence, "emberblade", "transformation_reversal", "Cinder Hunt fragments", "Fragment target selection",
                "EMBERBLADE_FRAGMENT_SEEK_RANGE 10; EMBERBLADE_FRAGMENT_SEEK_MULTIPLIER 1.2", "EmberbladeAbilityManager.fragments", "Execution-scoped",
                "Burning targets are sought over the larger range and receive the bonus");
        phase5Fire(evidence, "emberblade", "transformation_reserve", "Flashover combo", "Third shrapnel hit within five seconds",
                "EMBERBLADE_FLASHOVER_COUNT 3; EMBERBLADE_FLASHOVER_WINDOW_TICKS 100; EMBERBLADE_FLASHOVER_RADIUS 3; EMBERBLADE_FLASHOVER_DAMAGE_MULTIPLIER 0.4; EMBERBLADE_FLASHOVER_TARGET_CAP 8", "EmberbladeAbilityManager.applyRewards", "Combo window resets after detonation or expiry and clears with owner/world state",
                "Count, window, radius, cap and damage are all live");
        phase5Fire(evidence, "emberblade", "transformation_threshold", "Fed by Flame bank", "Melee hit on a burning enemy",
                "EMBERBLADE_BANK_GAIN 0.05; EMBERBLADE_BANK_CAP 0.25; EMBERBLADE_BANK_DURATION_TICKS 100", "EmberbladeAbilityManager.onMeleeHit and validBank", "Bank expires after five seconds, is capped and clears on owner/world lifecycle",
                "Each qualifying hit banks exactly 5% up to 25%");
        phase5Fire(evidence, "emberblade", "transformation_convergence", "Wildfire Volley jumps", "Side-fragment impact",
                "EMBERBLADE_FRAGMENT_JUMP_RANGE 6; EMBERBLADE_FRAGMENT_JUMP_MULTIPLIER 0.5", "EmberbladeAbilityManager.fragments", "One jump per fragment and execution-scoped target exclusion",
                "Each side fragment can select one new target for half fragment damage");
        phase5Fire(evidence, "emberblade", "transformation_focus", "Ember Barrage", "Full-charge release",
                "EMBERBLADE_FRAGMENT_COUNT 5; EMBERBLADE_FRAGMENT_DAMAGE_MULTIPLIER 0.45; EMBERBLADE_MIN_DAMAGE_MULTIPLIER x0.6; EMBERBLADE_MAX_DAMAGE_MULTIPLIER x0.6", "EmberbladeAbilityManager.fragments and release", "Execution-scoped",
                "Five fragments and the 40% primary penalty are independently consumed");
        phase5Fire(evidence, "emberblade", "transformation_release", "Ire Incarnate", "Full-charge shrapnel hit",
                "STATUS_AMPLIFIER 1; EMBERBLADE_INCARNATE_DURATION_TICKS 100; COOLDOWN_TICKS +80", "EmberbladeAbilityManager.applyRewards and releaseDelegated", "Vanilla status duration and execution cooldown",
                "Strength II, Haste II and the four-second cooldown surcharge share the full-charge gate");
        phase5Fire(evidence, "emberlash", "signature_opening", "Smouldering bonus damage", "Melee hit on a marked target",
                "EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER x1.1", "EmberlashAbilityManager.perStackDamage and onHit", "Mark deadline and execution-scoped tuning",
                "Uses an Emberlash-scoped multiplier rather than colliding with Hearthflame or Emberblade");
        phase5Fire(evidence, "emberlash", "signature_cadence", "Smouldering stack cap", "Smouldering application",
                "EMBERLASH_SMOULDER_STACK_CAP 6", "EmberlashAbilityManager.applyStacks", "Stacks expire with their mark and clear on owner/world lifecycle",
                "Exact stack counting now permits six stacks");
        phase5Fire(evidence, "emberlash", "signature_pressure", "Smouldering duration", "Smouldering application or refresh",
                "EMBERLASH_SMOULDER_DURATION_TICKS +60", "EmberlashAbilityManager.applyStacks", "All stacks share the refreshed mark deadline",
                "Adds three seconds and refreshes the complete stack set");
        phase5Fire(evidence, "emberlash", "signature_reversal", "Searing Lash", "Applying the third Smouldering stack",
                "COUNT 3; FIRE_TICKS 60", "EmberlashAbilityManager.onHit", "Vanilla fire duration",
                "The third stack applies exactly three seconds of fire");
        phase5Fire(evidence, "emberlash", "signature_reserve", "Sweep Smouldering", "Qualifying sweep attack",
                "EMBERLASH_SWEEP_RADIUS 3; EMBERLASH_SWEEP_TARGET_CAP 4", "EmberlashAbilityManager.isSweepAttack and onHit", "Target-capped; marks retain their own deadlines",
                "Up to four nearby enemies receive one stack without replacing the primary hit");
        phase5Fire(evidence, "emberlash", "signature_threshold", "Bellows Rhythm combo", "Third consecutive hit inside two seconds",
                "EMBERLASH_COMBO_HITS 3; EMBERLASH_COMBO_WINDOW_TICKS 40", "EmberlashAbilityManager.onHit", "Combo resets after proc, target change or window expiry",
                "The cadence counter awards one additional stack and is bounded");
        phase5Fire(evidence, "emberlash", "signature_convergence", "White Smoulder suppression", "Incoming melee damage from a maximum-marked attacker",
                "EMBERLASH_MAX_DAMAGE_REDUCTION 0.12; EMBERLASH_MAX_REDUCTION_DURATION_TICKS 60", "EmberlashAbilityManager.modifyIncomingDamage", "Requires the damaged wearer to own the mark and its live deadline",
                "Repaired ownership validation prevents another wielder's mark from granting protection");
        phase5Fire(evidence, "emberlash", "signature_focus", "Endless Smoulder echo", "Hit within the three-second cadence",
                "EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER x0.75; LOCKOUT_TICKS 60", "EmberlashAbilityManager.withinCadence and onHit", "Cadence state expires and clears with owner/world state",
                "Redesigned as a 50%-strength second Smouldering echo while retaining the advertised 25% per-stack penalty");
        phase5Fire(evidence, "emberlash", "signature_release", "Ashen Brand", "Smouldering application and hit",
                "EMBERLASH_SMOULDER_STACK_CAP 3; EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER x1.45", "EmberlashAbilityManager.applyStacks, onHit and sweep suppression", "One owned target at a time; prior mark is cleared",
                "The three-stack cap, 45% scaling and single-target restriction are all enforced");
        phase5Fire(evidence, "emberlash", "combat_opening", "Cauterizing evade distance", "Cauterizing activation",
                "EMBERLASH_EVADE_DISTANCE_MULTIPLIER 1.2", "EmberlashAbilityManager.activate", "One-shot movement",
                "The live evade vector is multiplied by 1.2");
        phase5Fire(evidence, "emberlash", "combat_cadence", "Cauterizing cooldown", "Successful activation",
                "COOLDOWN_TICKS -12", "Phase5MasterySkillEffect cooldown export and EmberlashSwordItem", "Execution cooldown",
                "Reduces the cooldown by exactly 0.6 seconds");
        phase5Fire(evidence, "emberlash", "combat_pressure", "Sealed Wounds", "Cauterizing activation",
                "EMBERLASH_CAUTERY_ABSORPTION 4; EMBERLASH_CAUTERY_ABSORPTION_TICKS 60", "EmberlashAbilityManager.activate through Phase4AbsorptionTracker", "Absorption expires after three seconds and clears on lifecycle teardown",
                "Targetless Cauterizing now grants bounded Absorption instead of requiring a victim");
        phase5Fire(evidence, "emberlash", "combat_reversal", "Smoke Screen", "Cauterizing activation",
                "EMBERLASH_BLIND_RADIUS 3; EMBERLASH_BLIND_TARGET_CAP 6; EMBERLASH_BLIND_DURATION_TICKS 30", "EmberlashAbilityManager.activate", "Vanilla status duration and target cap",
                "Radius, cap and 1.5-second Blindness are all consumed");
        phase5Fire(evidence, "emberlash", "combat_reserve", "Backlash stacks", "Next attack after evading",
                "EMBERLASH_BACKLASH_STACKS 2; EMBERLASH_BACKLASH_DURATION_TICKS 80", "EmberlashAbilityManager.activate and onHit", "Charge expires after four seconds or is consumed by the next attack",
                "The evasion charge applies exactly two additional stacks once");
        phase5Fire(evidence, "emberlash", "combat_threshold", "Burning Pace", "Cauterizing activation",
                "EMBERLASH_BURNING_PACE_DURATION_TICKS 40; EMBERLASH_BURNING_PACE_AMPLIFIER 1", "EmberlashAbilityManager.activate", "Vanilla status duration",
                "Grants Speed II for exactly two seconds");
        phase5Fire(evidence, "emberlash", "combat_convergence", "Emergency Brand", "Cauterizing below 35% health",
                "EMBERLASH_EMERGENCY_LOCKOUT_TICKS 160; EMBERLASH_EMERGENCY_RESISTANCE_TICKS 60", "EmberlashAbilityManager.activate", "Per-owner lockout expires and clears with world state",
                "Resistance I is health-gated and bounded by the eight-second lockout");
        phase5Fire(evidence, "emberlash", "combat_focus", "Phoenix Step", "Cauterizing movement segment",
                "EMBERLASH_PHOENIX_DAMAGE_MULTIPLIER 0.35; EMBERLASH_PHOENIX_TARGET_CAP 6; EMBERLASH_PHOENIX_FIRE_TICKS 60", "EmberlashAbilityManager.phoenixStep", "One-shot segment query and target cap",
                "Damages and ignites only enemies crossed by the evade while healing is suppressed");
        phase5Fire(evidence, "emberlash", "combat_release", "Surgeon's Flame", "Cauterizing activation",
                "HEAL_MULTIPLIER x1.5", "EmberlashAbilityManager.activate and removeOneHarmfulEffect", "One harmful effect removed; no persistent custom state",
                "Healing increases by 50%, one debuff is removed, and evade/Speed benefits are disabled");
        phase5Fire(evidence, "emberlash", "transformation_opening", "Hot Blood retaliation", "Melee damage taken",
                "EMBERLASH_HOT_BLOOD_LOCKOUT_TICKS 30", "EmberlashAbilityManager.onDamageApplied", "Per-owner lockout expires and clears with world state",
                "The post-damage hook applies one stack to the attacker once every 1.5 seconds");
        phase5Fire(evidence, "emberlash", "transformation_cadence", "Reprisal charges", "Damage taken",
                "EMBERLASH_REPRISAL_DURATION_TICKS 100; EMBERLASH_REPRISAL_STACK_CAP 3", "EmberlashAbilityManager.onDamageApplied and liveReprisalCharges", "Charges expire after five seconds and clear on owner/world lifecycle",
                "Exact charges replace the prior status-amplifier approximation");
        phase5Fire(evidence, "emberlash", "transformation_pressure", "Crackling Retort", "Attack with three reprisal charges",
                "EMBERLASH_REPRISAL_TRIGGER_COUNT 3; EMBERLASH_REPRISAL_DAMAGE_MULTIPLIER 0.25", "EmberlashAbilityManager.onHit and consumeReprisal", "Charges are consumed atomically",
                "Three live charges produce one 25% bonus hit and then clear");
        phase5Fire(evidence, "emberlash", "transformation_reversal", "Ash Burst", "Reprisal consumption",
                "EMBERLASH_ASH_RADIUS 2.5; EMBERLASH_ASH_DAMAGE_PER_STACK 0.08; EMBERLASH_ASH_TARGET_CAP 8", "EmberlashAbilityManager.consumeReprisal", "One-shot target-capped burst",
                "Only Smouldering enemies in range are detonated for their exact stack count");
        phase5Fire(evidence, "emberlash", "transformation_reserve", "Shared Embers", "Ash Burst hit",
                "EMBERLASH_ASH_APPLIED_STACKS 1", "EmberlashAbilityManager.consumeReprisal", "Applied marks retain their normal deadlines",
                "Every enemy hit by Ash Burst receives one Smouldering stack");
        phase5Fire(evidence, "emberlash", "transformation_threshold", "Lashback", "Maximum-Smouldering enemy hits its owner",
                "EMBERLASH_LASHBACK_DURATION_TICKS 40; EMBERLASH_LASHBACK_AMPLIFIER 1", "EmberlashAbilityManager.onDamageApplied", "Vanilla status duration",
                "Ownership and maximum-stack checks gate Slowness II");
        phase5Fire(evidence, "emberlash", "transformation_convergence", "Final Coal cooldown refund", "Kill an enemy at maximum Smouldering",
                "EMBERLASH_KILL_REFUND_TICKS 20", "EmberlashAbilityManager.onKill", "One refund per lethal callback",
                "The death hook verifies the victim's pre-clear mark and reduces Cauterizing by one second");
        phase5Fire(evidence, "emberlash", "transformation_focus", "Detonation Lash", "Attack a Smouldering target",
                "EMBERLASH_DETONATION_DAMAGE_PER_STACK 0.3", "EmberlashAbilityManager.onHit", "All owned stacks are consumed by the attack",
                "Sustained bonus damage is suppressed and replaced with a 30%-per-stack detonation");
        phase5Fire(evidence, "emberlash", "transformation_release", "Spitefire", "Damage taken and reprisal lifetime",
                "EMBERLASH_REPRISAL_DURATION_TICKS 200; EMBERLASH_REPRISAL_STACK_CAP 5; EMBERLASH_INCOMING_PER_CHARGE_MULTIPLIER 1.04", "EmberlashAbilityManager.modifyIncomingDamage and onDamageApplied", "Charges expire after ten seconds or are spent; state clears on owner/world lifecycle",
                "Up to five exact charges increase incoming damage multiplicatively until spent");
        hearthflame(evidence, "signature_opening", "Chain echo damage", "Melee hit on a bound enemy",
                "HEARTH_ECHO_DAMAGE_MULTIPLIER x1.1", "HearthflameAbilityManager.echoHit", "Execution-scoped",
                "Composes with Iron Chorus and Crucible Network");
        hearthflame(evidence, "signature_cadence", "Furnace chain binding range", "Ability preparation",
                "HEARTH_BIND_RANGE +2", "HearthflameAbilityManager.findTargets", "Execution-scoped",
                "The target cap is deliberately untouched, as the description states");
        hearthflame(evidence, "signature_pressure", "Furnace chain contraction", "Chain tick",
                "PULL_STRENGTH x1.2; HEARTH_MIN_LENGTH -0.5", "HearthflameAbilityManager.pullStrength and minimumLength", "Execution-scoped",
                "Repaired: the pull now reads the tuning already in scope instead of re-resolving the ability from the static map twice per target per tick");
        hearthflame(evidence, "signature_reversal", "Chain echo burning", "Echo hit",
                "HEARTH_ECHO_FIRE_TICKS 120", "HearthflameAbilityManager.igniteSeconds through applyAbilityDamage", "Vanilla fire duration",
                "Repaired: the node was inert. It set a two-second burn while the same damage call already ignited for four, and setOnFireForTicks only ever raises the timer. Fire duration now takes the longer of the weapon ignite and the tuned value, and the node grants six seconds");
        hearthflame(evidence, "signature_reserve", "Chain echo damage to other bound enemies", "Echo hit",
                "HEARTH_ECHO_DAMAGE_MULTIPLIER x1.15", "HearthflameAbilityManager.echoHit", "Execution-scoped",
                "Composes with Tempered Links and Crucible Network");
        hearthflame(evidence, "signature_threshold", "Chain snap pressure and radius", "Chain snap",
                "HEARTH_SNAP_PRESSURE_MULTIPLIER x1.2; HEARTH_SNAP_RADIUS +0.5", "HearthflameAbilityManager.pressureAfterSnap and snapChain", "Execution-scoped",
                "Repaired: the finale now resolves its radius through the same scoped-then-shared helper as the snap");
        hearthflame(evidence, "signature_convergence", "Final chain shatter damage", "Finale with every chain bound",
                "HEARTH_FINAL_CHAIN_REQUIREMENT 6; HEARTH_FINAL_CHAIN_MULTIPLIER 1.25", "HearthflameAbilityManager.gatedFinalDamage", "Execution-scoped",
                "Repaired: it was a multiply-then-divide with 1.25 duplicated in both repositories, so retuning the node in Simply Mastery would silently desynchronise the gate. The manager now applies the multiplier only when the requirement is met");
        hearthflame(evidence, "signature_focus", "Furnace chain capstone", "Ability preparation and echo hit",
                "TARGET_CAP 8; HEARTH_ECHO_DAMAGE_MULTIPLIER x1.2; PULL_STRENGTH x0.7", "HearthflameAbilityManager.findTargets, echoHit and pullStrength", "Execution-scoped",
                "All three values compose with their prerequisites");
        hearthflame(evidence, "signature_release", "Furnace chain single-target capstone", "Ability preparation and finale",
                "TARGET_CAP 1; HEARTH_CHAIN_DURATION_TICKS x0.6; HEARTH_FINAL_DAMAGE_MULTIPLIER x2", "HearthflameAbilityManager.findTargets and finishAbility", "Execution-scoped",
                "Note that it makes Sixfold Sentence unreachable by design, since one chain can never meet a six-chain requirement");
        hearthflame(evidence, "combat_opening", "Cast absorption", "Ability activation",
                "HEARTH_CAST_ABSORPTION 4; HEARTH_CAST_ABSORPTION_DURATION_TICKS 80", "Phase4AbsorptionTracker grant through grantTimedAbsorption", "Expires on the tracker sweep; cleared on unload and server stop",
                "Four absorption is Absorption I, matching the description");
        hearthflame(evidence, "combat_cadence", "Incoming fire damage while chains persist", "Fire damage taken",
                "HEARTH_FIRE_DAMAGE_REDUCTION .3", "HearthflameAbilityManager.modifyIncomingDamage", "Execution-scoped",
                "Gated on the key being present, so it is a no-op when unowned");
        hearthflame(evidence, "combat_pressure", "Chain snap defence", "Chain snap",
                "HEARTH_SNAP_RESISTANCE_DURATION_TICKS 30; HEARTH_SNAP_RESISTANCE_MAX_TICKS 90", "HearthflameAbilityManager.extendedResistanceTicks", "Status-effect duration",
                "Refreshes toward the cap rather than stacking; a higher-amplifier Resistance from Bastion Hearth correctly takes precedence");
        hearthflame(evidence, "combat_reversal", "Incoming damage from bound enemies", "Melee damage from a bound attacker",
                "HEARTH_BOUND_DAMAGE_REDUCTION .15; HEARTH_BOUND_DAMAGE_REDUCTION_RANGE 6", "HearthflameAbilityManager.modifyIncomingDamage bound branch", "Execution-scoped",
                "Checks that the attacker is bound and within range; defaults to a no-op");
        hearthflame(evidence, "combat_reserve", "Furnace Brand absorption", "Melee hit on a branded enemy",
                "HEARTH_BRAND_ABSORPTION 2; HEARTH_BRAND_ABSORPTION_DURATION_TICKS 200; HEARTH_BRAND_ABSORPTION_CAP 8; HEARTH_BRAND_ABSORPTION_LOCKOUT_TICKS 40", "grantTimedAbsorption through Phase4AbsorptionTracker", "Expires on the tracker sweep; cleared on unload and server stop",
                "Repaired: this was the one absorption grant in the profile that bypassed the tracker, adding two absorption every two seconds per branded enemy with no cap and no expiry. Hearthflame's three absorption sources now top up one bounded, expiring pool");
        hearthflame(evidence, "combat_threshold", "Chain preservation", "Hit that would snap a chain",
                "HEARTH_CHAIN_PRESERVE_TICKS 20", "HearthflameAbilityManager.onMeleeHit preserve branch", "Once per chain; the tick path respects the deadline",
                "Preserves the chain for one second on the first snapping hit only");
        hearthflame(evidence, "combat_convergence", "Completion absorption", "Finale after enough chains completed",
                "HEARTH_COMPLETION_ABSORPTION 8; HEARTH_COMPLETION_ABSORPTION_DURATION_TICKS 60; HEARTH_COMPLETION_MIN_CHAINS 3", "grantCompletionAbsorption through Phase4AbsorptionTracker", "Expires on the tracker sweep; cleared on unload and server stop",
                "Repaired: it granted four, which is Absorption I, while its description promised Absorption II. Eight matches the text and its position behind a three-chain condition");
        hearthflame(evidence, "combat_focus", "Anchored chain capstone", "Ability activation",
                "HEARTH_ANCHOR_SPEED_MULTIPLIER .75; ownership bit 16", "HearthflameAbilityManager.anchor, a managed movement modifier and Resistance II", "Modifier and effect restored in cleanupActor on every exit path",
                "restorePreviousEffect correctly returns a pre-existing Resistance rather than clobbering it");
        hearthflame(evidence, "combat_release", "Roaming chain capstone", "Ability activation and chain tick",
                "HEARTH_BREAK_RANGE +6; HEARTH_BIND_RANGE x0.8; HEARTH_FINAL_DAMAGE_MULTIPLIER x0.8; ownership bit 17", "HearthflameAbilityManager.tickAbilities break check, findTargets and finishAbility", "Speed restored in cleanupActor",
                "All four effects consumed, and the final damage penalty composes");
        hearthflame(evidence, "transformation_opening", "Furnace Brand chance", "Melee hit",
                "CHANCE +10", "HearthflameAbilityManager.rollBrand", "Execution-scoped",
                "Additive over the configured chance");
        hearthflame(evidence, "transformation_cadence", "Furnace Brand duration", "Brand application",
                "HEARTH_BRAND_DURATION_TICKS +80", "HearthflameAbilityManager.applyBrand", "Brand expiry; visual discarded with it",
                "Four seconds over the configured duration");
        hearthflame(evidence, "transformation_pressure", "Attacks against branded enemies", "Melee hit on a branded enemy",
                "HEARTH_BRAND_HIT_DAMAGE_MULTIPLIER 1.12; HEARTH_BRAND_HIT_FIRE_TICKS 120", "HearthflameAbilityManager.onMeleeHit brand branch and igniteSeconds", "Vanilla fire duration",
                "Repaired: the bonus damage worked but the one-second burn could not, being shorter than the weapon's own four-second ignite on the same call. It now extends the burn to six seconds");
        hearthflame(evidence, "transformation_reversal", "Reactive branding", "Melee damage taken",
                "HEARTH_REACTIVE_BRAND_DURATION_TICKS 120; HEARTH_REACTIVE_BRAND_LOCKOUT_TICKS 60", "HearthflameAbilityManager.onDamageApplied with a per-victim lockout", "Lockouts pruned on the timed sweep",
                "Six-second brand on the attacker, once every three seconds, matching the description");
        hearthflame(evidence, "transformation_reserve", "Chain snap damage and radius", "Chain snap",
                "HEARTH_SNAP_DAMAGE_MULTIPLIER x1.2; HEARTH_SNAP_RADIUS +0.5", "HearthflameAbilityManager.snapChain", "Execution-scoped",
                "Repaired: Judgment Pyre now multiplies rather than overwriting, so this bonus survives its capstone");
        hearthflame(evidence, "transformation_threshold", "Furnace Brand spread", "Snap of a branded chain",
                "HEARTH_BRAND_SPREAD_COUNT 2; HEARTH_BRAND_SPREAD_RANGE 4; HEARTH_BRAND_SPREAD_DURATION_TICKS 100", "HearthflameAbilityManager.spreadBrand", "Brand expiry",
                "Requires the snapped chain to have been branded, and skips already-branded candidates");
        hearthflame(evidence, "transformation_convergence", "Final chain shatter", "Finale",
                "HEARTH_FINAL_DAMAGE_MULTIPLIER x1.3; HEARTH_FINAL_KNOCKBACK_MULTIPLIER 1.25", "HearthflameAbilityManager.finishAbility and damageArea knockback", "Execution-scoped",
                "The description no longer says the knockback is increased: the base finale passes a zero knockback multiplier, so this node grants the knockback outright");
        hearthflame(evidence, "transformation_focus", "Forced chain snaps capstone", "Chain tick",
                "HEARTH_FORCED_SNAP_TICKS 60; HEARTH_SNAP_DAMAGE_MULTIPLIER x1.6; PULL_STRENGTH 0", "HearthflameAbilityManager.forcedSnapAt, snapChain and pullStrength", "Execution-scoped",
                "Repaired: an absolute snap multiplier discarded Violent Snap. The zeroed pull and the forced-snap path correctly bypass tension accumulation");
        hearthflame(evidence, "transformation_release", "Chain rebinding capstone", "Chain snap",
                "HEARTH_REBIND_COUNT 3; HEARTH_REBIND_RANGE 5; HEARTH_REBIND_DAMAGE_MULTIPLIER .75", "HearthflameAbilityManager.createReboundChain and rebindDamageMultiplier", "Rebound chains inherit the ability expiry",
                "Rebinds only branded enemies, consumes the brand, and decays compounding per generation");
        emberblade(evidence, "signature_opening", "Minimum-charge shrapnel damage", "Release",
                "EMBERBLADE_MIN_DAMAGE_MULTIPLIER x1.12", "EmberbladeAbilityManager.release minimum damage", "Execution-scoped",
                "Composes with both damage capstones");
        emberblade(evidence, "signature_cadence", "Maximum-charge shrapnel damage", "Release at charge",
                "EMBERBLADE_MAX_DAMAGE_MULTIPLIER x1.15", "EmberbladeAbilityManager.release maximum damage", "Execution-scoped",
                "Full charge time is deliberately untouched, as the description states");
        emberblade(evidence, "signature_pressure", "Shrapnel pierce", "Release",
                "EMBERBLADE_PIERCE_COUNT 1; EMBERBLADE_PIERCE_DAMAGE_MULTIPLIER .7; EMBERBLADE_PIERCE_RANGE 12", "EmberbladeAbilityManager.release pierce loop", "Execution-scoped",
                "Suppressed by Duelist's Release, as its description states");
        emberblade(evidence, "signature_reversal", "Aimed shrapnel damage", "Release within two degrees of the crosshair",
                "EMBERBLADE_AIM_DAMAGE_MULTIPLIER 1.2", "EmberbladeAbilityManager.aimAngle and the release multiplier", "Execution-scoped",
                "The two-degree cone matches the description and no other node writes it");
        emberblade(evidence, "signature_reserve", "Interrupted channel charge", "Channel interrupted after two seconds",
                "EMBERBLADE_BANK_DURATION_TICKS 60; EMBERBLADE_BANK_MULTIPLIER .5", "EmberbladeAbilityManager.bankInterrupted and interruptedBank", "Cleared on damage taken and on release",
                "Repaired: Fed by Flame shared this duration key and stretched the three-second window to five. The two banks were already separate stores; now their windows are too");
        emberblade(evidence, "signature_threshold", "Shrapnel impact", "Release",
                "FIRE_TICKS 60; EMBERBLADE_SPLASH_DAMAGE_MULTIPLIER .25; EMBERBLADE_SPLASH_RADIUS 2; EMBERBLADE_SPLASH_TARGET_CAP 6", "EmberbladeAbilityManager.ignite and splash", "Execution-scoped",
                "All four values default to zero, so the node is naturally gated when unowned");
        emberblade(evidence, "signature_convergence", "Late-release shrapnel damage", "Release inside the final window",
                "EMBERBLADE_FULL_CHARGE_WINDOW_TICKS 10; EMBERBLADE_FULL_CHARGE_MULTIPLIER 1.25; EMBERBLADE_FULL_CHARGE_FIRE_TICKS 80", "EmberbladeAbilityManager.fullChargeBonus and inFullChargeWindow", "Execution-scoped",
                "Repaired: it multiplied the maximum-charge damage unconditionally and the manager divided by a hardcoded 1.25 outside the window, duplicating the constant across both repositories. The gate and the bonus are now sent as data");
        emberblade(evidence, "signature_focus", "Channel length capstone", "Channel start",
                "EMBERBLADE_CHANNEL_TICKS +20; EMBERBLADE_MAX_DAMAGE_MULTIPLIER x1.6", "EmberbladeAbilityManager.channelTicks and a real Slowness II during the channel", "Slowness cleared on release, cancel and interrupt, restoring any prior instance",
                "Repaired: the advertised Slowness II was a movement attribute modifier. Numerically identical, but invisible to the player and to anything reading the status effect");
        emberblade(evidence, "signature_release", "Fast channel capstone", "Channel start and release",
                "EMBERBLADE_CHANNEL_TICKS x0.5; COOLDOWN_TICKS -20; EMBERBLADE_MAX_DAMAGE_MULTIPLIER x0.65", "EmberbladeAbilityManager.channelTicks and the execution cooldown key", "Execution-scoped",
                "The cooldown reduction composes with Ire Incarnate's increase");
        emberblade(evidence, "combat_opening", "Release movement reward", "Release after one second of channel",
                "EMBERBLADE_QUICKDRAW_DURATION_TICKS 60", "EmberbladeAbilityManager.applyRewards quickdraw branch", "Status-effect duration",
                "The one-second requirement matches the description");
        emberblade(evidence, "combat_cadence", "Shrapnel Haste", "Release that hits",
                "EMBERBLADE_HASTE_DURATION_TICKS 80", "EmberbladeAbilityManager.applyRewards haste branch", "Status-effect duration",
                "Refreshes rather than stacking");
        emberblade(evidence, "combat_pressure", "Release recoil", "Release",
                "EMBERBLADE_RECOIL_DISTANCE 1.5; EMBERBLADE_FALL_PROTECTION_TICKS 20", "EmberbladeAbilityManager.applyReleaseMovement and tickHolder fall protection", "Fall protection expires on its deadline",
                "Blastback deliberately supersedes the recoil distance as a capstone");
        emberblade(evidence, "combat_reversal", "Movement after a buff", "Moving while buffed",
                "EMBERBLADE_MOVE_DISTANCE 6; EMBERBLADE_NEXT_HIT_MULTIPLIER 1.15; EMBERBLADE_NEXT_HIT_DURATION_TICKS 80", "EmberbladeAbilityManager.tickHolder distance accumulator and modifyOutgoingDamage", "Consumed on the next hit or on expiry",
                "Distance is accumulated per tick with a per-tick clamp against teleports");
        emberblade(evidence, "combat_reserve", "Late-channel defence", "Damage taken in the final window",
                "EMBERBLADE_LATE_DAMAGE_REDUCTION .25; EMBERBLADE_LATE_GUARD_TICKS 8", "EmberbladeAbilityManager.modifyIncomingDamage", "Execution-scoped",
                "Uses its own guard window, separate from White Heat's");
        emberblade(evidence, "combat_threshold", "Full-charge Strength", "Full-charge release",
                "EMBERBLADE_IRE_RUSH_DURATION_TICKS 60; EMBERBLADE_FULL_BUFF_LOCKOUT_TICKS 100", "EmberbladeAbilityManager.applyRewards ire rush branch", "Per-actor lockout",
                "The five-second lockout is a real consumer");
        emberblade(evidence, "combat_convergence", "Shrapnel kill reward", "Killing an enemy with shrapnel",
                "EMBERBLADE_PURSUIT_SPEED_TICKS 40", "EmberbladeAbilityManager.dealAndRecord kill branch", "Status-effect duration; fall protection expires on its deadline",
                "The description no longer claims to reset Recoil Step, which has no cooldown to reset. It refreshes fall protection, which is what the code does");
        emberblade(evidence, "combat_focus", "Shrapnel teleport capstone", "Release that hits",
                "Ownership bit 16 only", "EmberbladeAbilityManager.applyReleaseMovement teleport and the suppressed pierce and splash", "Execution-scoped",
                "A pure mode node, which Phase 5's automatic per-node ownership bit makes legitimate without any tuned value");
        emberblade(evidence, "combat_release", "Blastback capstone", "Release",
                "EMBERBLADE_RECOIL_DISTANCE 4; EMBERBLADE_BLASTBACK_RESISTANCE_TICKS 40; min and max damage x0.8", "EmberbladeAbilityManager.applyReleaseMovement", "Status-effect duration",
                "Both damage penalties compose with their prerequisites");
        emberblade(evidence, "transformation_opening", "Ember Ire buff chance", "Release that hits",
                "CHANCE 10 as bonus points", "EmberbladeAbilityManager.ireChance", "Execution-scoped",
                "Added as percentage points without flattening the charge scaling");
        emberblade(evidence, "transformation_cadence", "Ember Ire buff duration", "Buff application",
                "EMBERBLADE_IRE_DURATION_BONUS_TICKS 40", "EmberbladeAbilityManager.ireDuration", "Status-effect duration",
                "Applies to the base buffs and to Ire Incarnate alike");
        emberblade(evidence, "transformation_pressure", "Full-charge fragments", "Full-charge release",
                "EMBERBLADE_FRAGMENT_COUNT 2; EMBERBLADE_FRAGMENT_DAMAGE_MULTIPLIER .35; EMBERBLADE_FRAGMENT_RANGE 5", "EmberbladeAbilityManager.fragments", "Execution-scoped",
                "Superseded by Ember Barrage, as that capstone's description states");
        emberblade(evidence, "transformation_reversal", "Fragment seeking", "Full-charge release near burning enemies",
                "EMBERBLADE_FRAGMENT_SEEK_RANGE 10; EMBERBLADE_FRAGMENT_SEEK_MULTIPLIER 1.2", "EmberbladeAbilityManager.fragments seek sort and bonus", "Execution-scoped",
                "Burning candidates sort first and take the bonus only inside the seek range");
        emberblade(evidence, "transformation_reserve", "Repeated shrapnel hits", "Enemies struck inside the window",
                "EMBERBLADE_FLASHOVER_COUNT 3; EMBERBLADE_FLASHOVER_WINDOW_TICKS 100; EMBERBLADE_FLASHOVER_RADIUS 3; EMBERBLADE_FLASHOVER_DAMAGE_MULTIPLIER .4; EMBERBLADE_FLASHOVER_TARGET_CAP 8", "EmberbladeAbilityManager.applyRewards flashover counter and splash", "Counter resets on the window lapsing and on the blast",
                "Repaired: it counted releases rather than shrapnel hits, and three releases cannot fit inside five seconds at the sixty-tick cooldown, so the node was unreachable. Its blast also bypassed kill accounting and so could not trigger Ember Pursuit");
        emberblade(evidence, "transformation_threshold", "Burning-enemy channel bank", "Melee hit on a burning enemy",
                "EMBERBLADE_BANK_GAIN .05; EMBERBLADE_BANK_CAP .25; EMBERBLADE_FLAME_BANK_DURATION_TICKS 100", "EmberbladeAbilityManager.onMeleeHit and chargeRatio", "Expires on its own deadline; consumed on release",
                "Repaired: it shared its window key with Banked Ire. Both banks now carry their own duration and are still summed into the charge");
        emberblade(evidence, "transformation_convergence", "Fragment chaining", "Fragment hit",
                "EMBERBLADE_FRAGMENT_JUMP_RANGE 6; EMBERBLADE_FRAGMENT_JUMP_MULTIPLIER .5", "EmberbladeAbilityManager.fragments jump branch", "Execution-scoped",
                "One jump per fragment, at half of that fragment's dealt damage");
        emberblade(evidence, "transformation_focus", "Fragment barrage capstone", "Full-charge release",
                "EMBERBLADE_FRAGMENT_COUNT 5; EMBERBLADE_FRAGMENT_DAMAGE_MULTIPLIER .45; min and max damage x0.6", "EmberbladeAbilityManager.fragments", "Execution-scoped",
                "Deliberately replaces Shrapnel Bloom's count and damage while both penalties compose");
        emberblade(evidence, "transformation_release", "Full-charge buff capstone", "Full-charge release",
                "STATUS_AMPLIFIER 1; EMBERBLADE_INCARNATE_DURATION_TICKS 100; COOLDOWN_TICKS +80", "EmberbladeAbilityManager.applyRewards incarnate branch and the execution cooldown key", "Status-effect duration",
                "Strength II and Haste II through a tuned amplifier, with the cooldown composing against Hair Trigger");
        emberlash(evidence, "signature_opening", "Smouldering bonus damage per stack", "Melee hit on a smouldering enemy",
                "EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER x1.1", "EmberlashAbilityManager.onHit sustained damage", "Smouldering effect duration",
                "Composes with Endless Smoulder and Ashen Brand");
        emberlash(evidence, "signature_cadence", "Smouldering stack cap", "Stack application",
                "EMBERLASH_SMOULDER_STACK_CAP 6", "EmberlashAbilityManager.nextStackCount", "Smouldering effect duration",
                "A real increase over the configured cap of five");
        emberlash(evidence, "signature_pressure", "Smouldering duration", "Stack application",
                "EMBERLASH_SMOULDER_DURATION_TICKS +60", "EmberlashAbilityManager.applyStacks effect duration", "Smouldering effect duration",
                "The description no longer claims to add a refresh: every application already re-applies the effect at full duration for every wielder");
        emberlash(evidence, "signature_reversal", "Third Smouldering stack", "Crossing the stack threshold",
                "COUNT 3; FIRE_TICKS 60", "EmberlashAbilityManager.onHit searing branch", "Vanilla fire duration",
                "Fires exactly on crossing the threshold, including when Backlash jumps from one stack to three");
        emberlash(evidence, "signature_reserve", "Sweep attacks", "Sweeping melee attack",
                "EMBERLASH_SWEEP_RADIUS 3; EMBERLASH_SWEEP_TARGET_CAP 4", "EmberlashAbilityManager.isSweepAttack and the sweep application loop", "Smouldering effect duration",
                "Repaired: sweep detection omitted vanilla's horizontal-movement condition, so it also fired on charged grounded attacks that do not sweep");
        emberlash(evidence, "signature_threshold", "Consecutive hits on one target", "Third hit inside the window",
                "EMBERLASH_COMBO_HITS 3; EMBERLASH_COMBO_WINDOW_TICKS 40", "EmberlashAbilityManager.onHit combo counter", "Counter resets on a new target or a lapsed window",
                "Both the count and the window are real consumers");
        emberlash(evidence, "signature_convergence", "Maximum Smouldering", "Reaching the stack cap",
                "EMBERLASH_MAX_DAMAGE_REDUCTION .12; EMBERLASH_MAX_REDUCTION_DURATION_TICKS 60", "EmberlashAbilityManager.applyStacks mark and modifyIncomingDamage", "Reduction deadline on the target mark",
                "The reduction is keyed to the smouldering attacker and checked against the mark owner, so it only protects the wielder who applied it");
        emberlash(evidence, "signature_focus", "Smouldering echo capstone", "Hit inside the cadence",
                "EMBERLASH_ECHO_DAMAGE_MULTIPLIER .5; LOCKOUT_TICKS 60; EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER x0.75", "EmberlashAbilityManager.onHit echo branch and withinCadence", "Cadence tracked on the target mark",
                "Repaired: the echo strength was the one hardcoded number in an otherwise fully keyed profile");
        emberlash(evidence, "signature_release", "Single-target Smouldering capstone", "Stack application",
                "EMBERLASH_SMOULDER_STACK_CAP 3; EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER x1.45", "EmberlashAbilityManager.prepareApplication ashen target and the suppressed sweep", "Previous target's stacks removed on retarget",
                "Single-target enforcement also suppresses Coal Rake and Shared Embers, matching its description");
        emberlash(evidence, "combat_opening", "Cauterizing evade distance", "Ability activation",
                "EMBERLASH_EVADE_DISTANCE_MULTIPLIER 1.2", "EmberlashAbilityManager.activate evade distance", "Execution-scoped",
                "Deliberately nullified by Surgeon's Flame, which says so");
        emberlash(evidence, "combat_cadence", "Cauterizing cooldown", "Ability activation",
                "COOLDOWN_TICKS -12", "The execution cooldown key on the active definition", "Execution-scoped",
                "Reaches only the active definition, since the smoulder passive has no cooldown key");
        emberlash(evidence, "combat_pressure", "Cauterizing absorption", "Ability activation",
                "EMBERLASH_CAUTERY_ABSORPTION 4; EMBERLASH_CAUTERY_ABSORPTION_TICKS 60", "Phase4AbsorptionTracker grant from activate", "Expires on the tracker sweep; cleared on unload, server stop and disconnect",
                "Replaces the heal outright, as the description states");
        emberlash(evidence, "combat_reversal", "Evade blindness", "Ability activation",
                "EMBERLASH_BLIND_RADIUS 3; EMBERLASH_BLIND_TARGET_CAP 6; EMBERLASH_BLIND_DURATION_TICKS 30", "EmberlashAbilityManager.activate smoke screen branch", "Status-effect duration",
                "Radius, cap and duration all consumed");
        emberlash(evidence, "combat_reserve", "Empowered next attack", "Ability activation then the next hit",
                "EMBERLASH_BACKLASH_STACKS 2; EMBERLASH_BACKLASH_DURATION_TICKS 80", "EmberlashAbilityManager.empoweredStacks", "Consumed on the next hit or on expiry",
                "Consumed exactly once, and only by the primary target of that hit");
        emberlash(evidence, "combat_threshold", "Evade Speed", "Ability activation",
                "EMBERLASH_BURNING_PACE_DURATION_TICKS 40; EMBERLASH_BURNING_PACE_AMPLIFIER 1", "EmberlashAbilityManager.activate burning pace branch", "Status-effect duration",
                "Suppressed under Surgeon's Flame, as that capstone's description states");
        emberlash(evidence, "combat_convergence", "Low-health evade", "Ability activation below the health threshold",
                "EMBERLASH_EMERGENCY_RESISTANCE_TICKS 60; EMBERLASH_EMERGENCY_LOCKOUT_TICKS 160", "EmberlashAbilityManager.activate emergency branch", "Per-owner lockout",
                "The eight-second lockout is a real consumer");
        emberlash(evidence, "combat_focus", "Evade path capstone", "Ability activation",
                "EMBERLASH_PHOENIX_DAMAGE_MULTIPLIER .35; EMBERLASH_PHOENIX_TARGET_CAP 6; EMBERLASH_PHOENIX_FIRE_TICKS 60", "EmberlashAbilityManager.phoenixStep with segment-distance filtering", "Execution-scoped",
                "Suppresses the heal, as its description states");
        emberlash(evidence, "combat_release", "Cauterizing healing capstone", "Ability activation",
                "HEAL_MULTIPLIER x1.5", "EmberlashAbilityManager.activate heal branch and removeOneHarmfulEffect", "Execution-scoped",
                "Removes both the evade distance and the Speed bonus, as its description states");
        emberlash(evidence, "transformation_opening", "Retaliatory Smouldering", "Melee damage taken",
                "EMBERLASH_HOT_BLOOD_LOCKOUT_TICKS 30", "EmberlashAbilityManager.onDamageApplied hot blood branch", "Per-owner lockout",
                "Melee-only, with a real 1.5 second lockout");
        emberlash(evidence, "transformation_cadence", "Reprisal charges", "Damage taken",
                "EMBERLASH_REPRISAL_DURATION_TICKS 100; EMBERLASH_REPRISAL_STACK_CAP 3", "EmberlashAbilityManager.onDamageApplied reprisal branch", "Charges expire on their own deadline",
                "Carries its own cap and duration keys, never sharing the Smouldering ones");
        emberlash(evidence, "transformation_pressure", "Reprisal consumption", "Attacking at the trigger count",
                "EMBERLASH_REPRISAL_TRIGGER_COUNT 3; EMBERLASH_REPRISAL_DAMAGE_MULTIPLIER .25", "EmberlashAbilityManager.consumeReprisal", "Charges cleared on consumption",
                "Both the trigger count and the bonus are real consumers");
        emberlash(evidence, "transformation_reversal", "Smouldering detonation", "Reprisal consumption",
                "EMBERLASH_ASH_RADIUS 2.5; EMBERLASH_ASH_DAMAGE_PER_STACK .08; EMBERLASH_ASH_TARGET_CAP 8", "EmberlashAbilityManager.consumeReprisal ash branch and perStackDamage", "Execution-scoped",
                "Repaired: the burst read the Smouldering cap and duration directly rather than through the scoped-then-shared helper used everywhere else");
        emberlash(evidence, "transformation_reserve", "Ash Burst stack application", "Ash Burst hit",
                "EMBERLASH_ASH_APPLIED_STACKS 1", "EmberlashAbilityManager.consumeReprisal shared embers branch", "Smouldering effect duration",
                "Applies only to enemies actually damaged, and is suppressed under Ashen Brand's single-target rule");
        emberlash(evidence, "transformation_threshold", "Maximum Smouldering retaliation", "Being struck by a capped enemy",
                "EMBERLASH_LASHBACK_DURATION_TICKS 40; EMBERLASH_LASHBACK_AMPLIFIER 1", "EmberlashAbilityManager.onDamageApplied lashback branch and isAtMarkedCap", "Status-effect duration",
                "Requires the attacker to be at the cap of a mark this wielder owns");
        emberlash(evidence, "transformation_convergence", "Maximum Smouldering kill", "Killing a capped enemy",
                "EMBERLASH_KILL_REFUND_TICKS 20", "EmberlashAbilityManager.onKill and SimplySwordsAPI.reduceWeaponCooldown", "Execution-scoped",
                "Repaired: it passed the raw configured cooldown as the refund total, so with Ready Cautery owned the remaining time was overestimated and the refund fell short of the advertised second");
        emberlash(evidence, "transformation_focus", "Smouldering detonation capstone", "Melee hit on a smouldering enemy",
                "EMBERLASH_DETONATION_DAMAGE_PER_STACK .3", "EmberlashAbilityManager.onHit detonation branch", "Stacks cleared on consumption",
                "Suppressing the sustained damage also suppresses Endless Smoulder's echo, consistent with its own description");
        emberlash(evidence, "transformation_release", "Reprisal capstone", "Damage taken and dealt",
                "EMBERLASH_REPRISAL_DURATION_TICKS 200; EMBERLASH_REPRISAL_STACK_CAP 5; EMBERLASH_INCOMING_PER_CHARGE_MULTIPLIER 1.04", "EmberlashAbilityManager.reprisalIncoming and the reprisal branch", "Charges expire on their own deadline",
                "The incoming penalty is gated on the capstone and scales with live charges only");
        flamewind(evidence, "signature_opening", "Flame seed periodic damage", "Seed tick",
                "PERIODIC_DAMAGE_MULTIPLIER x1.1", "FlameSeedEffect.applyUpdateEffect", "Seed effect duration",
                "Repaired: Fresh Fuel's absolute write discarded this, and now tunes duration instead");
        flamewind(evidence, "signature_cadence", "Detonation radius", "Detonation",
                "RADIUS +0.75", "FlameSeedEffect.triggerDetonation box", "Execution-scoped",
                "Repaired: dropped the TARGET_CAP write that only restated the default, and Cinder Reserve no longer reduces the cap");
        flamewind(evidence, "signature_pressure", "Detonation burning", "Detonation",
                "FIRE_TICKS 60", "FlameSeedEffect.triggerDetonation ignite", "Vanilla fire duration",
                "Three seconds on every detonation victim");
        flamewind(evidence, "signature_reversal", "Detonation Haste", "Detonation near the owner",
                "FLAMEWIND_HASTE_DURATION_TICKS 140; FLAMEWIND_HASTE_AMPLIFIER 1", "FlameSeedEffect.triggerDetonation haste increment", "Status-effect duration",
                "Repaired: the node was inert. Haste was applied with a hardcoded 120 ticks and amplifier, and both of its values were unread");
        flamewind(evidence, "signature_reserve", "Host-death detonation", "Seed expiring on a dead host",
                "FLAMEWIND_DEATH_DAMAGE_MULTIPLIER 1.25", "FlameSeedEffect expiry detonation branch", "Execution-scoped",
                "Repaired: it wrote FINAL_DAMAGE_MULTIPLIER, which applied to every detonation rather than only host-death ones and was discarded by all three release capstones");
        flamewind(evidence, "signature_threshold", "Nearby owned seeds", "Seed tick",
                "FLAMEWIND_DRAFT_PER_SEED .03; FLAMEWIND_DRAFT_CAP .15; FLAMEWIND_DRAFT_RANGE 8", "Phase5FlamewindManager.refreshDraft as a managed attack-speed modifier", "Modifier removed when no seeds remain, on unload and on server stop",
                "Repaired: the node was entirely inert. No attack-speed mechanic existed and all three of its values were unread");
        flamewind(evidence, "signature_convergence", "Detonation knockback", "Detonation",
                "PULL_STRENGTH .2; FLAMEWIND_KNOCKBACK_MULTIPLIER 1.2", "Phase5FlamewindManager.detonationKnockback", "Execution-scoped",
                "Repaired: PULL_STRENGTH was read only as a sign flip with a hardcoded magnitude, and the push reached only newly seeded targets rather than every detonation victim");
        flamewind(evidence, "signature_focus", "Seed pulse capstone", "Seed tick",
                "INTERVAL_TICKS 20; PERIODIC_DAMAGE_MULTIPLIER .3; FINAL_DAMAGE_MULTIPLIER x0.6", "FlameSeedEffect frequency and detonation damage", "Seed effect duration",
                "The absolute periodic write is deliberate, replacing the sustained damage rather than scaling it");
        flamewind(evidence, "signature_release", "Detonation capstone", "Detonation",
                "PERIODIC_DAMAGE_MULTIPLIER 0; RADIUS 5; FINAL_DAMAGE_MULTIPLIER x1.5; TARGET_CAP 12", "FlameSeedEffect.triggerDetonation", "Execution-scoped",
                "Zero periodic damage is deliberate, and the final multiplier composes");
        flamewind(evidence, "combat_opening", "Emberstorm seeding range", "Ability activation",
                "FLAMEWIND_SEED_RANGE +4", "Phase5FlamewindManager.findSeedTarget", "Execution-scoped",
                "Repaired: the node was inert. The seeding search used a hardcoded ten-block box");
        flamewind(evidence, "combat_cadence", "Flame seed duration", "Seed application",
                "DURATION_TICKS -20", "Phase5FlamewindManager.activate duration", "Seed effect duration",
                "Composes additively with Hardy Ember");
        flamewind(evidence, "combat_pressure", "Flame seed duration and damage", "Seed application and tick",
                "DURATION_TICKS +40; PERIODIC_DAMAGE_MULTIPLIER x1.08", "Phase5FlamewindManager.activate and FlameSeedEffect", "Seed effect duration",
                "Repaired: Fresh Fuel's absolute write discarded the damage half");
        flamewind(evidence, "combat_reversal", "Emberstorm fallback targeting", "Activation with no target in front",
                "FLAMEWIND_RETARGET_RANGE 6", "Phase5FlamewindManager.findSeedTarget extended range", "Execution-scoped",
                "Repaired and rescoped: the base weapon already fell back to the nearest enemy within ten blocks, so a six-block fallback would have been strictly worse. The range is now additive and the description says so");
        flamewind(evidence, "combat_reserve", "Detonation spread", "Detonation",
                "SPREAD_CAP +1", "FlameSeedEffect.triggerDetonation spread accounting", "Seed effect duration",
                "Composes additively over the configured spread cap");
        flamewind(evidence, "combat_threshold", "Spread distance", "Detonation",
                "FLAMEWIND_SPREAD_RANGE +2; FLAMEWIND_SEARCH_CAP 16", "FlameSeedEffect spread range check", "Execution-scoped",
                "Repaired: the node was inert. Spread used the detonation radius with no separate range");
        flamewind(evidence, "combat_convergence", "Spread seed duration", "Seed spreading to a new host",
                "FLAMEWIND_SPREAD_DURATION_FRACTION .8", "Phase5FlamewindManager.spreadDuration", "Seed effect duration",
                "Repaired: it wrote PERIODIC_DAMAGE_MULTIPLIER, an unadvertised damage cut that also discarded Hot Seed and Hardy Ember, while spread seeds used a hardcoded duration");
        flamewind(evidence, "combat_focus", "Spread generation capstone", "Detonation spread",
                "FLAMEWIND_GENERATION_CAP 3; FLAMEWIND_GENERATION_MULTIPLIER 1.25", "Phase5FlamewindManager.generationMultiplier and the spread generation limit", "Execution-scoped",
                "Repaired: the node was inert. SeedSnapshot tracked a generation that nothing read");
        flamewind(evidence, "combat_release", "Runaway spread capstone", "Detonation spread",
                "SPREAD_CAP 10; FLAMEWIND_SPREAD_RANGE +3; FLAMEWIND_GENERATION_MULTIPLIER .8", "FlameSeedEffect spread accounting and generationMultiplier", "Execution-scoped",
                "Repaired: only the spread cap worked; the range and the per-generation penalty were both unread");
        flamewind(evidence, "transformation_opening", "Detonation cooldown refund", "Detonation",
                "FLAMEWIND_HARVEST_REFUND_TICKS 6; FLAMEWIND_HARVEST_REFUND_CAP_TICKS 30", "Phase5FlamewindManager.onDetonation through reduceWeaponCooldown", "Per-cast refund ceiling reset on each cast",
                "Repaired: the node was inert. No per-detonation refund existed");
        flamewind(evidence, "transformation_cadence", "Closely spaced detonations", "Second detonation inside the window",
                "FLAMEWIND_CHAIN_WINDOW_TICKS 10; FLAMEWIND_CHAIN_DAMAGE_MULTIPLIER 1.15", "Phase5FlamewindManager.chainBonus", "Per-owner detonation timestamp",
                "Repaired: the node was inert. No detonation timing was tracked");
        flamewind(evidence, "transformation_pressure", "Rapid detonations", "Third detonation inside the window",
                "FLAMEWIND_FLASH_COUNT 3; FLAMEWIND_FLASH_WINDOW_TICKS 20; FLAMEWIND_FLASH_HASTE_TICKS 80; FLAMEWIND_FLASH_HASTE_AMPLIFIER 1", "Phase5FlamewindManager.onDetonation flash counter", "Counter resets on a lapsed window",
                "Repaired: the node was inert. No detonation counter existed");
        flamewind(evidence, "transformation_reversal", "Manual release", "Reactivation with seeds owned",
                "FINAL_DAMAGE_MULTIPLIER x0.7; FLAMEWIND_RECAST_LOCKOUT_TICKS 100; COOLDOWN_TICKS 100", "Phase5FlamewindManager.activate release branch", "Per-owner recast lockout",
                "Repaired: the advertised five-second lockout did not exist, and the multiplier was absolute. The contract-checked release cooldowns of 100, 140 and 160 are preserved");
        flamewind(evidence, "transformation_reserve", "Manual detonation pull", "Detonation",
                "PULL_STRENGTH .25", "Phase5FlamewindManager.detonationKnockback", "Execution-scoped",
                "The description now states that it reverses the push for all detonations, which is what the shared knockback path does");
        flamewind(evidence, "transformation_threshold", "Manual detonation absorption", "Detonation during a manual release",
                "FLAMEWIND_RESERVE_ABSORPTION 2; FLAMEWIND_RESERVE_ABSORPTION_CAP 8; FLAMEWIND_RESERVE_ABSORPTION_TICKS 80", "Phase4AbsorptionTracker grant from onDetonation", "Expires on the tracker sweep; cleared on unload and server stop",
                "Repaired: the node was inert, and its stray TARGET_CAP 8 was reducing the detonation cap from twelve to eight");
        flamewind(evidence, "transformation_convergence", "Kills in one release", "Manual release",
                "FLAMEWIND_RESET_KILL_COUNT 3; FLAMEWIND_RESET_REFUND_FRACTION .25", "Phase5FlamewindManager.onReleaseKill and applyReleaseReset", "Kill count reset at the start of each release",
                "Repaired: the node was inert. No release kill counting existed");
        flamewind(evidence, "transformation_focus", "Oldest-seed release capstone", "Reactivation with seeds owned",
                "FINAL_DAMAGE_MULTIPLIER x1.4; COOLDOWN_TICKS +40", "Phase5FlamewindManager.activate limited release", "Per-owner recast lockout",
                "Repaired: multiplies instead of overwriting, so the signature branch survives; its contract-checked 140 tick cooldown is preserved");
        flamewind(evidence, "transformation_release", "Full release capstone", "Reactivation with seeds owned",
                "SPREAD_CAP 0; FLAMEWIND_RECAST_LOCKOUT_TICKS 160; COOLDOWN_TICKS 160", "Phase5FlamewindManager.activate release branch and the suppressed spread", "Per-owner recast lockout",
                "Repaired: the advertised eight-second lockout did not exist, and its absolute FINAL_DAMAGE_MULTIPLIER of 1 reset every prior multiplier to neutral");
        molten(evidence, "signature_opening", "Heat gained from melee", "Successful melee hit",
                "MOLTEN_MELEE_HEAT_GAIN 6", "MoltenEdgeAbilityManager.gainHeatFromMelee", "Heat stored on the weapon stack",
                "Repaired: the shared tuning key collided with vent and rupture definitions; this value is now scoped to molten_edge/heat");
        molten(evidence, "signature_cadence", "Heat gained from damage", "Successful incoming damage",
                "MOLTEN_INCOMING_HEAT_GAIN 7", "MoltenEdgeAbilityManager.gainHeatFromIncomingDamage", "Heat stored on the weapon stack",
                "Repaired: incoming heat now has its own definition-scoped key instead of sharing a generic gain value");
        molten(evidence, "signature_pressure", "Incoming heat penalty", "Damage calculation while carrying heat",
                "MOLTEN_INCOMING_PENALTY_MULTIPLIER .85", "MoltenEdgeAbilityManager.modifyIncomingDamage", "Execution-scoped",
                "Repaired: the hardcoded penalty now reads a scoped tuning value");
        molten(evidence, "signature_reversal", "Redline melee damage", "Melee damage at 75 Heat or higher",
                "MOLTEN_REDLINE_THRESHOLD 75; MOLTEN_REDLINE_DAMAGE_MULTIPLIER 1.1", "MoltenEdgeAbilityManager.modifyOutgoingDamage", "Execution-scoped",
                "Repaired: both threshold and multiplier are real consumers, and the description states the exact melee-only trigger");
        molten(evidence, "signature_reserve", "Heat Sink absorption", "Crossing each 20-Heat threshold",
                "MOLTEN_HEAT_SINK_ABSORPTION 2; MOLTEN_HEAT_SINK_STEP 20; MOLTEN_HEAT_SINK_DURATION_TICKS 60; MOLTEN_HEAT_SINK_CAP 6", "MoltenEdgeAbilityManager.applyHeatGainRewards and Phase4AbsorptionTracker", "Timed absorption expires through the shared tracker",
                "Repaired: both melee and incoming heat paths now grant bounded, expiring absorption");
        molten(evidence, "signature_threshold", "Stable Furnace heat decay", "Carried but not wielded",
                "MOLTEN_UNWIELDED_DECAY_INTERVAL_TICKS 2", "MoltenEdgeAbilityManager.tickHeldStack", "Heat state is clamped and cleared with the stack lifecycle",
                "Redesigned with approval: heat now decays by one every two ticks while carried but not wielded instead of disappearing immediately");
        molten(evidence, "signature_convergence", "White Hot melee burn", "Melee hit at maximum Heat",
                "MOLTEN_WHITE_HOT_FIRE_TICKS 60; MOLTEN_WHITE_HOT_DAMAGE_MULTIPLIER 1.12", "MoltenEdgeAbilityManager.gainHeatFromMelee and modifyOutgoingDamage", "Execution-scoped",
                "Repaired: the capstone now has real fire and damage consumers without leaking to non-melee damage");
        molten(evidence, "signature_focus", "Overclock state", "Reaching 100 Heat",
                "MOLTEN_OVERCLOCK_HEAT_FLOOR 75; MOLTEN_OVERCLOCK_OUTGOING_MULTIPLIER 1.35; MOLTEN_OVERCLOCK_INCOMING_AMPLIFICATION 1", "MoltenEdgeAbilityManager heat, vent and damage paths", "Latched component clears on unwield, death, drop, respec and reset",
                "Repaired: Overclock now latches only after reaching 100 Heat, enforces its floor, and applies both advertised damage changes");
        molten(evidence, "signature_release", "Tempered heat model", "Heat gain, damage and venting",
                "MOLTEN_HEAT_MAX 75; MOLTEN_INCOMING_AMPLIFICATION_CAP .5; MOLTEN_OUTGOING_BONUS_CAP .35", "MoltenEdgeAbilityManager heat, damage and vent paths", "Heat stored on the weapon stack",
                "Repaired: maximum Heat is 75, a 75-Heat vent counts as full, attacks do not ignite, and incoming and outgoing scaling are capped");
        molten(evidence, "combat_opening", "Vent heat drain", "Active vent tick",
                "MOLTEN_VENT_DRAIN 2", "MoltenEdgeAbilityManager.tickVents", "ActiveVent owns its cast snapshot",
                "Repaired: the hardcoded drain now reads a definition-scoped vent tuning value");
        molten(evidence, "combat_cadence", "Vent movement speed", "Vent activation",
                "MOLTEN_VENT_SPEED_MULTIPLIER 1.15", "MoltenEdgeAbilityManager.startVent movement modifier", "Modifier is removed on every vent exit and world/server cleanup",
                "Repaired: the node was inert; venting now applies the advertised movement bonus");
        molten(evidence, "combat_pressure", "Vent shockwave reach", "Vent shockwave tick",
                "MOLTEN_SHOCKWAVE_RADIUS 6; MOLTEN_SHOCKWAVE_TARGET_CAP 16", "MoltenEdgeAbilityManager.tickShockwaves", "Each shockwave owns a bounded target set",
                "Repaired: radius is tunable and target iteration is deterministically capped");
        molten(evidence, "combat_reversal", "Blast Furnace force and speed", "Vent activation and shockwave hit",
                "MOLTEN_SHOCKWAVE_KNOCKBACK_MULTIPLIER 1.25; MOLTEN_BLAST_SPEED_TICKS 60", "MoltenEdgeAbilityManager.startVent and tickShockwaves", "Speed status expires normally",
                "Repaired: both advertised effects now have consumers");
        molten(evidence, "combat_reserve", "Rupture cooldown refund", "Successful rupture lane hit during a vent",
                "MOLTEN_RUPTURE_REFUND_TICKS 1; MOLTEN_RUPTURE_MIN_COOLDOWN_TICKS 3", "MoltenEdgeAbilityManager.damageRuptureTargets and refundedDeadline", "ActiveVent deadline is bounded by the minimum cooldown",
                "Repaired: each successful lane can refund one tick without reducing the swing interval below three ticks");
        molten(evidence, "combat_threshold", "Rupture homing", "Rupture cast",
                "MOLTEN_RUPTURE_SEEK_RANGE 5; MOLTEN_RUPTURE_TURN_DEGREES 20", "MoltenEdgeAbilityManager.spawnRuptureCast and turnToward", "Rupture owns its cast snapshot",
                "Repaired: the node was inert; each cast can turn toward a nearby target by at most twenty degrees");
        molten(evidence, "combat_convergence", "Heat Reclamation", "Normal vent completion after hitting targets",
                "MOLTEN_RECLAIM_TARGET_COUNT 3; MOLTEN_RECLAIM_HEAT 15", "MoltenEdgeAbilityManager recordVentHit and finishVent", "Per-vent hit accounting clears with ActiveVent",
                "Repaired: a normally completed vent now restores Heat after hitting three distinct enemies");
        molten(evidence, "combat_focus", "Stampede auto-ruptures", "Moving during a vent",
                "MOLTEN_AUTO_RUPTURE_INTERVAL_TICKS 8; MOLTEN_SHOCKWAVE_DAMAGE_MULTIPLIER .65", "MoltenEdgeAbilityManager.tickVents", "Execution-scoped",
                "Repaired: moving vents now emit periodic reduced-damage ruptures");
        molten(evidence, "combat_release", "Immovable Furnace", "Active vent",
                "MOLTEN_ROOT_SPEED_MULTIPLIER 0; MOLTEN_RESISTANCE_AMPLIFIER 1; MOLTEN_SHOCKWAVE_DAMAGE_MULTIPLIER 1.8", "MoltenEdgeAbilityManager.startVent, tickVents and tickShockwaves", "Movement and prior resistance state are restored on every vent exit",
                "Repaired: the wielder is rooted and resistant, shockwaves are stronger, and manual ruptures are suppressed");
        molten(evidence, "transformation_opening", "Rupture length", "Rupture cast",
                "MOLTEN_RUPTURE_LENGTH 9", "MoltenEdgeAbilityManager.spawnRuptureLane", "Rupture owns its cast snapshot",
                "Repaired: rupture length now has a definition-scoped consumer");
        molten(evidence, "transformation_cadence", "Rupture width and target cap", "Rupture segment",
                "MOLTEN_RUPTURE_WIDTH 2.75; MOLTEN_RUPTURE_SEGMENT_TARGET_CAP 8", "MoltenEdgeAbilityManager.damageRuptureTargets", "RuptureCast shares a bounded hit set across its lanes",
                "Repaired: width is tunable and each segment processes a deterministic bounded target list");
        molten(evidence, "transformation_pressure", "Rupture damage", "Rupture hit",
                "MOLTEN_RUPTURE_DAMAGE_MULTIPLIER 1.12", "MoltenEdgeAbilityManager.spawnRuptureCast and damageRuptureTargets", "Execution-scoped",
                "Repaired: damage is scoped to molten_edge/rupture rather than colliding with heat and vent definitions");
        molten(evidence, "transformation_reversal", "Rupture launch", "Rupture hit",
                "MOLTEN_RUPTURE_KNOCK_UP .35", "MoltenEdgeAbilityManager.damageRuptureTargets", "Execution-scoped",
                "Repaired: the advertised knock-up now has a real consumer");
        molten(evidence, "transformation_reserve", "Rupture ignition", "Rupture hit",
                "MOLTEN_RUPTURE_FIRE_TICKS 80", "MoltenEdgeAbilityManager.damageRuptureTargets", "Vanilla fire duration",
                "Repaired: the advertised four-second burn now has a real consumer");
        molten(evidence, "transformation_threshold", "Forked ruptures", "Every third manual rupture swing",
                "MOLTEN_FORK_SWING_INTERVAL 3; MOLTEN_FORK_COUNT 2; MOLTEN_FORK_ANGLE_DEGREES 18; MOLTEN_FORK_DAMAGE_MULTIPLIER .55", "MoltenEdgeAbilityManager.spawnRuptureCast", "Vent swing counter resets with ActiveVent",
                "Repaired: every third swing adds two reduced-damage forks, while Fissure suppresses forking");
        molten(evidence, "transformation_convergence", "Rupture sequence damage", "Repeated rupture hits on one target",
                "MOLTEN_SEQUENCE_WINDOW_TICKS 40; MOLTEN_SEQUENCE_PER_STACK_MULTIPLIER .1; MOLTEN_SEQUENCE_STACK_CAP 3", "MoltenEdgeAbilityManager.sequenceDamageMultiplier", "Per-world owner-target state expires after forty ticks and clears on lifecycle cleanup",
                "Repaired: consecutive casts now build the advertised bounded damage sequence");
        molten(evidence, "transformation_focus", "Fissure capstone", "Manual rupture cast",
                "MOLTEN_RUPTURE_LENGTH 12; MOLTEN_RUPTURE_WIDTH 3.5; MOLTEN_RUPTURE_DAMAGE_MULTIPLIER 1.7; MOLTEN_RUPTURE_COOLDOWN_MULTIPLIER 2", "MoltenEdgeAbilityManager.spawnRuptureCast and isRuptureAttackReady", "Execution-scoped",
                "Repaired: Fissure modifies only rupture cooldown, suppresses forks, and no longer doubles the vent cooldown through a generic key collision");
        molten(evidence, "transformation_release", "Shatter capstone", "Rupture cast",
                "MOLTEN_SHATTER_LANE_COUNT 5; MOLTEN_SHATTER_LENGTH 5; MOLTEN_SHATTER_ANGLE_DEGREES 18; MOLTEN_SHATTER_DAMAGE_MULTIPLIER .45", "MoltenEdgeAbilityManager.spawnRuptureCast", "All five lanes share one cast hit set and finish together",
                "Repaired: Shatter emits five five-block lanes at -36, -18, 0, 18 and 36 degrees without multi-hitting the same target");
        soulpyre(evidence, "signature_opening", "Soul Tether duration", "Tether activation",
                "Duration 700t", "ActivePyre deadline", "Cast-owned state", "Verified: the dedicated duration key adds five seconds");
        soulpyre(evidence, "signature_cadence", "Soul Tether opening area", "Activation and pulse",
                "Starting radius 4; target cap 32", "Bounded distance-ordered pulse query", "Cast-owned state", "Repaired: pulse searches now have an explicit cap");
        soulpyre(evidence, "signature_pressure", "Soul Tether growth", "Eligible kill",
                "Growth 1.25; maximum 12", "SoulPyreAbilityManager.growRadius", "Cast-owned state", "Repaired: growth and maximum use scoped settings");
        soulpyre(evidence, "signature_reversal", "Regular pulse soul scaling", "Regular pulse",
                "+6% per stored soul", "SoulPyreAbilityManager.performPulse", "Cast soul store", "Repaired: the scoped value applies only to regular pulses");
        soulpyre(evidence, "signature_reserve", "Binding pulse history", "Three pulse hits within 80t",
                "Slowness II 40t", "SoulPyreAbilityManager.recordBindingPulse", "Per-cast target history", "Repaired: previously unread values drive the hit-window mechanic");
        soulpyre(evidence, "signature_threshold", "Periodic inward pull", "Every fifth pulse",
                "Interval 5; strength .3", "SoulPyreAbilityManager.performPulse", "Cast cadence", "Repaired: undescribed base pulling was removed");
        soulpyre(evidence, "signature_convergence", "Early-kill absorption", "First three kills",
                "2 absorption 80t; cap 6", "Phase4AbsorptionTracker", "Bounded expiry", "Repaired: count, duration and cap are consumed");
        soulpyre(evidence, "signature_focus", "Devouring Tether", "Activation, kill and pulse",
                "Growth 2; +1 pulse; damage x.75", "SoulPyreAbilityManager schedule and growth", "Cast-owned state", "Repaired: all capstone clauses compose");
        soulpyre(evidence, "signature_release", "Closed Circle", "Activation and regular pulse",
                "Radius 4; no growth; damage x1.6; interval x.8", "Closed cadence", "Original deadline retained", "Repaired: faster pulses preserve the final collapse");
        soulpyre(evidence, "combat_opening", "Wisp damage", "Volley preparation",
                "Damage x1.15", "SoulPyreAbilityManager.launchWispVolley", "Volley snapshot", "Verified: applied once per prepared volley");
        soulpyre(evidence, "combat_cadence", "Wisp volley threshold", "Soul harvest",
                "Volley size 6", "SoulPyreAbilityManager.effectiveVolleySize", "Cast soul store", "Verified: six souls launch six wisps");
        soulpyre(evidence, "combat_pressure", "Wisp acquisition", "Volley and targetless flight",
                "Range +4; search cap 24", "SoulPyreAbilityManager.findWispTargets", "Wisp lifetime", "Repaired: range and deterministic cap are consumed");
        soulpyre(evidence, "combat_reversal", "Wisp retarget", "Assigned target dies",
                "One retarget within 5", "SoulPyreAbilityManager.tickWisps", "Per-wisp counter", "Repaired: base targetless acquisition remains without unlimited retargeting");
        soulpyre(evidence, "combat_reserve", "Owner-specific Pyre Mark", "Successful wisp hit",
                "Fire 60t; mark 80t", "SoulPyreAbilityManager.WISP_MARKS", "TTL and lifecycle cleanup", "Repaired: ignition and ownership-safe marks consume the settings");
        soulpyre(evidence, "combat_threshold", "Marked-target damage", "Wisp hits owner-marked target",
                "Direct damage x1.2", "Wisp mark gate", "Mark TTL", "Repaired: bonus applies once to direct wisp damage");
        soulpyre(evidence, "combat_convergence", "Full-volley explosion", "Last wisp of six hits",
                "Radius 2.5; damage x.4; secondary cap 8", "explodeWailingWisp", "Volley snapshot", "Repaired: the inert node now has a bounded blast");
        soulpyre(evidence, "combat_focus", "Legion volley", "Volley preparation",
                "Size x2; damage x.55; three assignments per target", "launchWispVolley", "Volley assignment map", "Repaired: Full Volley composes to twelve wisps");
        soulpyre(evidence, "combat_release", "Soul Lance", "Normal final collapse",
                "Damage x.25 per soul; cap 10 souls and 6 targets", "launchSoulLance", "Single-wisp hit set", "Redesigned: automatic volleys become one bounded piercing wisp");
        soulpyre(evidence, "transformation_opening", "Tether damage reduction", "Incoming damage",
                "Reduction .55", "modifyIncomingDamage", "Tether lifetime", "Verified: scoped reduction replaces the base value");
        soulpyre(evidence, "transformation_cadence", "Knockback resistance", "Activation and end",
                "Full resistance; linger 60t", "Managed attribute modifier", "End, death, disconnect, unload and stop", "Repaired: base resistance and safe linger are restored");
        soulpyre(evidence, "transformation_pressure", "Nether Ward", "Activation and end",
                "Fire Resistance I plus 40t", "Managed status state", "Prior-effect restoration", "Repaired: stronger and pre-existing effects are preserved");
        soulpyre(evidence, "transformation_reversal", "Harvest absorption", "Every fifth harvested soul",
                "2 absorption; cap 8", "harvestSoul", "Cast counter and cap", "Repaired: spent volleys do not erase progress");
        soulpyre(evidence, "transformation_reserve", "Shrinking collapse damage", "Collapse every 20t",
                "Duration 80t; damage x.2", "tickCollapse", "Collapse deadline", "Repaired: current area and normal target cap are used");
        soulpyre(evidence, "transformation_threshold", "Requiem harvest scaling", "Normal final collapse",
                "+12% per harvest; cap 10", "finalSoulMultiplier", "Cast harvest counter", "Redesigned: scaling is independent of volley spending");
        soulpyre(evidence, "transformation_convergence", "Last Rites", "Final collapse after five harvests",
                "Resistance I 80t", "beginCollapse", "Status duration", "Repaired: harvested souls count after firing");
        soulpyre(evidence, "transformation_focus", "Undying safeguard", "Totem-eligible lethal damage",
                "Reserve 10; health 1; Resistance III 60t", "tryUndyingDominion", "Reserve cap and cancellation", "Redesigned: separate reserve cancels without collapse, healing, wisps or Requiem");
        soulpyre(evidence, "transformation_release", "Funeral Sun finale", "Normal final collapse",
                "No reduction; radius 8; damage x1.75; cap 24", "Final-only Funeral branch", "Cast-owned state", "Repaired: settings affect only defense and finale");
        stormbringer(evidence, "signature_opening", "Shock Deflect parry window", "Guard activation", "+4t capped by block duration", "Scoped parry deadline", "Guard execution", "Repaired: 24t window is isolated from Supercell");
        stormbringer(evidence, "signature_cadence", "Normal-block charge gain", "Normal block", "2 charges", "Bounded stack grant", "Stack component", "Repaired: generic counts cannot overwrite the gain");
        stormbringer(evidence, "signature_pressure", "Shock Deflect counter", "Perfect counter", "Damage x1.12; radius 3.5; cap 10", "Distance-ordered counter query", "Guard execution", "Repaired: counter channels are isolated from Chain Tempest");
        stormbringer(evidence, "signature_reversal", "Guard defense", "Normal block or perfect parry", "Resistance I 30t", "Common ward path", "Status duration", "Repaired: both successful block paths grant the ward");
        stormbringer(evidence, "signature_reserve", "Perfect charge gain", "Perfect parry", "7 charges", "Bounded stack grant", "Stack component", "Verified: composes before Short Circuit rounding");
        stormbringer(evidence, "signature_threshold", "Perfect counter force", "Damaged counter target", "Knockback x1.25; fire 20t", "Counter hit path", "Fire duration", "Verified: exact tick ignition and force are applied");
        stormbringer(evidence, "signature_convergence", "Shock Deflect cooldown", "Perfect completion", "Refund 30t", "Post-install cooldown reduction", "Once per guard", "Repaired: the active cooldown now consumes the refund");
        stormbringer(evidence, "signature_focus", "Razor Guard", "Guard and counter", "Window 8t; counter x2; normal gain 0", "Scoped guard consumers", "Exclusive capstone", "Repaired: chain damage cannot leak into the counter");
        stormbringer(evidence, "signature_release", "Bulwark Coil", "Guard and counter", "Duration 70t; gain 3; counter x.5", "Live max-use and counter consumers", "Exclusive capstone", "Repaired: channel and guard last the additional second");
        stormbringer(evidence, "combat_opening", "Storm Charge storage", "Gain and chain preparation", "Cap 12", "Cap normalization and ParryComponent", "Stack component", "Repaired: stored state normalizes when the cap changes");
        stormbringer(evidence, "combat_cadence", "Free chain", "Successful non-Full chain after lockout", "Cost 0; lockout 80t", "Owner free-proc state", "Lifecycle-cleared owner state", "Repaired: unrelated lockouts cannot overwrite the cadence");
        stormbringer(evidence, "combat_pressure", "Stormbringer melee", "Melee at five charges", "Damage x1.08", "Outgoing damage hook", "Per-hit execution", "Repaired: the multiplier is consumed before chain release");
        stormbringer(evidence, "combat_reversal", "Charge overflow", "Gain attempt while capped", "Absorption 2 for 60t; lockout 40t", "Bounded absorption tracker", "Timed owner grant", "Repaired: overflow is reachable, bounded and cleaned");
        stormbringer(evidence, "combat_reserve", "Spending primary hit", "Chain that spends charge", "Slowness I 30t", "Post-spend primary gate", "Status duration", "Repaired: free chains do not satisfy the gate");
        stormbringer(evidence, "combat_threshold", "Charge-spend rhythm", "Three charges in 100t", "Haste I 80t", "Spend timestamp queue", "Bounded owner window", "Repaired: actual spent charges drive the threshold");
        stormbringer(evidence, "combat_convergence", "Full Battery", "Successful chain at cap", "Damage x1.25; cost 2", "Full-state snapshot", "Execution-scoped", "Repaired: one conditional multiplier replaces the double application");
        stormbringer(evidence, "combat_focus", "Supercell", "Held tick and chain", "Cap 15; +4% per charge; decay 40t", "Held decay and chain snapshot", "Owner and stack state", "Repaired: decay is isolated from parry timing");
        stormbringer(evidence, "combat_release", "Short Circuit", "Gain and chain", "Cap 6; cooldown x.5; gain x.5", "Rounded grant and cooldown helpers", "Exclusive capstone", "Repaired: final combined gains are halved and rounded up");
        stormbringer(evidence, "transformation_opening", "Chain reach", "Chain preparation", "Range 7.5", "Bounded chain query", "Execution-scoped", "Verified: composes with Rolling Thunder");
        stormbringer(evidence, "transformation_cadence", "Chain damage", "Chain hit", "Damage x1.12", "Base chain damage", "Execution-scoped", "Repaired: cannot affect Shock Deflect counters");
        stormbringer(evidence, "transformation_pressure", "Forked bonus target", "After normal chain", "+1 unvisited target", "Bounded chain count", "Execution-scoped", "Redesigned: preserves base chain and respects both capstones");
        stormbringer(evidence, "transformation_reversal", "Conductive mark", "Successful chain or burst hit", "Mark 80t; later damage x1.15", "Owner-target pre-hit mark snapshot", "TTL, death, actor and world cleanup", "Repaired: current chain prepares only the next chain");
        stormbringer(evidence, "transformation_reserve", "Ground Current burst", "Successful final chain hit", "Radius 2; damage x.25; cap 6", "Distance-ordered secondary query", "Execution-scoped", "Repaired: main targets are excluded and Focused suppresses it");
        stormbringer(evidence, "transformation_threshold", "Chain kill refund", "Distinct chain or burst kill", "Refund cap 2", "Execution kill set", "Execution-scoped", "Repaired: refunds follow real charge spending");
        stormbringer(evidence, "transformation_convergence", "Chain cooldown cadence", "Distinct damaged enemies", "-4t each; minimum 8t", "Post-hit cooldown resolver", "Owner cooldown state", "Repaired: one hit resolves to 16t instead of the minimum");
        stormbringer(evidence, "transformation_focus", "Focused Bolt", "Primary chain release", "3 hits at x.55", "Focused hit sequence", "Exclusive capstone", "Repaired: cost occurs once and arcs and bursts are suppressed");
        stormbringer(evidence, "transformation_release", "Rolling Thunder", "Ordered jumps", "Cap 10; range +3; jump x.82", "Indexed chain damage", "Exclusive capstone", "Repaired: damage compounds per jump with bounded targeting");
        mjolnir(evidence, "signature_opening", "Storm duration", "Storm cast", "+40t on the configured duration", "Cast duration resolver", "Execution ends the storm", "Repaired: the bonus is additive on config instead of a literal 200");
        mjolnir(evidence, "signature_cadence", "Storm pulse cadence", "Storm tick", "Interval 9t", "Pulse scheduler", "Execution-scoped", "Verified: 0.45s against the configured 0.5s");
        mjolnir(evidence, "signature_pressure", "Storm targeting radius", "Target selection", "+2 blocks; candidate cap 20", "Scoped storm radius resolver", "Execution-scoped", "Repaired: was inert at the configured radius and also widened the final clap");
        mjolnir(evidence, "signature_reversal", "Storm bolt damage", "Storm cast", "Bolt damage x1.12", "Cast damage snapshot", "Execution-scoped", "Verified: composes multiplicatively with both capstones");
        mjolnir(evidence, "signature_reserve", "Conductive duration", "Mjolnir melee hit", "+40t on the configured duration", "Scoped conductive resolver", "Status expiry", "Repaired: +1s became +2s and no combat node can shorten it");
        mjolnir(evidence, "signature_threshold", "Forked bolt", "Every fourth storm strike", "Fork count 4; range 5; damage x.6", "Conductive fork release", "Execution-scoped", "Repaired: the fork now exists and no longer retunes the final sequence");
        mjolnir(evidence, "signature_convergence", "Final thunderclap scaling", "Final clap", "+5% per Conductive target, cap 6", "Conductive target set", "Execution-scoped", "Repaired: counts Conductive targets rather than every target struck");
        mjolnir(evidence, "signature_focus", "Storm duration and finale", "Storm cast", "Duration x2; bolts x.8; clap suppressed", "Duration resolver and clap gate", "Exclusive capstone", "Repaired: a real suppression gate replaces a zero damage multiplier");
        mjolnir(evidence, "signature_release", "Short storm profile", "Storm cast", "100t; interval 6t; bolts x1.35; radius x.75", "Duration, cadence and radius resolvers", "Exclusive capstone", "Repaired: the cut is a real 25% of the configured radius");
        mjolnir(evidence, "combat_opening", "Cast movement buff", "Storm cast", "Speed I for 100t", "Scoped speed grant", "Status expiry", "Repaired: its duration can no longer shorten Conductive");
        mjolnir(evidence, "combat_cadence", "Storm entry burst", "Enemy enters the storm", "Radius 2.5; damage x.3; lockout 40t", "Per-tick storm membership diff", "Execution-scoped membership set", "Repaired: implemented, and its radius no longer shrinks the storm");
        mjolnir(evidence, "combat_pressure", "Charged advance", "8 blocks moved inside the storm", "Next attack x1.15", "Owner outgoing damage hook", "Consumed on use; owner state expiry", "Repaired: implemented; both keys previously had no consumer");
        mjolnir(evidence, "combat_reversal", "Early finale release", "Secondary action after 60t", "Arm delay 60t", "Secondary action release path", "Once per cast; execution-scoped", "Redesigned: relocation was redundant and cooldown-blocked");
        mjolnir(evidence, "combat_reserve", "Sprint shock", "Sprinting inside the storm", "Radius 2; cap 4; damage x.2; lockout 20t", "Bounded nearest-first sprint pulse", "Execution-scoped", "Repaired: implemented, and its radius no longer shrinks the storm");
        mjolnir(evidence, "combat_threshold", "Bolt mobility reward", "Storm bolt hit", "Jump Boost II 40t and fall protection", "Bolt reward path and owner fall guard", "Owner state expiry", "Repaired: implemented; the node was entirely inert");
        mjolnir(evidence, "combat_convergence", "Conductive kill reward", "Killing a Conductive enemy", "Speed II 60t; refund 20t", "Owner death hook and cooldown refund", "Conductive memory window", "Repaired: implemented through the real cooldown refund helper");
        mjolnir(evidence, "combat_focus", "Bolt ride", "Storm bolt hit", "Resistance 20t; single-target lock", "Teleport and target lock", "Exclusive capstone; execution-scoped", "Repaired: implemented, and its drawback is now real");
        mjolnir(evidence, "combat_release", "Anchored storm", "Storm cast", "Anchor radius 3", "Positional anchor and hold check", "Exclusive capstone; ends on leaving", "Redesigned: the storm anchors so its stated boundary can exist");
        mjolnir(evidence, "transformation_opening", "Cast absorption", "Storm cast", "4 absorption for 80t", "Tracked absorption grant", "Timed tracker expiry", "Repaired: was permanent and untracked");
        mjolnir(evidence, "transformation_cadence", "Conductive damage reduction", "Damage from a Conductive attacker", "Incoming x.88", "Owner incoming damage hook", "Owner state expiry", "Repaired: implemented; the key had no consumer");
        mjolnir(evidence, "transformation_pressure", "Melee reprisal", "Melee damage taken", "Damage x.2; lockout 30t", "Owner post-damage hook", "Recursion guard and lockout", "Repaired: implemented; the key had no consumer");
        mjolnir(evidence, "transformation_reversal", "Conductive burst physics", "Conductive burst", "Knockback x1.15; lift x1.25", "Conductive burst knock resolver", "Execution-scoped", "Repaired: reaches the burst its text names rather than the final clap");
        mjolnir(evidence, "transformation_reserve", "Final sequence ward", "Final bolt sequence begins", "Resistance II for the sequence", "Final sequence entry", "Expires with the sequence", "Repaired: implemented; the amplifier had no consumer");
        mjolnir(evidence, "transformation_threshold", "Final bolt count", "Final sequence", "4 bolts", "Scoped final bolt count", "Execution-scoped", "Verified: no other node can retune the count");
        mjolnir(evidence, "transformation_convergence", "Final thunderclap reach", "Final clap", "+1.5 blocks; damage x1.25; cap 24", "Scoped final radius and cap", "Execution-scoped", "Repaired: raises only the clap and no longer narrows the storm");
        mjolnir(evidence, "transformation_focus", "Defensive finale", "Final clap", "8 absorption and Resistance II for 120t", "Clap-time tracked grant", "Timed tracker expiry; exclusive capstone", "Repaired: granted at the clap, expiring, with clap damage really suppressed");
        mjolnir(evidence, "transformation_release", "Offensive finale", "Final clap", "Damage x2; knockback x1.5; buffs suppressed", "Clap damage and knock resolvers", "Exclusive capstone", "Repaired: the drawback exists and Skybreaker survives");
        thunderbrand(evidence, "signature_opening", "Melee cooldown refresh", "Thunderbrand melee hit", "+5 percentage points", "Passive refresh execution", "Per-hit snapshot", "Repaired: the melee proc now consumes mastery tuning for players and non-players");
        thunderbrand(evidence, "signature_cadence", "Thunder Blitz cooldown", "Active execution preparation", "-25t", "Execution cooldown key", "Execution-scoped", "Repaired: subtracts from the configured cooldown instead of a 250-tick literal");
        thunderbrand(evidence, "signature_pressure", "Charge duration", "Active execution", "-6t", "Configured charge resolver", "Execution-scoped", "Repaired: no longer shares duration with Steadfast Shelter");
        thunderbrand(evidence, "signature_reversal", "Dash duration", "Dash start", "+3t", "Configured dash resolver", "Execution-scoped", "Repaired: chain count and thresholds cannot overwrite dash time");
        thunderbrand(evidence, "signature_reserve", "Dash speed", "Dash movement", "Speed x1.12", "Steering velocity resolver", "Execution-scoped", "Repaired: multiplies configured speed and composes with Thunderclap Rush");
        thunderbrand(evidence, "signature_threshold", "Collision radius", "Dash collision query", "+0.5 blocks", "Swept collision box", "Execution-scoped", "Repaired: no longer changes any chain or burst target cap");
        thunderbrand(evidence, "signature_convergence", "Dash-end burst", "Completed dash", "Radius 3; damage x.45; cap 10", "Nearest-first Blitz burst", "Execution-scoped", "Repaired: radius and cap are isolated from collision and Chain Release");
        thunderbrand(evidence, "signature_focus", "Thunderclap Rush", "Dash start and movement", "Duration 8t; speed x1.5; burst x1; steering 0", "Fixed dash profile", "Exclusive capstone", "Repaired: locked steering is now a real tradeoff");
        thunderbrand(evidence, "signature_release", "Rolling Blitz", "Dash start and movement", "Duration x2; collision/bursts x.65; steering x1.5", "Composed dash profile", "Exclusive capstone", "Repaired: freer steering exists and Chain Release cannot shorten the dash");
        thunderbrand(evidence, "combat_opening", "Charge defense padding", "Dash start", "+6t", "Immunity or Grounded Resistance window", "Execution-scoped", "Repaired: extends defense into the dash without delaying the charge");
        thunderbrand(evidence, "combat_cadence", "Interrupted stored hits", "Charge interruption and reactivation", "Memory 40t", "Secondary-action resume path", "TTL, actor and world cleanup", "Repaired: resumes through the cooldown gate without a second mana charge or cooldown");
        thunderbrand(evidence, "combat_pressure", "Stored-hit capacity", "Incoming charge hit", "Cap 17", "Stored-hit and release scheduler", "Execution-scoped", "Verified: both storage and release use the same cap");
        thunderbrand(evidence, "combat_reversal", "Dash absorption", "Dash begins", "2 per hit; cap 8; duration 40t", "Owned absorption grant", "Timed and lifecycle cleanup", "Repaired: granted at the advertised time and removes only its remaining contribution");
        thunderbrand(evidence, "combat_reserve", "Melee retaliation", "Absorbed direct melee damage", "Slowness I 30t", "Incoming damage interceptor", "Status expiry", "Repaired: projectile and Grounded damage do not satisfy the absorbed-melee gate");
        thunderbrand(evidence, "combat_threshold", "Stored-hit collision threshold", "Dash collision at eight hits", "Threshold 8; damage x1.2", "Conditional collision multiplier", "Execution-scoped", "Repaired: applies exactly once and cannot alter dash duration");
        thunderbrand(evidence, "combat_convergence", "Full-cap cooldown refund", "Completed dash at stored cap", "Refund 40t", "Post-dash cooldown reduction", "Once per execution", "Repaired: uses the tuned cooldown installed on the live weapon");
        thunderbrand(evidence, "combat_focus", "Grounded Capacitor", "Charge defense and dash collision", "Resistance III; +12% per hit", "Incoming interceptor and collision resolver", "Exclusive capstone", "Repaired: Railbolt cannot overwrite its per-hit scaling");
        thunderbrand(evidence, "combat_release", "Perfect Insulator", "Charge and dash", "No stored hits; collision x1.4", "Immunity and stored-cap gate", "Exclusive capstone", "Repaired: the description now states the actual no-chain tradeoff");
        thunderbrand(evidence, "transformation_opening", "Chain target count", "Each stored release", "+1 target; cap 6", "Configured chain-cap resolver", "Execution-scoped", "Repaired: no longer writes dash duration and honours custom chain configuration");
        thunderbrand(evidence, "transformation_cadence", "Chain range", "Chain discovery", "+1.5 blocks", "Configured range resolver", "Execution-scoped", "Repaired: additive on the configured range");
        thunderbrand(evidence, "transformation_pressure", "Chain damage", "Chain Release hit", "Damage x1.15", "Chain damage resolver", "Execution-scoped", "Repaired: Crosscurrent is a separate return strike instead of an overwrite");
        thunderbrand(evidence, "transformation_reversal", "Dash conductor mark", "Successful dash collision", "Later chain x1.2 for 60t", "Owner-target mark lookup", "TTL, actor and world cleanup", "Repaired: owner-scoped marks are consumed only by later Chain Release damage");
        thunderbrand(evidence, "transformation_reserve", "Crosscurrent return", "Completed normal chain or Railbolt", "Return damage x.4", "Dash-hit append or rail return pass", "Execution-scoped", "Repaired: preserves normal chain damage and remains meaningful for Railbolt");
        thunderbrand(evidence, "transformation_threshold", "Arc Wake", "Each chain endpoint", "Radius 2; damage x.2; cap 4", "Nearest-first endpoint burst", "Execution-scoped", "Repaired: one bounded burst occurs at every final chain enemy");
        thunderbrand(evidence, "transformation_convergence", "Final Discharge", "Last stored release", "Radius 3.5; damage x.6; cap 12", "Nearest-first final thunderclap", "Once per execution", "Repaired: requires stored charge and composes with Arc Wake and Blitz Finish");
        thunderbrand(evidence, "transformation_focus", "Railbolt", "First combined release time", ".3 damage per stored hit; stored cap 6", "Finite piercing line and return pass", "Exclusive capstone", "Redesigned: one bounded line replaces the jumping chains while prerequisite nodes retain value");
        thunderbrand(evidence, "transformation_release", "Web Lightning", "Each stored release", "2 chains at x.55", "Cast-wide unique-target chain scheduler", "Exclusive capstone; execution-scoped", "Redesigned: doubled chains share one hit set across all Chain Release damage");
        tempest(evidence, "signature_opening", "Vortex starting radius", "Vortex cast", "+1 block", "Config-relative radius resolver", "Execution-scoped", "Repaired: the bonus composes with consumed-stack growth instead of replacing it");
        tempest(evidence, "signature_cadence", "Vortex radius growth", "Consumed stacks", "Growth x1.1", "Effective-stack radius resolver", "Execution-scoped", "Repaired: the per-stack multiplier is consumed without changing passive mark caps");
        tempest(evidence, "signature_pressure", "Vortex pulse damage", "Vortex pulse", "Damage x1.1", "Scoped pulse damage resolver", "Execution-scoped", "Repaired: passive fire and frost damage are unaffected");
        tempest(evidence, "signature_reversal", "Vortex pull", "Vortex pulse", "Pull 1.2", "Post-damage inward velocity", "Execution-scoped", "Repaired: grants real inward pull and the description no longer claims a nonexistent base pull");
        tempest(evidence, "signature_reserve", "Activation absorption", "At least five consumed stacks", "4 absorption for 80t", "Tracked absorption grant", "Timed and lifecycle cleanup", "Repaired: counts real stacks and removes only its surviving contribution");
        tempest(evidence, "signature_threshold", "Vortex lifetime and shrink", "Vortex cast and tick", "+40t; shrink interval x1.25", "Duration and effective-size resolvers", "Execution-scoped", "Repaired: the base now loses one effective size stack every 40 ticks");
        tempest(evidence, "signature_convergence", "Maximum-size cadence", "Vortex at maximum effective size", "Pulse interval 8t", "Conditional pulse scheduler", "Execution-scoped", "Repaired: no longer accelerates every pulse or retunes passive caps");
        tempest(evidence, "signature_focus", "Mobile Vortex tradeoff", "Vortex cast", "Speed I; size and damage x.75", "Status grant and scoped multipliers", "Exclusive capstone", "Repaired: the redundant follows-owner claim was removed");
        tempest(evidence, "signature_release", "Fixed Vortex tradeoff", "Vortex cast", "Size and damage x1.35; range 12", "Cast-origin centre and range gate", "Exclusive capstone; execution-scoped", "Repaired: the Vortex is stationary and ends when its owner leaves range");
        tempest(evidence, "combat_opening", "Element mark duration", "Tempest melee hit", "+200t", "Configured mark-duration resolver", "Owner-target mark TTL", "Repaired: no longer shortens the active Vortex");
        tempest(evidence, "combat_cadence", "Element stack capacity", "Tempest melee hit", "Cap 12 per element", "Owner-target stack clamp", "Mark TTL", "Repaired: actual one-based counts are independent of Vortex maximum size");
        tempest(evidence, "combat_pressure", "Opposite-element bonus", "Every third attack", "+1 opposite stack", "Per-owner attack cadence", "Bounded cadence state", "Repaired: bonus applications do not recurse or advance attack cadence");
        tempest(evidence, "combat_reversal", "Fire mark damage", "Fire mark pulse", "Damage x1.12", "Scoped fire pulse resolver", "Mark TTL", "Repaired: frost marks and the active Vortex are unaffected");
        tempest(evidence, "combat_reserve", "Frost mark slow", "Frost mark active", "+5 percentage points; 45% cap", "Managed movement-speed modifier", "Removed with marks, actor and world", "Repaired: Frost marks now have real bounded base slowing");
        tempest(evidence, "combat_threshold", "Alternating cadence", "Fourth consecutive alternating attack", "+1 opposite stack", "Per-owner alternating streak", "Bounded cadence state", "Repaired: bonus stacks do not advance the streak");
        tempest(evidence, "combat_convergence", "Dual-element sequence reward", "Both elements within 100t", "Haste I 100t", "Owner-target application timestamps", "Status expiry", "Repaired: the real dual-element window drives the grant");
        tempest(evidence, "combat_focus", "Element specialization", "First element during the lock", "Lock 200t; potency x1.5", "Primary selection and weighted consumption", "Exclusive capstone; owner lock TTL", "Repaired: opposite bonus elements are suppressed and potency reaches marks and Vortex scaling");
        tempest(evidence, "combat_release", "Prismatic alternation", "Each Tempest attack and mixed activation", "Cap 6; mixed Vortex damage x1.25", "Deterministic selector and active snapshot", "Exclusive capstone", "Repaired: individual marks are not incorrectly damage-boosted");
        tempest(evidence, "transformation_opening", "Element recall efficiency", "Vortex activation", "Retain one stack; consume at least one", "Owner-target consumption resolver", "Remaining mark TTL", "Repaired: leaves one where possible instead of deleting every stack");
        tempest(evidence, "transformation_cadence", "Consumed-stack Vortex scaling", "Vortex cast", "+12% per consumed stack", "Cast damage snapshot", "Execution-scoped", "Repaired: composes with signature and capstone damage multipliers");
        tempest(evidence, "transformation_pressure", "Thermal Shock burst", "Both elements consumed", "Radius 3; damage x.4; cap 10", "Nearest-first activation burst", "Once per execution", "Repaired: separate burst keys no longer overwrite Vortex radius");
        tempest(evidence, "transformation_reversal", "Rimefire root", "Four of each consumed and third target hit", "Root 20t", "Per-target Vortex hit count", "Once per target; status expiry", "Repaired: qualification and hit counting are both real");
        tempest(evidence, "transformation_reserve", "Steam Burst", "Every fifth main pulse after mixed consumption", "Radius 3; damage x.3; cap 8", "Nearest-first pulse burst", "Execution-scoped", "Repaired: uses a separate bounded secondary path");
        tempest(evidence, "transformation_threshold", "Twin Core resistance", "Both elements consumed", "Resistance I for Vortex duration", "Cast-time status grant", "Status and execution expiry", "Repaired: duration is the final composed Vortex lifetime");
        tempest(evidence, "transformation_convergence", "Final Convergence", "Natural Vortex expiry", "10% per stack; cap 20; radius 4; cap 16", "Nearest-first expiry burst", "Natural expiry only", "Repaired: cancellation and cleanup cannot trigger the finale");
        tempest(evidence, "transformation_focus", "Singularity profile", "Vortex cast", "Radius 4; pull and damage x1.8; duration x.5", "Scoped radius, force, damage and duration resolvers", "Exclusive capstone", "Repaired: follows the chosen signature movement mode instead of contradicting both capstones");
        tempest(evidence, "transformation_release", "Elemental Exodus waves", "Vortex activation", "Two moving waves at x.75", "Bounded moving wave states", "Exclusive capstone; execution-scoped", "Redesigned: waves supplement the Vortex so the signature branch retains value");
        frostfall(evidence, "signature_opening", "Direct throw damage", "Direct impact", "Damage x1.12", "Scoped direct-impact resolver", "Projectile snapshot", "Repaired: pulse damage is unaffected");
        frostfall(evidence, "signature_cadence", "Direct impact control", "Direct impact", "Slowness I 40t", "Direct-hit status path", "Status expiry", "Repaired: pulse Slowness retains configured duration");
        frostfall(evidence, "signature_pressure", "Flight-time impact bonus", "Impact after 10 flight ticks", "+20% frozen base damage", "Flight-age direct resolver", "Projectile snapshot", "Repaired: the flat bonus composes with legacy age scaling");
        frostfall(evidence, "signature_reversal", "Slowed-target impact", "Direct hit on Slowed target", "Damage x1.15", "Pre-impact status sample", "Projectile snapshot", "Repaired: the condition is evaluated before Creeping Chill applies");
        frostfall(evidence, "signature_reserve", "Direct freeze progress", "Direct impact", "+30 frozen ticks", "Bounded freeze update", "Target freeze state", "Verified: direct hits alone consume the scoped value");
        frostfall(evidence, "signature_threshold", "Impact splinters", "Direct impact", "3 targets; range 4; damage x.25", "Nearest-first position-origin burst", "Once per impact", "Repaired: dead triggering targets do not prevent secondary discovery");
        frostfall(evidence, "signature_convergence", "Long-range pulse mark", "Direct impact from 12 blocks", "Mark 100t; pulse x1.2", "Projectile-owned marked target", "Mark TTL and projectile removal", "Repaired: only the marked target gains pulse damage");
        frostfall(evidence, "signature_focus", "Downward throw tradeoff", "Launch pitch at least 35 degrees", "Impact x1.8; pulses x.5", "Frozen launch-angle snapshot", "Exclusive capstone; projectile snapshot", "Redesigned: reduced pulses preserve the Glacial Impact branch");
        frostfall(evidence, "signature_release", "Skirmisher throw profile", "Throw and return", "Cooldown x.5; impact x.7; return speed x1.25", "Execution cooldown and return acceleration resolvers", "Exclusive capstone; projectile snapshot", "Repaired: cooldown is config-relative and installed before disarming");
        frostfall(evidence, "combat_opening", "Loyalty return speed", "Return begins", "Speed x1.15", "Additive thrown-sword speed hook", "Projectile snapshot", "Repaired: composes multiplicatively with Skirmisher's Frost");
        frostfall(evidence, "combat_cadence", "Return-path damage", "Inbound contact", "Damage x.35; cap 6", "Per-target return sweep", "Cast-wide return hit set", "Repaired: each eligible target is hit at most once");
        frostfall(evidence, "combat_pressure", "Return-path pull", "Inbound hit", "Pull .2 toward owner", "Post-hit return pull", "Cast-wide return hit set", "Repaired: field pull is unaffected");
        frostfall(evidence, "combat_reversal", "Catch speed reward", "Owner catches Frostfall", "Speed I 60t", "Projectile pickup hook", "Status expiry", "Repaired: successful owner pickup is the authoritative catch");
        frostfall(evidence, "combat_reserve", "Direct-hit catch refund", "Catch after direct impact", "Refund 15t", "Live item cooldown reduction", "Once per catch", "Repaired: uses the installed tuned cooldown");
        frostfall(evidence, "combat_threshold", "Outbound-return root", "Same target hit both ways", "Root 20t", "Outbound and return UUID sets", "Status expiry", "Repaired: only double-hit targets qualify");
        frostfall(evidence, "combat_convergence", "Timed return reward", "Catch within 40t after return begins", "Next melee x1.2 for 80t", "Owner outgoing melee hook", "Consumed, expired or lifecycle-cleared", "Redesigned: the window is reachable after a normal five-pulse field");
        frostfall(evidence, "combat_focus", "Boomerang orbit", "Field completion", "Radius 5; damage x.5; duration 20t", "One-turn projectile orbit", "Exclusive capstone; cast-wide hit set", "Repaired: orbit delays return without shrinking the field");
        frostfall(evidence, "combat_release", "Sneak recall", "New sneak input after throw", "Line damage x.8", "Bounded projectile-to-owner line query", "Exclusive capstone; field terminates", "Redesigned: remains reachable while the weapon stack is in flight");
        frostfall(evidence, "transformation_opening", "Pulse-field reach", "Each field pulse", "+1 radius; cap 20", "Config-relative pulse radius and nearest-first cap", "Field execution", "Repaired: default radius 8 resolves to 9 before pulse progression");
        frostfall(evidence, "transformation_cadence", "Pulse damage", "Each field pulse", "Damage x1.1", "Scoped pulse resolver", "Field execution", "Repaired: direct impact is unaffected");
        frostfall(evidence, "transformation_pressure", "Pulse Slowness duration", "Pulse hit", "+20t", "Configured status-duration resolver", "Status expiry", "Repaired: adds to the live configuration without changing direct hits");
        frostfall(evidence, "transformation_reversal", "Pulse pull", "Pulse hit", "Pull x1.25", "Scoped field pull resolver", "Field execution", "Verified: composes with field-form pull multipliers");
        frostfall(evidence, "transformation_reserve", "Pulse ice spires", "Third or sole pulse", "5 targets; damage x.2", "Bounded entity spike bursts", "Once per qualifying pulse", "Repaired: Glacier Break retains its prerequisite");
        frostfall(evidence, "transformation_threshold", "All-pulse control", "Target hit by every selected-form pulse", "Slowness III 60t", "Per-target contiguous pulse count", "Field execution and status expiry", "Redesigned: adapts to five, eight or one pulse");
        frostfall(evidence, "transformation_convergence", "Conditional final shatter", "Final pulse against frozen or Slowness III target", "Damage x1.4", "Per-target final-pulse condition", "Field execution", "Repaired: earlier pulses and unqualified targets are unaffected");
        frostfall(evidence, "transformation_focus", "Avalanche field", "Impact field", "8 pulses over 100t; damage x.65; pull 0", "Deterministic generalized pulse scheduler", "Exclusive capstone; field execution", "Repaired: no negative early damage and direct impact is unchanged");
        frostfall(evidence, "transformation_release", "Glacier Break field", "30t after impact", "1 warned pulse; damage x2.5; pull x2", "Impact warning and sole-pulse scheduler", "Exclusive capstone; field execution", "Repaired: warning, timing and pulse-only scaling are explicit");
        icewhisper(evidence, "signature_opening", "Permafrost pulse damage", "Each aura pulse", "Aura damage x1.1", "Scoped aura damage resolver", "Passive execution per pulse", "Repaired: comet damage is no longer moved by an aura node");
        icewhisper(evidence, "signature_cadence", "Permafrost reach", "Each aura pulse", "+1 aura radius; cap 20", "Config-relative aura radius and target cap", "Passive execution per pulse", "Repaired: adds exactly one block to the live radius and no longer shrinks the comet storm");
        icewhisper(evidence, "signature_pressure", "Permafrost slow duration", "Aura hit", "+20t Slowness", "Aura slow duration resolver", "Status expiry", "Repaired: adds to the 120t base instead of resolving to 60t");
        icewhisper(evidence, "signature_reversal", "Cold Snap dwell pulse", "Third pulse against the same enemy", "Dwell 60t; damage x.3; lockout 60t", "Owner-scoped per-target dwell timers", "Dwell entries expire with the aura window", "Implemented: the node had no dwell tracking and instead tripled comet fall time");
        icewhisper(evidence, "signature_reserve", "Hoarfrost freeze accrual", "Each aura pulse", "+20 frozen ticks; cap 100", "Manager-held freeze floor re-asserted per pulse", "Floor expires with the aura window", "Repaired: 4 ticks per 35-tick pulse could never survive vanilla thaw; the cap is now reachable");
        icewhisper(evidence, "signature_threshold", "Frozen Prey bonus", "Aura hit on a target above 60 frozen ticks", "Damage x1.15", "Pre-damage freeze sample", "Passive execution per pulse", "Implemented: the multiplier had no consumer and the node set 60 comets per wave");
        icewhisper(evidence, "signature_convergence", "Absolute Zero inner ring", "Aura hit within 2 blocks", "Inner radius 2; amplifier 4", "Distance-split slow amplifier", "Status expiry", "Implemented: the amplifier had no consumer and the radius write shrank both the aura and the storm");
        icewhisper(evidence, "signature_focus", "Killing Cold profile", "Each aura pulse", "Radius x.65; damage x1.8; slow radius 2", "Scoped radius, damage and slow-boundary resolvers", "Exclusive capstone; passive execution", "Repaired: the no-slow boundary now exists and comet tuning is untouched");
        icewhisper(evidence, "signature_release", "White Expanse profile", "Each aura pulse and incoming damage", "Radius x2; damage x.55; incoming x.9", "Scoped resolvers and owner-side aura-affected attacker check", "Exclusive capstone; affected entries expire", "Implemented: the defensive half had no consumer at all");
        icewhisper(evidence, "combat_opening", "Comet fall time", "Comet spawn", "Fall -4t", "Config-relative fall resolver", "Comet snapshot", "Verified: the reduction composes against the configured fall time");
        icewhisper(evidence, "combat_cadence", "Comet splash reach", "Comet impact", "+0.5 splash; cap 8", "Config-relative splash radius and splash-only cap", "Once per impact", "Repaired: the cap no longer reaches the Permafrost aura");
        icewhisper(evidence, "combat_pressure", "Comet damage", "Comet impact", "Damage x1.12", "Scoped comet damage resolver", "Storm snapshot", "Repaired: aura damage is unaffected");
        icewhisper(evidence, "combat_reversal", "Wave cadence", "Storm tick", "Interval 12t", "Scoped wave interval resolver", "Storm snapshot", "Verified: 12t against the configured 14t");
        icewhisper(evidence, "combat_reserve", "Twin Wake extra comet", "Every third wave", "Wave interval 3; +1 comet", "Wave-index counter on the storm", "Storm snapshot", "Repaired: the node dropped an extra comet on every wave, not every third");
        icewhisper(evidence, "combat_threshold", "Hunter's Sky retarget", "Wave spawn", "Range 8; 1 comet", "Nearest aura-affected target lookup", "Falls back to normal placement", "Implemented: the range had no consumer and the node capped both splash and aura to one target");
        icewhisper(evidence, "combat_convergence", "Fracture stacks", "Consecutive comet impacts", "Window 60t; +15% per stack; cap 3", "Owner-scoped stack ledger read before it counts the hit", "Stacks expire with the window", "Implemented: the multiplier had no consumer and the node tripled comet fall time");
        icewhisper(evidence, "combat_focus", "Hailstorm profile", "Wave spawn and impact", "Count x2; damage x.55; splash 1.5; +40t", "Scoped count, damage, splash and duration resolvers", "Exclusive capstone; storm snapshot", "Repaired: the count multiplier composes against the configured count and aura damage is untouched");
        icewhisper(evidence, "combat_release", "Extinction Comet profile", "Wave spawn and impact", "Count 1; damage x2.2; splash 4; interval 24t", "Absolute count that suppresses Twin Wake", "Exclusive capstone; storm snapshot", "Repaired: a single comet per wave is authoritative and aura damage is untouched");
        icewhisper(evidence, "transformation_opening", "Frost Ward absorption", "Comet storm activation", "4 absorption for 80t", "Phase4AbsorptionTracker grant", "Tracked, capped and expiring", "Repaired: the grant was permanent and its duration key shortened the Permafrost slow");
        icewhisper(evidence, "transformation_cadence", "Snowblind", "Comet impact", "Blindness 30t", "Mode-gated blind duration key", "Status expiry", "Repaired: the blind duration no longer collapses the aura slow below its pulse period");
        icewhisper(evidence, "transformation_pressure", "Ice Armor", "Projectile damage while Permafrost is active", "Incoming x.85", "Owner-side aura tuning window", "Aura tuning window expiry", "Implemented: the node had no consumer");
        icewhisper(evidence, "transformation_reversal", "Winter Step", "Comet impact within 3 blocks", "Speed I 40t", "Impact-relative owner distance check", "Status expiry", "Implemented: no Speed consumer existed and the radius write shrank both abilities");
        icewhisper(evidence, "transformation_reserve", "Frozen Rebuke", "Melee taken during the storm", "+20 frozen ticks; lockout 30t", "Storm-window damage hook with a recursion guard", "Storm window and lockout", "Implemented: no retaliation existed and the freeze value leaked into the aura");
        icewhisper(evidence, "transformation_threshold", "Veiled Ground", "Aura hit", "Attack speed x.9 for 40t", "Managed attack-speed modifier with a swept deadline", "Modifier released on expiry, death, quit and unload", "Implemented: the node had no consumer");
        icewhisper(evidence, "transformation_convergence", "Last Snow", "Wave spawn below 35% health", "Health 35%; Resistance II 60t", "Once-per-cast redirected comet", "Cast-scoped flag reset at storm start", "Implemented: nothing was gated on health and the node set 35 comets per wave");
        icewhisper(evidence, "transformation_focus", "Snowglobe containment", "Wave spawn", "Resistance I for the storm duration", "Comets confined to the live Permafrost radius", "Exclusive capstone; storm snapshot", "Redesigned: the old text described the storm's existing owner-following behaviour");
        icewhisper(evidence, "transformation_release", "Black Ice", "Comet impact on a frozen target", "Damage x1.6; freezing shattered", "Frozen-target branch that suppresses every defensive grant", "Exclusive capstone; storm snapshot", "Implemented: neither key had a consumer, so the capstone did nothing");
        return evidence;
    }

    private static void phase5Fire(Map<String, MasteryNodeAuditReport.Evidence> evidence,
                                   String profile, String node, String baseMechanic, String trigger,
                                   String tuning, String consumer, String cleanup, String finding) {
        String definitions = switch (profile) {
            case "hearthflame" -> "hearthflame/furnace_chains, furnace_brand";
            case "emberblade" -> "emberblade/shrapnel";
            case "emberlash" -> "emberlash/smoulder, cauterizing";
            default -> throw new IllegalArgumentException("Unknown Phase 5 fire profile: " + profile);
        };
        String displayName = switch (profile) {
            case "hearthflame" -> "Hearthflame";
            case "emberblade" -> "Emberblade";
            case "emberlash" -> "Emberlash";
            default -> profile;
        };
        evidence.put(profile + "/" + profile + "_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, definitions, tuning, consumer, cleanup,
                displayName + " route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
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

    private static void gloampiercer(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                     String baseMechanic, String trigger, String definitions, String tuning,
                                     String consumer, String cleanup, String finding) {
        evidence.put("gloampiercer/gloampiercer_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, definitions, tuning, consumer, cleanup,
                "Gloampiercer route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }

    private static void wraithfang(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                   String baseMechanic, String trigger, String tuning,
                                   String consumer, String cleanup, String finding) {
        evidence.put("wraithfang/wraithfang_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "wraithfang/throw", tuning, consumer, cleanup,
                "Wraithfang route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }

    private static void wraithmaw(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                  String baseMechanic, String trigger, String tuning,
                                  String consumer, String cleanup, String finding) {
        evidence.put("wraithmaw/wraithmaw_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "wraithmaw/muster", tuning, consumer, cleanup,
                "Wraithmaw route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void stormscale(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                   String baseMechanic, String trigger, String tuning,
                                   String consumer, String cleanup, String finding) {
        evidence.put("stormscale/stormscale_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "stormscale/lightning_rod", tuning, consumer, cleanup,
                "Stormscale route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void ionbound(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                 String baseMechanic, String trigger, String tuning,
                                 String consumer, String cleanup, String finding) {
        evidence.put("ionbound_stormscale/ionbound_stormscale_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger,
                "ionbound_stormscale/ion_reserve, ion_crusher, paralysis_beam", tuning, consumer, cleanup,
                "Ionbound Stormscale route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void soulrender(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                   String baseMechanic, String trigger, String tuning,
                                   String consumer, String cleanup, String finding) {
        evidence.put("soulrender/soulrender_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "soulrender/rendmark, reaping, gravebound", tuning, consumer, cleanup,
                "Soulrender route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void soulstalker(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                    String baseMechanic, String trigger, String tuning,
                                    String consumer, String cleanup, String finding) {
        evidence.put("soulstalker/soulstalker_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "soulstalker/hunting_tendrils, gloam_stride", tuning, consumer, cleanup,
                "Soulstalker route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void whisperwind(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                    String baseMechanic, String trigger, String tuning,
                                    String consumer, String cleanup, String finding) {
        evidence.put("whisperwind/whisperwind_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "whisperwind/petal_step, zephyr_rhythm", tuning, consumer, cleanup,
                "Whisperwind route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void dreadwhisper(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                     String baseMechanic, String trigger, String tuning,
                                     String consumer, String cleanup, String finding) {
        evidence.put("dreadwhisper/dreadwhisper_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "dreadwhisper/reaving_front, corrupted_wound", tuning, consumer, cleanup,
                "Dreadwhisper route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void lichblade(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                  String baseMechanic, String trigger, String tuning,
                                  String consumer, String cleanup, String finding) {
        evidence.put("awakened_lichblade/awakened_lichblade_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "awakened_lichblade/soul_anguish_aura, soul_anguish_channel",
                tuning, consumer, cleanup,
                "Awakened Lichblade route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void sunfire(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                String baseMechanic, String trigger, String tuning,
                                String consumer, String cleanup, String finding) {
        evidence.put("sunfire/sunfire_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "sunfire/radiant_standard, regeneration", tuning, consumer, cleanup,
                "Sunfire route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void harbinger(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                  String baseMechanic, String trigger, String tuning,
                                  String consumer, String cleanup, String finding) {
        evidence.put("harbinger/harbinger_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "harbinger/gravity_standard, weakness_omen", tuning, consumer, cleanup,
                "Harbinger route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void hearthflame(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                    String baseMechanic, String trigger, String tuning,
                                    String consumer, String cleanup, String finding) {
        evidence.put("hearthflame/hearthflame_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "hearthflame/furnace_chains, furnace_brand", tuning, consumer, cleanup,
                "Hearthflame route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void emberblade(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                   String baseMechanic, String trigger, String tuning,
                                   String consumer, String cleanup, String finding) {
        evidence.put("emberblade/emberblade_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "emberblade/shrapnel_release", tuning, consumer, cleanup,
                "Emberblade route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void emberlash(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                  String baseMechanic, String trigger, String tuning,
                                  String consumer, String cleanup, String finding) {
        evidence.put("emberlash/emberlash_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "emberlash/smoulder, cauterizing_step", tuning, consumer, cleanup,
                "Emberlash route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void flamewind(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                  String baseMechanic, String trigger, String tuning,
                                  String consumer, String cleanup, String finding) {
        evidence.put("flamewind/flamewind_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "flamewind/flame_seed", tuning, consumer, cleanup,
                "Flamewind route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void molten(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                               String baseMechanic, String trigger, String tuning,
                               String consumer, String cleanup, String finding) {
        evidence.put("molten_edge/molten_edge_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "molten_edge/heat, vent, rupture", tuning, consumer, cleanup,
                "Molten Edge route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void soulpyre(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                 String baseMechanic, String trigger, String tuning,
                                 String consumer, String cleanup, String finding) {
        evidence.put("soulpyre/soulpyre_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "soulpyre/soul_tether, pyre_wisp", tuning, consumer, cleanup,
                "Soul Pyre route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
    private static void mjolnir(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                String baseMechanic, String trigger, String tuning,
                                String consumer, String cleanup, String finding) {
        evidence.put("mjolnir/mjolnir_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "mjolnir/storm", tuning, consumer, cleanup,
                "Mjolnir route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }

    private static void thunderbrand(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                     String baseMechanic, String trigger, String tuning,
                                     String consumer, String cleanup, String finding) {
        evidence.put("thunderbrand/thunderbrand_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "thunderbrand/refresh, thunder_blitz", tuning, consumer, cleanup,
                "Thunderbrand route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }

    private static void tempest(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                String baseMechanic, String trigger, String tuning,
                                String consumer, String cleanup, String finding) {
        evidence.put("tempest/tempest_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "tempest/elemental_cadence, elemental_vortex", tuning, consumer, cleanup,
                "Tempest route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }

    private static void frostfall(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                  String baseMechanic, String trigger, String tuning,
                                  String consumer, String cleanup, String finding) {
        evidence.put("frostfall/frostfall_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "frostfall/frost_fury, glacial_impact", tuning, consumer, cleanup,
                "Frostfall route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }


    private static void icewhisper(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                   String baseMechanic, String trigger, String tuning,
                                   String consumer, String cleanup, String finding) {
        evidence.put("icewhisper/icewhisper_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "icewhisper/permafrost, comet_storm", tuning, consumer, cleanup,
                "Icewhisper route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }

    private static void stormbringer(Map<String, MasteryNodeAuditReport.Evidence> evidence, String node,
                                     String baseMechanic, String trigger, String tuning,
                                     String consumer, String cleanup, String finding) {
        evidence.put("stormbringer/stormbringer_" + node, new MasteryNodeAuditReport.Evidence(
                baseMechanic, trigger, "stormbringer/shock_deflect, chain_tempest", tuning, consumer, cleanup,
                "Stormbringer route and focused regression suite",
                MasteryNodeAuditReport.Verdict.VERIFIED, finding));
    }
}
