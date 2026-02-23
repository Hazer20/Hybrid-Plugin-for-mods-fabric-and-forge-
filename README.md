# SanguineCompatibilityEngine

Production-ready PaperMC plugin for **automatic datapack + resourcepack transpilation** from **1.20.4** to **1.21.8**.

- Plugin name: `SanguineCompatibilityEngine`
- Author: `Hazer_2_0`
- Build system: **Maven**
- Java: **21**

## What the engine does

On server startup:

1. Creates directories:
   - `plugins/SanguineCompatibilityEngine/input_datapack/`
   - `plugins/SanguineCompatibilityEngine/input_resourcepack/`
   - `plugins/SanguineCompatibilityEngine/generated_datapack/`
   - `plugins/SanguineCompatibilityEngine/generated_resourcepack/`
2. Unpacks zip/folder sources with `ZipInputStream`.
3. Runs full conversion pipeline:
   - datapack command AST parser + command transpiler;
   - JSON converters for predicates/loot/advancements/damage_type;
   - resourcepack `models/item/*` override migration to `assets/minecraft/items/*.json` with `minecraft:select` and `minecraft:custom_model_data`.
4. Writes generated packs and zip archives.
5. Installs generated datapack into world `datapacks/` and runs `/minecraft:reload`.
6. Enables runtime compatibility listeners for item behavior bridging.

## Build

```bash
mvn clean package
```

Jar output:

```text
target/sanguine-compatibility-engine-2.0.0.jar
```

## Runtime resource-pack delivery

Set in `plugins/SanguineCompatibilityEngine/config.yml`:

```yaml
engine:
  resource-pack-url: "https://your-cdn/generated_resourcepack.zip"
  resource-pack-sha1: ""
```

The engine generates `generated_resourcepack.zip`; host it on HTTP(S) and configure the URL.
