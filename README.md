# Sanguine Datapack Bridge (1.20.4 -> 1.21.8)

Плагин создаёт **мост-датапак** для сервера 1.21.8 из исходного датапака Sanguine, который остаётся в формате 1.20.4.

## Что делает

- читает папку `world/datapacks/Sanguine` (по умолчанию);
- копирует её в `world/datapacks/Sanguine_121_bridge`;
- обновляет `pack.mcmeta` до `pack_format` для 1.21.8;
- применяет набор replacement-правил к `.mcfunction` файлам (частые несовместимости между 1.20.4 и 1.21.8);
- позволяет добавить **свои** правила в `config.yml` через `extra-replacements`.

Исходный датапак не изменяется.

## Важно про "все команды"

Автоматически покрыть *абсолютно все* несовместимости любых datapack-команд невозможно без полноценного парсера + ручной валидации конкретного Sanguine.

Поэтому в плагине есть:
- широкий дефолтный набор частых замен;
- расширение через `extra-replacements`, чтобы закрыть именно ваши команды/пути/functions/ids.

## Настройка

`plugins/SanguineBridge/config.yml`

```yml
source-datapack: Sanguine
output-datapack: Sanguine_121_bridge
world-name: world

extra-replacements:
  "sanguine:old/path": "sanguine:new/path"
  "generic.flying_speed": "minecraft:generic.flying_speed"
```

## Сборка через Maven

```bash
mvn -q clean package
```

Готовый jar: `target/sanguine-datapack-bridge-1.0.1.jar`

## Запуск

1. Положи jar в `plugins/` сервера Paper 1.21.8.
2. Убедись, что исходный датапак есть в `world/datapacks/Sanguine`.
3. Запусти сервер.
4. После старта выполни `/minecraft:reload`.

## Расширение правил

- Базовые правила: `src/main/java/dev/sanguine/bridge/BridgeRuleSet.java`
- Ваши точечные правила: `config.yml -> extra-replacements`
