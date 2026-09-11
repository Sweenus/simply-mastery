# Troubleshooting

[Documentation index](README.md)

## The Mastery button is missing or greyed out

- Is `enabled` set to `true` in the [server configuration](configuration.md)?
- Does this weapon have a mastery profile? Check [Supported weapons](weapons.md).
- Are you holding an item on the cursor? Put it down first.
- Is Simply Swords: Mastery installed on both the client and the server, at the same build? A protocol mismatch leaves trees empty.

## No mastery XP

- Is the weapon already at its point cap?
- Is the target eligible? Passive mobs, other players, your own pets, teammates and the training dummy award nothing by default.
- Are **all** applicable source switches enabled? A summoned projectile needs both the summon and projectile switches. See [Progression](progression.md#attack-sources).
- Have you killed this enemy type repeatedly in the last 60 seconds? The fifth and later encounters pay 20% by default.
- Did something else land the damage? Only health actually lost to eligible sources counts, and environmental damage reduces the share.

## The Rewards list is empty

- Insert a weapon that is eligible for the reward, not just any mastery weapon. The bundled rewards need an item in `simplyswordsmastery:starter_reward_weapons`.
- For a consumable, carry the payment items in your inventory.
- For an advancement, complete it **after** the reward was installed. Advancements finished earlier are not rewarded retroactively.
- Check `firstKillRewardsEnabled`, `masteryConsumablesEnabled` and `advancementRewardsEnabled` on the server.

## Cannot claim a reward

Read the reason shown in the panel. The usual causes:

- Not enough payment items. Payment comes from your inventory, not the Forge slots, and creative mode does not waive it.
- The full reward will not fit in the weapon's remaining capacity. The whole amount must fit before payment is taken.
- A reward switch is turned off on the server.

Reopen the panel if the weapon or the server rules changed while it was open.

## A datapack change did nothing

- Run `/reload`, then `/datapack list` to confirm the pack is enabled.
- **Check the server log.** The reload is all or nothing: a single invalid file rejects every registry, skill trees included, and the previous definitions stay active. The log names the offending resource under `Simply Swords: Mastery/Profiles` or `Simply Swords: Mastery/Rewards`.
- Is your pack above the mod's resources in priority? Use `/datapack enable "file/<pack>" last`.
- Is your rule being outranked? Definition priority, then selector specificity, then alphabetical resource ID decide the winner. See [which definition wins](datapacks/README.md#which-definition-wins).

Common rejections: a `version` that is not `1`, an unknown field, a `null` value, a selector with more or fewer than one of `id`/`tag`/`group`, an advancement that does not exist, or forms in one progression group that disagree on cap and curve.

## Ownership did not change

`progressionOwnership` is captured once at startup. **Restart the server or world.**

Each mode keeps its own separate records, so switching does not transfer or merge progress. Switching back restores what that mode had saved.

## Cap and curve changes did nothing

`maximumEarnedPoints` and the four `xpCurve` settings are baked into the progression snapshot when datapacks load. Run `/reload` after changing them.

Edits made directly to the TOML file always need a restart, because the file is only read at startup.

## Config options have no names in the settings screen

Only `maximumEarnedPoints` currently has a translated label among the server options. The rest show a placeholder name and description. Edit `config/simplyswordsmastery/server.toml` directly instead, with the server stopped. See [Configuration](configuration.md).

## A client setting has no effect

`auraParticleDensity` is reserved. Nothing reads it in the current build. Every other client setting applies live.
