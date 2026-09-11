# Progression rules

[Datapacks overview](README.md) | [Documentation index](../README.md)

Override the XP rate, point cap or XP curve for specific weapons.

**Path:** `data/<namespace>/simplyswordsmastery/progression_rules/<name>.json`

```json
{
  "version": 1,
  "priority": 10,
  "selector": {"id": "simplyswords:bramblethorn"},
  "xp_percent": 125,
  "point_cap": 4,
  "curve": {"costs": [100, 135, 170, 205]}
}
```

This gives Bramblethorn 125% combat XP and a four-point cap, with those four explicit costs.

## Fields

| Field | Type | Required | Default | Meaning |
|---|---|---|---|---|
| `version` | integer | yes | | Must be `1`. |
| `priority` | integer | no | `0` | See [which definition wins](README.md#which-definition-wins). |
| `selector` | object | yes | | Item `id`, item `tag`, or progression `group`. |
| `xp_percent` | integer | no | `100` | 0 to 10000. Multiplies combat XP earned by that weapon. |
| `point_cap` | integer | no | server fallback | 1 to 64. |
| `curve` | object | no | server fallback | See below. |

`point_cap` and `curve` inherit from the server fallback **independently**, so you can override one without the other. Neither merges field by field.

## Curves

A curve has two mutually exclusive shapes.

### Explicit costs

```json
{"costs": [100, 135, 170, 205]}
```

1 to 64 entries, each from 1 to 1,000,000,000. The list must cover every point up to the effective cap.

You may not combine `costs` with any of `base`, `growth`, `unchanged_points` or `final_multiplier`.

### Formula

```json
{"base": 100, "growth": 35}
```

| Field | Type | Required | Default | Range |
|---|---|---|---|---|
| `base` | integer | yes | | 1 to 1,000,000,000 |
| `growth` | integer | yes | | 0 to 1,000,000,000 |
| `unchanged_points` | integer | no | `4` | 1 to 64 |
| `final_multiplier` | integer | no | **`1`** | 1 to 100 |

> **Watch the default.** Inside an explicit formula curve, `final_multiplier` defaults to **1**, not to the server's `8`. A curve written as `{"base": 100, "growth": 35}` is therefore **linear**. To reproduce the server's default steep ramp, say so:
>
> ```json
> {"base": 100, "growth": 35, "unchanged_points": 4, "final_multiplier": 8}
> ```

Omit the `curve` field entirely to inherit the server curve, scaled to this rule's effective cap.

The formula is in [Progression](../progression.md#what-a-point-costs). Caps at or below `unchanged_points` stay linear. Every resulting per-point cost must fit in 2,147,483,647.

## Related forms must agree

**All weapon forms in a progression group must resolve to the same cap and cost sequence.** If they do not, the reload is rejected with a message naming the group and the two conflicting forms.

Prefer a group selector when changing the cap or curve for a weapon family:

```json
{
  "version": 1,
  "priority": 10,
  "selector": {"group": "simplyswordsmastery:decaying_relic"},
  "point_cap": 12
}
```

That covers the Magiscythe, Magiblade and Magispear paths together.

`xp_percent` may differ freely per form. Only cap and curve must agree.

Group IDs are listed in [Supported weapons](../weapons.md#progression-groups).

## Effect on existing progress

Earned points and stored XP survive a curve change, and future XP conversion uses the new curve.

Lowering a cap limits available points and reconciles skill allocations. It does not erase saved earned points, so raising the cap again makes them available.
