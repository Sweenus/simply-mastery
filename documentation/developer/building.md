# Building

[Developer notes](README.md) | [Documentation index](../README.md)

## Layout

The repository holds two Gradle projects. Build from inside whichever one you are targeting.

| Directory | Status |
|---|---|
| `mc1.21.1/` | The active implementation. Build here. |
| `mc1.20.1/` | A development scaffold. It ships no mastery data and is outside the current release scope. |

`mc1.21.1` uses Architectury with three source sets: `common`, `fabric` and `neoforge`. Gameplay code belongs in `common`. The loader modules hold entrypoints and metadata only.

## Versions

Dependency versions are pinned in `mc1.21.1/gradle.properties`. That file is the single source of truth, so update it rather than hardcoding a version anywhere else.

At the time of writing it targets Minecraft 1.21.1, Java 21, Architectury API 13.0.11, Fzzy Config 0.7.6, Fabric Loader 0.18.4, NeoForge 21.1.219, and Simply Swords 1.71.0.

## Build

```bash
cd mc1.21.1
./gradlew build
```

Jars land under each loader module's `build/libs`.

## Matched dependencies

This build's Simply Swords integration depends on matching Swords changes. A published Swords version number alone does not identify a compatible build.

Use the project's dependency build workflow to produce the matched artifact set, and verify against the recorded hashes, rather than pulling a published Swords jar by version. The maintainer notes in the internal `docs/` tree cover that workflow and the release gating; they are not part of the published documentation.

## Adding data-driven content

Bundled datapack content lives in `mc1.21.1/common/src/main/resources/data/simplyswordsmastery/`. Anything you add there ships in the jar and is active without pack activation, so use `"priority": -100` to keep it overridable by user datapacks.

Language keys live in `shared-resources/assets/simplyswordsmastery/lang/en_us.json`. Most server config options currently have no translated label; adding them is a good first contribution.

See [Data pipeline](data-pipeline.md) for how that content is loaded and validated.
