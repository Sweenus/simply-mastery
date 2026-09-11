# Entity rewards

[Datapacks overview](README.md) | [Documentation index](../README.md)

Change what an enemy is worth, and grant a one-off bonus for the first time a weapon or player kills one.

**Path:** `data/<namespace>/simplyswordsmastery/entity_rewards/<name>.json`

```json
{
  "version": 1,
  "selector": {"id": "minecraft:wither"},
  "base_xp": 100,
  "boss": true,
  "first_kill": {"points": 1}
}
```

This sets the Wither's base combat budget to 100 XP and grants one mastery point for a qualifying first kill.

## Fields

| Field | Type | Required | Default | Meaning |
|---|---|---|---|---|
| `version` | integer | yes | | Must be `1`. |
| `priority` | integer | no | `0` | See [which definition wins](README.md#which-definition-wins). |
| `selector` | object | yes | | Entity `id` or entity `tag`. `group` is not allowed here. |
| `base_xp` | integer | no | | 0 upward. **Replaces** the formula-derived budget. |
| `xp_percent` | integer | no | | 0 to 10000. **Multiplies** the formula-derived budget. |
| `boss` | boolean | no | vanilla classification | Overrides whether this entity counts as a boss. |
| `first_kill` | object | no | | A one-off bonus. `{"xp": N}` or `{"points": N}`. |

`base_xp` and `xp_percent` are mutually exclusive. Omit both to keep the normal formula.

## Combat budget

The default budget formula is in [Progression](../progression.md#the-enemys-budget).

`base_xp` replaces that formula's result and is **not** limited by the server's `maximumKillXp`. `xp_percent` scales the formula result, which is still capped.

Source rates, the damage and death split, and anti-farm discounts all still apply on top.

`boss` changes which category multiplier applies and affects eligibility, but it cannot bypass global exclusions such as `excludedEntityIds`.

## First-kill bonuses

The first-kill bonus is separate from the damage and death split, and it respects the point cap.

It is granted once per weapon or progression group and entity type, or once per player and entity type under player ownership. History is shared across related forms.

Respecs, transformations, and removing and restoring the rule do not reset it. Neither does the `/simplyswordsmastery clear` command.

First-kill bonuses require `firstKillRewardsEnabled` in the [server configuration](../configuration.md#reward-categories).

## Rewards

A `first_kill` object contains **exactly one** of:

| Field | Range |
|---|---|
| `xp` | 1 upward |
| `points` | 1 to 64 |

Never both.
