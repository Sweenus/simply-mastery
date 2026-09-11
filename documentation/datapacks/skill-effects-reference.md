# Skill effect reference

[Custom skill trees](weapon-skills.md) | [Datapacks overview](README.md) | [Documentation index](../README.md)

Every node in a mastery tree declares one effect:

```json
"effect": {
  "type": "simplyswordsmastery:echo_strike",
  "parameters": {"damage_tenths": 25, "cooldown_ticks": 40}
}
```

| Field | Type | Required | Default |
|---|---|---|---|
| `type` | identifier | yes | | Must be one of the 77 registered types below. |
| `parameters` | object | no | `{}` | Up to 16 entries. |

## Parameter rules

- **Values must be integers.** No decimals, booleans or strings.
- Fractional quantities are encoded as **tenths**. `damage_tenths: 25` means 2.5 damage. `radius_tenths: 40` means a radius of 4.0 blocks. `growth_hundredths` is in hundredths.
- Every parameter is optional. Each type falls back to its own runtime default when a key is absent. The one exception is the nine cohort types, which require exactly one key.
- Unknown keys are rejected, and each value is range-checked. Either error rejects the whole reload.

All type IDs are in the `simplyswordsmastery` namespace.

Any of these IDs can also be listed in the server's `disabledEffects` setting to stop that effect running. See [Configuration](../configuration.md#skill-restrictions).

## Generic effects

These work on any weapon.

| Type | Trigger | Parameters, with range and default |
|---|---|---|
| `none` | nothing | none accepted |
| `stormstep` | on attack | `duration_ticks` 1-1200 (server `stormstepDurationTicks`), `amplifier` 0-10 (server `stormstepAmplifier`), `cooldown_ticks` 1-72000 (1) |
| `echo_strike` | on attack | `damage_tenths` 1-200 (20), `cooldown_ticks` 1-72000 (20) |
| `combo_surge` | on attack | `hits` 2-20 (3), `window_ticks` 1-1200 (60), `duration_ticks` 1-1200 (60), `amplifier` 0-10 (0), `cooldown_ticks` 1-72000 (100) |
| `combo_strike` | on attack | `hits` 2-20 (3), `window_ticks` 1-1200 (60), `damage_tenths` 1-200 (10), `cooldown_ticks` 1-72000 (60) |
| `battle_flow` | on attack | `hits` 2-20 (3), `window_ticks` 1-1200 (60), `duration_ticks` 1-1200 (50), `amplifier` 0-4 (0), `cooldown_ticks` 1-72000 (100), `mode` 0-4 (0) |
| `cleaving_echo` | on attack | `hits` 2-20 (3), `window_ticks` 1-1200 (60), `damage_tenths` 1-200 (10), `radius_tenths` 10-120 (35), `cooldown_ticks` 1-72000 (100) |
| `hindering_strike` | on attack | `duration_ticks` 1-1200 (40), `amplifier` 0-4 (0), `cooldown_ticks` 1-72000 (100), `mode` 0-3 (0) |
| `finishing_strike` | on attack | `health_percent` 5-80 (30), `damage_tenths` 1-200 (15), `cooldown_ticks` 1-72000 (80) |
| `leeching_strike` | on attack | `heal_tenths` 1-200 (5), `cooldown_ticks` 1-72000 (120) |
| `soul_mend` | on kill | `heal_tenths` 1-200 (10), `cooldown_ticks` 1-72000 (100) |
| `kill_flow` | on kill | `duration_ticks` 1-1200 (60), `amplifier` 0-4 (0), `cooldown_ticks` 1-72000 (100), `mode` 0-4 (0) |
| `counterstrike` | on damage taken | `damage_tenths` 1-200 (10), `cooldown_ticks` 1-72000 (120) |
| `guarded_recovery` | on damage taken | `duration_ticks` 1-1200 (40), `amplifier` 0-4 (0), `cooldown_ticks` 1-72000 (160) |

