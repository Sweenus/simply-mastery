# Installation

[Documentation index](README.md)

Simply Swords: Mastery targets **Minecraft 1.21.1** and **Java 21**, on Fabric or NeoForge.

## Required mods

| Dependency | Minimum version | Notes |
|---|---|---|
| Simply Swords | 1.71.0-1.21.1 | Provides the weapons and abilities that mastery modifies. |
| Simply Tooltips | 0.1.5 | Required, not optional. Supplies weapon theming and the cooldown tooltip formatter. |
| Architectury API | 13.0.11 | |
| Fzzy Config | 0.7.6 | Provides the configuration screen and the config sync. |
| Fabric Loader | 0.18.4 | Fabric only. Fabric API is also required. |
| NeoForge | 21.1.219 | NeoForge only. |

Also install whatever Simply Swords itself declares as a dependency for your loader.

## Where the mod goes

Install Simply Swords: Mastery on **both the client and the server**. The mastery tree is drawn on the client but every unlock is validated and stored by the server.

Use one loader's jars per installation. Do not mix the Fabric and NeoForge builds.

Client and server must run the same build. The two sides exchange mastery profiles over a versioned packet, and a mismatch shows up as missing or empty trees.

## Files the mod creates

Launch once to generate:

- `config/simplyswordsmastery/server.toml`, the gameplay rules. See [Configuration](configuration.md).
- `config/simplyswordsmastery/client.toml`, your own display settings. See [Client settings](client-settings.md).

In singleplayer your own game acts as the server, so `server.toml` applies to you.

## Release candidate note

This build's Simply Swords integration depends on matching Swords changes. A version number alone does not identify a compatible published Swords build. Use the matched artifacts produced by the project's dependency build workflow until a release pair is published.

The `mc1.20.1` directory in this repository is a development scaffold. It ships no mastery data and is outside the scope of these guides.
