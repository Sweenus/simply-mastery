# Client settings

[Documentation index](README.md) | [Configuration](configuration.md)

These are your own settings. They affect presentation only, never earned progression or server rules, and they are read live so changes apply straight away.

They live in `config/simplyswordsmastery/client.toml`, or in the Fzzy Config screen in game.

| Setting | Default | Range | Meaning |
|---|---|---|---|
| `reducedMotion` | `false` | | Turns off drift, pulsing and entrance animation in the mastery screens. |
| `motionIntensity` | `1.0` | 0.0 to 1.0 | Scales animation amplitude. Ignored when `reducedMotion` is on. |
| `backgroundDim` | `0.72` | 0.2 to 0.95 | How strongly the screen dims the world behind it. |
| `uiSoundVolume` | `0.6` | 0.0 to 1.0 | Hover, select and unlock sound volume. 0 is silent. |
| `auraParticleDensity` | `0.75` | 0.0 to 1.0 | Reserved. Nothing reads this value in the current build, so changing it has no effect. |
| `zoomSensitivity` | `1.0` | 0.25 to 2.0 | Scroll zoom speed. The resulting zoom is still limited to 1.0 to 1.35. |
| `confirmUnlocks` | `false` | | Ask before unlocking an ordinary node. Capstones always ask regardless of this setting. |
| `themedFromTooltips` | `true` | | Colour the mastery interface from the weapon's Simply Tooltips theme. Off gives the default chrome with no accent blending. |
| `branchAccentBlend` | `0.5` | 0.0 to 1.0 | How far branch colours shift toward the weapon theme. 0 keeps the profile's own colours. Only applies when `themedFromTooltips` is on. |

## Accessibility

- Motion sensitivity: set `reducedMotion` to `true`. To soften rather than remove the animation, leave it off and lower `motionIntensity` instead.
- Low vision: raise `backgroundDim` toward 0.95 for stronger contrast against the world behind the screen.
- Quiet play: set `uiSoundVolume` to 0.
- Accidental clicks: set `confirmUnlocks` to `true` so every unlock asks first.

The mastery screen also supports keyboard navigation and narration. See [The Mastery screen](mastery-screen.md#controls).