### `mode` values

For the beneficial effects `battle_flow` and `kill_flow`:

| `mode` | Status effect |
|---:|---|
| 0 | Speed |
| 1 | Haste |
| 2 | Resistance |
| 3 | Regeneration |
| 4 | Strength |

For `hindering_strike`:

| `mode` | Status effect |
|---:|---|
| 0 | Slowness |
| 1 | Weakness |
| 2 | Glowing |
| 3 | Poison |

## Stormbreak ability tuners

These 27 types tune the Stormbreak unique ability and do nothing on a weapon that does not have it.

| Type | Parameters and ranges |
|---|---|
| `stormbreak_conduit` | `width_tenths` 0-320, `slowness_ticks` 1-1200, `amplifier` 0-10 |
| `slipstream` | `distance_percent` 1-1000, `speed_percent` 1-1000 |
| `crosswind` | none accepted |
| `capacitor` | `per_hit_percent` 0-1000, `cap_percent` 0-1000 |
| `storm_chaser` | `cooldown_percent` 0-1000 |
| `flashguard` | `duration_ticks` 1-1200, `amplifier` 0-10 |
| `afterimage` | `duration_ticks` 1-1200, `damage_percent` 0-1000 |
| `eye_of_storm` | `radius_percent` 1-1000, `damage_percent` 0-1000 |
| `thunderhead` | `radius_tenths` 0-320 |
| `static_reserve` | `sprint_chance` 0-100 |
| `charged_pursuit` | `duration_ticks` 1-1200, `amplifier` 0-10 |
| `building_voltage` | `chance_per_stack` 0-100, `max_stacks` 1-20, `window_ticks` 1-1200 |
| `live_wire` | `damage_percent` 0-1000, `cooldown_ticks` 1-72000 |
| `feedback_loop` | `window_ticks` 1-1200, `damage_percent` 0-1000 |
| `quickening_current` | `extension_ticks` 1-1200, `max_remaining_ticks` 1-1200 |
| `unbroken_pace` | `duration_ticks` 1-1200, `speed_amplifier` 0-10, `resistance_amplifier` 0-10, `cooldown_ticks` 1-72000 |
| `perpetual_motion` | `window_ticks` 1-1200, `cooldown_percent` 0-1000 |
| `flashover` | `targets` 1-16, `range_tenths` 5-320, `damage_percent` 0-1000 |
| `ionize` | `duration_ticks` 1-1200 |
| `arc_lash` | `damage_percent` 0-1000, `cooldown_ticks` 1-72000, `range_tenths` 5-320 |
| `pressure_drop` | `duration_ticks` 1-1200, `amplifier` 0-10 |
| `updraft` | `knock_up_percent` 0-1000, `slow_falling_ticks` 1-1200 |
| `fulmination` | `damage_percent` 0-1000, `radius_tenths` 5-320, `max_targets` 1-64 |
| `stormshield` | `duration_ticks` 1-1200, `strong_threshold` 1-64 |
| `reverberation` | `delay_ticks` 1-1200, `damage_percent` 0-1000 |
| `judgment_bolt` | `damage_percent` 0-1000 |
| `supercell` | `duration_ticks` 1-1200, `interval_ticks` 1-1200, `damage_percent` 0-1000, `pull_tenths` 0-160 |

## Brimstone Claymore ability tuners

These 27 types tune the Brimstone Claymore's unique ability.

