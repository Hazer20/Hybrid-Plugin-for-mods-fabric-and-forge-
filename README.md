# Sanguine Datapack Bridge (1.20.4 -> 1.21.8)

Плагин создаёт **мост-датапак** для сервера 1.21.8 из исходного датапака Sanguine, который остаётся в формате 1.20.4.

## Что делает

- читает папку `world/datapacks/Sanguine` (по умолчанию);
- копирует её в `world/datapacks/Sanguine_121_bridge`;
- обновляет `pack.mcmeta` до формата 1.21.8;
- применяет набор replacement-правил к `.mcfunction` файлам (ключевые несовместимости между 1.20.4 и 1.21.8).

Исходный датапак не изменяется.

## Настройка

`plugins/SanguineBridge/config.yml`

```yml
source-datapack: Sanguine
output-datapack: Sanguine_121_bridge
world-name: world
```

## Сборка

```bash
./gradlew build
```

Готовый jar: `build/libs/sanguine-datapack-bridge-1.0.0.jar`

## Запуск

1. Положи jar в `plugins/` сервера Paper 1.21.8.
2. Убедись, что исходный датапак есть в `world/datapacks/Sanguine`.
3. Запусти сервер.
4. После старта выполни `/minecraft:reload`.

## Расширение правил

Файл: `src/main/java/dev/sanguine/bridge/BridgeRuleSet.java`

Добавляй новые replacement-правила, если в Sanguine есть другие несовместимые ID/пути команд.
