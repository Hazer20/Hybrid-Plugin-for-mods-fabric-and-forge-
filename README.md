# Sanguine Datapack Bridge (1.20.4 -> 1.21.8)

Плагин создаёт **мост-датапак** для сервера 1.21.8 из исходного датапака Sanguine, который остаётся в формате 1.20.4.

## Что делает теперь (полный проход по датапаку)

- полностью сканирует все папки и файлы внутри исходного datapack;
- копирует структуру 1:1 в `world/datapacks/Sanguine_121_bridge`;
- для текстовых файлов (`.mcfunction`, `.json`, `.mcmeta`, `.txt`) применяет миграционные правила;
- для `pack.mcmeta` автоматически ставит `pack_format` для 1.21.8;
- логирует предупреждения, если после миграции остались legacy-токены;
- исходный datapack остаётся нетронутым.

## Важно

Полностью автоматический порт "до последнего символа" для любого кастомного datapack без ручных правил невозможен, потому что кастомные механики могут иметь произвольные id/структуры.

Поэтому реализовано максимально близко к «полному порту»:
- полный скан всех файлов;
- авто-замены по широкому набору дефолтных правил;
- доп. ваши правила через `extra-replacements`.

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
