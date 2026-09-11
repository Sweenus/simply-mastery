# Consumables and advancement rewards

[Datapacks overview](README.md) | [Documentation index](../README.md)

Both of these appear in the Rewards panel at the Runic Forge. See [Rewards](../rewards.md) for the player-facing side.

## Consumables

Trade items from your inventory for mastery.

**Path:** `data/<namespace>/simplyswordsmastery/mastery_consumables/<name>.json`

```json
{
  "version": 1,
  "ingredient": {"id": "minecraft:amethyst_shard"},
  "quantity": 4,
  "weapon": {"id": "simplyswords:bramblethorn"},
  "reward": {"xp": 100}
}
```

Carry four Amethyst Shards, insert Bramblethorn, then open **Mastery**, **Rewards**, **Claim**.

| Field | Type | Required | Default | Meaning |
|---|---|---|---|---|
| `version` | integer | yes | | Must be `1`. |
| `priority` | integer | no | `0` | |
| `ingredient` | object | yes | | Item `id` or item `tag`. `group` is not allowed here. |
| `quantity` | integer | yes | | 1 to 2304. Items consumed per claim. |
| `weapon` | object | yes | | Item `id`, item `tag`, or `group`. |
| `reward` | object | yes | | `{"xp": N}` or `{"points": N}`. |

Payment comes from the player's inventory and can span several stacks. It does not use the Forge's gem or tablet slots, and creative mode does not waive it.

Consumables require `masteryConsumablesEnabled` in the [server configuration](../configuration.md#reward-categories).

## Advancement rewards

Grant a claimable reward when a player completes an advancement.

**Path:** `data/<namespace>/simplyswordsmastery/advancement_rewards/<name>.json`

```json
{
  "version": 1,
  "advancement": "minecraft:adventure/kill_a_mob",
  "weapon": {"id": "simplyswords:bramblethorn"},
  "reward": {"xp": 100}
}
```

| Field | Type | Required | Default | Meaning |
|---|---|---|---|---|
| `version` | integer | yes | | Must be `1`. |
| `priority` | integer | no | `0` | |
| `advancement` | identifier | yes | | One exact advancement ID. It must already exist when the pack loads, or the reload is rejected. |
| `weapon` | object | yes | | Item `id`, item `tag`, or `group`. |
| `reward` | object | yes | | `{"xp": N}` or `{"points": N}`. |

Completing the advancement creates a **pending** reward. The player claims it later by inserting an eligible weapon and using the Rewards panel. They do not need to be holding the weapon when they complete the advancement.

Advancement rewards require `advancementRewardsEnabled` in the [server configuration](../configuration.md#reward-categories). Turning that off pauses claims without deleting pending entitlements.

### Rules

- Advancements completed **before** the definition was installed are not rewarded retroactively.
- Revoking and re-completing an advancement cannot repeat its reward.
- Pending rewards keep the amount and weapon restrictions they had when earned, even if the datapack later changes.
- Pending rewards survive death and logout.
- A claim uses the ownership mode active at claim time.

## The reward object

Both registries use the same reward shape. It contains **exactly one** of:

| Field | Range |
|---|---|
| `xp` | 1 upward |
| `points` | 1 to 64 |

Never both, and never zero. Use `{"points": 1}` to grant a mastery point directly instead of XP.

**The entire reward must fit** before payment is taken or a pending reward is consumed. If the weapon has less remaining capacity than the full amount, the claim stays unavailable.

Weapon `xp_percent` combat multipliers do **not** scale consumable, advancement or first-kill amounts.
