# Supported weapons

[Documentation index](README.md)

The mod ships **54 mastery profiles**. This page is the lookup table for the profile IDs, progression groups and cohort indexes that datapacks and server settings refer to.

Every profile ID is in the `simplyswordsmastery` namespace, so `bramblethorn` is written `simplyswordsmastery:bramblethorn` in `disabledProfiles` or as a `weapon_skills` `id`.

## Progression groups

Weapons in the same group share earned XP and points while keeping separate skill choices. Transforming inside a group preserves progression.

| Group | Profiles in it |
|---|---|
| `simplyswordsmastery:watcher_claymore` | `watcher_claymore`, `the_devourer` |
| `simplyswordsmastery:stormscale` | `stormscale`, `ionbound_stormscale` |
| `simplyswordsmastery:lichblade` | `awakened_lichblade` |
| `simplyswordsmastery:relic` | `sunfire`, `harbinger` |
| `simplyswordsmastery:soulrender` | `soulrender`, `soulstalker` |
| `simplyswordsmastery:whisperwind` | `whisperwind`, `dreadwhisper` |
| `simplyswordsmastery:wickpiercer` | `wickpiercer`, `gloampiercer` |
| `simplyswordsmastery:wraithfang` | `wraithfang`, `wraithmaw` |
| `simplyswordsmastery:decaying_relic` | `magiscythe`, `magiblade`, `magispear` |

Every other profile forms a group of its own, named after the profile. A datapack rule targeting `{"group": "simplyswordsmastery:bramblethorn"}` therefore matches just Bramblethorn.

**All forms in a group must resolve to the same point cap and XP curve.** Prefer a `group` selector when changing either. See [progression rules](datapacks/progression-rules.md).

## Cohorts

Nine cohorts share a skill effect implementation. The **order matters**: a profile's position in its cohort is the `profileIndex` used by the `cohort/<id>` effect type's `kind` parameter. See the [skill effect reference](datapacks/skill-effects-reference.md#cohort-tuners).

| Cohort effect type | Profiles, in index order (0 upward) |
|---|---|
| `simplyswordsmastery:cohort/abyssal_spectral` | `watcher_claymore`, `the_devourer`, `wickpiercer`, `gloampiercer`, `wraithfang`, `wraithmaw` |
| `simplyswordsmastery:cohort/storm_soul` | `stormscale`, `ionbound_stormscale`, `soulrender`, `soulstalker`, `whisperwind`, `dreadwhisper` |
| `simplyswordsmastery:cohort/long_path_final_forms` | `awakened_lichblade`, `sunfire`, `harbinger` |
| `simplyswordsmastery:cohort/fire_forge` | `hearthflame`, `emberblade`, `emberlash`, `flamewind`, `molten_edge`, `soulpyre` |
| `simplyswordsmastery:cohort/storm_frost_water` | `stormbringer`, `mjolnir`, `thunderbrand`, `tempest`, `frostfall`, `icewhisper`, `livyatan` |
| `simplyswordsmastery:cohort/nature_swarm` | `bramblethorn`, `waxweaver`, `hiveheart`, `chompolotl` |
| `simplyswordsmastery:cohort/death_shadow_blood` | `toxic_longsword`, `soulkeeper`, `soulstealer`, `twisted_blade`, `shadowsting`, `bloodwake` |
| `simplyswordsmastery:cohort/arcane_cosmic` | `arcanethyst`, `stars_edge`, `magiscythe`, `magiblade`, `magispear`, `enigma`, `caelestis` |
| `simplyswordsmastery:cohort/martial_command_eldritch` | `watching_warglaive`, `ribboncleaver`, `riftmane`, `dawnquiver`, `dreadtide` |

`storms_edge` and `brimstone_claymore` are not in a cohort. They use the dedicated Stormbreak and Brimstone ability tuner effects instead.

## Reward-eligible items

The bundled item tag `simplyswordsmastery:starter_reward_weapons` gates the [bundled rewards](rewards.md). It uses `"replace": false` and lists 56 `simplyswords:` items, including transformation and dormant forms that have no profile of their own:

`arcanethyst`, `awakened_lichblade`, `bloodwake`, `bramblethorn`, `brimstone_claymore`, `caelestis`, `chompolotl`, `dawnquiver`, `dormant_relic`, `dreadwhisper`, `emberblade`, `emberlash`, `enigma`, `flamewind`, `frostfall`, `gloampiercer`, `harbinger`, `hearthflame`, `hiveheart`, `icewhisper`, `ionbound_stormscale`, `livyatan`, `magiblade`, `magiscythe`, `magispear`, `mjolnir`, `molten_edge`, `ribboncleaver`, `riftmane`, `righteous_relic`, `shadowsting`, `slumbering_lichblade`, `soulkeeper`, `soulpyre`, `soulrender`, `soulstalker`, `soulstealer`, `stars_edge`, `stormbringer`, `storms_edge`, `stormscale`, `sunfire`, `tainted_relic`, `tempest`, `the_devourer`, `thunderbrand`, `toxic_longsword`, `twisted_blade`, `waking_lichblade`, `watcher_claymore`, `watching_warglaive`, `waxweaver`, `whisperwind`, `wickpiercer`, `wraithfang`, `wraithmaw`

Adding an item to this tag makes it eligible for those rewards. It does **not** give the item a mastery profile. Dreadtide has a profile but is not in the tag.

## Branches

Every profile has the same three branch IDs: `signature`, `combat` and `transformation`. Each branch has its own display name per weapon, shown in game.
