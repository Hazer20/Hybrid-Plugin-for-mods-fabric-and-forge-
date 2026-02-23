# SanguineCompatibilityEngine

Production-oriented Paper plugin for automatic datapack + resource pack conversion from **1.20.4** to **1.21.8**.

- Plugin: `SanguineCompatibilityEngine`
- Author: `Hazer_2_0`
- Build: Maven
- Java: 21
- Target: Paper / Spigot 1.21.8

## Startup flow

On server startup the engine:

1. Creates folders:
   - `plugins/SanguineCompatibilityEngine/input_datapack/`
   - `plugins/SanguineCompatibilityEngine/input_resourcepack/`
   - `plugins/SanguineCompatibilityEngine/generated_datapack/`
   - `plugins/SanguineCompatibilityEngine/generated_resourcepack/`
2. Creates timestamped backup in `plugins/SanguineCompatibilityEngine/backup/<timestamp>/`.
3. Unpacks zip/folder inputs with `ZipInputStream`.
4. Converts datapack/resourcepack using parser + AST + translation layer.
5. Runs validation pass for JSON/resource consistency.
6. Writes:
   - `plugins/SanguineCompatibilityEngine/conversion-report.txt`
   - `plugins/HybridConverter/logs/conversion.log`
   - `plugins/HybridConverter/logs/conversion-report.txt`
7. Installs generated datapack and reloads datapacks (optional).

## Safety guarantees

- Invalid JSON is skipped with warning (no startup crash).
- Unknown/invalid files are moved into `generated_*/unsupported/`.
- Converters are wrapped with failsafe logging.
- NBT-like command fragments get recovery attempt for brace mismatch.
- Conversion and validation issues are written to report and log.

## Build

```bash
mvn clean package
```

Output jar:

```text
target/sanguine-compatibility-engine-2.0.0.jar
```

## Config

```yaml
engine:
  world-name: world
  auto-reload-datapacks: true
  resource-pack-url: ""
  resource-pack-sha1: ""
```

Set `resource-pack-url` to hosted `generated_resourcepack.zip` to auto-send the pack to online players.
