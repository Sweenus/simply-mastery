# Datapacks

[Documentation index](../README.md)

Datapacks can add rewards, override progression for specific weapons and enemies, and replace whole skill trees.

They belong on the server or in your singleplayer world. Players do not need their own copy.

## The five registries

| Directory under `data/<namespace>/simplyswordsmastery/` | What it defines |
|---|---|
| `progression_rules/` | [Per-weapon XP rate, point cap and curve](progression-rules.md) |
| `entity_rewards/` | [Enemy XP values and first-kill bonuses](entity-rewards.md) |
| `mastery_consumables/` | [Items claimable for mastery at the Forge](consumables-and-advancements.md) |
| `advancement_rewards/` | [Advancements that grant a claimable reward](consumables-and-advancements.md) |
| `weapon_skills/` | [Complete mastery trees](weapon-skills.md) |

## Create a pack

Put this in your world's `datapacks` directory. On a dedicated server that is `<level-name>/datapacks`; in singleplayer it is `saves/<world>/datapacks`.

```text
mastery_rewards/
  pack.mcmeta
  data/
    my_rewards/
      simplyswordsmastery/
        progression_rules/
          bramblethorn.json
        entity_rewards/
          wither.json
        mastery_consumables/
          amethyst.json
        advancement_rewards/
          first_battle.json
```

`pack.mcmeta` for Minecraft 1.21.1:

```json
{
  "pack": {
    "pack_format": 48,
    "description": "My mastery rewards"
  }
}
```

`my_rewards` is your namespace. Use lowercase letters, numbers and underscores. Include only the files you actually want.

Run `/reload` after installing or editing the pack, and `/datapack list` to confirm it is enabled. `/datapack enable "file/mastery_rewards" last` gives your pack the highest priority.

## Reloading is all or nothing

The five registries load together as one snapshot. **A single invalid file rejects the entire reload**, skill trees included, and the previously installed definitions stay active.

The error names the offending resource. Check the server log for entries from `Simply Swords: Mastery/Profiles` and `Simply Swords: Mastery/Rewards`, fix the file, then `/reload` again.

If the very first load fails, there are no definitions at all until you fix the pack.

## Selectors

Every reward registry uses the same selector shape. It must contain **exactly one** of these fields:

| Field | Matches | Example |
|---|---|---|
| `id` | One exact item or entity ID | `{"id": "minecraft:wither"}` |
| `tag` | Members of an existing item or entity tag | `{"tag": "my_rewards:mastery_weapons"}` |
| `group` | A mastery progression group. Weapons only. | `{"group": "simplyswordsmastery:decaying_relic"}` |

Do not write a leading `#` on a tag value. The tag must exist at reload time, though it may be empty.

A consumable `ingredient` and an entity reward `selector` cannot use `group`.

Group IDs are listed in [Supported weapons](../weapons.md#progression-groups).

## Which definition wins

Definitions never merge and rewards never stack. Exactly one wins per context, chosen in this order:

1. Higher `priority`. The default is `0`; the bundled definitions all use `-100`.
2. More specific selector: `id` beats `tag` beats `group`.
3. Resource ID alphabetically. A file at `my_rewards/amethyst.json` has resource ID `my_rewards:amethyst`.

Consumables compare **ingredient** specificity. Advancement rewards compare **weapon** specificity.

That ranking is separate from pack priority. Pack priority decides which file wins at the same path; definition priority decides between different matching definitions. A file at the same path as a bundled one **wholly replaces** it, with no field-level merging.

See [bundled content](bundled-content.md) for how to replace, outrank or remove the shipped definitions.

## Validation

Files are checked strictly before they are parsed:

- Unknown top-level keys are rejected.
- A JSON `null` anywhere is rejected.
- Any string value over 256 characters is rejected.
- `version` must be exactly `1` on every reward definition.

Cross-references are checked too. An unknown advancement, a missing tag, or a group whose forms disagree on cap and curve will all reject the reload.

## Limits

| Limit | Value |
|---|---|
| Definitions per reward registry | 4,096 |
| Bytes per reward file | 65,536 |
| Characters per identifier or string value | 256 |
| Mastery profiles in total | 256 |
| Bytes per `weapon_skills` file | 262,144 |
| Bytes for the whole profile registry, encoded | 1,048,576 |

The profile registry limit also bounds the packet sent to clients.
