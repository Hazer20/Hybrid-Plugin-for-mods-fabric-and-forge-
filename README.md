# HybridConverter (Paper 1.21.8)

Автор: **Hazer_2_0**  
Разработка: **Notepad++**  
Сборка: **Maven**  
Java: **21**

Плагин автоматически конвертирует:
- datapack (папки) из `/plugins/HybridConverter/input/datapacks/`
- resourcepack (ZIP) из `/plugins/HybridConverter/input/resourcepacks/`

в формат Minecraft **1.21.8** с генерацией в:
- `/plugins/HybridConverter/generated/datapacks/`
- `/plugins/HybridConverter/generated/resourcepacks/`

## Команда

- `/convertpack` — запустить полную конвертацию асинхронно.

## Что делает конвертер

- Асинхронно читает и распаковывает входные файлы.
- Делает backup исходников в `/plugins/HybridConverter/backup/<timestamp>/`.
- Локальный AI-модуль (offline rule engine) переводит legacy-ключи, включая:
  - `location_minecraft:predicate -> location_predicate`
  - `input_minecraft:predicate -> input_predicate`
  - `surface_minecraft:structures -> structures`
  - `minecraft:structures -> structures`
- Обновляет `pack.mcmeta` (`pack_format`), predicates, loot tables, advancements, recipes, blockstates, models.
- Конвертирует item model overrides в новую схему `minecraft:select` + `minecraft:custom_model_data`.
- Невалидные/неизвестные JSON файлы переносит в `/plugins/HybridConverter/backup/unsupported/`.
- Ведёт лог ошибок в `/plugins/HybridConverter/logs/conversion.log`.
- Формирует отчёт `/plugins/HybridConverter/conversion-report.txt`.

## Сборка

```bash
mvn clean package
```

JAR:

```text
target/hybrid-converter-2.0.0.jar
```
