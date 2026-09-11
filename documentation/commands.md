# Commands

[Documentation index](README.md) | [Configuration](configuration.md)

There is one command tree. It requires **operator permission level 2**, and it does nothing while `enabled` is `false` in the [server configuration](configuration.md).

```text
/simplyswordsmastery grant xp     <amount> [<players>]
/simplyswordsmastery grant points <amount> [<players>]
/simplyswordsmastery set   xp     <amount> [<players>]
/simplyswordsmastery set   points <amount> [<players>]
/simplyswordsmastery clear                 [<players>]
```

`grant` requires an amount of 1 or more. `set` accepts 0 or more.

## What it acts on

Each command affects the target player's **main-hand weapon** under the active ownership mode.

Omit the player selector to target yourself. The server console must always supply one.

The command reports an error if the target's hand is empty, or if the held item resolves to no mastery profile.

## Behaviour

| Command | Effect |
|---|---|
| `grant xp` | Adds mastery XP, converted to points using the active curve. |
| `grant points` | Adds earned points, respecting the effective cap. |
| `set xp` | Replaces stored mastery XP. |
| `set points` | Replaces earned points, respecting the effective cap. |
| `clear` | Resets XP to 0, earned points to the configured starting amount, and the visible tree's allocations. |

`clear` does **not** reset first-kill history and does not make already-claimed advancement rewards earnable again.

To change only skill choices, use the Forge respec instead. See [The Mastery screen](mastery-screen.md#buttons).

Feedback is broadcast to operators, and the command returns the number of players actually changed.
