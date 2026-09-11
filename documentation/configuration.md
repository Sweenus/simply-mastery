# Configuration

[Documentation index](README.md) | [Client settings](client-settings.md) | [Commands](commands.md)

## Where the settings live

Launch the game or server once to generate `config/simplyswordsmastery/server.toml`.

These are the gameplay rules. The server owns them for everyone, and they sync to connected clients. In singleplayer, your own game owns them.

You can edit them in two ways:

- **The Fzzy Config screen in game.** Changes apply immediately for most settings. Editing synced values needs permission level 2. Note that only `maximumEarnedPoints` currently has a translated label, so the rest appear with a placeholder name and description.
- **The TOML file directly**, with the game or server stopped. This is the more readable route given the missing labels. Keep the generated structure and change only the values.

Out-of-range values are corrected back into range when the file loads.

### Restart and reload

| Setting | Needs |
|---|---|
| `progressionOwnership` | A full server or world **restart**. The value is captured once at startup. |
| `maximumEarnedPoints`, `xpBaseRequirement`, `xpRequirementGrowth`, `xpCurveUnchangedPoints`, `xpCurveFinalMultiplier` | A `/reload`. These are baked into the progression snapshot when datapacks load. |
| Everything else | Nothing. Read live at each decision point. |

Edits made directly to the TOML file always need a restart, because the file is only read at startup.

Use server settings for global rules and [datapacks](datapacks/README.md) for specific weapons or enemies. A datapack cap or curve override replaces the global fallback, but global disablement and target exclusions still take precedence.

## Master switch and ownership

