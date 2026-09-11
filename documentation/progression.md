# Progression

[Documentation index](README.md)

## Ownership

The server chooses where progression is stored. Changing this needs a **server restart**, and each mode keeps its own separate records, so switching does not transfer or merge anything.

| Mode | Behaviour |
|---|---|
| `WEAPON` (default) | Mastery lives on the item. Give the weapon away and its earned mastery goes with it. |
| `PLAYER` | Mastery is yours. Your XP, skill choices and mastery cooldowns stay with you, shared across your weapons in the same progression group. |

### Progression groups

A **progression group** is a set of related weapon forms that share earned XP and points while keeping their own skill choices. Transforming a weapon inside its group preserves its progression.

For example, the Decaying Relic group covers the Magiscythe, Magiblade and Magispear paths. See [Supported weapons](weapons.md) for the full list.

A weapon with no declared group forms a group of its own.

## Mastery points

By default a weapon starts at **0 points** and can earn up to **18**. The hard ceiling is 64.

Ordinary skill nodes cost 1 point and capstones cost 2.

### What a point costs

The base cost of the point you are earning is `xpBaseRequirement + already-earned-points * xpRequirementGrowth`, which is `100 + n * 35` by default.

After the first `xpCurveUnchangedPoints` points, that base is multiplied by a ramp:

```
1 + (xpCurveFinalMultiplier - 1) * progress^2

progress = (point being earned - unchangedPoints) / (cap - unchangedPoints)
```

The result is rounded up. The ramp is scaled to the **effective** cap, including a datapack override. If the cap is at or below `xpCurveUnchangedPoints`, every cost stays linear.

### Default cost table

The first four points keep their linear cost. After that, costs accelerate toward eight times linear at the cap.

| Point | Linear cost | Actual cost | Running total |
|---:|---:|---:|---:|
| 1 | 100 | 100 | 100 |
| 2 | 135 | 135 | 235 |
| 3 | 170 | 170 | 405 |
| 4 | 205 | 205 | 610 |
| 5 | 240 | 249 | 859 |
| 6 | 275 | 315 | 1,174 |
| 7 | 310 | 410 | 1,584 |
| 8 | 345 | 543 | 2,127 |
| 9 | 380 | 720 | 2,847 |
| 10 | 415 | 949 | 3,796 |
| 11 | 450 | 1,238 | 5,034 |
| 12 | 485 | 1,594 | 6,628 |
| 13 | 520 | 2,025 | 8,653 |
| 14 | 555 | 2,538 | 11,191 |
| 15 | 590 | 3,140 | 14,331 |
| 16 | 625 | 3,840 | 18,171 |
| 17 | 660 | 4,644 | 22,815 |
| 18 | 695 | 5,560 | 28,375 |

A full 18-point weapon costs 28,375 mastery XP at the defaults.

For gentler progression, lower `xpCurveFinalMultiplier`. Set it to 1 for a purely linear curve. Raise `xpCurveUnchangedPoints` to keep more early points at their linear cost.

### Changing the curve or cap

Earned points and stored XP survive a curve change; future XP uses the new curve. Lowering the cap limits available points and reconciles skill allocations, but does not erase saved earned points, so raising the cap again makes them available. Both need a `/reload` to take effect.

## How combat XP is earned

### The enemy's budget

Each enemy is worth a budget, captured when you first damage it:

```
(killBaseXp + ceil(maxHealth / 20) * maxHealthXpPerTwenty) * categoryPercent / 100
```

rounded down and capped at `maximumKillXp`. The category multiplier is 250% for a boss, 125% for a hostile mob, 100% otherwise.

With the defaults, a 20-health hostile mob is worth 15 XP before source rates and farming reductions.

### Damage now, remainder on death

By default **20%** of the budget is paid out progressively as you deal damage, and the rest is shared on death among everyone who contributed in the last **600 ticks** (30 seconds).

A solo kill worth 15 XP therefore pays 3 during damage and 12 on death, before any discounts.

Only health actually lost counts. Absorption and overkill add nothing, and healing cannot refill an enemy's lifetime damage allowance. The damage phase is consumed against at most one full health bar per enemy lifetime.

Small fractional amounts accumulate rather than rounding away. Environmental and ineligible damage reduces the share available to eligible attackers rather than being handed to someone else.

### Attack sources

Each damage source has its own on/off switch and its own rate. **Every applicable switch must be enabled**, or the award is zero. Only **one** rate applies, chosen by this priority:

```
summon > damage over time > projectile > ability > melee
```

A summoned projectile respects both switches but uses the summon rate. The rates are not multiplied together.

Delayed attacks credit the weapon and ownership mode captured when they began, even if you swap weapons before they land. A summon's attacks credit its original source.

### Eligible targets

These award nothing by default: passive mobs, other players, your own tamed pets, teammates, and creative and spectator players.

`allowPassiveMobXp` and `allowPvpXp` lift the first two restrictions. Pet, teammate, creative and spectator exclusions always apply.

### Repeat farming

Within a rolling **1200 tick** (60 second) window, the first **4** encounters with a given enemy type pay full value and later ones pay **20%**. An encounter counts once per player and enemy type, covering both the damage and death payouts, and weapon swaps do not reset it.

Set `antiFarmRepeatedPercent` to 100 to remove the discount.

## Persistence

Progress survives logout and ordinary world saves. Rewards can reach a stored weapon's saved balance, and its display catches up when the weapon becomes accessible again.

Attacks that are cancelled or not saved follow Simply Swords' own lifetime rules and are not recreated after a restart. Effects with no recorded weapon source receive no inferred credit.

All of the numbers above are configurable. See [Configuration](configuration.md).