| Type | Parameters and ranges |
|---|---|
| `sulfurous_edge` | `chance_bonus` 0-100 |
| `scorching_brand` | `fire_ticks` 0-72000, `burning_damage_percent` 0-1000 |
| `blast_furnace` | `radius_tenths` 0-640, `damage_percent` 0-1000 |
| `kindling_blows` | `chance_per_stack` 0-100, `max_stacks` 1-20, `window_ticks` 1-1200 |
| `flashpoint` | `required_stacks` 1-20 |
| `cinder_scatter` | `count` 1-16, `range_tenths` 1-640, `damage_percent` 0-1000, `fire_ticks` 0-72000 |
| `backdraft` | `pull_tenths` 0-160, `slowness_ticks` 0-72000 |
| `chain_reaction` | `damage_percent` 0-1000, `radius_tenths` 1-640, `max_detonations` 1-64 |
| `crucible_strike` | `radius_percent` 0-1000, `primary_damage_percent` 0-1000 |
| `lengthened_chain` | `range_tenths` 0-1280 |
| `furnace_bellows` | `interval_ticks` 1-72000 |
| `stoked_furnace` | `growth_hundredths` 0-6400, `max_radius_tenths` 0-640 |
| `shackling_heat` | `pull_tenths` 0-160, `slowness_ticks` 0-72000 |
| `overpressure` | `per_pulse_percent` 0-1000, `cap_percent` 0-1000 |
| `snapback` | `damage_percent` 0-1000, `radius_tenths` 1-640 |
| `molten_wake` | `duration_ticks` 1-72000, `interval_ticks` 1-72000, `damage_percent` 0-1000, `min_move_tenths` 1-640 |
| `executioners_drop` | `duration_ticks` 1-72000, `final_damage_percent` 0-1000 |
| `perpetual_furnace` | `duration_ticks` 1-72000, `start_percent` 0-1000, `per_pulse_percent` 0-1000, `cap_percent` 0-1000, `final_damage_percent` 0-1000 |
| `cinder_mantle` | `duration_ticks` 1-72000, `amplifier` 0-10 |
| `tempered_flesh` | `padding_ticks` 0-1200 |
| `heat_sink` | `max_stacks` 1-10, `window_ticks` 1-1200, `duration_ticks` 1-1200 |
| `furnace_reprisal` | `damage_percent` 0-1000, `cooldown_ticks` 1-72000, `fire_ticks` 0-72000 |
| `forged_resolve` | `health_percent` 1-100, `resistance_ticks` 1-1200, `absorption_ticks` 1-1200 |
| `ashen_step` | `duration_ticks` 1-1200, `cooldown_ticks` 1-72000 |
| `bulwark_pulse` | `target_threshold` 1-64, `duration_ticks` 1-1200 |
| `walking_furnace` | `radius_percent` 0-1000, `pull_tenths` 0-160 |
| `last_reprisal` | `health_percent` 1-100, `final_damage_percent` 0-1000, `duration_ticks` 1-1200 |

## Cohort tuners

The 54 built-in trees share nine cohort effect types. Each accepts **exactly** one parameter, `kind`, and nothing else. Omitting it is an error.

```json
"effect": {"type": "simplyswordsmastery:cohort/fire_forge", "parameters": {"kind": 31}}
```

`kind` is a packed index that selects one of 27 hard-coded tunings per weapon:

```
kind = profileIndex * 27 + branchIndex * 9 + slot
```

- `profileIndex` is the weapon's position in its cohort. See [Supported weapons](../weapons.md#cohorts).
- `branchIndex` is 0 for `signature`, 1 for `combat`, 2 for `transformation`.
- `slot` is 0 to 8 within the branch.

| Type | Valid `kind` range |
|---|---|
| `cohort/abyssal_spectral` | 0-161 |
| `cohort/storm_soul` | 0-161 |
| `cohort/long_path_final_forms` | 0-80 |
| `cohort/fire_forge` | 0-161 |
| `cohort/storm_frost_water` | 0-188 |
| `cohort/nature_swarm` | 0-107 |
| `cohort/death_shadow_blood` | 0-161 |
| `cohort/arcane_cosmic` | 0-188 |
| `cohort/martial_command_eldritch` | 0-134 |

Each `kind` maps to a specific tuning delta chosen for that weapon and slot. These are **not** generically configurable from a datapack. Use a generic effect or an ability tuner if you want to define your own behaviour.
