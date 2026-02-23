# Hybrid-Plugin-for-mods-fabric-and-forge-

## Sanguine Legacy Datapack + SanguineBridge Plugin (1.21.8)

Сделан связанный комплект: datapack + Paper plugin, чтобы убрать ошибки и дать удобный интерфейс управления.

## Исправлены ваши ошибки

### 1) `Unknown scoreboard objective 'sg.trigger'`
Исправлено:
- плагин теперь гарантированно создает core objectives (`sg.trigger`, `sg.role`, `sg.blood`, `sg.thirst`);
- при старте, на join и в цикле плагин включает `/trigger` для игроков.

### 2) `Unknown function sanguine:bridge/tick`
Исправлено:
- порядок старта исправлен: сначала `reload`, потом инициализация и вызов `function sanguine:load`;
- bridge тик вызывается только после bootstrap, плюс есть `/sanguinebridge reloadpack`.

## Интерфейс (GUI) плагина

Добавлено меню: `/sanguinebridge menu`

Кнопки:
- Force Blood Moon
- Create Ritual Altar
- Give Hunter Kit
- Give Vampire Kit
- Run Full Smoke Test

## Команды плагина

- `/sanguinebridge status`
- `/sanguinebridge forcebloodmoon`
- `/sanguinebridge cleanup`
- `/sanguinebridge reloadpack`
- `/sanguinebridge smoketest`
- `/sanguinebridge enabletriggers`
- `/sanguinebridge menu`
- `/sanguinebridge ritualaltar`
- `/sanguinebridge ritualhelp`

Алиасы: `/sgbridge`, `/sgb`

## Команды datapack для игры/ритуалов

- `/function sanguine:admin/help`
- `/function sanguine:ritual/embrace`
- `/function sanguine:ritual/cure`
- `/function sanguine:ritual/feed`
- `/function sanguine:ritual/create_ward`
- `/function sanguine:ritual/remove_ward`
- `/function sanguine:ritual/infect_target`
- `/function sanguine:ritual/channel_blood`
- `/function sanguine:blocks/create_ritual_altar`
- `/trigger sg.trigger set 1..8`

## Установка

### Datapack
1. Положить репозиторий в `<world>/datapacks/sanguine_legacy/`
2. Выполнить `/reload`

### Plugin
1. `cd plugin`
2. `mvn -DskipTests package`
3. Взять `plugin/target/sanguine-bridge-1.1.0.jar` (или актуальную версию)
4. Положить jar в `plugins/`
5. Перезапустить сервер

## Bridge storage API

`storage sanguine:bridge`

- datapack -> plugin:
  - `world.bloodmoon`, `world.wave`, `world.moon_time`
  - `last_player.role`, `last_player.blood`, `last_player.thirst`, `last_player.rank`

- plugin -> datapack:
  - `flags.force_bloodmoon:1`
  - `flags.global_cleanup:1`
