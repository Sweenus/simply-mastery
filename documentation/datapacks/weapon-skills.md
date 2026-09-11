# Custom skill trees

[Datapacks overview](README.md) | [Skill effect reference](skill-effects-reference.md) | [Documentation index](../README.md)

A `weapon_skills` profile defines a complete mastery tree: its branches, its nodes, what each node does, and which weapons it applies to.

**Path:** `data/<namespace>/simplyswordsmastery/weapon_skills/<anything>.json`

Any namespace, any nested path. Unlike the reward registries, **the file path is not the identity**. The profile's identity is the `id` field inside the document.

## Replacing a built-in tree

The 54 shipped trees are generated in Java, not shipped as JSON, so there is no file to copy and edit.

Declare a profile with the same `id` as a built-in and yours **completely replaces** it. Nothing merges, so you must declare all of its nodes yourself. Built-in trees are only added for IDs that no datapack supplied, which means you can shadow one but you cannot delete it.

The `weapon_skills` directory in the mod's own jar is empty.

## Root fields

| Field | Type | Required | Default | Meaning |
|---|---|---|---|---|
| `schema` | integer | yes | | Must be `1`. |
| `id` | identifier | yes | | The profile's identity. |
| `profile_version` | integer | yes | | 1 or higher. **Values of 2 or more turn on four extra rules**, see below. |
| `selectors` | array | yes | | 1 to 128 entries. Which weapons use this tree. |
| `progression_group` | identifier | no | the `id` | The XP and point banking group shared across related forms. |
| `branches` | array | yes | | **Exactly 3.** |
| `nodes` | array | yes | | 1 to 128. |
| `migrations` | array | no | `[]` | Up to 64. |

## Selectors

Each entry holds **exactly one** of `item`, `form_stage` or `form_family`, plus an optional `priority`.

| Field | Type | Default | Range |
|---|---|---|---|
| `item` | identifier | | one of the three |
| `form_stage` | identifier | | one of the three |
| `form_family` | identifier | | one of the three |
| `priority` | integer | `0` | -10000 to 10000 |

At runtime the resolution order is `form_stage` exact match, then `item` exact match, then `form_family`. Within a kind the highest `priority` wins, and **a tie matches nothing**.

Two different profiles tying at the top priority for the same selector is a hard error that rejects the reload.

Duplicate `(target, priority)` pairs inside one profile are also rejected.

## Branches

Exactly three, no more and no fewer.

| Field | Type | Required | Meaning |
|---|---|---|---|
| `id` | string | yes | 1 to 64 characters, not blank, unique in the profile. |
| `name` | string | yes | A translation key for the display name. |
| `color` | string | yes | `"#RRGGBB"` or `"RRGGBB"`. **Exactly six hex digits.** |

