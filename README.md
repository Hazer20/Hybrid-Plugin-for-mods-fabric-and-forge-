# Hybrid-Plugin-for-mods-fabric-and-forge-

## Sanguine Legacy Datapack Port (1.21.8) — Ultimate Edition

Сделан максимально расширенный **порт Sanguine в datapack**: кровавые луны, волны монстров, ритуалы, прокачка, классы, кастомизированные оружия/предметы, ритуальные блоки и админ-набор полного тестирования.

## Важное ограничение Minecraft Datapack

Datapack не может 1:1 создать настоящие новые Java-типы блоков/предметов/мобов как мод.

Поэтому «кастомные» сущности здесь реализованы так:
- кастом-мобы: ванильные сущности с тегами, именами, атрибутами, эффектами и отдельным AI-тик-обработчиком;
- кастом-оружие/предметы: через NBT/components (custom_name/lore/enchants);
- ритуальные блоки: структурные комбинации ванильных блоков + маркеры и серверная логика.

Это максимум, который можно стабильно получить на чистом datapack.

## Реализовано

- Кровавые луны с волнами жутких монстров:
  - Кровавые Рабы
  - Пожиратели Плоти
  - Костяные Каратели
  - Кровеплёты
  - Лунные Разорители
  - Тираны Крови
  - Багровый Бегемот (мини-босс)
- Роли и прогрессия:
  - Смертный / Вампир / Охотник
  - XP/ранг
  - Заражение и авто-обращение
  - Контракты охотника
  - Кровавые связи
- Вампирские способности:
  - Форма, Укус, Туманная поступь, Кровавая клятва, Канал крови
- Охотничьи способности:
  - Скан, Удар колом, Очищение, Канал крови
- Ритуалы и ритуальные блоки:
  - Алтарь крови
  - Печать охотника (ward)
  - Форс кровавой луны
- Кастом оружие/наборы:
  - Клинок Багровой Клятвы
  - Арбалет Очищения
  - Освященный Кол

## Установка

1. Помести папку в `<world>/datapacks/sanguine_legacy/`
2. Выполни `/reload`
3. Проверь `/datapack list`
4. Получи справку `/function sanguine:admin/help`

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

## Админ-команды теста (OP + tag sg.admin)

1) выдать доступ:
- `/function sanguine:admin/tests/grant_admin`

2) тесты:
- `/function sanguine:admin/tests/setup_vampire`
- `/function sanguine:admin/tests/setup_hunter`
- `/function sanguine:admin/tests/fill_resources`
- `/function sanguine:admin/tests/force_bloodmoon`
- `/function sanguine:admin/tests/spawn_all_mobs`
- `/function sanguine:admin/tests/run_smoke`
- `/function sanguine:admin/tests/test_all_features`

## Быстрый полный прогон

1. `/function sanguine:admin/tests/grant_admin`
2. `/function sanguine:admin/tests/test_all_features`
