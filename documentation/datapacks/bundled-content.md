# Bundled content

[Datapacks overview](README.md) | [Documentation index](../README.md)

The mod ships a small starter reward pack inside its own jar, under `data/simplyswordsmastery/simplyswordsmastery/`. It is active immediately and needs no pack activation.

Every bundled definition uses `"version": 1` and `"priority": -100`, so any rule of yours at the default priority of 0 outranks it.

## What ships

| File | What it does |
|---|---|
| `progression_rules/starter_weapons.json` | 100% weapon XP for the starter weapon tag. Server cap and curve. |
| `entity_rewards/wither.json` | 100 XP first-kill bonus for the Wither. |
| `entity_rewards/ender_dragon.json` | 200 XP first-kill bonus for the Ender Dragon. |
| `mastery_consumables/empowered_remnant.json` | One Empowered Remnant for 1000 XP, repeatable. |
| `advancement_rewards/monster_hunter.json` | `minecraft:adventure/kill_a_mob`, claim 50 XP. |
| `advancement_rewards/into_fire.json` | `minecraft:nether/obtain_blaze_rod`, claim 100 XP. |

Plus the item tag `tags/item/starter_reward_weapons.json`, which gates all of them except the entity rewards. It uses `"replace": false` and lists 56 Simply Swords items. See [Supported weapons](../weapons.md#reward-eligible-items).

The bundled files in full:

```json
{
  "version": 1,
  "priority": -100,
  "ingredient": {"id": "simplyswords:empowered_remnant"},
  "quantity": 1,
  "weapon": {"tag": "simplyswordsmastery:starter_reward_weapons"},
  "reward": {"xp": 1000}
}
```

```json
{
  "version": 1,
  "priority": -100,
  "selector": {"id": "minecraft:wither"},
  "first_kill": {"xp": 100}
}
```

The entity rewards set no `base_xp` and no `boss`, so those enemies keep their normal combat budget and their vanilla boss classification.

Global feature switches, exclusions and ownership settings still apply to all of them. See [Configuration](../configuration.md#reward-categories).

## Replace a definition

Use the **same namespace and path** in your world datapack. A file at the same path wholly replaces the bundled one; fields do not merge, so write a complete definition.

Create `data/simplyswordsmastery/simplyswordsmastery/mastery_consumables/empowered_remnant.json`:

```json
{
  "version": 1,
  "priority": -100,
  "ingredient": {"id": "simplyswords:empowered_remnant"},
  "quantity": 2,
  "weapon": {"tag": "simplyswordsmastery:starter_reward_weapons"},
  "reward": {"xp": 1500}
}
```

Your pack must sit above the mod's own resources. `/datapack enable "file/mastery_rewards" last` gives a folder pack the highest priority. Then `/reload`.

## Outrank a definition

Instead of replacing the file, add your own in **your** namespace with a matching selector and `priority` of 0 or higher. It wins wherever it matches, because the bundled rules sit at -100.

This is usually simpler than a replacement, and it survives a change to the bundled files.

## Change which weapons qualify

Override the item tag at `data/simplyswordsmastery/tags/item/starter_reward_weapons.json`:

```json
{
  "replace": true,
  "values": ["simplyswords:bramblethorn"]
}
```

That restricts the bundled progression rule, consumable and advancement rewards to Bramblethorn alone. Use `"replace": false` to add items on top of the existing list instead, which is standard vanilla tag behaviour.

Adding an item to this tag does not give it a mastery profile. First-kill definitions select enemies and are not restricted by this tag.

## Remove a definition

To disable a whole category, use the server's `firstKillRewardsEnabled`, `masteryConsumablesEnabled` or `advancementRewardsEnabled` switches.

To remove one specific bundled file, block its path from a higher-priority datapack:

```json
{
  "pack": {
    "pack_format": 48,
    "description": "Disable the bundled Empowered Remnant reward"
  },
  "filter": {
    "block": [
      {
        "namespace": "^simplyswordsmastery$",
        "path": "^simplyswordsmastery/mastery_consumables/empowered_remnant[.]json$"
      }
    ]
  }
}
```

Filters hide matching resources from lower-priority packs. Change the path to remove a different bundled definition.

There is no `enabled` field, and a zero-value reward is rejected. Neither is a supported way to switch something off.

## Effect on pending rewards

Already-completed advancements are never rewarded retroactively.

Pending advancement rewards keep their original amounts and eligible weapons through a replacement, a tag change or a removal. To pause them, disable advancement claims globally instead.