The built-in trees use the branch IDs `signature`, `combat` and `transformation`. Keep those if you want [cohort effect](skill-effects-reference.md#cohort-tuners) `kind` values to line up.

## Nodes

| Field | Type | Required | Default | Meaning |
|---|---|---|---|---|
| `id` | string | yes | | 1 to 64 characters, unique in the profile. |
| `branch` | string | yes | | Must match a declared branch `id`. |
| `x` | number | yes | | 0.0 to 1.0. Built-ins run from `0.90` at the root down to `0.16` at capstones. |
| `y` | number | yes | | 0.0 to 1.0. Built-in branch rows sit at `0.22`, `0.50` and `0.78`. |
| `cost` | integer | yes | | 1 to 21 mastery points. |
| `capstone` | boolean | no | `false` | |
| `choice_group` | string | no | `""` | The mutual-exclusion key. |
| `requires` | string array | no | `[]` | Up to 16 prerequisite node IDs. |
| `effect` | object | yes | | See [skill effects](skill-effects-reference.md). |
| `name` | string | no | `skill.simplyswordsmastery.<node id>` | Translation key, up to 160 characters. |
| `description` | string | no | `skill.simplyswordsmastery.<node id>.description` | Translation key, up to 160 characters. |
| `icon` | identifier | no | | Texture. `textures/` is prepended and `.png` appended if missing. |

## Structural rules

These are strict, and getting one wrong rejects the whole reload.

- **A capstone and a `choice_group` imply each other.** A capstone must declare a non-empty `choice_group`, and a non-capstone must not declare one.
- **Each branch has exactly one root**, meaning exactly one node with an empty `requires`.
- **Each branch has exactly two capstones**, and both must share one identical non-empty `choice_group`. They are the mutually exclusive pair.
- **Both capstones must be reachable** from their branch root through the prerequisite graph.
- **Prerequisites may not cross branches.** Every `requires` entry must name an existing node in the same branch.
- **The prerequisite graph must be acyclic.**
- **Capstones may not overlap.** Any two capstones anywhere in the profile must be at least 0.05 apart in layout space, that is `dx^2 + dy^2 >= 0.0025`.

## The `profile_version` cliff

Setting `profile_version` to 2 or higher adds four more requirements:

- `effect.type` may not be `simplyswordsmastery:none`.
- Every node must declare an `icon`.
- Each branch must have **7 to 9** non-capstone nodes.
- The cheapest route to a capstone, summing `cost` along its prerequisite closure, must total **6 to 8** points.

The built-in trees use `profile_version` 2 or higher, so a replacement that keeps the same version must satisfy all four. Use `profile_version: 1` for a simpler custom tree.

## Migrations

When you change a tree's shape, bump `profile_version` and describe what moved.

| Field | Type | Required | Default | Meaning |
|---|---|---|---|---|
| `from_version` | integer | yes | | 1 or higher. Unique across this profile's migrations. |
| `to_version` | integer | yes | | Greater than `from_version`, and no greater than `profile_version`. |
| `renamed_nodes` | map | no | `{}` | Old node ID to new node ID. |
| `removed_node_refunds` | map | no | `{}` | Node ID to points refunded, 0 to 21. |

```json
"migrations": [
  {
    "from_version": 1,
    "to_version": 2,
    "renamed_nodes": {"old_opening": "new_opening"},
    "removed_node_refunds": {"retired_node": 1}
  }
]
```

> **Back up the world first.** Bumping `profile_version` without a matching migration entry resets players' allocations for that tree.

## Worked example

A minimal valid tree with `profile_version: 1`. Three branches, one root and two exclusive capstones each.

```json
{
  "schema": 1,
  "id": "my_pack:example_blade",
  "profile_version": 1,
  "selectors": [
    {"item": "simplyswords:bramblethorn", "priority": 10}
  ],
  "branches": [
    {"id": "signature", "name": "branch.my_pack.example_blade.signature", "color": "#4FA3FF"},
    {"id": "combat", "name": "branch.my_pack.example_blade.combat", "color": "#FF8A4F"},
    {"id": "transformation", "name": "branch.my_pack.example_blade.transformation", "color": "#B478FF"}
  ],
  "nodes": [
    {
      "id": "sig_root", "branch": "signature", "x": 0.90, "y": 0.22, "cost": 1,
      "effect": {"type": "simplyswordsmastery:echo_strike", "parameters": {"damage_tenths": 25}}
    },
    {
      "id": "sig_edge", "branch": "signature", "x": 0.16, "y": 0.165, "cost": 2,
      "capstone": true, "choice_group": "sig_capstone", "requires": ["sig_root"],
      "effect": {"type": "simplyswordsmastery:cleaving_echo", "parameters": {"hits": 3, "radius_tenths": 40}}
    },
    {
      "id": "sig_surge", "branch": "signature", "x": 0.16, "y": 0.275, "cost": 2,
      "capstone": true, "choice_group": "sig_capstone", "requires": ["sig_root"],
      "effect": {"type": "simplyswordsmastery:combo_surge", "parameters": {"hits": 4, "amplifier": 1}}
    },
    {
      "id": "cbt_root", "branch": "combat", "x": 0.90, "y": 0.50, "cost": 1,
      "effect": {"type": "simplyswordsmastery:battle_flow", "parameters": {"mode": 0, "amplifier": 1}}
    },
    {
      "id": "cbt_guard", "branch": "combat", "x": 0.16, "y": 0.445, "cost": 2,
      "capstone": true, "choice_group": "cbt_capstone", "requires": ["cbt_root"],
      "effect": {"type": "simplyswordsmastery:guarded_recovery", "parameters": {"duration_ticks": 60}}
    },
    {
      "id": "cbt_finish", "branch": "combat", "x": 0.16, "y": 0.555, "cost": 2,
      "capstone": true, "choice_group": "cbt_capstone", "requires": ["cbt_root"],
      "effect": {"type": "simplyswordsmastery:finishing_strike", "parameters": {"health_percent": 35}}
    },
    {
      "id": "trn_root", "branch": "transformation", "x": 0.90, "y": 0.78, "cost": 1,
      "effect": {"type": "simplyswordsmastery:stormstep", "parameters": {"duration_ticks": 60, "amplifier": 1}}
    },
    {
      "id": "trn_mend", "branch": "transformation", "x": 0.16, "y": 0.725, "cost": 2,
      "capstone": true, "choice_group": "trn_capstone", "requires": ["trn_root"],
      "effect": {"type": "simplyswordsmastery:soul_mend", "parameters": {"heal_tenths": 30}}
    },
    {
      "id": "trn_leech", "branch": "transformation", "x": 0.16, "y": 0.835, "cost": 2,
      "capstone": true, "choice_group": "trn_capstone", "requires": ["trn_root"],
      "effect": {"type": "simplyswordsmastery:leeching_strike", "parameters": {"heal_tenths": 12}}
    }
  ]
}
```

Node names and descriptions are omitted here, so they fall back to `skill.simplyswordsmastery.sig_root` and `skill.simplyswordsmastery.sig_root.description`. Supply your own `name` and `description` keys, and the matching language file, for readable text.

## Limits

| Limit | Value |
|---|---|
| Profiles in total | 256 |
| Nodes per profile | 128 |
| Selectors per profile | 128 |
| Migrations per profile | 64 |
| Prerequisites per node | 16 |
| Parameters per effect | 16 |
| Bytes per file | 262,144 |
| Bytes for the whole registry, encoded | 1,048,576 |

The registry limit also bounds the packet sent to clients, so an oversized registry is rejected even if every individual file is valid.

Duplicate profile IDs and ambiguous selectors are both hard errors.
