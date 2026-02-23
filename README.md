# Hybrid-Plugin-for-mods-fabric-and-forge-

## Sanguine Legacy Datapack + SanguineBridge Plugin (1.21.8)

Теперь проект содержит **и datapack, и plugin-мост** для стабильной работы «на 900%»:
- datapack отвечает за механики, ритуалы, волны, роли и события;
- plugin автоматизирует сервисные команды, помогает с `/trigger`, и управляет bridge-флагами.

## Что исправлено по проблемам

1. Ошибки вида «неизвестная задача/команда» при `/trigger`:
- datapack: auto-enable `sg.trigger` каждый тик;
- plugin: на join и по таймеру дополнительно включает `sg.trigger`.

2. Связка datapack c plugin:
- `storage sanguine:bridge` используется как API-контракт;
- plugin пишет флаги (`force_bloodmoon`, `global_cleanup`), datapack их читает и выполняет;
- datapack экспортирует world/player snapshot, plugin может читать и логировать.

## Установка

### Datapack
1. Положить репозиторий в `<world>/datapacks/sanguine_legacy/`
2. Выполнить `/reload`

### Plugin
1. Собрать jar:
   - `cd plugin`
   - `mvn -DskipTests package`
2. Взять `plugin/target/sanguine-bridge-1.0.0.jar`
3. Положить в папку `plugins/` Paper-сервера
4. Перезапустить сервер

## Команда плагина

- `/sanguinebridge status` — вывести состояние bridge
- `/sanguinebridge forcebloodmoon` — форс-флаг кровавой луны
- `/sanguinebridge cleanup` — очистить кастомные сущности
- `/sanguinebridge reloadpack` — reload + reinit datapack
- `/sanguinebridge smoketest` — прогон тестов для онлайн-админов
- `/sanguinebridge enabletriggers` — принудительно включить trigger

Алиасы: `/sgbridge`, `/sgb`

## Контракт bridge storage

Путь: `storage sanguine:bridge`

- datapack -> plugin:
  - `world.bloodmoon`
  - `world.wave`
  - `world.moon_time`
  - `last_player.role`
  - `last_player.blood`
  - `last_player.thirst`
  - `last_player.rank`

- plugin -> datapack:
  - `flags.force_bloodmoon:1`
  - `flags.global_cleanup:1`

## Быстрый запуск

1. `/function sanguine:admin/help`
2. `/sanguinebridge reloadpack`
3. `/sanguinebridge smoketest`
