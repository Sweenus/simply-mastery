# The Mastery screen

[Documentation index](README.md)

Place a supported weapon in the Runic Forge and press **Mastery**.

## Layout

Every mastery tree has **three branches** and **27 nodes**, nine per branch.

| Branch | Focus |
|---|---|
| Signature | The weapon's unique ability. |
| Combat | General melee behaviour. |
| Transformation | The weapon's transformed or final form. |

Each branch runs right to left from a single root node. Seven ordinary nodes cost **1 point** each. The branch ends in **two capstones**, each costing **2 points**, and the two are mutually exclusive. Taking one permanently blocks the other until you respec.

The cheapest route to a capstone is seven points.

## Node glyphs

| Glyph | Meaning |
|---|---|
| Padlock | Locked. Its prerequisites are not met yet. |
| Plus | Unlockable now. |
| Check | Unlocked. |
| Diamond | An unlocked capstone. |
| Plus with a slash through it | Blocked by a conflicting choice. You already took the other capstone in this pair. |

A dashed link means the connection is not yet available. A solid link means the parent is unlocked.

## Controls

| Input | Action |
|---|---|
| Left click a node | Select it and show its details. |
| Left click again | Unlock it. |
| Arrow keys | Move to the neighbouring node. |
| Enter or Space | Unlock the selected node. |
| Escape | Close the screen, or dismiss a confirmation. |
| Scroll wheel | Zoom, on narrow screens only. Zoom is limited to 1.0 to 1.35. |
| Middle or right drag | Pan, while zoomed in. |

Capstones **always** ask for confirmation. Set `confirmUnlocks` in [client settings](client-settings.md) to be asked about ordinary nodes too.

## Buttons

| Button | What it does |
|---|---|
| Rewards | Opens the [rewards panel](rewards.md). |
| Respec | Refunds every point spent in the current tree. |
| Back | Returns to the Runic Forge. |
| Close | Closes the screen. |

## Reading a node

Selecting a node shows its name, its description, its point cost, and why you cannot take it yet if that applies. Common reasons:

- You do not have enough available points.
- Its prerequisites are not unlocked.
- The route to it costs more than your remaining points allow.
- It conflicts with a capstone you already took.
- The server has disabled this weapon's mastery profile.

The header shows spent points, available points, and either your current mastery XP against the next point or a "capped" marker.

## Cooldown previews

Tooltips show ability cooldowns with your unlocked mastery adjustments already applied, before any compatibility reductions. Abilities whose real cooldown depends on context or on a mode label their base value explicitly.

## Artwork and language

English text ships with the mod. Node glyphs are drawn procedurally rather than from per-node artwork. Datapack profiles may supply their own node icons. See [custom skill trees](datapacks/weapon-skills.md).
