# FracturedUniverse (Paper 1.21.8)

Гибридная система **плагин + datapack** для сезонного ARG-ивента:
**«Нарушение структуры вселенной»**.

Автор: **Hazer_2_0**

## Что реализовано
- Глобальный BossBar: **«Состояние вселенной: X%»**, скрыт до старта.
- Фазовая деградация стабильности мира (100 → 0).
- Разломы трёх типов: малый / великий / живой.
- Нестабильность игрока после разломов + «Стабилизация организма».
- Портальные механики:
  - `/universe portals destroy`
  - `/universe portals wait`
- ARG система на **30 событий**:
  - `/universe arg start <номер>`
  - `/universe arg random`
  - `/universe arg stop`
  - `/universe arg list`
- Событие падения звезды.
- Пространственный коллапс чанков раз в **9 часов** (с восстановлением).
- Финал сезона:
  - `/universe final`
  - спавн босса **The Fractured Architect**
  - сообщение победы: «Структура реальности восстановлена».
- Datapack контент: sky/events/particles/portals/models.
- Автообнаружение кастомных моделей в `datapack/data/fractured_universe/models`.

## Сборка
```bash
mvn clean package
```
Артефакт: `target/FracturedUniverse-1.0.jar`

## Установка
1. Скопируйте JAR в папку `plugins/`.
2. Запустите сервер, чтобы создалась папка `plugins/FracturedUniverse/`.
3. Скопируйте папку `src/main/resources/datapack` в `<ваш_мир>/datapacks/fractured_universe/`.
4. Выполните `/reload confirm` или перезапустите сервер.
5. Проверьте работу:
   - `/datapack list`
   - `/universe debug`

## Базовые команды
- `/universe start`
- `/universe stop`
- `/universe phase <0-4>`
- `/universe starfall`
- `/universe fissure <small|great|living|disable>`
- `/universe portals <destroy|wait>`
- `/universe arg <start|random|stop|list>`
- `/universe final`
- `/universe lore add <текст>`

## Структура datapack
```text
data/fractured_universe
  functions/
  structures/
  particles/
  events/
  bosses/
  sky/
  models/
```
