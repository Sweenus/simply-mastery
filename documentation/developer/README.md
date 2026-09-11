# Developer notes

[Documentation index](../README.md)

Notes for contributors. For using the mod, start at the [documentation index](../README.md).

- [Data pipeline](data-pipeline.md): how datapack content reaches the game.
- [Skill effects](skill-effects.md): adding a new effect type.
- [Building](building.md): project layout and build tasks.

## Repository layout

| Path | Contents |
|---|---|
| `mc1.21.1/` | The active implementation. Architectury, with `common`, `fabric` and `neoforge` source sets. |
| `mc1.20.1/` | A development scaffold. It ships no mastery data and is outside the current release scope. |
| `shared-resources/` | Shared assets, including `assets/simplyswordsmastery/lang/en_us.json`. |
| `documentation/` | These guides. |
| `docs/` | Internal working notes, excluded from git. |

## Package map

All paths are relative to `mc1.21.1/common/src/main/java/net/sweenus/simplyswordsmastery/`.

| Package | Responsibility |
|---|---|
| `client/` | Client entrypoint and the cooldown tooltip formatter. |
| `client/mastery/` | The mastery and rewards screens. |
| `client/mastery/ui/` | Layout, palette, node state, drawing and animation helpers. |
| `client/mastery/theme/` | Simply Tooltips theme bridge, detected by reflection. |
| `config/` | `MasteryConfig` registers `MasteryServerConfig` and `MasteryClientConfig` with Fzzy Config. |
| `mastery/combat/` | Encounter tracking and the damage and death XP split. |
| `mastery/definition/` | Profile codec, validator, registry, reload listener, and the code-generated built-in trees. |
| `mastery/effect/` | Skill effect types and their runtime. |
| `mastery/network/` | Packets and the unlock service. `MasteryNetwork.PROTOCOL_VERSION` is the client and server compatibility gate. |
| `mastery/progression/` | The ledger, ownership modes, reward service and the admin command. |
| `mastery/reward/` | Reward definition codecs, loader, catalog, resolution and the progression policy. |
| `mastery/state/` | Item components, portfolios, runtime state and migration. |
| `mixin/` | Injection points, including the datapack reload hook and the Runic Forge screen button. |

## Where behaviour is decided

| Question | File |
|---|---|
| What does a mastery point cost? | `mastery/reward/ProgressionPolicy.java` |
| Which source rate applies? | `mastery/reward/CombatSourcePolicy.java` |
| How is an enemy's budget split? | `mastery/combat/CombatRewards.java` |
| Is this node unlockable? | `mastery/network/UnlockRules.java` |
| Is this profile valid? | `mastery/definition/MasteryProfileValidator.java` |
| What do the built-in trees look like? | `mastery/definition/BuiltInFamilyProfiles.java` |
