# Rewards

[Documentation index](README.md)

Besides combat XP, mastery can be granted by three kinds of reward. All three are defined by datapacks, and a starter set ships in the mod.

Open **Mastery** at the Runic Forge, then press **Rewards**.

## What ships by default

| Reward | Value |
|---|---|
| First qualifying Wither kill | 100 XP |
| First qualifying Ender Dragon kill | 200 XP |
| Monster Hunter advancement (`minecraft:adventure/kill_a_mob`) | 50 XP to claim |
| Into Fire advancement (`minecraft:nether/obtain_blaze_rod`) | 100 XP to claim |
| Consume one Empowered Remnant | 1000 XP, repeatable |

Claims are restricted to weapons in the bundled `simplyswordsmastery:starter_reward_weapons` tag, which covers 56 Simply Swords items including transformation items. The weapon must still have an eligible mastery profile.

These rewards use the server's normal XP curve and cap. Server owners can change or remove any of them: see [bundled content](datapacks/bundled-content.md).

## How each kind works

| Kind | How you receive it |
|---|---|
| First-kill bonus | Defeat the configured enemy with a qualifying contribution. Granted automatically, once per weapon or group and enemy type, or once per player and enemy type in player ownership mode. Respecs and transformations do not reset it. |
| Consumable | Carry the configured items, insert an eligible weapon, then open **Mastery**, **Rewards**, **Claim**. |
| Advancement | Complete a configured advancement after its reward is installed. Insert an eligible weapon and claim the pending reward under **Rewards**. You do not need to be holding the weapon when you complete the advancement. |

## The Rewards panel

The panel lists reward amounts, consumable costs, and the reason a claim is unavailable. Use the arrow buttons to page through it.

Consumable payment comes from **your inventory**, not the Forge's gem or tablet slots, and it can span multiple stacks. Creative mode does not waive consumable payment.

**The entire reward must fit** before payment is taken. If the weapon does not have enough remaining progression capacity for the full amount, the claim stays unavailable rather than paying out partially.

## Rules worth knowing

- Advancements completed **before** a reward was installed are not rewarded retroactively. Revoking and re-completing an advancement cannot repeat its reward.
- Pending rewards survive death and logout, and keep the amount and weapon restrictions they had when earned, even if the datapack later changes.
- A claim uses the ownership mode active at the moment of claiming.
- Disabling a reward category on the server pauses claims. It does not delete entitlements you have already earned.
- Overlapping reward definitions do not stack. Exactly one wins per context.

See [Configuration](configuration.md) for `firstKillRewardsEnabled`, `masteryConsumablesEnabled` and `advancementRewardsEnabled`.
