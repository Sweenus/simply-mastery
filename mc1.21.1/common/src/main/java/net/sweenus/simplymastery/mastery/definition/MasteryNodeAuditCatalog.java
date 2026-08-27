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
}
