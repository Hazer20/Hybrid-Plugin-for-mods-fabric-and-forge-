# Hybrid-Plugin-for-mods-fabric-and-forge-

## Sanguine Legacy Datapack Port (1.21.8) — Ultimate Edition+

Максимально расширенный datapack-порт Sanguine: кровавые луны, эскалация волн, фазы боссов, кастом-мобы, ритуалы, прогрессия, лут, лор-ивенты, оружие и админ-утилиты.

## Ограничение формата datapack

Datapack не создаёт реальные новые Java-регистры сущностей/предметов/блоков как Forge/Fabric-мод.

Вместо этого реализовано:
- кастом-мобы: ванильные мобы + теги + атрибуты + эффекты + отдельный tick-AI;
- кастом-оружие: компоненты предметов (имя/лоры/зачары);
- ритуальные блоки: построение алтарей из ванильных блоков + маркеры + логика функции.

## Что добавлено сверх предыдущей версии

- Фазы боссов кровавой луны (P2/P3) с усилениями и визуальными эффектами.
- Лут-система за убийства волн (`sg.kills`) с ритуальными материалами.
- Лор-раскрытие по рангу (3/6/9) через tellraw-события.
- Новые админ-утилиты:
  - reset профиля игрока
  - очистка всех datapack-сущностей

## Игровые команды

- `/function sanguine:ritual/embrace`
- `/function sanguine:ritual/cure`
- `/function sanguine:admin/become_hunter`
- `/function sanguine:ritual/feed`
- `/function sanguine:ritual/infect_target`
- `/function sanguine:ritual/create_ward`
- `/function sanguine:ritual/remove_ward`
- `/function sanguine:blocks/create_ritual_altar`
- `/function sanguine:items/give_vampire_kit`
- `/function sanguine:items/give_hunter_kit`

## Триггеры

- `/trigger sg.trigger set 1` — форма
- `/trigger sg.trigger set 2` — укус
- `/trigger sg.trigger set 3` — скан
- `/trigger sg.trigger set 4` — туманная поступь
- `/trigger sg.trigger set 5` — удар колом
- `/trigger sg.trigger set 6` — кровавая клятва
- `/trigger sg.trigger set 7` — очищение
- `/trigger sg.trigger set 8` — канал крови

## Админ-команды

- `/function sanguine:admin/tests/grant_admin`
- `/function sanguine:admin/tests/setup_vampire`
- `/function sanguine:admin/tests/setup_hunter`
- `/function sanguine:admin/tests/fill_resources`
- `/function sanguine:admin/tests/force_bloodmoon`
- `/function sanguine:admin/tests/spawn_all_mobs`
- `/function sanguine:admin/tests/run_smoke`
- `/function sanguine:admin/tests/test_all_features`
- `/function sanguine:admin/tools/reset_player`
- `/function sanguine:admin/tools/cleanup_entities`

## Быстрый полный прогон

1. `/function sanguine:admin/tests/grant_admin`
2. `/function sanguine:admin/tests/test_all_features`
