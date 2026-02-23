# Hybrid-Plugin-for-mods-fabric-and-forge-

## Sanguine Legacy Datapack Port (1.21.8) — Ultimate Edition++

Расширенный datapack-порт Sanguine с кровавыми лунами, волнами монстров, ритуалами, прогрессией, лором и мостом для интеграции с плагином.

## Исправления по вашим замечаниям

1. **"неизвестная задача/команда"**
   - Добавлен авто-enable trigger каждый тик: теперь `/trigger sg.trigger set X` работает стабильно без ручного `scoreboard players enable`.
   - Добавлен алиас команды: `/function sanguine:ritual/channel_blood` (раньше была только `rituals/channel_blood`).

2. **Интеграция datapack + plugin**
   - Добавлен мост `sanguine:bridge` через `storage`:
     - Экспорт мира: `world.bloodmoon`, `world.wave`, `world.moon_time`
     - Экспорт состояния последнего игрока: `last_player.*`
     - Импорт флагов от плагина: `flags.force_bloodmoon`, `flags.global_cleanup`

## Быстрые команды

- `/function sanguine:admin/help`
- `/function sanguine:ritual/channel_blood` (алиас)
- `/trigger sg.trigger set 1..8`

## Контракт интеграции для плагина

Плагин может читать/писать `storage sanguine:bridge`:

- Чтение (datapack -> plugin):
  - `world.bloodmoon`
  - `world.wave`
  - `world.moon_time`
  - `last_player.role`, `last_player.blood`, `last_player.thirst`, `last_player.rank`

- Запись (plugin -> datapack):
  - `flags.force_bloodmoon:1` — форсировать кровавую луну
  - `flags.global_cleanup:1` — очистить кастомные сущности

## Пример цикла плагина

1. Раз в тик/секунду считывать `storage sanguine:bridge`.
2. При условии сервера писать нужные `flags.*`.
3. Datapack автоматически подхватывает флаги в `sanguine:bridge/import_flags`.