| Setting | Default | Range | Meaning |
|---|---|---|---|
| `enabled` | `true` | | Master switch. When off, all XP, unlocks, effects, rewards, the Forge button and the command are disabled. |
| `progressionOwnership` | `"WEAPON"` | `WEAPON`, `PLAYER` | Where progression is stored. Restart required. Each mode keeps separate records, so switching transfers nothing. See [Progression](progression.md#ownership). |

## Point cap and XP curve

| Setting | Default | Range | Meaning |
|---|---|---|---|
| `maximumEarnedPoints` | `18` | 1 to 64 | Fallback cap on earned points per progression record. Datapacks can override it; 64 is the hard ceiling. |
| `verticalSliceStartingPoints` | `0` | 0 to 18 | Points granted to a brand new progression record. Also clamped to the effective cap at runtime. Note this maximum stays 18 even if you raise the cap above it. |
| `xpBaseRequirement` | `100` | 1 to 10000 | XP needed for the first point. |
| `xpRequirementGrowth` | `35` | 0 to 10000 | Extra base XP per already-earned point, before the ramp. |
| `xpCurveUnchangedPoints` | `4` | 1 to 64 | How many early points keep their unramped linear cost. |
| `xpCurveFinalMultiplier` | `8` | 1 to 100 | Multiplier applied to the final point's cost by the ramp. Set 1 for a linear curve. |

The formula and the resulting cost table are in [Progression](progression.md#what-a-point-costs).

Existing worlds pick up these settings at their defaults. Datapacks with an explicit curve keep it; ones that omit a curve inherit these values.

## Which attacks earn XP

| Source | Switch | Rate |
|---|---|---|
| Melee | `meleeXpEnabled` | `meleeXpPercent` |
| Abilities | `abilityXpEnabled` | `abilityXpPercent` |
| Projectiles | `projectileXpEnabled` | `projectileXpPercent` |
| Summons | `summonXpEnabled` | `summonXpPercent` |
| Damage over time | `damageOverTimeXpEnabled` | `damageOverTimeXpPercent` |

Every switch defaults to `true`. Every rate defaults to `100` and accepts 0 to 10000, so 200 doubles the share and 0 awards nothing.

**All applicable switches must allow the attack** or it awards nothing. Only **one** rate applies, chosen in this order: summon, damage over time, projectile, ability, melee. A summoned projectile respects both switches but uses the summon rate; the rates are never multiplied together.

## Damage and death split

| Setting | Default | Range | Meaning |
|---|---|---|---|
| `damageXpEnabled` | `true` | | Pay part of the enemy budget progressively as damage is dealt. When off, the whole budget waits for death. |
| `damageXpBudgetPercent` | `20` | 0 to 100 | Share of the budget reserved for the damage phase. Consumed against at most one full health bar per enemy lifetime. |
| `contributionSharingEnabled` | `true` | | Share the remaining death reward among recent contributors. When off, only an attributed killing blow pays. |
| `contributionWindowTicks` | `600` | 1 to 72000 | How far back death sharing looks. 600 ticks is 30 seconds. |

Rates and farming discounts apply after shares are allocated. Excluded or discounted portions are not handed to other players. Fractional XP is stored rather than rounded away.

## Enemy rewards and farming

| Setting | Default | Range | Meaning |
|---|---|---|---|
| `killBaseXp` | `10` | 0 to 1000 | Flat base budget before health and category scaling. |
| `maxHealthXpPerTwenty` | `2` | 0 to 100 | Extra XP per 20 maximum health, rounded up. |
| `maximumKillXp` | `100` | 1 to 1000 | Cap on the formula-derived budget. An explicit datapack `base_xp` can exceed it. |
| `hostilePercent` | `125` | 0 to 1000 | Multiplier for hostile enemies. |
| `bossPercent` | `250` | 0 to 1000 | Multiplier for bosses, used instead of the hostile multiplier. |
| `allowPassiveMobXp` | `false` | | Allow otherwise eligible passive mobs to award XP. |
| `allowPvpXp` | `false` | | Allow otherwise eligible player targets. Creative and spectator targets stay excluded. |
| `excludedEntityIds` | `["simplyswords:training_dummy"]` | valid identifiers | Entity types that never award XP or first-kill rewards. |
| `antiFarmWindowTicks` | `1200` | 0 to 72000 | Repeated-encounter window. 1200 ticks is 60 seconds. |
| `antiFarmFullValueKills` | `4` | 1 to 100 | Encounters per player and enemy type in that window that pay full value. This counts encounters, not only killing blows. |
| `antiFarmRepeatedPercent` | `20` | 0 to 100 | Value paid for later encounters. Set 100 to remove the discount. |

The budget formula is in [Progression](progression.md#the-enemys-budget). 20 ticks is 1 second at normal server speed.

## Reward categories

| Setting | Default | Meaning |
|---|---|---|
| `firstKillRewardsEnabled` | `true` | Allow first-kill bonuses. |
| `masteryConsumablesEnabled` | `true` | Allow consumable claims at the Forge. |
| `advancementRewardsEnabled` | `true` | Allow pending advancement claims. Turning this off pauses claims without deleting earned entitlements. |

These control both the bundled starter rewards and any custom [datapack definitions](datapacks/README.md). See [Rewards](rewards.md).

## Respec

| Setting | Default | Range | Meaning |
|---|---|---|---|
| `respecEnabled` | `true` | | Allow resetting the current tree's allocations. |
| `respecUsesTag` | `false` | | Pay with an item tag instead of a single item ID. |
| `respecCostItem` | `"simplyswords:runic_tablet"` | identifier | Payment item when tag mode is off. |
| `respecCostTag` | `"c:runic_tablets"` | identifier | Payment tag when tag mode is on. Do not write a leading `#`. |
| `respecCostCount` | `3` | 1 to 64 | Payment items taken from the player's inventory. |
| `creativeRespecIsFree` | `true` | | Waive respec payment for creative players. This does not waive consumable payments. |

## Skill restrictions

| Setting | Default | Meaning |
|---|---|---|
| `disabledProfiles` | `[]` | Mastery profile IDs that cannot progress or unlock. Saved allocations remain visible and respec still works when enabled. Profile IDs are listed in [Supported weapons](weapons.md). |
| `disabledEffects` | `[]` | Skill **effect type** IDs whose effects will not run, even on an unlocked node. These are not weapon IDs. All 77 are listed in the [skill effect reference](datapacks/skill-effects-reference.md). |
| `stormstepDurationTicks` | `40` | Fallback Stormstep speed duration, 5 to 200. A skill's own `duration_ticks` takes precedence. |
| `stormstepAmplifier` | `1` | Fallback Stormstep amplifier, 0 to 4. 1 means Speed II. A skill's own `amplifier` takes precedence. |
