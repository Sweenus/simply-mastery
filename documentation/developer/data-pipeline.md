# Data pipeline

[Developer notes](README.md) | [Documentation index](../README.md)

How datapack content becomes live game state.

## Injection point

`mixin/DataPackContentsMasteryMixin` adds `MasteryProfileReloadListener` to `DataPackContents.getContents()`, so it runs as an ordinary server datapack reloader, and calls `publish()` at the tail of `DataPackContents.refresh`.

## Reload sequence

`MasteryProfileReloadListener.reload(ResourceManager)` runs these in order:

1. `MasteryProfileRegistry.prepare(manager)` reads `weapon_skills` and validates each profile.
2. `RewardDefinitionLoader.prepare(manager)` reads the four reward registries.
3. `RewardCatalogFactory.create(...)` builds the catalog of valid targets from the item and entity registries, the freshly loaded tags via `TagManagerLoader`, the freshly loaded advancements via `ServerAdvancementLoader`, and the progression groups derived from the accepted profiles.
4. `ResolvedRewardDefinitions.prepare(...)` cross-validates every definition against that catalog and computes the effective policy per progression group.
5. `ProfileSyncPacket.encode(candidate)` rejects a candidate that exceeds the sync payload bound.

Only if all five succeed does the new snapshot replace the old one.

## All or nothing

Any failure at any step rejects the **entire** reload, skill trees and rewards together. The previously installed snapshot and its epoch are retained.

Errors are logged under `Simply Swords: Mastery/Profiles` and `Simply Swords: Mastery/Rewards`, prefixed with the offending resource ID. The rejection message is `Rejected mastery reload; retaining installed definitions: ...`.

On first startup a failure leaves the empty snapshot in place, so there are no profiles and no rewards at all until a clean reload.

This is deliberate. A partially applied snapshot could leave a progression group with inconsistent caps, which is exactly what step 4 exists to prevent.

## Why curve settings need a reload

`ProgressionPolicy.defaults()` reads `maximumEarnedPoints` and the four `xpCurve` options from `MasteryServerConfig` and bakes them into the snapshot as the fallback policy. It is called from the reload listener, not at each read site.

That is why those five options need a `/reload` while the rest of the server config is read live. If you add a config option that participates in the policy, it inherits the same requirement, so document it.

## Overriding and merging

Both loaders use `ResourceManager.findResources`, which yields one `Resource` per path: the one from the highest-priority pack. A datapack file at the same path **wholly replaces** the built-in one. There is no field-level merging anywhere in this system.

`pack.mcmeta` `filter.block` is the supported way to delete a bundled definition.

Built-in mastery trees are different. They are not resources at all: `BuiltInFamilyProfiles.addMissing(accepted)` injects them with `putIfAbsent` **after** datapack documents are parsed. A datapack profile therefore shadows a built-in of the same ID but cannot delete it.

## Definition precedence

`RewardDefinitionLoader.precedence` picks one winner per reward context:

1. `priority` descending. Built-ins use `-100`.
2. Selector specificity descending: `id` 2, `tag` 1, `group` 0.
3. Resource ID ascending.

Consumables rank by ingredient specificity, advancement rules by weapon specificity. Rewards never stack.

## Client sync

The accepted snapshot is encoded to canonical JSON and sent as `ProfileSyncPacket`, carrying `MasteryNetwork.PROTOCOL_VERSION` and the snapshot epoch.

Client and server builds must agree on the protocol version. The canonical registry is capped at 1,048,576 bytes, which bounds that packet.

## Adding a registry

If you add a sixth data-driven registry, wire it into `MasteryProfileReloadListener.reload` rather than registering a separate reload listener. Sharing the listener is what keeps the snapshot atomic and lets `RewardCatalogFactory` see consistent tags and advancements.
