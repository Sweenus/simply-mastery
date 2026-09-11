# Skill effects

[Developer notes](README.md) | [Skill effect reference](../datapacks/skill-effects-reference.md) | [Documentation index](../README.md)

A skill effect type is what a mastery node actually does. All of them live in `mastery/effect/` and register into `SkillEffectRegistry`.

## The interface

`SkillEffectType` requires two methods and offers six optional hooks:

```java
Identifier id();
void validate(MasteryProfile.Effect effect, String where, List<String> errors);

default void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {}
default void onKill(SkillEffectContext.Kill context, MasteryProfile.Node node) {}
default void onHeldTick(SkillEffectContext.HeldTick context, MasteryProfile.Node node) {}
default void onDamageReceived(SkillEffectContext.DamageReceived context, MasteryProfile.Node node) {}
default void onProjectile(SkillEffectContext.Projectile context, MasteryProfile.Node node) {}
default void onAbility(SkillEffectContext.Ability context, MasteryProfile.Node node) {}
default boolean needsHeldTick() { return false; }
```

Override `needsHeldTick()` to return `true` if you implement `onHeldTick`, so the runtime knows to tick the weapon.

`AbilitySkillEffectType` extends this with a `tune(...)` method for types that adjust a Simply Swords unique ability rather than acting on their own.

## Adding a type

1. Implement `SkillEffectType`, giving `id()` a stable `Identifier.of(SimplySwordsMastery.MOD_ID, "<path>")`.
2. Implement `validate` with `validateParameters(effect, where, errors, allowedKeys, ranges)`. Pass the exact set of accepted keys and a `Range` per key. Anything outside that set is rejected at reload.
3. Implement the hooks you need.
4. Call `register(...)` from the static block in `SkillEffectRegistry`.

Validation is not optional. A type that skips it will accept any parameter a datapack sends, which then fails at runtime instead of at reload.

## Parameters are integers

`MasteryProfile.Effect.parameters()` is a `Map<String, Integer>`. The codec accepts nothing else.

Encode fractional values by scale, and say so in the key name. The established conventions:

| Suffix | Meaning | Example |
|---|---|---|
| `_tenths` | value divided by 10 | `damage_tenths: 25` is 2.5 damage |
| `_hundredths` | value divided by 100 | `growth_hundredths: 150` is 1.5 |
| `_percent` | a percentage | `damage_percent: 150` is 150% |
| `_ticks` | a duration in ticks | `duration_ticks: 60` is 3 seconds |

Read a value with `parameter(node, "key", fallback)`. The fallback is the runtime default, used when a datapack omits the key. Document any new key and its default in the [skill effect reference](../datapacks/skill-effects-reference.md).

## Cooldowns

Use `context.access().startCooldown(node.id(), context.tick(), cooldownTicks)`. It returns `false` when the effect is still on cooldown, so the usual shape is an early return:

```java
if (!context.access().startCooldown(node.id(), context.tick(), parameter(node, "cooldown_ticks", 100))) return;
```

Cooldown state is keyed by node ID, so two nodes using the same type track independently.

## Cohort types

The nine `cohort/<id>` types pack 27 hard-coded tunings per weapon behind a single `kind` integer:

```
kind = profileIndex * 27 + branchIndex * 9 + slot
```

They require exactly `{"kind": N}` and nothing else. `MasteryCohort` owns the profile ordering, so **inserting a weapon into the middle of a cohort's list shifts every later weapon's `kind` values**. Append instead, or write the matching migration.

This design exists because the built-in trees need per-weapon tuning that would be impractical to express as generic parameters. Do not extend it for new generic behaviour: add a parameterised type instead.

## Guards and gating

`SkillOriginGuard` marks the thread while a mastery effect is running, so effects that deal damage do not recursively re-trigger themselves or feed back into XP attribution as a fresh source. Wrap any effect that causes damage or applies another weapon's behaviour.

The server's `disabledEffects` setting holds effect type IDs. A disabled type does not run even on an unlocked node, so a server owner can switch off one behaviour without disabling the whole profile. Nothing in an effect implementation needs to check this; the runtime does it.
